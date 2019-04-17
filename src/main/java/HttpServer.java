import java.net.*;
import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpServer implements Runnable {
    static final int PORT = 10006;
    private Socket socket;
    private String serverTime;
    private String parentPath = "src/main/resources/";
    private String requestFile;
    private InputStream inputStream = null;
    private BufferedReader bufferedReader = null;
    private OutputStream outputStream = null;
    private PrintWriter printWriter = null;
    private boolean rangeRequest;
    private String range;
    private byte[] byteFile;
    private BufferedOutputStream bufferedOutputStream = null;
    // Use thread pool for better thread management
    private static final Executor exec = Executors.newCachedThreadPool();

    /**
     * Constructor of the HttpServer
     *
     * @param socket
     */
    HttpServer(Socket socket) {
        this.socket = socket;
    }

    /**
     * Main Function that starts the server
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                HttpServer server = new HttpServer(serverSocket.accept());
                exec.execute(server);
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
            bufferedReader =
                    new BufferedReader(new InputStreamReader(inputStream));
            outputStream = socket.getOutputStream();
            // OutputStream accepts bytes. If wants to send String instead of
            // byte, PrintWriter is needed.
            printWriter = new PrintWriter(outputStream);
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            String firstLine = bufferedReader.readLine();
            if (firstLine != null && firstLine.split(" ").length >= 2) {
                String method = firstLine.split(" ")[0];
                requestFile = firstLine.split(" ")[1];

                String requestHeader = "";
                while ((requestHeader = bufferedReader.readLine()).length() != 0) {
                    System.out.println(requestHeader);
                    if (requestHeader.split(":")[0].toLowerCase().equals("range")) {
                        this.rangeRequest = true;
                        this.range = requestHeader.split(":")[1].split("=")[1];
                    }
                }

                // If this is a GET request
                if (method.toUpperCase().equals("GET")) {
                    if (requestFile.equals("/")) {
                        requestFile = "index.html";
                    }

                    File file = new File(parentPath, requestFile);
                    byteFile = getFileByte(file);

                    if (this.rangeRequest) {
                        int start, end = 0;
                        start = Integer.parseInt(this.range.split("-")[0]);
                        if (this.range.split("-").length == 1) {
                            end = byteFile.length;
                        } else {
                            end = Integer.parseInt(this.range.split("-")[1]) + 1;
                        }
                        byteFile = Arrays.copyOfRange(byteFile, start, end);
                        printWriter.println("HTTP/1.0 206 Partial content");
                    } else {
                        printWriter.println("HTTP/1.0 200 OK");
                    }

                    // Generate Server Time in RFC 1123-format
                    serverTime =
                            DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
                    printWriter.println("Date: " + serverTime);
                    String suffix =
                            requestFile.substring(requestFile.lastIndexOf(".") + 1).toLowerCase();

                    // Parse the request file type
                    String contentType = "";
                    switch (suffix) {
                        case "txt":
                            contentType = "text/plain";
                            break;
                        case "css":
                            contentType = "text/css";
                            break;
                        case "gif":
                            contentType = "image/gif";
                            break;
                        case "jpg":
                        case "jpeg":
                            contentType = "image/jpeg";
                            break;
                        case "png":
                            contentType = "image/png";
                            break;
                        case "js":
                            contentType = "application/javascript";
                            break;
                        case "html":
                        case "htm":
                            contentType = "text/html";
                            break;
                        case "mp4":
                            contentType = "video/mp4";
                            break;
                        case "webm":
                            contentType = "video/webm";
                            break;
                        case "ogg":
                            contentType = "video/ogg";
                            break;
                        default:
                            contentType = "application/octet-stream";
                    }
                    String contentLength = Integer.toString((int) file.length());
                    if (this.rangeRequest) {
                        printWriter.println("Content-Range: bytes" + this.range + "/" + contentLength);
                        contentLength = Integer.toString(byteFile.length);
                    }
                    printWriter.println("Content-Length: " + contentLength);
                    printWriter.println("Content-Type: " + contentType);
                    // Empty line -> indicate the end of the header field
                    printWriter.println("");
                    printWriter.flush();
                    bufferedOutputStream.write(byteFile);
                    bufferedOutputStream.flush();
                }
            }
        } catch (IOException e1) {
            System.out.println("General error: " + e1.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (printWriter != null) {
                    printWriter.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
            } catch (IOException e) {
                System.out.println("Finally error: " + e.getMessage());
            }
        }
    }

    /**
     * Method that transform file into byte array
     *
     * @param file
     * @return byte array
     */
    private byte[] getFileByte(File file) {
        byte[] byteFile = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteFile);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            requestFile = "404.html";
            File notFoundFile = new File(parentPath, requestFile);
            byte[] bytefile = getFileByte(notFoundFile);
            printWriter.println("HTTP/1.0 404 Not Found");
            // Generate Server Time in RFC 1123-format
            serverTime =
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
            printWriter.println("Date: " + serverTime);
            String contentType = "text/html";
            printWriter.println("Content-Type: " + contentType);
            String contentLength =
                    Integer.toString((int) notFoundFile.length());
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
        } finally {
            try {
                printWriter.close();
                bufferedOutputStream.close();
            } catch (IOException e) {
                System.out.println("404 Finally error: " + e.getMessage());
            }
        }
        return byteFile;
    }
}
