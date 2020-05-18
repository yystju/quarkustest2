package shi.quan;

import io.agroal.api.AgroalDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.*;

@ApplicationScoped
public class HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloService.class);

    @Inject
    AgroalDataSource dataSource;

    public String hello(String name) {
        Connection conn = null;

        try {
            conn = dataSource.getConnection();

            PreparedStatement pstmt = conn.prepareStatement("SELECT CURRENT_TIMESTAMP");

            ResultSet rs = pstmt.executeQuery();

            ResultSetMetaData meta = rs.getMetaData();

            while(rs.next()) {
                for(int i = 1; i <= meta.getColumnCount(); ++i) {
                    logger.info("{} ({}) : {}", meta.getColumnName(i), meta.getColumnType(i), rs.getObject(i));
                }
            }

            rs.close();

            pstmt.close();
        } catch (Exception ex) {
            logger.error("ERROR IN [hello]", ex);
        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return String.format("Hello, %s", name);
    }
}
