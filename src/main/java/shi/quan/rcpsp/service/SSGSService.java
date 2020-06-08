package shi.quan.rcpsp.service;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.exception.BuzzException;
import shi.quan.common.vo.Duo;
import shi.quan.common.vo.Quartet;
import shi.quan.rcpsp.util.GraphUtil;
import shi.quan.rcpsp.util.RangeUtil;
import shi.quan.rcpsp.vo.Resource;
import shi.quan.rcpsp.vo.ResourceInstance;
import shi.quan.rcpsp.vo.Task;

import javax.enterprise.context.ApplicationScoped;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SSGSService {
    private static final Logger logger = LoggerFactory.getLogger(SSGSService.class);

    public static final String VERBOSE = "VERBOSE";
    public static final String OFFSET_MAX = "OFFSET_MAX";
    public static final String OFFSET_STEP = "OFFSET_STEP";
    public static final String TIME_MAP = "TIME_MAP";
    public static final String CPM_SET = "CPM_SET";

    private static final String START_NODE = "START_NODE";
    private static final String END_NODE = "END_NODE";
    private static final String AMOUNT_CALCULATOR = "AMOUNT_CALCULATOR";
    private static final String TIME_CALCULATOR = "TIME_CALCULATOR";
    private static final String TIME_EXTRACTOR = "TIME_EXTRACTOR";
    private static final String LAST_RANGES = "LAST_RANGES";
    private static final String LAST_SELECTED_TIME = "LAST_SELECTED_TIME";

    public interface EventListener<TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>> {
        void onTaskProcessed(Map<String, Object> context, Task<TimeType, PayloadType, AmountType> task, int processed, int total);
    }

    /**
     * The main entry for SSGS.
     */
    @SuppressWarnings("unchecked")
    public
    <TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>, EdgeType>
    void ssgs(Map<String, Object> context
            , Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph
            , Map<String, Resource<TimeType, AmountType>> resources
            , RangeUtil.AmountCalculator<AmountType> amountCalculator
            , GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType> timeCalculator
            , GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>> timeExtractor
            , EventListener eventListener
            , long uBound) throws BuzzException {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[ssgs]");

        long start = System.currentTimeMillis();

        context.put(AMOUNT_CALCULATOR, amountCalculator);
        context.put(TIME_CALCULATOR, timeCalculator);
        context.put(TIME_EXTRACTOR, timeExtractor);

        timeCalculation(context, graph);
        resourceAdjustment(context, resources);

        Set<Task<TimeType, PayloadType, AmountType>> lastAvailableTasks = new HashSet<>();

        Set<Task<TimeType, PayloadType, AmountType>> proceededTasks = new HashSet<>();

        Set<Task<TimeType, PayloadType, AmountType>> visited;

        if (uBound < 0L) {
            uBound = 100L;
        }

        String MAX_LOOP_STRING = context.containsKey(OFFSET_MAX) ? (String) context.get(OFFSET_MAX) : "1000";
        String OFFSET_STEP_STRING = context.containsKey(OFFSET_STEP) ? (String) context.get(OFFSET_STEP) : "1";

        long MAX_LOOP = Long.parseLong(MAX_LOOP_STRING);
        long offsetStep = Long.parseLong(OFFSET_STEP_STRING);

        long loopCount = 0;

        for(;;) {
            if (loopCount > uBound) {
                throw new BuzzException(String.format("Exceed the uBound (%d/%d)...", loopCount, uBound));
            }

            loopCount++;

            visited = new HashSet<>(proceededTasks);

            List<Task<TimeType, PayloadType, AmountType>> availableTasks = getCurrentAvailableTasks(context, graph, visited);

            Set<Task<TimeType, PayloadType, AmountType>> availableTaskSet = new HashSet<>(availableTasks);

            if (lastAvailableTasks != null && lastAvailableTasks.equals(availableTaskSet)) {
                logger.error("availableTasks : {}", availableTasks);
                logger.error("lastAvailableTasks : {}", lastAvailableTasks);
                throw new BuzzException("Endless loop detected...");
            }

            lastAvailableTasks = availableTaskSet;

            if(verbose) logger.info("[MAIN LOOP] loopCount : {}, availableTasks : {}", loopCount, availableTasks);

            if (availableTasks.isEmpty()) {
                Set<Task<TimeType, PayloadType, AmountType>> finalVisited = visited;

                List<Task<TimeType, PayloadType, AmountType>> unvisited = graph.vertexSet().stream()
                        .filter(t -> !finalVisited.contains(t))
                        .collect(Collectors.toList());

                if (!unvisited.isEmpty()) {
                    logger.error("Not all tasks has been chosen. The unvisited task(s) : {}", unvisited);
                    throw new BuzzException("Failed to select available tasks for all vertices.");
                } else {
                    dumpProceededTasks(start, proceededTasks);
                    return;
                }
            }

            for (;;) {
                if(verbose) logger.info("[INNER LOOP]");
                if(verbose) logger.info("\tvisited : {}", visited);
                if(verbose) logger.info("\tproceededTasks : {}", proceededTasks);

                Task<TimeType,PayloadType,AmountType> task = chooseTask(context, availableTasks);

                if (task == null && availableTasks.isEmpty()) {
                    break;
                } else if (task == null) {
                    logger.error("Failed to choose a task from availableTasks : {}", availableTasks);
                    throw new BuzzException("Failed to choose a task...");
                }

                if(verbose) logger.info(">> CHOOSE THE TASK : {}, # of PROCEEDED : {}", task, proceededTasks.size());

                if (resourceConstraintCheck(context, resources, task)) {
                    boolean isDone = false;
                    long counter = 0;

                    long offset = 0;

                    while(!isDone) {
                        if(counter > MAX_LOOP) {
                            if(verbose) logger.warn("Cannot find a good resource for the task.");
                            break;
                        }
                        ++counter;

                        if (verbose) logger.info("counter : {}, offset : {}", counter, offset);

                        Duo<Map<String, List<ResourceInstance<TimeType, AmountType>>>, Duo<TimeType, TimeType>> result = resourceSaturationCalculation(context, graph, resources, task, proceededTasks, offset);

                        if (result != null) {
                            Map<String, List<ResourceInstance<TimeType, AmountType>>> availableResourceInstanceMap = result.getK();
                            Duo<TimeType, TimeType> time = result.getV();

                            if (verbose) logger.info("availableResourceInstanceMap : {}", availableResourceInstanceMap);
                            if (verbose) logger.info("time : {}", time);

                            Map<String, ResourceInstance<TimeType, AmountType>> chosenResources = chooseResource(context, resources, task, time, availableResourceInstanceMap);

                            if (chosenResources != null) {
                                updateTask(context, task, time, chosenResources);
                                updateResource(context, chosenResources, task);
                                visited.add(task);
                                proceededTasks.add(task);
                                isDone = true;

                                if(eventListener != null) {
                                    try {
                                        eventListener.onTaskProcessed(context, task, proceededTasks.size(), graph.vertexSet().size());
                                    } catch (Exception ex) {
                                        logger.error("Error on calling EventListener.", ex);
                                    }
                                }
                            }
                        }

                        if (!isDone) {
                            offset += offsetStep;
//                            if (isOnCriticalPath(context, task)) {
//                                if (verbose) logger.info("task {} is on critical path. Attempt to move offset...", task);
//                                offset += offsetStep;
//                            } else {
//                                if (verbose) logger.info("task {} is NOT on critical path. Drop for next loop...", task);
//                                isDone = true;
//                            }
                        }
                    }
                }

                availableTasks.remove(task);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private
    <TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>, EdgeType>
    void timeCalculation(Map<String, Object> context, Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph) throws BuzzException {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[timeCalculation]");

        List<Task<TimeType, PayloadType, AmountType>> startList = graph.vertexSet().stream()
                .filter(v-> graph.incomingEdgesOf(v).isEmpty())
                .collect(Collectors.toList());

        if (startList.size() != 1) {
            throw new BuzzException("Only one start vertex is allowed.");
        }

        Task<TimeType, PayloadType, AmountType> startNode =  startList.get(0);

        context.put(START_NODE, startNode);

        if(verbose) logger.info("START_NODE : {}", startNode);

        List<Task<TimeType, PayloadType, AmountType>> endList = graph.vertexSet().stream()
                .filter(v-> graph.outgoingEdgesOf(v).isEmpty())
                .collect(Collectors.toList());

        if (endList.size() != 1) {
            throw new BuzzException("Only one end vertex is allowed.");
        }

        Task<TimeType, PayloadType, AmountType> endNode =  endList.get(0);

        context.put(END_NODE, endNode);

        if(verbose) logger.info("END_NODE : {}", endNode);

        GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>> timeExtractor = (GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>>)context.get(TIME_EXTRACTOR);

        Map<Task<TimeType, PayloadType, AmountType>, Long> durationMap = new HashMap<>();

        for(Task<TimeType, PayloadType, AmountType> task : graph.vertexSet()) {
            durationMap.put(task, timeExtractor.duration(task));
        }

        Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>> map = GraphUtil.cpm(graph, startNode, endNode, (v) -> {
            return durationMap.get(v);
        });

        context.put(TIME_MAP, map);

        if(verbose) logger.info("TIME_MAP : {}", map);

        Set<Task<TimeType, PayloadType, AmountType>> cmp = GraphUtil.cpm(map);

        context.put(CPM_SET, cmp);

        if(verbose) logger.info("CMP_SET : {}", cmp);
    }

    private void resourceAdjustment(Map<String, Object> context, Object resources) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[resourceAdjustment]");
        //For now no adjustment is needed...
    }

    @SuppressWarnings("unchecked")
    private
    <PayloadType, TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, EdgeType>
    List<Task<TimeType,PayloadType,AmountType>> getCurrentAvailableTasks(Map<String, Object> context, Graph<Task<TimeType,PayloadType,AmountType>,EdgeType> graph, Set<Task<TimeType,PayloadType,AmountType>> visited) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[getCurrentAvailableTasks]");

        Task<TimeType, PayloadType, AmountType> startNode = (Task<TimeType, PayloadType, AmountType>)context.get(START_NODE);

        List<Task<TimeType,PayloadType,AmountType>> ret = new ArrayList<>();

        GraphUtil.forwardBreadthVisit(graph, startNode, (v) -> {
            if (!visited.contains(v)) {
                if (!graph.incomingEdgesOf(v).stream().anyMatch(e -> !visited.contains(graph.getEdgeSource(e)))) {
                    ret.add(v);
                }
            }
        });

        visited.addAll(ret);

        return ret;
    }

    private
    <PayloadType, TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>>
    Task<TimeType,PayloadType,AmountType> chooseTask(Map<String, Object> context, List<Task<TimeType,PayloadType,AmountType>> availableTasks) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[chooseTask]");

        Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>> map = (Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>>) context.get(TIME_MAP);

        if(verbose) logger.info("map : {}", map);

        Task<TimeType,PayloadType,AmountType> task = availableTasks.stream()
                .filter(t -> isOnCriticalPath(context, t))
                .sorted((c1, c2) -> -map.get(c1).getOne().compareTo(map.get(c2).getOne()))
                .findFirst().orElse(null);

        if(task == null) {
            task = availableTasks.stream()
                    .sorted((c1, c2) -> -map.get(c1).getOne().compareTo(map.get(c2).getOne()))
                    .findFirst().orElse(null);
        }

        return task;
    }

    private <TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>>
    boolean resourceConstraintCheck(Map<String, Object> context, Map<String, Resource<TimeType, AmountType>> resources, Task<TimeType, PayloadType, AmountType> task) {
        //TODO: Add real logic of resource constraint checking...
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        return true;
    }

    @SuppressWarnings("unchecked")
    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType, EdgeType>
    Duo<Map<String, List<ResourceInstance<TimeType, AmountType>>>, Duo<TimeType, TimeType>> resourceSaturationCalculation(Map<String, Object> context
            , Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph
            , Map<String, Resource<TimeType, AmountType>> resources
            , Task<TimeType, PayloadType, AmountType> task
            , Set<Task<TimeType, PayloadType, AmountType>> visited, long offset) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[resourceSaturationCalculation] offset : {}", offset);

        Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>> map = (Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>>) context.get(TIME_MAP);

//        if(verbose) logger.info("map : {}", map);

        RangeUtil.AmountCalculator<AmountType> amountCalculator = (RangeUtil.AmountCalculator<AmountType>)context.get(AMOUNT_CALCULATOR);

        Duo<TimeType, TimeType> selectedTime = calcTimeRange(context, graph, task, map.get(task), offset);

        if(verbose) logger.info("selectedTime : {}", selectedTime);

        boolean isAcceptable = true;

        Map<String, List<ResourceInstance<TimeType, AmountType>>> availableResourceInstanceMap = new HashMap<>();

        for(String resourceId : task.getResourceMap().keySet()) {
            if(verbose) logger.info("resourceId : {}", resourceId);

            Resource<TimeType, AmountType> resource = resources.get(resourceId);

            for(ResourceInstance<TimeType, AmountType> instance : resource.getInstanceList()) {
                if(verbose) logger.info("\tinstance : {}", instance.getId());

//                Trio<List<Duo<TimeType, TimeType>>, Map<Duo<TimeType, TimeType>, AmountType>, Set<Task<TimeType, PayloadType, AmountType>>> trio = null;
//
//                String cacheKey = ".resourceSaturationCalculation.rangeResourceCache";
//
//                if(!context.containsKey(cacheKey)) {
//                    context.put(cacheKey, new HashMap<String, Trio<List<Duo<TimeType, TimeType>>, Map<Duo<TimeType, TimeType>, AmountType>, Set<Task<TimeType, PayloadType, AmountType>>>>());
//                }
//
//                Map<String, Trio<List<Duo<TimeType, TimeType>>, Map<Duo<TimeType, TimeType>, AmountType>, Set<Task<TimeType, PayloadType, AmountType>>>> cache =
//                        (Map<String, Trio<List<Duo<TimeType, TimeType>>, Map<Duo<TimeType, TimeType>, AmountType>, Set<Task<TimeType, PayloadType, AmountType>>>>)context.get(cacheKey);
//
//                if(cache.containsKey(task.getId())) {
//                    trio = cache.get(task.getId());
//
//                    // Sync from the last...
//                    if(trio != null) {
//                        Set<Task<TimeType, PayloadType, AmountType>> newVisisted = new HashSet<>(visited);
//
//                        newVisisted.removeAll(trio.getThree());
//
//                        for(Task<TimeType, PayloadType, AmountType> v : newVisisted) {
//                            if(v.getResourceMap().containsKey(resourceId) && v.getChosenResources().values().contains(instance)) {
////                        if(verbose) logger.info("instance : {}, v : {}", instance, v);
//                                Duo<TimeType, TimeType> d = (v != task) ? Duo.duo(v.getPlannedStartTime(), v.getPlannedEndTime()) : selectedTime;
////                        if(verbose) logger.info("instance : {}, duo : {}", instance, duo);
//                                trio.getOne().add(d);
//                                trio.getTwo().put(d, task.getResourceMap().get(resourceId));
//                            }
//                        }
//
//                        if(!trio.getOne().contains(selectedTime)) {
//                            trio.getOne().add(selectedTime);
//                            trio.getTwo().put(selectedTime, task.getResourceMap().get(resourceId));
//                        }
//
//                        trio.setThree(new HashSet<>(visited));
//
//                        cache.put(task.getId(), trio);
//                    }
//                } else {
//                    List<Duo<TimeType, TimeType>> rangesByInstance = new ArrayList<>();
//                    Map<Duo<TimeType, TimeType>, AmountType> resourceMapByInstance = new HashMap<>();
//                    Set<Task<TimeType, PayloadType, AmountType>> currentVisisted = new HashSet<>(visited);
//
//                    for(Task<TimeType, PayloadType, AmountType> v : visited) {
//                        if(v.getResourceMap().containsKey(resourceId) && v.getChosenResources().values().contains(instance)) {
////                        if(verbose) logger.info("instance : {}, v : {}", instance, v);
//                            Duo<TimeType, TimeType> d = (v != task) ? Duo.duo(v.getPlannedStartTime(), v.getPlannedEndTime()) : selectedTime;
////                        if(verbose) logger.info("instance : {}, duo : {}", instance, duo);
//                            rangesByInstance.add(d);
//                            resourceMapByInstance.put(d, task.getResourceMap().get(resourceId));
//                        }
//                    }
//
//                    if(!visited.contains(task)) {
//                        rangesByInstance.add(selectedTime);
//                        resourceMapByInstance.put(selectedTime, task.getResourceMap().get(resourceId));
//                    }
//
//                    trio = Trio.trio(rangesByInstance, resourceMapByInstance, currentVisisted);
//
//                    cache.put(task.getId(), trio);
//                }

                List<Duo<TimeType, TimeType>> rangesByInstance = new ArrayList<>();
                Map<Duo<TimeType, TimeType>, AmountType> resourceMapByInstance = new HashMap<>();

                for(Task<TimeType, PayloadType, AmountType> v : visited) {
                    if(v.getResourceMap().containsKey(resourceId)
                            && v.getChosenResources().containsKey(resourceId)
                            && v.getChosenResources().get(resourceId).getId().equals(instance.getId())) {
//                        if(verbose) logger.info("instance : {}, v : {}", instance, v);
                        Duo<TimeType, TimeType> d = (v != task) ? Duo.duo(v.getPlannedStartTime(), v.getPlannedEndTime()) : selectedTime;
//                        if(verbose) logger.info("instance : {}, duo : {}", instance, duo);
                        rangesByInstance.add(d);
                        resourceMapByInstance.put(d, task.getResourceMap().get(resourceId));
                    }
                }

                if(!visited.contains(task)) {
                    rangesByInstance.add(selectedTime);
                    resourceMapByInstance.put(selectedTime, task.getResourceMap().get(resourceId));
                }

//                if(verbose) logger.info("\trangesByInstance : {}", duo.getK());
//                if(verbose) logger.info("\tresourceMapByInstance : {}", duo.getV());

//                if(RangeUtil.resourceCalculationByTimeRange(verbose, trio.getOne(), trio.getTwo(), selectedTime, amountCalculator, instance.getProvider())) {
                if(RangeUtil.resourceCalculationByTimeRange(verbose, rangesByInstance, resourceMapByInstance, selectedTime, amountCalculator, instance.getProvider())) {
                    if(!availableResourceInstanceMap.containsKey(resourceId)) {
                        availableResourceInstanceMap.put(resourceId, new ArrayList<>());
                    }

                    availableResourceInstanceMap.get(resourceId).add(instance);
                }
            }

            if(verbose) logger.info("availableResourceInstanceMap : {}", availableResourceInstanceMap);

            isAcceptable = !(availableResourceInstanceMap.isEmpty() || availableResourceInstanceMap.values().stream().anyMatch(l->l.size() == 0));

            if(!isAcceptable) {
                break;
            }
        }

        if(verbose) logger.info("isAcceptable : {}", isAcceptable);

        return isAcceptable ? Duo.duo(availableResourceInstanceMap, selectedTime) : null;
    }

    @SuppressWarnings("unchecked")
    private
    <TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>, EdgeType>
    Duo<TimeType,TimeType> calcTimeRange(Map<String, Object> context
            , Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph
            , Task<TimeType, PayloadType, AmountType> task
            , Quartet<Long, Long, Long, Long> taskTime, long offset) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

