package shi.quan.rcpsp.util;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.Quartet;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtil {
    private static final Logger logger = LoggerFactory.getLogger(GraphUtil.class);

    public interface TimeExtractor<V> {
        long duration(V v);
    }

    /**
     * See https://youtu.be/-TDh-5n90vk for details...
     * Quartet<Long, Long, Long, Long> -> ES, EF, LS, LF
     */
    public static <V, E> Map<V, Quartet<Long, Long, Long, Long>> cpm(Graph<V, E> graph, V start, V end, TimeExtractor extractor) {
        Map<V, Quartet<Long, Long, Long, Long>> timeMap = new HashMap<>();

        for(V v : graph.vertexSet()) {
            timeMap.put(v, new Quartet<>(0L, 0L, 0L, 0L));
        }

        forwardBreadthVisit(graph, start, (v) -> {
            long maxEF = graph.incomingEdgesOf(v).stream()
                    .mapToLong(e-> timeMap.get(graph.getEdgeSource(e)).getTwo() /* EF */ ).max().orElse(0L);

            long duration = extractor.duration(v);

            if(timeMap.containsKey(v)) {
                timeMap.get(v).setOne(maxEF); //ES
                timeMap.get(v).setTwo(maxEF + duration); //EF
            }
        });

        long projectDuration = timeMap.values().stream().mapToLong(q-> q.getTwo()).max().orElse(0L);

        backwardBreadthVisit(graph, end, (v) -> {
            List<Long> lfList = graph.outgoingEdgesOf(v).stream()
                    .map(e-> {
                        V t = graph.getEdgeTarget(e);
                        return timeMap.get(t).getFour() /* LF */ - extractor.duration(t);
                    }).collect(Collectors.toList());

            long minLF = lfList.stream().mapToLong(l->l).max().orElse(projectDuration);

            long duration = extractor.duration(v);

            if(timeMap.containsKey(v)) {
                timeMap.get(v).setThree(minLF - duration); //LS
                timeMap.get(v).setFour(minLF); //LF
            }
        });

        return timeMap;
    }

    public static <V> Set<V> cpm(Map<V, Quartet<Long, Long, Long, Long>> map) {
        Set<V> cpm = new HashSet<>();

        for(V v : map.keySet()) {
            Quartet<Long, Long, Long, Long> quartet = map.get(v);

            if(quartet.getOne().equals(quartet.getThree()) && quartet.getTwo().equals(quartet.getFour())) {
                cpm.add(v);
            }
        }

        return cpm;
    }

    public interface Visitor<V> {
        void visit(V vertex);
    }

    public static <V, E> void forwardBreadthVisit(Graph<V, E> graph, V start, Visitor<V> visitor) {
        Set<V> vertices = new HashSet(graph.vertexSet());
        List<V> visited = new ArrayList<>();

        while(!vertices.isEmpty()) {
            for(var itor = vertices.iterator(); itor.hasNext(); ) {
                V v = itor.next();

                if(visitor != null && !graph.incomingEdgesOf(v).stream().anyMatch(e-> !visited.contains(graph.getEdgeSource(e)))) {
                    visitor.visit(v);
                    visited.add(v);
                    itor.remove();
                }
            }
        }
    }

    public static <V, E> void backwardBreadthVisit(Graph<V, E> graph, V end, Visitor<V> visitor) {
        Set<V> vertices = new HashSet(graph.vertexSet());
        List<V> visited = new ArrayList<>();

        while(!vertices.isEmpty()) {
            for(var itor = vertices.iterator(); itor.hasNext(); ) {
                V v = itor.next();

                if(visitor != null && !graph.outgoingEdgesOf(v).stream().anyMatch(e-> !visited.contains(graph.getEdgeTarget(e)))) {
                    visitor.visit(v);
                    visited.add(v);
                    itor.remove();
                }
            }
        }
    }
}
