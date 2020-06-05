package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.exception.BuzzException;
import shi.quan.common.vo.Duo;
import shi.quan.rcpsp.service.SSGSService;
import shi.quan.rcpsp.util.GraphUtil;
import shi.quan.rcpsp.util.RangeUtil;
import shi.quan.rcpsp.vo.Task;

import javax.inject.Inject;
import java.util.*;

@QuarkusTest
public class RangeTest {
    private static final Logger logger = LoggerFactory.getLogger(RangeTest.class);

    @Test
    public void test() throws BuzzException {
        List<Duo<Integer, Integer>> ranges = new ArrayList<>();

        int N = 10;

        for(int i = 0; i < N; ++i) {
            int one = (int)(Math.random() * N);
            int two = (int)(Math.random() * N);

            ranges.add(Duo.duo(Math.min(one, two), Math.max(one, two)));
        }

        Duo<Integer, Integer> selected = ranges.get((int)(Math.random() * ranges.size()));

        RangeUtil.getRangeSplitter(true, ranges, selected);
    }
    @Test
    public void test2() throws BuzzException {
        List<Duo<Integer, Integer>> ranges = new ArrayList<>();
        Map<Duo<Integer,Integer>, Integer> resourceMap = new HashMap<>();

        int N = 10;

        for(int i = 0; i < N; ++i) {
            int one = (int)(Math.random() * N);
            int two = (int)(Math.random() * N);

            Duo<Integer, Integer> duo = Duo.duo(Math.min(one, two), Math.max(one, two));

            ranges.add(duo);

            resourceMap.put(duo, 1);
        }

        Duo<Integer, Integer> selected = ranges.get((int)(Math.random() * ranges.size()));

        boolean result = RangeUtil.resourceCalculationByTimeRange(true, ranges, resourceMap, selected, new RangeUtil.AmountCalculator<>() {
            @Override
            public Integer zero() {
                return 0;
            }

            @Override
            public Integer plus(Integer a, Integer b) {
                return a + b;
            }

            @Override
            public Integer minus(Integer a, Integer b) {
                return a - b;
            }
        }, new RangeUtil.ResourceAmountProvider<Integer, Integer>() {
            @Override
            public Integer getResourceByTimeRange(Integer start, Integer end) {
                return 8;
            }

            @Override
            public Integer getResourceExtraTime(Integer start, Integer end) {
                return 0;
            }
        });

        logger.info("result : {}", result);

    }
}