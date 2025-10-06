package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
   private static HikariDataSource dataSource;


    public static void init(Properties props) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/chat_server?serverTimezone=UTC&useSSL=false");
            config.setUsername("chat_user");

            // 从传入的props获取数据库密码，只需要导入一次properties文件
            String dbPassword = props.getProperty("db.password");
            if (dbPassword == null || dbPassword.trim().isEmpty()) {
                System.err.println("致命错误: 配置文件 config.properties 中缺少或未设置 db.password");
                System.exit(1);
            }
            config.setPassword(dbPassword);

            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(10000); // 加上连接超时是一个好习惯

            // 创建数据源实例
            dataSource = new HikariDataSource(config);
            System.out.println("数据库连接池初始化成功。");

        } catch (Exception e) {
            System.err.println("致命错误: 数据库连接池初始化失败！请检查数据库配置、网络连接或JDBC驱动。");
            e.printStackTrace();
            System.exit(1); // 初始化失败是致命错误，直接退出
        }
    }

   // 此处可以使用 throws 来预定可能的错误，之后调用再使用catch方法处理，因为后续不知道这个链接
    // 会链接到哪里，这样留给后续精细化处理应该是最佳实践
   public static Connection getConnection() throws SQLException {
       return dataSource.getConnection();
   }

   public static boolean registerUser(String username, String password) {
//       System.out.println("11--------------------");
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users(username,password_hash) VALUES(?,?)";
//       System.out.println("22--------------------------");
        try(
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);

                ) {
//            System.out.println("33---------------------------");
            pstmt.setString(1, username);
//            System.out.println("44---------------------------");
            pstmt.setString(2, hashedPassword);
//            System.out.println("55----------------------------");
            pstmt.executeUpdate();
//            System.out.println("66----------------------");
            return true;
        } catch (SQLException e) {
//            System.out.println("13213121312312312312312312312");
            System.err.println("注册数据库操作失败: " + e.getMessage());
            return false;
        }
   }

   public static boolean checkLogin(String username, String password) {
       String sql = "SELECT password_hash FROM users WHERE username = ?";
       try(
               Connection conn = getConnection();
               PreparedStatement pstmt = conn.prepareStatement(sql)
               ) {
           pstmt.setString(1, username);
           ResultSet rs = pstmt.executeQuery();
           if (rs.next()) {
               String storedHash = rs.getString("password_hash");
               return BCrypt.checkpw(password, storedHash);
           }
            return  false;
       } catch (SQLException e) {
           e.printStackTrace();
           return false;
       }

   }

   public static long getTotalUsers() {
       String sql = "SELECT COUNT(*) FROM users";
       try(
               Connection conn = getConnection();
               PreparedStatement pstmt = conn.prepareStatement(sql);
               ResultSet rs = pstmt.executeQuery();
               ) {
           if (rs.next()) {
               return rs.getLong(1);
           }

       } catch (SQLException e) {
           System.err.println("【数据库错误】 查询用户总数失败: " + e.getMessage());
           e.printStackTrace();
           return -1;
       }
       return 0;
   }


}
