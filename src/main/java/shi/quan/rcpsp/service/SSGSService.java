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

import javax.enterprise.context.RequestScoped;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequestScoped
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

        Set<Task<TimeType, PayloadType, AmountType>> lastAvailableTasks = null;

        Set<Task<TimeType, PayloadType, AmountType>> proceededTasks = new HashSet<>();

        Set<Task<TimeType, PayloadType, AmountType>> visited;

        if (uBound < 0L) {
            uBound = 5000L;
        }

        long loopCount = 0;

        for(;;) {
            if (loopCount > uBound) {
                throw new BuzzException(String.format("Exceed the uBound (%d/%d)...", loopCount, uBound));
            }

            loopCount++;

            visited = new HashSet<>(proceededTasks);

            List<Task<TimeType, PayloadType, AmountType>> availableTasks = getCurrentAvailableTasks(context, graph, visited);

            if (lastAvailableTasks != null && lastAvailableTasks.equals(availableTasks)) {
                logger.error("availableTasks : {}", availableTasks);
                logger.error("lastAvailableTasks : {}", lastAvailableTasks);
                throw new BuzzException("Endless loop detected...");
            }

            lastAvailableTasks = new HashSet<>(availableTasks);

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

                if(verbose) logger.info(">> CHOOSE THE TASK : {}", task);

                if (resourceConstraintCheck(context, resources, task)) {
                    String MAX_LOOP_STRING = context.containsKey(OFFSET_MAX) ? (String) context.get(OFFSET_MAX) : "1000";
                    String OFFSET_STEP_STRING = context.containsKey(OFFSET_STEP) ? (String) context.get(OFFSET_STEP) : "1";

                    long MAX_LOOP = Long.parseLong(MAX_LOOP_STRING);
                    long offsetStep = Long.parseLong(OFFSET_STEP_STRING);

                    boolean isDone = false;
                    long counter = 0;

                    long offset = 0;

                    while(!isDone) {
                        if(counter > MAX_LOOP) {
                            if(verbose) logger.warn("Cannot find a good resource for the task.");
                            break;
                        }
                        ++counter;

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
                            }
                        }

                        if (!isDone) {
                            if (isOnCriticalPath(context, task)) {
                                offset += offsetStep;
                            } else {
                                isDone = true;
                            }
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

        return availableTasks.stream().sorted((c1, c2) -> -map.get(c1).getOne().compareTo(map.get(c2).getOne())).findFirst().orElse(null);
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

        if(verbose) logger.info("[resourceSaturationCalculation]");

        Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>> map = (Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>>) context.get(TIME_MAP);

        if(verbose) logger.info("map : {}", map);

        RangeUtil.AmountCalculator<AmountType> amountCalculator = (RangeUtil.AmountCalculator<AmountType>)context.get(AMOUNT_CALCULATOR);

        Duo<TimeType, TimeType> selectedTime = calcTimeRange(context, graph, task, map.get(task), offset);

        if(verbose) logger.info("selectedTime : {}", selectedTime);

        boolean isAcceptable = true;

        Map<String, List<ResourceInstance<TimeType, AmountType>>> availableResourceInstanceMap = new HashMap<>();

        for(String resourceId : task.getResourceMap().keySet()) {
            if(verbose) logger.info("resourceId : {}", resourceId);

            Resource<TimeType, AmountType> resource = resources.get(resourceId);

            if(verbose) logger.info("resource : {}", resource);

            List<Duo<TimeType, TimeType>> ranges = new ArrayList<>();
            Map<Duo<TimeType, TimeType>, AmountType> resourceMap = new HashMap<>();

            for(Task<TimeType, PayloadType, AmountType> v : visited) {
                if(task.getResourceMap().containsKey(resourceId)) {
                    if(verbose) logger.info("v : {}", v);
                    Duo<TimeType, TimeType> duo = (v != task) ? Duo.duo(v.getPlannedStartTime(), v.getPlannedEndTime()) : selectedTime;
                    if(verbose) logger.info("duo : {}", duo);
                    ranges.add(duo);
                    resourceMap.put(duo, task.getResourceMap().get(resourceId));
                }
            }

            if(!visited.contains(task)) {
                ranges.add(selectedTime);
                resourceMap.put(selectedTime, task.getResourceMap().get(resourceId));
            }

            if(verbose) logger.info("ranges : {}", ranges);
            if(verbose) logger.info("resourceMap : {}", resourceMap);

            context.put(LAST_RANGES, ranges);
            context.put(LAST_SELECTED_TIME, selectedTime);

            for(ResourceInstance<TimeType, AmountType> instance : resource.getInstanceList()) {
                if(RangeUtil.resourceCalculationByTimeRange(verbose, ranges, resourceMap, selectedTime, amountCalculator, instance.getProvider())) {
                    if(!availableResourceInstanceMap.containsKey(resourceId)) {
                        availableResourceInstanceMap.put(resourceId, new ArrayList<>());
                    }

                    availableResourceInstanceMap.get(resourceId).add(instance);
                }
            }

            if(verbose) logger.info("availableResourceInstanceMap : {}", availableResourceInstanceMap);

            isAcceptable = !availableResourceInstanceMap.isEmpty();

            if(!isAcceptable) {
                break;
            }
        }


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

        if(verbose) logger.info("[calcTimeRange]");
        GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType> timeCalculator = (GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType>)context.get(TIME_CALCULATOR);
        GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>> timeExtractor = (GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>>)context.get(TIME_EXTRACTOR);
        //RangeUtil.AmountCalculator<AmountType> amountCalculator = (RangeUtil.AmountCalculator<AmountType>)context.get(AMOUNT_CALCULATOR);

        TimeType latestEndTime = graph.incomingEdgesOf(task).stream().map(e-> {
            Task<TimeType, PayloadType, AmountType> predecessor = graph.getEdgeSource(e);
            return predecessor.getPlannedEndTime();
        }).sorted(Comparator.reverseOrder()).findFirst().orElse(timeCalculator.zero());

        if(verbose) logger.info("latestEndTime : {}", latestEndTime);

        TimeType offsetTime = timeCalculator.fromLong(graph, task, offset);

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
