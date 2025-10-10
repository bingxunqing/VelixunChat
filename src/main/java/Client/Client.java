package Client;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class Client {
    private Queue<String> queue = new PriorityQueue<>();
    public static void main(String[] args) throws IOException {
        String serverIP = "48.210.84.122";
        int port = 50000;

        // 相关加密部分
        System.setProperty("javax.net.ssl.trustStore", "client.truststore");

//         根目录下密钥文件的密码
        System.setProperty("javax.net.ssl.trustStorePassword", "321681398");

//        System.out.println("正在链接服务器"+serverIP+":"+port);
        try(Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try(
                    Socket socket = sf.createSocket(serverIP,port);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    ){
                System.out.println("已连接到服务器！");
                ServerListener listener = new ServerListener(br);
                new Thread(listener).start();

                String msg;
                while(!(msg = sc.nextLine()).equalsIgnoreCase("exit")){
                    pw.println(msg);
                    pw.flush();
                }

            }catch (IOException e) {
                System.err.println("客户端错误: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("断开链接...");
        }

    }

    static class ServerListener implements Runnable{
        private BufferedReader reader;

        public ServerListener(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            try{
                String serverMsg;
                while((serverMsg = reader.readLine())!=null){
                    System.out.println(serverMsg);
                }
            } catch (IOException e) {
                System.out.println("【系统消息】 与服务器的连接已断开，程序将退出。");
                System.exit(0);
            }
        }
    }

}
