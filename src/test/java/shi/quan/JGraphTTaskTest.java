package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.rcpsp.vo.PrecedenceDiagrammingConstraint;
import shi.quan.rcpsp.vo.Relationship;
import shi.quan.rcpsp.vo.Task;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

@QuarkusTest
public class JGraphTTaskTest {
    private static final Logger logger = LoggerFactory.getLogger(JGraphTTaskTest.class);


    @Test
    public void test() {
        Graph<Task<Integer, Integer, Integer>, Relationship<Task<Integer, Integer, Integer>, Long>> graph = GraphTypeBuilder
                        .<Task<Integer, Integer, Integer>, Relationship<Task<Integer, Integer, Integer>, Long>> directed()
                        .allowingMultipleEdges(false)
                        .allowingSelfLoops(false)
                        .edgeSupplier(() -> new Relationship<>())
                        .weighted(true)
                        .buildGraph();

        Task<Integer, Integer, Integer> task1 = new Task<>("1", "Task1", 1);
        graph.addVertex(task1);

        Task<Integer, Integer, Integer> task2 = new Task<>("2", "Task2", 1);
        graph.addVertex(task2);

        Task<Integer, Integer, Integer> task3 = new Task<>("3", "Task3", 2);
        graph.addVertex(task3);

        Task<Integer, Integer, Integer> task4 = new Task<>("4", "Task4", 3);
        graph.addVertex(task4);

        Task<Integer, Integer, Integer> task5 = new Task<>("5", "Task5", 5);
        graph.addVertex(task5);

        Task<Integer, Integer, Integer> task6 = new Task<>("6", "Task6", 9);
        graph.addVertex(task6);

        graph.addEdge(task1, task2)
                .setLagDuration(1000L)
                .setPrecedenceDiagrammingConstraint(PrecedenceDiagrammingConstraint.FS);
        graph.addEdge(task1, task3)
                .setLagDuration(1000L)
                .setPrecedenceDiagrammingConstraint(PrecedenceDiagrammingConstraint.FS);
        graph.addEdge(task2, task4)
                .setLagDuration(1000L)
                .setPrecedenceDiagrammingConstraint(PrecedenceDiagrammingConstraint.FS);
        graph.addEdge(task3, task4)
                .setLagDuration(1000L)
                .setPrecedenceDiagrammingConstraint(PrecedenceDiagrammingConstraint.FS);
        graph.addEdge(task4, task5)
                .setLagDuration(1000L)
                .setPrecedenceDiagrammingConstraint(PrecedenceDiagrammingConstraint.FS);
        graph.addEdge(task5, task6)
                .setLagDuration(1000L)
                .setPrecedenceDiagrammingConstraint(PrecedenceDiagrammingConstraint.FS);

        logger.info("graph : {}", graph);

        Iterator<Task<Integer, Integer, Integer>> iterator = new BreadthFirstIterator<>(graph, task1);

        while (iterator.hasNext()) {
            Task<Integer, Integer, Integer> task = iterator.next();

            logger.info("VISITING {}", task.getName());

            Set<Relationship<Task<Integer, Integer, Integer>, Long>> incomings = graph.incomingEdgesOf(task);

            if(incomings.isEmpty()) { // START UP POINT...
                logger.info("\t*START POINT*");
            } else {
                for(Relationship<Task<Integer, Integer, Integer>, Long> r : incomings) {
                    Task<Integer, Integer, Integer> source =  graph.getEdgeSource(r);
                    logger.info("\t<-{}", source.getName());
                }
            }

            Set<Relationship<Task<Integer, Integer, Integer>, Long>> outgoings = graph.outgoingEdgesOf(task);

            if(outgoings.isEmpty()) { // END POINT...
                logger.info("\t*END POINT*");
            } else {
                for(Relationship<Task<Integer, Integer, Integer>, Long> r : outgoings) {
                    Task<Integer, Integer, Integer> target =  graph.getEdgeTarget(r);
                    logger.info("\t->{}", target.getName());
                }
            }
        }
    }
}