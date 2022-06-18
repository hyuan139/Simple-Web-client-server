import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Simple Web Server.
 * Implementing non-persistent HTTP
 */
public class Server {
    private static final int PORT = 80;
    private static final String SERVER_FILE_PATH = "C:/CS158_SERVER/";
    private static final String ACCEPTED = "HTTP/1.1 200 OK";
    private static final String ERROR = "HTTP/1.1 404 Not Found";
    private static BufferedOutputStream bout;
    private static BufferedInputStream bin;

    /**
     * Parses the received GET message for the requested file name
     * @param request
     * @return
     */
    public static String parseGET(String request) {
        String parsed = "";
        // GET C:\CS158\hello.txt HTTP/1.1
        int pos = 0;
        for (int i = 0; i < request.length(); i++) {
            if (request.substring(i, i + 1).equals("/")) {
                pos = i;
            }
            if (request.substring(i, i + 1).equals(".")) {
                parsed = request.substring(pos + 1, i + 4);
                // find first occurrence of . then break
                break;
            }
        }
        return parsed;
    }

    /**
     * Check if the requested file exists on the web server
     * @param filename
     * @return
     */
    public static boolean fileExists(String filename) {
        boolean exists = false;
        String filePath = SERVER_FILE_PATH + filename;
        File file = new File(filePath);
        if (file.exists()) {
            exists = true;
        }
        return exists;
    }

    /**
     * Main application of web server
     * @param args
     */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            // waiting for connection; then accept, and create new socket
            Socket socket = serverSocket.accept();
            System.out.println("SOCKET CREATED WITH " + socket.getInetAddress() + "/" + socket.getLocalPort());
            byte[] data = new byte[1024];
            bin = new BufferedInputStream(socket.getInputStream());
            int readSize = bin.read(data);
            data = Arrays.copyOf(data, readSize);
            String request = new String(data).trim();
            String requestedFile = parseGET(request);
            System.out.println("A MESSAGE RECEIVED: " + request);
            System.out.println("THE PARSED HTTP REQUEST / FILE BEING REQUESTED: " + requestedFile);
            bout = new BufferedOutputStream(socket.getOutputStream());
            // CHECK IF FILE EXISTS ON SERVER
            if (fileExists(requestedFile)) {
                String url = SERVER_FILE_PATH + requestedFile;
                String response = ACCEPTED + "\n" + url;
                System.out.println("THE RESPONSE MESSAGE CONTENT: " + url);
                bout.write(response.getBytes());
                System.out.println("A MESSAGE HAS BEEN SENT: " + response);
            } else {
                System.out.println("THE RESPONSE MESSAGE CONTENT: 404 NOT FOUND. Please request the file again...");
                bout.write(ERROR.getBytes());
                System.out.println("A MESSAGE HAS BEEN SENT: " + ERROR);
            }
            bout.flush();

            // Close the connections
            bout.close();
            bin.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
