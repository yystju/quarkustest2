package shi.quan;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class RCPSPService {
    private static final Logger logger = LoggerFactory.getLogger(RCPSPService.class);

    public interface VertexAccessor<V> {
        int getLevel(V v);
    }

    public interface VertexMutator<V> {
        void setLevel(V v, int value);
    }

    public <V, E, T> List<V> ssgs(Graph<V, E> graph, V startNode) {
        Map<V, Integer> levelMap = new HashMap<>();

        visit(graph, startNode, new ArrayList<>()
                , s -> (levelMap.containsKey(s) ? levelMap.get(s) : 0)
                , (s, value) -> levelMap.put(s, value));

        logger.info("[ssgs] levelMap : {}", levelMap);

        return null;
    }

    public <V, E, T> List<V> psgs(Graph<V, E> graph, V startNode) {
        Map<V, Integer> timeMap = new HashMap<>();

        visit(graph, startNode, new ArrayList<>()
                , s -> (timeMap.containsKey(s) ? timeMap.get(s) : 0)
                , (s, value) -> timeMap.put(s, value));

        logger.info("[psgs] levelMap : {}", timeMap);

        return null;
    }

    private <V, E, T> void visit(Graph<V, E> graph, V node, List<V> visited, VertexAccessor<V> accessor, VertexMutator<V> mutator) {
        if(!visited.contains(node)) {
            visited.add(node);

            int maxLevel = graph.incomingEdgesOf(node).stream().mapToInt(e-> accessor.getLevel(graph.getEdgeSource(e))).max().orElse(0);

            mutator.setLevel(node, maxLevel + 1);

            Set<E> edges = graph.outgoingEdgesOf(node);

            for (E edge : edges) {
                visit(graph, graph.getEdgeTarget(edge), visited, accessor, mutator);
            }
        }
    }
}
