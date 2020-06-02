package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@QuarkusTest
public class ConcurrencyTests {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyTests.class);

    @Test
    public void test() {
        int processorNumber = Runtime.getRuntime().availableProcessors();

        logger.info("# of processor : {}", processorNumber);

        int threadPoolSize = /* 2 * */ processorNumber;

        logger.info("# of threads : {}", threadPoolSize);

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);


        List<Future<String>> list = new ArrayList<Future<String>>();

        int N = 30000000;

        long start = System.currentTimeMillis();

        for(int i = 2; i < N; ++i) {
            final int number = i;
            Future<String> future = executor.submit(() -> {
                int n = (int)(Math.sqrt((double)number) + 1.0);
                boolean isPrime = true;

                for(int j = 2; j < n; ++j) {
                    if(number % j == 0) {
                        isPrime = false;
                        break;
                    }
                }

                if(isPrime)
                    return String.format("%s is a prime number.", number);
                else
                    return String.format("%s is NOT a prime number.", number);
            });

            list.add(future);
        }

        int totalPrime = 0;

        for(Future<String> future : list) {
            try {
                String result = future.get();

                if(!result.contains("NOT")) {
                    ++totalPrime;
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error("ERROR ON FUTURE GET.", e);
            }
        }

        long end = System.currentTimeMillis();

        logger.info("TOTAL PRIME IS {} FROM {}. INTERVAL : {}", totalPrime, N, end - start);

        executor.shutdown();
    }
}