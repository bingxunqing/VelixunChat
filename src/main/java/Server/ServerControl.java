package Server;

import db.DatabaseManager;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerControl {

    private static Properties props =  new Properties();

    static {
        try (InputStream input = ServerControl.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("致命错误: 找不到 classpath 下的 config.properties 文件！");
                System.exit(1);
            }
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    // 需要构建防护底线，如果被攻进来，限制此为最大注册数量，超过则失败
    private static final long Max_Users = 10000;
    // 多线程需要使用这个AtomicBoolean方法，否则线程干扰会导致重复执行
    private static AtomicBoolean registerDisable = new AtomicBoolean(false);
    // 存储时间用的map，每个ip对应一个时间队列
    private static final Map<String, Queue<Long>> registerHistory = new ConcurrentHashMap<>();
    // 每Limit_time可注册的用户数量
    private  static final int Limit_Cout_Per_Time = 30;
    private static final long Limit_Time = 5*60*1000;


    // 每次注册前需要调用的，判断ip是不是频繁注册
    public static boolean canRegister(String ip){
        if(registerDisable.get()){
            System.out.println("【系统日志】 服务器注册数量过多，已拒绝所有新注册请求。");
            return false;
        }

        long currentTime = System.currentTimeMillis();

        // computeIfAbsent方法，查找键值对的值，如果存在则返回值，不存在就执行第二个表达式
        Queue<Long> timestamp = registerHistory.computeIfAbsent(ip,k ->new ConcurrentLinkedQueue<>());
        timestamp.removeIf(ts -> currentTime-ts>Limit_Time);

        if(timestamp.size()>Limit_Cout_Per_Time){
            System.out.println("【系统日志】IP: " + ip + " 注册过于频繁 (5分钟内超过10次)，已拒绝。");
            return false;
        }

        return true;
    }

    // 每次注册完后判断有没有超过数据库限制的总人数，防止被攻破！
    public static void successRegister(String ip){

        long currentTime = System.currentTimeMillis();
        Queue<Long> timestamp = registerHistory.computeIfAbsent(ip,k ->new ConcurrentLinkedQueue<>());
        timestamp.add(currentTime);
        System.out.println("【系统日志】IP: " + ip + " 已更新注册记录。");

        long totalUsers = DatabaseManager.getTotalUsers();
        System.out.println("【系统日志】当前总用户数: " + totalUsers);
        if(totalUsers>Max_Users){
            registerDisable.set(true);
            System.out.println("【【【严重警告】】用户总数已达到阈值 " + Max_Users + "！已自动关闭全站注册功能！");
//            broadcastMessage("【【【严重警告】】 注册用户数量过多，暂时关闭注册功能！");
        }
    }

    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    private static ExecutorService theadpool = Executors.newFixedThreadPool(10);
    public static void main(String[] args) {
        DatabaseManager.init(props);
        System.out.println("等待链接中..");
        ServerControl server = new ServerControl();
        // 相关加密部分
        System.setProperty("javax.net.ssl.keyStore", "server.keystore");

        // 根目录下密钥文件的密码
        System.setProperty("javax.net.ssl.keyStorePassword", props.getProperty("keystore.password"));
        try
        {
            // 这个方法可以读取前面配置好的密钥信息并创建
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            try(ServerSocket serversocket = ssf.createServerSocket(50000)){
                while(true){
                    Socket clientSocket = serversocket.accept();
                    System.out.println("新客户端链接"+clientSocket.getRemoteSocketAddress());

                    ClientHandler clientTask = new ClientHandler(clientSocket,server);
                    theadpool.submit(clientTask);
                }
            } catch (IOException e) {
                theadpool.shutdown();
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    public void addClientHandler(ClientHandler clientTask){
        clients.add(clientTask);
        System.out.println("当前在线人数:"+clients.size());
    }

    public void removeClientHandler(ClientHandler clientTask){
        clients.remove(clientTask);
        System.out.println("当前在线人数"+clients.size());
    }

    public void broadcastMessage(String message){
        System.out.println("正在广播:"+message);
        for(ClientHandler clientTask : clients){
            clientTask.sendMessage(message);
        }
    }

    public ClientHandler getClientByUserName(String userName){
        for(ClientHandler clientTask : clients){
            if (clientTask.getCurrentUsername().equals(userName)){
                return clientTask;
            }
        }
        return null;
    }
}
