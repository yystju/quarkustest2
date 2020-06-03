package shi.quan.rcpsp.service;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.vo.Duo;
import shi.quan.common.vo.Quartet;
import shi.quan.common.exception.BuzzException;
import shi.quan.rcpsp.util.GraphUtil;
import shi.quan.rcpsp.util.RangeUtil;
import shi.quan.rcpsp.vo.Resource;
import shi.quan.rcpsp.vo.Task;

import javax.enterprise.context.RequestScoped;
import java.util.*;
import java.util.stream.Collectors;

@RequestScoped
public class SSGSService {
    private static final Logger logger = LoggerFactory.getLogger(SSGSService.class);
    public static final String TIME_MAP = "TIME_MAP";
    public static final String CMP_SET = "CMP_SET";

    private static final String START_NODE = "START_NODE";
    private static final String END_NODE = "END_NODE";
    private static final String AMOUNT_CALCULATOR = "AMOUNT_CALCULATOR";
    private static final String TIME_CALCULATOR = "TIME_CALCULATOR";
    private static final String TIME_EXTRACTOR = "TIME_EXTRACTOR";

    /**
     * The main entry for SSGS.
     */
    public
    <TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>, EdgeType>
    void ssgs(Map<String, Object> context
            , Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph
            , Map<String, Resource<TimeType, AmountType>> resources
            , RangeUtil.AmountCalculator<AmountType> amountCalculator
            , GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType> timeCalculator
            , GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>> timeExtractor
            , long uBound) throws BuzzException {
        logger.info("[ssgs]");

        context.put(AMOUNT_CALCULATOR, amountCalculator);
        context.put(TIME_CALCULATOR, timeCalculator);
        context.put(TIME_EXTRACTOR, timeExtractor);

        timeCalculation(context, graph);
        resourceAdjustment(context, resources);

        Set<Task<TimeType, PayloadType, AmountType>> lastAvailableTasks = null;

        Set<Task<TimeType, PayloadType, AmountType>> visited = new HashSet<>();

        if (uBound < 0L) {
            uBound = 5000L;
        }

        long loopCount = 0;

        for(;;) {
            if (loopCount > uBound) {
                throw new BuzzException(String.format("Exceed the uBound (%d/%d)...", loopCount, uBound));
            }

            loopCount++;

            List<Task<TimeType, PayloadType, AmountType>> availableTasks = getCurrentAvailableTasks(context, graph, visited);

            if (lastAvailableTasks != null && lastAvailableTasks.equals(availableTasks)) {
                logger.error("availableTasks : {}", availableTasks);
                logger.error("lastAvailableTasks : {}", lastAvailableTasks);
                throw new BuzzException("Endless loop detected...");
            }

            if (availableTasks.isEmpty()) {
                return;
            }

            lastAvailableTasks = new HashSet<>(availableTasks);

            logger.info("[MAIN LOOP] loopCount : {}, availableTasks : {}", loopCount, availableTasks);

            if (availableTasks.isEmpty()) {
                List<Task<TimeType, PayloadType, AmountType>> unvisited = graph.vertexSet().stream()
                        .filter(t -> !visited.contains(t))
                        .collect(Collectors.toList());

                if (!unvisited.isEmpty()) {
                    logger.error("Not all tasks has been chosen. The unvisited task(s) : {}", unvisited);
                    throw new BuzzException("Failed to select available tasks for all vertices.");
                }
            }

            for (;;) {
                Task<TimeType,PayloadType,AmountType> task = chooseTask(context, availableTasks);

                if (task == null && availableTasks.isEmpty()) {
                    break;
                } else if (task == null) {
                    logger.error("Failed to choose a task from availableTasks : {}", availableTasks);
                    throw new BuzzException("Failed to choose a task...");
                }

                logger.info(">> CHOOSE THE TASK : {}", task);

                if (resourceConstraintCheck(context, resources, task)) {
                    Map<String, AmountType> diff = resourceSaturationCalculation(context, graph, resources, task, visited);
                    if (diff != null) {
                        Resource<TimeType, AmountType> chosenResource = chooseResource(context, resources, task, diff);

                        if (chosenResource != null) {
                            updateResource(context, chosenResource, task, diff);
                            updateTask(context, task);
                            visited.add(task);
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
        logger.info("[timeCalculation]");
        List<Task<TimeType, PayloadType, AmountType>> startList = graph.vertexSet().stream()
                .filter(v-> graph.incomingEdgesOf(v).isEmpty())
                .collect(Collectors.toList());

        if (startList.size() != 1) {
            throw new BuzzException("Only one start vertex is allowed.");
        }

        Task<TimeType, PayloadType, AmountType> startNode =  startList.get(0);

        context.put(START_NODE, startNode);

        logger.info("START_NODE : {}", startNode);

        List<Task<TimeType, PayloadType, AmountType>> endList = graph.vertexSet().stream()
                .filter(v-> graph.outgoingEdgesOf(v).isEmpty())
                .collect(Collectors.toList());

        if (endList.size() != 1) {
            throw new BuzzException("Only one end vertex is allowed.");
        }

        Task<TimeType, PayloadType, AmountType> endNode =  endList.get(0);

        context.put(END_NODE, endNode);

        logger.info("END_NODE : {}", endNode);

        GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>> timeExtractor = (GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>>)context.get(TIME_EXTRACTOR);

        Map<Task<TimeType, PayloadType, AmountType>, Long> durationMap = new HashMap<>();

        for(Task<TimeType, PayloadType, AmountType> task : graph.vertexSet()) {
            durationMap.put(task, timeExtractor.duration(task));
        }

        Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>> map = GraphUtil.cpm(graph, startNode, endNode, (v) -> {
            return durationMap.get(v);
        });

        context.put(TIME_MAP, map);

        logger.info("TIME_MAP : {}", map);

        Set<Task<TimeType, PayloadType, AmountType>> cmp = GraphUtil.cpm(map);

        context.put(CMP_SET, cmp);

        logger.info("CMP_SET : {}", cmp);
    }

    private void resourceAdjustment(Map<String, Object> context, Object resources) {
        logger.info("[resourceAdjustment]");
        //For now no adjustment is needed...
    }

    @SuppressWarnings("unchecked")
    private
    <PayloadType, TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, EdgeType>
    List<Task<TimeType,PayloadType,AmountType>> getCurrentAvailableTasks(Map<String, Object> context, Graph<Task<TimeType,PayloadType,AmountType>,EdgeType> graph, Set<Task<TimeType,PayloadType,AmountType>> visited) {
        logger.info("[getCurrentAvailableTasks]");
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
        logger.info("[chooseTask]");
        return availableTasks.size() > 0 ? availableTasks.get((int)((Math.random() * availableTasks.size()))) : null;
    }

    private <TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>>
    boolean resourceConstraintCheck(Map<String, Object> context, Map<String, Resource<TimeType, AmountType>> resources, Task<TimeType, PayloadType, AmountType> task) {
        //TODO: Add real logic of resource constraint checking...
        return true;
    }

    @SuppressWarnings("unchecked")
    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType, EdgeType>
    Map<String, AmountType> resourceSaturationCalculation(Map<String, Object> context
            , Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph
            , Map<String, Resource<TimeType, AmountType>> resources
            , Task<TimeType, PayloadType, AmountType> task
            , Set<Task<TimeType, PayloadType, AmountType>> visited) {
        logger.info("[resourceSaturationCalculation]");

        Map<String, AmountType> diff = new HashMap<>();

        Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>> map = (Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>>) context.get(TIME_MAP);

        RangeUtil.AmountCalculator<AmountType> amountCalculator = (RangeUtil.AmountCalculator<AmountType>)context.get(AMOUNT_CALCULATOR);

        Duo<TimeType, TimeType> selectedTime = calcTimeRange(context, graph, task, map.get(task));

        boolean isAcceptable = true;

        for(String resourceId : task.getResourceMap().keySet()) {
            Resource<TimeType, AmountType> resource = resources.get(resourceId);

            List<Duo<TimeType, TimeType>> ranges = new ArrayList<>();
            Map<Duo<TimeType, TimeType>, AmountType> resourceMap = new HashMap<>();

            for(Task<TimeType, PayloadType, AmountType> v : visited) {
                Duo<TimeType, TimeType> duo = Duo.duo(v.getPlannedStartTime(), v.getPlannedEndTime());
                ranges.add(duo);
                resourceMap.put(duo, task.getResourceMap().get(resourceId));
            }

            isAcceptable = RangeUtil.resourceCalculationByTimeRange(ranges, resourceMap, selectedTime, amountCalculator, resource.getProvider());

            if(!isAcceptable) {
                break;
            }

            diff.put(resourceId, task.getResourceMap().get(resourceId));
        }


        return isAcceptable ? diff : null;
    }

    @SuppressWarnings("unchecked")
    private
    <TimeType extends Comparable<TimeType>, PayloadType, AmountType extends Comparable<AmountType>, EdgeType>
    Duo<TimeType,TimeType> calcTimeRange(Map<String, Object> context
            , Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph
            , Task<TimeType, PayloadType, AmountType> task
            , Quartet<Long, Long, Long, Long> taskTime) {
        logger.info("[calcTImeRange]");
        GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType> timeCalculator = (GraphUtil.TimeCalculator<TimeType, Task<TimeType, PayloadType, AmountType>, EdgeType>)context.get(TIME_CALCULATOR);
        GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>> timeExtractor = (GraphUtil.TimeExtractor<Task<TimeType, PayloadType, AmountType>>)context.get(TIME_EXTRACTOR);
        //RangeUtil.AmountCalculator<AmountType> amountCalculator = (RangeUtil.AmountCalculator<AmountType>)context.get(AMOUNT_CALCULATOR);

        TimeType latestEndTime = graph.incomingEdgesOf(task).stream().map(e-> {
            Task<TimeType, PayloadType, AmountType> predecessor = graph.getEdgeSource(e);
            return predecessor.getPlannedEndTime();
        }).sorted(Comparator.reverseOrder()).findFirst().orElse(null);

        return Duo.duo(latestEndTime, timeCalculator.plus(latestEndTime, timeCalculator.fromLong(graph, task, timeExtractor.duration(task))));
    }

    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType>
    Resource<TimeType, AmountType> chooseResource(Map<String, Object> context
            , Map<String, Resource<TimeType, AmountType>> resources
            , Task<TimeType, PayloadType, AmountType> task, Map<String, AmountType> diff) {
        logger.info("[chooseResource]");
        return new ArrayList<>(resources.values()).get(0);
    }

    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType>
    void updateResource(Map<String, Object> context, Resource<TimeType, AmountType> resource, Task<TimeType, PayloadType, AmountType> task, Map<String, AmountType> diff) {
        logger.info("[updateResource] task : {}", task);
    }

    private
    <TimeType extends Comparable<TimeType>, AmountType extends Comparable<AmountType>, PayloadType>
    void updateTask(Map<String, Object> context, Task<TimeType, PayloadType, AmountType> task) {
        logger.info("[updateTask] task : {}", task);
    }
}
