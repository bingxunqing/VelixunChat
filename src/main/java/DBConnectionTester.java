import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class DBConnectionTester {

    public static void main(String[] args) {
        System.out.println("正在初始化数据库连接池配置...");
        HikariConfig config = new HikariConfig();



        config.setJdbcUrl("jdbc:mysql://localhost:3306/chat_server?serverTimezone=UTC");
        config.setUsername("chat_user");
        config.setPassword("-------"); //测试再填密码


        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(10000);

        System.out.println("配置完成，正在创建数据源并尝试获取连接...");

        try (HikariDataSource ds = new HikariDataSource(config);
             Connection conn = ds.getConnection()) {

            System.out.println("!!!!!!!!!! 数据库连接成功 !!!!!!!!!!");
            System.out.println("!!!!!!!!!! 配置信息完全正确 !!!!!!!!!!");

        } catch (Exception e) {
            System.err.println("---------- 数据库连接失败！----------");
            System.err.println("---------- 请检查你的URL、用户名或密码！----------");
            e.printStackTrace();
        }
    }
}