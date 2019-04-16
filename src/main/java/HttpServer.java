import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.net.*;
import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HttpServer implements Runnable {
    static final int PORT = 10006;
    private Socket socket;
    private String serverTime;
    private String parentPath = "src/main/resources/";
    private String requestFile;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private OutputStream outputStream;
    private PrintWriter printWriter;
    private BufferedOutputStream bufferedOutputStream;

    HttpServer(Socket socket) {
        this.socket = socket;
    }
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                HttpServer server = new HttpServer(serverSocket.accept());
                Thread thread = new Thread(server);
                thread.start();
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
        try {
            // Tell the operating system that the socket actively want the
            // client to connect this port. If client connect this port, we
            // get this socket.
            inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream = socket.getOutputStream();
            // OutputStream accepts bytes. If wants to send String instead of
            // byte, PrintWriter is needed.
            printWriter = new PrintWriter(outputStream);
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            String firstLine = bufferedReader.readLine();
            String method = firstLine.split(" ")[0];
            requestFile = firstLine.split(" ")[1];
            // If this is a GET request
            if (method.toUpperCase().equals("GET")) {
                if (requestFile.equals("/")) {
                    requestFile = "index.html";
                }
                File file = new File(parentPath, requestFile);
                byte[] byteFile = getFileByte(file);

                printWriter.println("HTTP/1.0 200 OK");
                // Generate Server Time in RFC 1123-format
                serverTime = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
                printWriter.println("Date: " + serverTime);
                String contentType = "text/html";
                printWriter.println("Content-Type: " + contentType);
                String contentLength = Integer.toString((int)file.length());
                printWriter.println("Content-Length: " + contentLength);
                // Empty line -> indicate the end of the header field
                printWriter.println("");
                printWriter.flush();

                bufferedOutputStream.write(byteFile);
                bufferedOutputStream.flush();
            } else {



            }
        } catch (IOException e1) {
            System.out.println("General error: " + e1.getMessage());
        } finally {
            try{
                inputStream.close();
                bufferedReader.close();
                outputStream.close();
                printWriter.close();
                bufferedOutputStream.close();
            } catch (IOException e) {
                System.out.println("Finally error: " + e.getMessage());
            }

        }
    }


    private byte[] getFileByte(File file) {
        byte[] byteFile = new byte[(int) file.length()];
        try{
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteFile);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            requestFile = "404.html";
            File notFoundFile = new File(parentPath, requestFile);
            byte[] bytefile = getFileByte(notFoundFile);
            printWriter.println("HTTP/1.0 404 Not Found");
            // Generate Server Time in RFC 1123-format
            serverTime = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
            printWriter.println("Date: " + serverTime);
            String contentType = "text/html";
            printWriter.println("Content-Type: " + contentType);
            String contentLength = Integer.toString((int)notFoundFile.length());
            printWriter.println("Content-Length: " + contentLength);
            // Empty line -> indicate the end of the header field
            printWriter.println("");
            printWriter.flush();
            try {
                bufferedOutputStream.write(bytefile);
                bufferedOutputStream.flush();
            } catch (IOException e2) {
                System.out.println("404 error: " + e2.getMessage());
            }
        } catch (IOException e1) {
            System.out.println("getFileByte error: " + e1.getMessage());
        }
        return byteFile;
    }
}
