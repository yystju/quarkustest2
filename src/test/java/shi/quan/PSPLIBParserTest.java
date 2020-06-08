package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.exception.BuzzException;
import shi.quan.common.vo.Duo;
import shi.quan.rcpsp.service.SSGSService;
import shi.quan.rcpsp.util.GraphUtil;
import shi.quan.rcpsp.util.PSPLibUtil;
import shi.quan.rcpsp.util.RangeUtil;
import shi.quan.rcpsp.vo.Resource;
import shi.quan.rcpsp.vo.Task;
import shi.quan.vo.PSPModel;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@QuarkusTest
public class PSPLIBParserTest {
    private static final Logger logger = LoggerFactory.getLogger(PSPLIBParserTest.class);

    private static boolean verbose = true;

    @Inject
    SSGSService ssgsService;

    @Test
    public void test() throws IOException, BuzzException {
        long uBound = -1;

        URL url = PSPLIBParserTest.class.getResource("/");

        logger.info("url : {}", url);

        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/example_x.sm");
//        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/example_4.sm");
//        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/example_10.sm");

        PSPModel data = PSPLibUtil.load(verbose, ins);

        ins.close();

        logger.info("data : {}", data);

        Duo<Graph<Task<Integer, Integer, Integer>, DefaultEdge>, Map<String, Resource<Integer, Integer>>> duo = PSPLibUtil.convert(data);


        logger.info("graph.v : {}", duo.getK().vertexSet().stream().map(t -> String.format("%s::%s", t.getId(), t.getResourceMap())).collect(Collectors.toList()));
        logger.info("graph.e : {}", duo.getK().edgeSet().stream().map(e -> String.format("%s->%s", duo.getK().getEdgeSource(e).getId(), duo.getK().getEdgeTarget(e).getId())).collect(Collectors.toList()));
        logger.info("resource : {}", duo.getV());

        int total = duo.getK().vertexSet().stream().mapToInt(t->t.getPayload()).sum();
        double avg = total / duo.getK().vertexSet().size();
        int step = (int) (avg / 2.0d);
        int loopMax = (int)((double)total / (double)step);

        logger.info("total : {}", total);
        logger.info("avg : {}", avg);
        logger.info("step : {}", step);
        logger.info("loopMax : {}", loopMax);

        Map<String, Object> context = new HashMap<>();

//        context.put(SSGSService.VERBOSE, "true");
        context.put(SSGSService.OFFSET_MAX, String.format("%d", loopMax));
        context.put(SSGSService.OFFSET_STEP, String.format("%d", step));

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

        GraphUtil.TimeCalculator<Integer, Task<Integer, Integer, Integer>, DefaultEdge> timeCalculator = new GraphUtil.TimeCalculator<>() {
            @Override
            public Integer zero() {
                return 0;
            }

            @Override
            public Integer now() {
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

            @Override
            public Integer fromLong(Graph<Task<Integer, Integer, Integer>, DefaultEdge> graph, Task<Integer, Integer, Integer> task, long value) {
                return (int)value;
            }
        };

        GraphUtil.TimeExtractor<Task<Integer, Integer, Integer>> timeExtractor = task -> task.getPayload();

        ssgsService.ssgs(context, duo.getK(), duo.getV(), amountCalculator, timeCalculator, timeExtractor, new SSGSService.EventListener<Integer, Integer, Integer>() {
            @Override
            public void onTaskProcessed(Map<String, Object> context, Task<Integer, Integer, Integer> task, int processed, int total) {
                System.out.println(String.format("[%05d/%05d] %s (%d:%d - %d)", processed, total, task.getId(), task.getPlannedStartTime(), task.getPlannedEndTime(), task.getPayload()));
            }
        }, uBound);
    }
}