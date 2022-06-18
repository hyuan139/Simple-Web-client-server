import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Simple Web Client
 */
public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int PORT = 80;
    private static final String GET = "GET ";
    private static final String HTTP_VERSION = " HTTP/1.1";
    private static final String URL_SERVER = "C:/CS158_SERVER/";
    private static final String URL_CLIENT = "C:/CS158_CLIENT/";
    private static BufferedOutputStream bout;
    private static BufferedInputStream bin;

    /**
     * Saves the requested file to the Client's local directory
     * @param url
     */
    public static void saveFile(String url) {
        String requestedFile = parseGETForFile(url);
        String clientDirectory = URL_CLIENT + requestedFile;
        File original = new File(url);
        File clientCopy = new File(clientDirectory);
        try {
            Files.copy(original.toPath(), clientCopy.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses the GET request for the file name
     * @param request
     * @return
     */
    public static String parseGETForFile(String request) {
        String parsed = "";
        // GET C:\CS158\hello.txt HTTP/1.1
        int pos = 0;
        for (int i = 0; i < request.length(); i++) {
            if (request.substring(i, i + 1).equals("/")) {
                pos = i;
            }
            if (request.substring(i, i + 1).equals(".")) {
                parsed = request.substring(pos + 1);
                break;
            }
        }
        return parsed;
    }

    /**
     * Parses the response message for status code
     * @param response
     * @return
     */
    public static String parseResponseForStatus(String response) {
        String parsed = "";
        if (response.contains("404")) {
            return response;
        }
        int posStatusEnd = response.indexOf("\n");
        parsed = response.substring(0, posStatusEnd);
        return parsed;
    }

    /**
     * Checks if the status code is 200 OK or 404 Not Found
     * @param status
     * @return
     */
    public static boolean checkStatus(String status) {
        if (status.contains("404")) {
            return false;
        }
        return true;
    }

    /**
     * Main application of web client
     * @param args
     */
    public static void main(String[] args) {
        try {
            // create connection socket given server info
            Socket socket = new Socket(SERVER_HOST, PORT);
            System.out.println("SOCKET CREATED WITH " + socket.getInetAddress() + "/" + socket.getLocalPort());
            // ask user what file they want to request
            System.out.print("Enter name of file to request for file. Include file extension (e.g hello.txt): ");
            Scanner scan = new Scanner(System.in);
            String fileRequest = scan.nextLine();
            // send HTTP GET request specified by user input
            bout = new BufferedOutputStream(socket.getOutputStream());
            String getRequest = GET + URL_SERVER + fileRequest + HTTP_VERSION;
            // sends the message to byte stream
            bout.write(getRequest.getBytes());
            bout.flush();
            System.out.println("A MESSAGE HAS BEEN SENT: " + getRequest);
            System.out.println("THE PARSED HTTP REQUEST / FILE BEING REQUESTED: " + fileRequest);
            // read the response
            bin = new BufferedInputStream(socket.getInputStream());
            byte[] data = new byte[1024];
            int dataSize = bin.read(data);
            data = Arrays.copyOf(data, dataSize);
            String response = new String(data).trim();
            System.out.println("A MESSAGE RECEIVED: " + response);
            String status = parseResponseForStatus(response);
            boolean accept = checkStatus(status);
            if (!accept) {
                System.out.print("THE RESPONSE MESSAGE CONTENT: 404 NOT FOUND. Please request the file again...");
            } else {
                int urlPOS = response.indexOf("\n");
                String url = response.substring(urlPOS + 1);
                System.out.println("THE RESPONSE MESSAGE CONTENT: " + url);
                saveFile(url);
            }

            // Close the connections
            bin.close();
            bout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
