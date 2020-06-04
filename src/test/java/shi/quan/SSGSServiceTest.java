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
import shi.quan.rcpsp.vo.PrecedenceDiagrammingConstraint;
import shi.quan.rcpsp.vo.Relationship;
import shi.quan.rcpsp.vo.Resource;
import shi.quan.rcpsp.vo.Task;

import javax.inject.Inject;
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

        resources.put("1", new Resource<>("1", "Resource 1", (start, end) -> 3));

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

        ssgsService.ssgs(context, graph, resources, amountCalculator, timeCalculator, timeExtractor, 100);
    }

    @Test
    public void test2() throws BuzzException {
        Map<String, Object> context = new HashMap<>();

        Graph<Task<Long, Integer, Integer>, DefaultEdge> graph = GraphTypeBuilder
                .<Task<Long, Integer, Integer>, DefaultEdge> directed()
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(true)
                .buildGraph();

        int N = 10;
        int M = 5;

        for(int i = 0; i < N; ++i) {
            Task<Long, Integer, Integer> task = new Task<>(String.format("%d", i), String.format("Task%d", i), ((int)(Math.random() * 5.0) * 10));
            for(int j = 0; j < M; ++j) {
                task.getResourceMap().put(String.format("%d", j), 1);
            }
            graph.addVertex(task);
        }

        Set<Task<Long, Integer, Integer>> vertices = graph.vertexSet();

        for(Task<Long, Integer, Integer> v1 : vertices) {
           for(Task<Long, Integer, Integer> v2: vertices) {
               if(v1.compareTo(v2) < 0) {
                   graph.addEdge(v1, v2);
               }
           }
        }

        logger.info("graph : {}", graph);

        Map<String, Resource<Long, Integer>> resources = new HashMap<>();

        for(int i = 0; i < M; ++i) {
            String id = String.format("%d", i);
            resources.put(id, new Resource<>(id, String.format("Resource %d", i), (start, end) -> 50));
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
                return (long)task.getPayload();
            }
        };

        GraphUtil.TimeExtractor<Task<Long, Integer, Integer>> timeExtractor = task -> task.getPayload();

        ssgsService.ssgs(context, graph, resources, amountCalculator, timeCalculator, timeExtractor, 100);
    }

}