//        if(verbose) logger.info("[calcTimeRange]");
        GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType> timeCalculator = (GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType>)context.get(TIME_CALCULATOR);
        GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>> timeExtractor = (GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>>)context.get(TIME_EXTRACTOR);
        //RangeUtil.AmountCalculator<AmountType> amountCalculator = (RangeUtil.AmountCalculator<AmountType>)context.get(AMOUNT_CALCULATOR);

        String cacheKey = ".calcTimeRange.incomingSourceCache";

        if(!context.containsKey(cacheKey)) {
            context.put(cacheKey, new HashMap<String, Set<Task<TimeType, PayloadType, AmountType>>>());
        }
        Map<String, Set<Task<TimeType, PayloadType, AmountType>>> edgeCache = (Map<String, Set<Task<TimeType, PayloadType, AmountType>>>)context.get(cacheKey);

        Set<Task<TimeType, PayloadType, AmountType>> sources = null;

        if(edgeCache.containsKey(task.getId())) {
            sources = edgeCache.get(task.getId());
        } else {
            sources = graph.incomingEdgesOf(task).stream().map(e -> graph.getEdgeSource(e)).collect(Collectors.toSet());
            edgeCache.put(task.getId(), sources);
        }

        TimeType latestEndTime = sources.stream().map(t-> t.getPlannedEndTime()).max(Comparable::compareTo).orElse(timeCalculator.zero());

