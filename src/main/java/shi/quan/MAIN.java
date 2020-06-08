package shi.quan;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.vo.Duo;
import shi.quan.rcpsp.service.SSGSService;
import shi.quan.rcpsp.util.GraphUtil;
import shi.quan.rcpsp.util.PSPLibUtil;
import shi.quan.rcpsp.util.RangeUtil;
import shi.quan.rcpsp.vo.Resource;
import shi.quan.rcpsp.vo.Task;
import shi.quan.vo.PSPModel;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@QuarkusMain
public class MAIN implements QuarkusApplication {
    private static final Logger logger = LoggerFactory.getLogger(MAIN.class);

    private static boolean verbose = false;

    @Inject
    SSGSService ssgsService;

    @Override
    public int run(String... args) throws Exception {
        long uBound = -1;

        if(args.length > 0) {
            File f = new File(args[0]);

            if(f.exists() && f.isFile()) {
                InputStream ins = new FileInputStream(f);

                PSPModel data = PSPLibUtil.load(verbose, ins);

                ins.close();

                if (verbose) logger.info("data : {}", data);

                Duo<Graph<Task<Integer, Integer, Integer>, DefaultEdge>, Map<String, Resource<Integer, Integer>>> duo = PSPLibUtil.convert(data);


                if (verbose) logger.info("graph.v : {}", duo.getK().vertexSet().stream().map(t -> String.format("%s::%s", t.getId(), t.getResourceMap())).collect(Collectors.toList()));
                if (verbose) logger.info("graph.e : {}", duo.getK().edgeSet().stream().map(e -> String.format("%s->%s", duo.getK().getEdgeSource(e).getId(), duo.getK().getEdgeTarget(e).getId())).collect(Collectors.toList()));
                if (verbose) logger.info("resource : {}", duo.getV());

                int total = duo.getK().vertexSet().stream().mapToInt(t->t.getPayload()).sum();
                double avg = total / duo.getK().vertexSet().size();
                int step = (int) (avg / 2.0d);
                int loopMax = (int)((double)total / (double)step);

                if (verbose) logger.info("total : {}", total);
                if (verbose) logger.info("avg : {}", avg);
                if (verbose) logger.info("step : {}", step);
                if (verbose) logger.info("loopMax : {}", loopMax);

                Map<String, Object> context = new HashMap<>();

                if (verbose) context.put(SSGSService.VERBOSE, "true");

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

        logger.error("--END--");

        return 0;
    }
}
