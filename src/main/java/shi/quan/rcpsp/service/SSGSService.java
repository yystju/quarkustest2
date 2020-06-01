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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
public class SSGSService {
    private static final Logger logger = LoggerFactory.getLogger(SSGSService.class);
    public static final String DURATION_MAP = "DURATION_MAP";
    public static final String TIME_MAP = "TIME_MAP";
    public static final String CMP_SET = "CMP_SET";

    public <TimeType extends Comparable<TimeType>, PayloadType, AmountType, EdgeType> void ssgs(Map<String, Object> context, Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph, Object resources) throws BuzzException {
        timeCalculation(context, graph);
        resourceAdjustment(context, resources);

        Set<Task<TimeType, PayloadType, AmountType>> lastAvailableTasks = null;

        for(;;) {
            Set<Task<TimeType, PayloadType, AmountType>> availableTasks = getCurrentAvailableTasks();

            if((lastAvailableTasks != null && lastAvailableTasks.equals(availableTasks)) || (lastAvailableTasks == null && availableTasks == null)) {
                logger.error("availableTasks : {}", availableTasks);
                throw new BuzzException("Endless loop detected...");
            }

            if(availableTasks == null || availableTasks.isEmpty()) {
                return;
            }

            for (var task : availableTasks) {
                task = resourceConstraintCheck(task);

                if (task != null) {
                    Duo<TimeType, TimeType> timeDuo = startEndTimeCalculate(task);

                    var resourceHandler = resourceOccupationCalculation(task);

                    task = choosingResource(task, resourceHandler);

                    if (task != null) {
                        updateResource(task, resourceHandler);
                        updateTask(task);
                    }
                }
            }

            lastAvailableTasks = availableTasks;
        }
    }

    @SuppressWarnings("unchecked")
    private <TimeType extends Comparable<TimeType>, PayloadType, AmountType, EdgeType> void timeCalculation(Map<String, Object> context, Graph<Task<TimeType, PayloadType, AmountType>, EdgeType> graph) throws BuzzException {
        List<Task<TimeType, PayloadType, AmountType>> startList = graph.vertexSet().stream().filter(v-> graph.incomingEdgesOf(v).isEmpty()).collect(Collectors.toList());

        if(startList.size() != 1) {
            throw new BuzzException("Only one start vertex is allowed.");
        }

        List<Task<TimeType, PayloadType, AmountType>> endList = graph.vertexSet().stream().filter(v-> graph.outgoingEdgesOf(v).isEmpty()).collect(Collectors.toList());

        if(endList.size() != 1) {
            throw new BuzzException("Only one end vertex is allowed.");
        }


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
        throw new UnsupportedOperationException();
    }

    private <TimeType, PayloadType, AmountType> Set<Task<TimeType, PayloadType, AmountType>> getCurrentAvailableTasks() {
        throw new UnsupportedOperationException();
    }

    private <TimeType, PayloadType, AmountType> Task<TimeType, PayloadType, AmountType> resourceConstraintCheck(Task<TimeType, PayloadType, AmountType> task) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> Duo<TimeType, TimeType> startEndTimeCalculate(Task<TimeType, PayloadType, AmountType> task) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> Object resourceOccupationCalculation(Task<TimeType, PayloadType, AmountType> task) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> Task<TimeType, PayloadType, AmountType> choosingResource(Task<TimeType, PayloadType, AmountType> task, Object resource) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> void updateResource(Task<TimeType, PayloadType, AmountType> task, Object resource) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, AmountType, PayloadType> void updateTask(Task<TimeType, PayloadType, AmountType> task) {
        throw new UnsupportedOperationException();
    }
}
