package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.vo.Quartet;
import shi.quan.rcpsp.util.GraphUtil;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class JGraphTTest {
    private static final Logger logger = LoggerFactory.getLogger(JGraphTTest.class);


    @Test
    public void test() {
        Graph<String, DefaultEdge> graph = //new DefaultDirectedGraph<>(DefaultEdge.class);
                GraphTypeBuilder
                        .<String, DefaultEdge> directed()
                        .allowingMultipleEdges(false)
                        .allowingSelfLoops(false)
                        .edgeClass(DefaultEdge.class)
                        .weighted(true)
                        .buildGraph();

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("D", "E");
        graph.addEdge("B", "C");
        graph.addEdge("C", "E");

        graph.addEdge("F", "G");


//        Iterator<String> iterator = new DepthFirstIterator<>(graph, "A");
        Iterator<String> iterator = new BreadthFirstIterator<>(graph, "A");

        while (iterator.hasNext()) {
            String v = iterator.next();
            logger.info("{}" , v);
        }
    }

    @Test
    public void test1() {
        Graph<String, DefaultEdge> directedGraph =
                new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        directedGraph.addVertex("a");
        directedGraph.addVertex("b");
        directedGraph.addVertex("c");
        directedGraph.addVertex("d");
        directedGraph.addVertex("e");
        directedGraph.addVertex("f");
        directedGraph.addVertex("g");
        directedGraph.addVertex("h");
        directedGraph.addVertex("i");
        directedGraph.addEdge("a", "b");
        directedGraph.addEdge("b", "d");
        directedGraph.addEdge("d", "c");
        directedGraph.addEdge("c", "a");
        directedGraph.addEdge("e", "d");
        directedGraph.addEdge("e", "f");
        directedGraph.addEdge("f", "g");
        directedGraph.addEdge("g", "e");
        directedGraph.addEdge("h", "e");
        directedGraph.addEdge("i", "h");

        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>(v -> String.format("[%s]", v));

        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        exporter.exportGraph(directedGraph, outs);

        System.out.println("==================");
        System.out.println(outs.toString());
        System.out.println("------------------");

        // computes all the strongly connected components of the directed graph
        StrongConnectivityAlgorithm<String, DefaultEdge> scAlg =
                new KosarajuStrongConnectivityInspector<>(directedGraph);
        List<Graph<String, DefaultEdge>> stronglyConnectedSubgraphs =
                scAlg.getStronglyConnectedComponents();

        // prints the strongly connected components
        System.out.println("Strongly connected components:");
        for (int i = 0; i < stronglyConnectedSubgraphs.size(); i++) {
            System.out.println(stronglyConnectedSubgraphs.get(i));
        }
        System.out.println();

        // Prints the shortest path from vertex i to vertex c. This certainly
        // exists for our particular directed graph.
        System.out.println("Shortest path from i to c:");
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg =
                new DijkstraShortestPath<>(directedGraph);
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> iPaths = dijkstraAlg.getPaths("i");
        System.out.println(iPaths.getPath("c") + "\n");

        // Prints the shortest path from vertex c to vertex i. This path does
        // NOT exist for our particular directed graph. Hence the path is
        // empty and the result must be null.
        System.out.println("Shortest path from c to i:");
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> cPaths = dijkstraAlg.getPaths("c");
        System.out.println(cPaths.getPath("i"));
    }

    @Test
    public void test2() {
        Graph<String, DefaultEdge> directedGraph =
                new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        directedGraph.addVertex("a");
        directedGraph.addVertex("b");
        directedGraph.addVertex("c");
        directedGraph.addVertex("d");
        directedGraph.addVertex("e");
        directedGraph.addVertex("f");
        directedGraph.addVertex("g");
        directedGraph.addVertex("h");
        directedGraph.addVertex("i");

        directedGraph.addEdge("a", "b");
        directedGraph.addEdge("a", "c");
        directedGraph.addEdge("b", "d");
        directedGraph.addEdge("b", "e");
        directedGraph.addEdge("c", "e");
        directedGraph.addEdge("c", "f");
        directedGraph.addEdge("d", "g");
        directedGraph.addEdge("d", "h");
        directedGraph.addEdge("e", "h");
        directedGraph.addEdge("f", "g");
        directedGraph.addEdge("g", "i");
        directedGraph.addEdge("h", "i");

        Iterator<String> iterator = new BreadthFirstIterator<>(directedGraph, "a");

        List<String> expected = new ArrayList<>();

        while (iterator.hasNext()) {
            String v = iterator.next();
            expected.add(v);
        }

        List<String> actual = new ArrayList<>();
        GraphUtil.forwardBreadthVisit(directedGraph, "a", (v) -> {
            actual.add(v);
        });

        assertEquals(expected, actual, "FORWARD TRAVERSAL...");

        List<String> reversed = new ArrayList<>();

        GraphUtil.backwardBreadthVisit(directedGraph, "i", (v) -> {
            reversed.add(v);
        });

        logger.info("expected : {}", expected);
        logger.info("actual : {}", actual);
        logger.info("reversed : {}", reversed);
    }

    @Test
    public void test3() {
        Graph<String, DefaultEdge> directedGraph =
                new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        directedGraph.addVertex("a");
        directedGraph.addVertex("b");
        directedGraph.addVertex("c");
        directedGraph.addVertex("d");
        directedGraph.addVertex("e");
        directedGraph.addVertex("f");
        directedGraph.addVertex("g");
        directedGraph.addVertex("h");
        directedGraph.addVertex("i");

        directedGraph.addEdge("a", "b");
        directedGraph.addEdge("a", "c");
        directedGraph.addEdge("b", "d");
        directedGraph.addEdge("b", "e");
        directedGraph.addEdge("c", "e");
        directedGraph.addEdge("c", "f");
        directedGraph.addEdge("d", "g");
        directedGraph.addEdge("d", "h");
        directedGraph.addEdge("e", "h");
        directedGraph.addEdge("f", "g");
        directedGraph.addEdge("g", "i");
        directedGraph.addEdge("h", "i");

        Map<String, Long> durationMap = new HashMap<>();

        for(String v : directedGraph.vertexSet()) {
            durationMap.put(v, Math.random() > 0.5 ? 1L : 2L);
        }

        Map<String, Quartet<Long, Long, Long, Long>> map = GraphUtil.cpm(directedGraph, "a", "i", (v) -> durationMap.get(v));

        for(String v : directedGraph.vertexSet()) {
            logger.info("{} ({})= {}", v, durationMap.get(v), map.get(v));

        }

        Set<String> cpm = GraphUtil.cpm(map);

        logger.info("cpm : {}", cpm);
    }
}