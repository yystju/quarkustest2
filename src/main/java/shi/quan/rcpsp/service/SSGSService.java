package shi.quan.rcpsp.service;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.Duo;
import shi.quan.common.Quartet;
import shi.quan.common.exception.BuzzException;
import shi.quan.rcpsp.util.GraphUtil;
import shi.quan.rcpsp.vo.Task;

import javax.enterprise.context.RequestScoped;
import java.util.*;
import java.util.stream.Collectors;

@RequestScoped
public class SSGSService {
    private static final Logger logger = LoggerFactory.getLogger(SSGSService.class);
    public static final String DURATION_MAP = "DURATION_MAP";
    public static final String TIME_MAP = "TIME_MAP";
    public static final String CMP_SET = "CMP_SET";

    private static final String START_NODE = "START_NODE";
    private static final String END_NODE = "END_NODE";

    public <TimeType extends Comparable<TimeType>, PayloadType, AmountType, EdgeType> void ssgs(Map<String, Object> context, Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph, Object resources, long uBound) throws BuzzException {
        timeCalculation(context, graph);
        resourceAdjustment(context, resources);

        Set<Task<TimeType, PayloadType, AmountType>> lastAvailableTasks = null;

        Set<Task<TimeType, PayloadType, AmountType>> visited = new HashSet<>();

        if(uBound < 0L) {
            uBound = 5000L;
        }

        long loopCount = 0;

        for(;;) {
            if(loopCount > uBound) {
                throw new BuzzException(String.format("Exceed the uBound (%d/%d)...", loopCount, uBound));
            }

            loopCount++;

            List<Task<TimeType, PayloadType, AmountType>> availableTasks = getCurrentAvailableTasks(context, graph, visited);

            if(lastAvailableTasks != null && lastAvailableTasks.equals(availableTasks)) {
                logger.error("availableTasks : {}", availableTasks);
                logger.error("lastAvailableTasks : {}", lastAvailableTasks);
                throw new BuzzException("Endless loop detected...");
            }

            if(availableTasks == null || availableTasks.isEmpty()) {
                return;
            }

            lastAvailableTasks = new HashSet<>(availableTasks);

            logger.info("[MAIN LOOP] loopCount : {}, availableTasks : {}", loopCount, availableTasks);



            for (;;) {
                var task = chooseTask(context, availableTasks);

                if(task == null && availableTasks.isEmpty()) {
                    break;
                } else if(task == null) {
                    logger.error("Failed to choose a task from availableTasks : {}", availableTasks);
                    throw new BuzzException("Failed to choose a task...");
                }

                logger.info(">> CHOOSE THE TASK : {}", task);

                if (resourceConstraintCheck(context, task)) {
                    Object resourceHandler = resourceOccupationCalculation(context, task);

                    var chosen = chooseResource(context, task, resourceHandler);

                    if (chosen != null) {
                        updateResource(context, chosen, resourceHandler);
                        updateTask(context, chosen);
                    }
                }

                availableTasks.remove(task);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <TimeType extends Comparable<TimeType>, PayloadType, AmountType, EdgeType> void timeCalculation(Map<String, Object> context, Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph) throws BuzzException {
        List<Task<TimeType, PayloadType, AmountType>> startList = graph.vertexSet().stream().filter(v-> graph.incomingEdgesOf(v).isEmpty()).collect(Collectors.toList());

        if(startList.size() != 1) {
            throw new BuzzException("Only one start vertex is allowed.");
        }

        context.put(START_NODE, startList.get(0));

        logger.info("START_NODE : {}", startList.get(0));

        List<Task<TimeType, PayloadType, AmountType>> endList = graph.vertexSet().stream().filter(v-> graph.outgoingEdgesOf(v).isEmpty()).collect(Collectors.toList());

        if(endList.size() != 1) {
            throw new BuzzException("Only one end vertex is allowed.");
        }

        context.put(END_NODE, endList.get(0));

        logger.info("END_NODE : {}", endList.get(0));


        Map<Task<TimeType, PayloadType, AmountType>, Long> durationMap = (Map<Task<TimeType, PayloadType, AmountType>, Long>) context.get(DURATION_MAP);

        if(durationMap == null) {
            throw new BuzzException("DURATION_MAP is mandatory.");
        }

        Map<Task<TimeType, PayloadType, AmountType>, Quartet<Long, Long, Long, Long>> map = GraphUtil.cpm(graph, startList.get(0), endList.get(0), (v) -> {
            return durationMap.get(v);
        });


        context.put(TIME_MAP, map);

        logger.info("TIME_MAP : {}", map);

        Set<Task<TimeType, PayloadType, AmountType>> cmp = GraphUtil.cpm(map);

        context.put(CMP_SET, cmp);

        logger.info("CMP_SET : {}", cmp);
    }

    private void resourceAdjustment(Map<String, Object> context, Object resources) {
        //For now no adjustment is needed...
    }

    @SuppressWarnings("unchecked")
    private <PayloadType, TimeType extends Comparable<TimeType>, AmountType, EdgeType> List<Task<TimeType,PayloadType,AmountType>> getCurrentAvailableTasks(Map<String, Object> context, Graph<Task<TimeType,PayloadType,AmountType>,EdgeType> graph, Set<Task<TimeType,PayloadType,AmountType>> visited) {
        Task<TimeType, PayloadType, AmountType> startNode = (Task<TimeType, PayloadType, AmountType>)context.get(START_NODE);

        List<Task<TimeType,PayloadType,AmountType>> ret = new ArrayList<>();

        GraphUtil.forwardBreadthVisit(graph, startNode, (v) -> {
            if(!visited.contains(v)) {
                if (!graph.incomingEdgesOf(v).stream().anyMatch(e -> !visited.contains(graph.getEdgeSource(e)))) {
                    ret.add(v);
                }
            }
        });

        visited.addAll(ret);

        return ret;
    }

    private <PayloadType, TimeType extends Comparable<TimeType>, AmountType> Task<TimeType,PayloadType,AmountType> chooseTask(Map<String, Object> context, List<Task<TimeType,PayloadType,AmountType>> availableTasks) {
        return availableTasks.size() > 0 ? availableTasks.get((int)((Math.random() * availableTasks.size()))) : null;
    }

    private <TimeType, PayloadType, AmountType> boolean resourceConstraintCheck(Map<String, Object> context, Task<TimeType, PayloadType, AmountType> task) {
        //TODO: Add real logic of resource constraint checking...
        return true;
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> Object resourceOccupationCalculation(Map<String, Object> context, Task<TimeType, PayloadType, AmountType> task) {
        return new Object();
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> Task<TimeType, PayloadType, AmountType> chooseResource(Map<String, Object> context, Task<TimeType, PayloadType, AmountType> task, Object resource) {
        return task;
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> void updateResource(Map<String, Object> context, Task<TimeType, PayloadType, AmountType> task, Object resourceHandler) {
        logger.info("[updateResource] task : {}", task);
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> void updateTask(Map<String, Object> context, Task<TimeType, PayloadType, AmountType> task) {
        logger.info("[updateTask] task : {}", task);
    }
}
