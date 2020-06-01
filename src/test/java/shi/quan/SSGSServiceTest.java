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
import shi.quan.rcpsp.vo.PrecedenceDiagrammingConstraint;
import shi.quan.rcpsp.vo.Relationship;
import shi.quan.rcpsp.vo.Task;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

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

        Task<Long, Integer, Integer> task1 = new Task<>("1", "Task1", 1);
        graph.addVertex(task1);

        Task<Long, Integer, Integer> task2 = new Task<>("2", "Task2", 1);
        graph.addVertex(task2);

        Task<Long, Integer, Integer> task3 = new Task<>("3", "Task3", 2);
        graph.addVertex(task3);

        Task<Long, Integer, Integer> task4 = new Task<>("4", "Task4", 3);
        graph.addVertex(task4);

        Task<Long, Integer, Integer> task5 = new Task<>("5", "Task5", 5);
        graph.addVertex(task5);

        Task<Long, Integer, Integer> task6 = new Task<>("6", "Task6", 9);
        graph.addVertex(task6);

        graph.addEdge(task1, task2);
        graph.addEdge(task1, task3);
        graph.addEdge(task2, task4);
        graph.addEdge(task3, task4);
        graph.addEdge(task4, task5);
        graph.addEdge(task5, task6);

        logger.info("graph : {}", graph);

        Object resource = new Object();

        Map<Task<Long, Integer, Integer>, Long> durationMap = new HashMap<>();

        for(Task<Long, Integer, Integer> task : graph.vertexSet()) {
            durationMap.put(task, (long)task.getPayload());
        }

        context.put(SSGSService.DURATION_MAP, durationMap);

        ssgsService.ssgs(context, graph, resource, 100);
    }

}