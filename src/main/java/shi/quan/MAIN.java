package shi.quan;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@QuarkusMain
public class MAIN implements QuarkusApplication {
    private static final Logger logger = LoggerFactory.getLogger(MAIN.class);


    @Override
    public int run(String... args) throws Exception {
        logger.info("MAIN args : {}", (Object[])args);
        return 0;
    }
}
