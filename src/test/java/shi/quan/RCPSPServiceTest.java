package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class RCPSPServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(RCPSPServiceTest.class);

    @Inject
    RCPSPService rcpspService;

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

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("D", "E");
        graph.addEdge("B", "C");
        graph.addEdge("C", "E");
        graph.addEdge("C", "F");
        graph.addEdge("E", "F");

        rcpspService.ssgs(graph, "A");
    }
}