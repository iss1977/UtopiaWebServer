import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Read the full article https://dev.to/mateuszjarzyna/build-your-own-http-server-in-java-in-less-than-one-hour-only-get-method-2k02
public class UtopiaWebServer {

    public static String myHTMLPage = new String();

    public static  String getMyHTMLPage(){
        return
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body {background-color: powderblue;}" +
            "h1   {color: blue;}" +
            "p    {color: red;}"+
            "</style>" +
            "</head>" +
            "<body>" +
            "<h1>Blue Citizen</h1>" +
            "<p>Red little citizen</p>" +
            new Date().toString()+
            "</body>" +
            "</html>";
    }

    public static void main( String[] args ) throws Exception {






        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                serverSocket.setSoTimeout(900);
                try (Socket client = serverSocket.accept()) {
                    UtopiaWebServer.myHTMLPage= UtopiaWebServer.getMyHTMLPage();
                    System.out.println("TICK !");
                    handleClient(client);
                }catch (Exception se){
                    System.out.print(".");
                }
            }
        }catch(Exception e){
            System.out.println("BAAAAAD");
        }
    }

    private static void handleClient(Socket client) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }

        String request = requestBuilder.toString();
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];
        String version = requestLine[2];
        String host = requestsLines[1].split(" ")[1];

        List<String> headers = new ArrayList<>();
        for (int h = 2; h < requestsLines.length; h++) {
            String header = requestsLines[h];
            headers.add(header);
        }

        String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s, headers %s",
                client.toString(), method, path, version, host, headers.toString());
        System.out.println(accessLog);



        sendResponse(client, "200 OK", "text/html", myHTMLPage.getBytes(StandardCharsets.UTF_8));




//        Path filePath = getFilePath(path);
//        if (Files.exists(filePath)) {
//            // file exist
//            String contentType = guessContentType(filePath);
//            sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
//        } else {
//            // 404
//            byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
//            sendResponse(client, "404 Not Found", "text/html", notFoundContent);
//        }

    }

    private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "index.html";
        }

        return Paths.get("./", path);
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

}