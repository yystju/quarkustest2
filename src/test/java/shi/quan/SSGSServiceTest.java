package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.exception.BuzzException;
import shi.quan.rcpsp.service.SSGSService;
import shi.quan.rcpsp.util.GraphUtil;
import shi.quan.rcpsp.util.RangeUtil;
import shi.quan.rcpsp.vo.*;

import javax.inject.Inject;
import java.sql.Time;
import java.util.*;

@QuarkusTest
public class SSGSServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(SSGSServiceTest.class);

    @Inject
    SSGSService ssgsService;

    @Test
    public void test() throws BuzzException {
        Map<String, Object> context = new HashMap<>();

        Graph<Task<Long, Integer, Integer>, DefaultEdge> graph = GraphTypeBuilder
                .<Task<Long, Integer, Integer>, DefaultEdge> directed()
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(true)
                .buildGraph();

        Task<Long, Integer, Integer> task1 = new Task<>("1", "Task1", 10);
        task1.getResourceMap().put("1", 1);
        graph.addVertex(task1);

        Task<Long, Integer, Integer> task2 = new Task<>("2", "Task2", 10);
        task2.getResourceMap().put("1", 1);
        graph.addVertex(task2);

        Task<Long, Integer, Integer> task3 = new Task<>("3", "Task3", 20);
        task3.getResourceMap().put("1", 1);
        graph.addVertex(task3);

        Task<Long, Integer, Integer> task4 = new Task<>("4", "Task4", 30);
        task4.getResourceMap().put("1", 1);
        graph.addVertex(task4);

        Task<Long, Integer, Integer> task5 = new Task<>("5", "Task5", 50);
        task5.getResourceMap().put("1", 1);
        graph.addVertex(task5);

        Task<Long, Integer, Integer> task6 = new Task<>("6", "Task6", 90);
        task6.getResourceMap().put("1", 1);
        graph.addVertex(task6);

        graph.addEdge(task1, task2);
        graph.addEdge(task1, task3);
        graph.addEdge(task2, task4);
        graph.addEdge(task3, task4);
        graph.addEdge(task4, task5);
        graph.addEdge(task5, task6);

        logger.info("graph : {}", graph);

        Map<String, Resource<Long, Integer>> resources = new HashMap<>();

        Resource<Long, Integer> resource = new Resource<Long, Integer>("0", "Resource 1", new RangeUtil.ResourceAmountProvider<>() {
            @Override
            public Integer getResourceByTimeRange(Long start, Long end) {
                return 1;
            }

            @Override
            public Long getResourceExtraTime(Long start, Long end) {
                return 1L;
            }
        });

        resource.getInstanceList().add(new ResourceInstance<Long, Integer>(resource, "1", "Resource Instance 1", null));

        resources.put("1", resource);

        RangeUtil.AmountCalculator<Integer> amountCalculator = new RangeUtil.AmountCalculator<>() {
            @Override
            public Integer zero() {
                return 0;
            }

            @Override
            public Integer plus(Integer a, Integer b) {
                return (a != null ? a : 0) + (b != null ? b : 0);
            }

            @Override
            public Integer minus(Integer a, Integer b) {
                return (a != null ? a : 0) - (b != null ? b : 0);
            }
        };

        GraphUtil.TimeCalculator<Long, Task<Long, Integer, Integer>, DefaultEdge> timeCalculator = new GraphUtil.TimeCalculator<>() {
            @Override
            public Long zero() {
                return 0L;
            }

            @Override
            public Long now() {
                return System.currentTimeMillis();
            }

            @Override
            public Long plus(Long a, Long b) {
                return (a != null ? a : 0L) + (b != null ? b : 0L);
            }

            @Override
            public Long minus(Long a, Long b) {
                return (a != null ? a : 0L) - (b != null ? b : 0L);
            }

            @Override
            public Long fromLong(Graph<Task<Long, Integer, Integer>, DefaultEdge> graph, Task<Long, Integer, Integer> task, long value) {
                return (long)task.getPayload();
            }
        };

        GraphUtil.TimeExtractor<Task<Long, Integer, Integer>> timeExtractor = task -> task.getPayload();

        ssgsService.ssgs(context, graph, resources, amountCalculator, timeCalculator, timeExtractor, new SSGSService.EventListener<Long, Integer, Integer>() {
            @Override
            public void onTaskProcessed(Map<String, Object> context, Task<Long, Integer, Integer> task, int processed, int total) {
                System.out.println(String.format("[%05d/%05d] %s (%d:%d - %d)", processed, total, task.getId(), task.getPlannedStartTime(), task.getPlannedEndTime(), task.getPayload()));
            }
        }, 100);
    }

    @Test
    public void test2() throws BuzzException {
        int HORIZONTAL = 50;
        int VERTICAL = 50;
        int RESOURCE_SIZE = 40;
        int RESOURCE_INSTANCE_SIZE = Math.max(HORIZONTAL, VERTICAL);
        int RESOURCE_INSTANCE_CAP = 1;
        int uBound = 1000;
        int PAYLOAD = 10;

        Map<String, Object> context = new HashMap<>();

        Graph<Task<Long, Integer, Integer>, DefaultEdge> graph = GraphTypeBuilder
                .<Task<Long, Integer, Integer>, DefaultEdge> directed()
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(true)
                .buildGraph();


        int total = HORIZONTAL * VERTICAL * PAYLOAD;
        double avg = PAYLOAD;
        int step = 1;
        int loopMax = 100; // HORIZONTAL * VERTICAL;

        logger.info("total : {}", total);
        logger.info("avg : {}", avg);
        logger.info("step : {}", step);
        logger.info("loopMax : {}", loopMax);

//        context.put(SSGSService.VERBOSE, "true");
        context.put(SSGSService.OFFSET_MAX, String.format("%d", loopMax));
        context.put(SSGSService.OFFSET_STEP, String.format("%d", step));

        ArrayList<ArrayList<Task<Long, Integer, Integer>>> tasksArray = new ArrayList<>();

        for(int i = 1; i <= HORIZONTAL; ++i) {
            ArrayList<Task<Long, Integer, Integer>> tasks = new ArrayList<>();
            for(int j = 1; j <= VERTICAL; ++j) {
                String id = String.format("%05d::%05d", i, j);
                Task<Long, Integer, Integer> task = new Task<>(id, String.format("TASK %s", id), PAYLOAD);

                for(int k = 1; k <= RESOURCE_SIZE; ++k) {
                    task.getResourceMap().put(String.format("%05d", k), 1);
                }

                graph.addVertex(task);

                tasks.add(task);
            }

            tasksArray.add(tasks);
        }

        for(int i = 0; i < tasksArray.size(); ++i) {
            ArrayList<Task<Long, Integer, Integer>> tasks = tasksArray.get(i);
            for(int j = 0; j < tasks.size(); ++j) {
                if(j != 0) {
                    graph.addEdge(tasks.get(j -1), tasks.get(j));
                }

                if(i != 0) {
                    graph.addEdge(tasksArray.get(i - 1).get(j), tasks.get(j));
                }
            }
        }

        logger.info("graph : {}", graph);

        Map<String, Resource<Long, Integer>> resources = new HashMap<>();

        for(int i = 1; i <= RESOURCE_SIZE; ++i) {
            String id = String.format("%05d", i);
            Resource<Long, Integer> resource =  new Resource<>(id, String.format("RESOURCE %s", id), new RangeUtil.ResourceAmountProvider<Long, Integer>() {
                @Override
                public Integer getResourceByTimeRange(Long start, Long end) {
                    return RESOURCE_INSTANCE_CAP;
                }

                @Override
                public Long getResourceExtraTime(Long start, Long end) {
                    return 0L;
                }
            });

            for(int j = 1; j <= RESOURCE_INSTANCE_SIZE; ++j) {
                String instanceId = String.format("%05d::%05d", i, j);
                resource.getInstanceList().add(new ResourceInstance(resource, instanceId, String.format("Resource Instance %s", instanceId), null));
            }
            resources.put(id, resource);
        }

        RangeUtil.AmountCalculator<Integer> amountCalculator = new RangeUtil.AmountCalculator<>() {
            @Override
            public Integer zero() {
                return 0;
            }

            @Override
            public Integer plus(Integer a, Integer b) {
                return (a != null ? a : 0) + (b != null ? b : 0);
            }

            @Override
            public Integer minus(Integer a, Integer b) {
                return (a != null ? a : 0) - (b != null ? b : 0);
            }
        };

        GraphUtil.TimeCalculator<Long, Task<Long, Integer, Integer>, DefaultEdge> timeCalculator = new GraphUtil.TimeCalculator<>() {
            @Override
            public Long zero() {
                return 0L;
            }

            @Override
            public Long now() {
                return System.currentTimeMillis();
            }

            @Override
            public Long plus(Long a, Long b) {
                return (a != null ? a : 0L) + (b != null ? b : 0L);
            }

            @Override
            public Long minus(Long a, Long b) {
                return (a != null ? a : 0L) - (b != null ? b : 0L);
            }

            @Override
            public Long fromLong(Graph<Task<Long, Integer, Integer>, DefaultEdge> graph, Task<Long, Integer, Integer> task, long value) {
                return (long)value;
            }
        };

        GraphUtil.TimeExtractor<Task<Long, Integer, Integer>> timeExtractor = task -> task.getPayload();

        ssgsService.ssgs(context, graph, resources, amountCalculator, timeCalculator, timeExtractor, new SSGSService.EventListener<Long, Integer, Integer>() {
            @Override
            public void onTaskProcessed(Map<String, Object> context, Task<Long, Integer, Integer> task, int processed, int total) {
                System.out.println(String.format("[%05d/%05d] %s (%d:%d - %d)", processed, total, task.getId(), task.getPlannedStartTime(), task.getPlannedEndTime(), task.getPayload()));
            }
        }, uBound);
    }

}