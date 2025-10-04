import javax.imageio.IIOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        System.out.println("服务器启动，等待客户端链接...");
        try(ServerSocket serverSocket = new ServerSocket(50000)){
            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("一个客户端链接成功");
                try(
                        InputStream is = socket.getInputStream();
                        OutputStream os = socket.getOutputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        PrintWriter pw = new PrintWriter(os,true);
                ){
                    String str;
                    while((str=br.readLine())!=null){
                        System.out.println("收到客户端消息"+str);
                        pw.println("[服务器回复]：收到啦！");
                    }
                }catch(IOException e){
                    System.out.println("客户端"+socket.getRemoteSocketAddress());
                }
                System.out.println("客户端"+socket.getRemoteSocketAddress());
                break;
            }
        }catch(IOException e){
            System.out.println("服务器异常"+e.getMessage());
            e.printStackTrace();
        }
    }
}
