import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class test {
    private static final Executor threadPool = Executors.newFixedThreadPool(5);

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(10009);
            while (true) {
                HttpServer server = new HttpServer(serverSocket.accept());
                threadPool.execute(server);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