//        if(verbose) logger.info("latestEndTime : {}", latestEndTime);

        TimeType offsetTime = timeCalculator.fromLong(graph, task, offset);

//        if(verbose) logger.info("offsetTime : {}", offsetTime);

        return Duo.duo(timeCalculator.plus(latestEndTime, offsetTime), timeCalculator.plus(timeCalculator.plus(latestEndTime, timeCalculator.fromLong(graph, task, timeExtractor.duration(task))), offsetTime));
    }

    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType>
    Map<String, ResourceInstance<TimeType, AmountType>> chooseResource(Map<String, Object> context
            , Map<String, Resource<TimeType, AmountType>> resources
            , Task<TimeType, PayloadType, AmountType> task
            , Duo<TimeType, TimeType> timeslot
            , Map<String, List<ResourceInstance<TimeType, AmountType>>> availableInstanceMap) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[chooseResource]");

        Map<String, ResourceInstance<TimeType, AmountType>> ret = new HashMap<>();

        for(String resourceId : availableInstanceMap.keySet()) {
            List<ResourceInstance<TimeType, AmountType>> list = availableInstanceMap.get(resourceId);

            if(list.size() > 0) {
                ret.put(resourceId, list.get((int)(Math.random() * list.size())));
            } else {
                ret = null;
                break;
            }
        }

        return ret;
    }

    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType>
    void updateResource(Map<String, Object> context
            , Map<String, ResourceInstance<TimeType, AmountType>> chosenResources
            , Task<TimeType, PayloadType, AmountType> task) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[updateResource] task : {}", chosenResources);
    }

    @SuppressWarnings("unchecked")
    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType, EdgeType>
    void updateTask(Map<String, Object> context
            , Task<TimeType, PayloadType, AmountType> task
            , Duo<TimeType, TimeType> time
            , Map<String, ResourceInstance<TimeType, AmountType>> chosenResources) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        if(verbose) logger.info("[updateTask] task : {}, time : {}", task, time);

        Duo<TimeType, TimeType> finalTime = time;

        TimeType maxExtraTime = chosenResources.values().stream().map(r -> r.getResourceExtraTime(finalTime.getK(), finalTime.getV())).max((t1, t2)-> -t1.compareTo(t2)).orElse(null);

        if(maxExtraTime != null) {
            GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType> timeCalculator = (GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType>) context.get(TIME_CALCULATOR);

            Duo<TimeType, TimeType> newTime = Duo.duo(time.getK(), timeCalculator.plus(time.getV(), maxExtraTime));

            if (verbose) logger.info("newTime : {}", newTime);

            time = newTime;

            //TODO: A recursive extra time calculation and resource available checking is needed here...
        }

        task.setPlannedStartTime(time.getK());
        task.setPlannedEndTime(time.getV());
        task.setChosenResources(chosenResources);
    }


    @SuppressWarnings("unchecked")
    private
    <PayloadType, TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>>
    boolean isOnCriticalPath(Map<String, Object> context, Task<TimeType,PayloadType,AmountType> task) {
        String verboseString = context.containsKey(VERBOSE) ? (String)context.get(VERBOSE) : "false";
        boolean verbose = Boolean.parseBoolean(verboseString);

        Set<Task<TimeType, PayloadType, AmountType>> cpm = (Set<Task<TimeType, PayloadType, AmountType>>) context.get(CPM_SET);
        return cpm.contains(task);
    }

    private
    <AmountType extends Comparable<AmountType>, TimeType extends Comparable<TimeType>, PayloadType>
    void dumpProceededTasks(long start, Set<Task<TimeType,PayloadType,AmountType>> proceededTasks) {
        long end = System.currentTimeMillis();
        List<Task<TimeType, PayloadType, AmountType>> sorted = proceededTasks.stream()
                        .sorted(Comparator.comparing(Task::getPlannedStartTime))
                        .collect(Collectors.toList());
        for(Task<TimeType, PayloadType, AmountType> task : sorted) {
            //logger.info("[DUMP] {{} : ({}, {} | {})}", task.getName(), task.getPlannedStartTime(), task.getPlannedEndTime(), task.getPayload());
            System.out.println(MessageFormat.format("[DUMP] {0} : ({1}, {2} | {3})", task.getName(), task.getPlannedStartTime(), task.getPlannedEndTime(), task.getPayload()));
            for(String resourceId : task.getChosenResources().keySet()) {
                System.out.println(String.format("\tR : %s -> %s (%s)", resourceId, task.getChosenResources().get(resourceId).getId(), task.getResourceMap().get(resourceId)));
            }
        }

        System.out.println(String.format("TOTOAL INTERVAL : %d", end - start));
    }
}
