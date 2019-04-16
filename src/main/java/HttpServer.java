import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class HttpServer implements Runnable {
    static final int PORT = 10008;




    public static void main(String[] args) {
        try {
            final ServerSocket serverSocket = new ServerSocket(PORT);

            // Tell the operating system that the socket actively want the
            // client to connect this port. If client connect this port, we
            // get this socket.
            while (true) {
                final Socket socket = serverSocket.accept();
                System.out.println("Connect to " + socket.getRemoteSocketAddress());

                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }

                OutputStream outputStream = socket.getOutputStream();

                // OutputStream accepts bytes. If wants to send String instead of
                // byte, PrintWriter is needed.
                PrintWriter printWriter = new PrintWriter(outputStream);
                printWriter.println("HTTP/1.0 200 OK");

                // Generate Server Time in RFC 1123-format
                String serverTime = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
                printWriter.println("Date: " + serverTime);
                String contentType = "text/html";
                printWriter.println("Content-Type: " + contentType);
                String contentLength = "12";
                printWriter.println("Content-Length: " + contentLength);
                // Empty line -> indicate the end of the header field
                printWriter.println("");
                printWriter.println("Hello World!");
                printWriter.flush();

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {


    }
}
