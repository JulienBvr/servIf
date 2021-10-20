package http.server;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.nio.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;

public class WebServerThread extends Thread {
    Socket remote;

    WebServerThread(Socket r) {
        this.remote = r;
    }

    public void run(){
        try {
            // remote is now the connected socket
            System.out.println("Connection, sending data.");

            BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
            PrintWriter out = new PrintWriter(remote.getOutputStream());
            BufferedOutputStream out2 = new BufferedOutputStream(remote.getOutputStream());

            String body = "";

            // Lecture de la requête
            String str = in.readLine();
            if (str != null) {
                String[] request = str.split(" ");
                String requestType = request[0];

                switch (requestType) {
                    case "GET":
                        System.out.println("GET received");
                        // Tests sur la page web demandée dans un Get avant de passer
                        String requestPageName;
                        if (request[1].equals("/")) {
                            if (readFile("http\\server\\index.html").equals("")) {
                                requestPageName = "ErrorsPages\\NoIndex.html";
                            } else {
                                requestPageName = "index.html";
                            }
                        } else {
                            requestPageName = (request[1].split("/"))[1];
                        }

                        getRequest(out, requestPageName);
                        break;

                    case "POST":
                        System.out.println("POST received");

                        String filename;
                        if (request[1].equals("/")) {
                            String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                            out2.write(Header.getBytes());
                            out2.flush();
                        } else {
                            filename = (request[1].split("/"))[1];
                            // Lecture du header
                            while (str != null && !str.equals("")) {
                                str = in.readLine();
                                System.out.println(str);
                            }                        
                            body = "";
                            // Lecture du contenu de la requête, BIEN METTRE UN RETOUR A LA LIGNE APRES LA
                            // BALISE FERMANTE
                            str = ".";
                            while (str != null && !str.equals("\\EOF")) {
                                System.out.println(str);
                                str = in.readLine();
                                if (body.equals("")) {
                                    body = str;
                                } else {
                                    body += "\n" + str;
                                }
                            }
                            postRequest(out, body, filename);
                        }

                        
                        break;

                    case "PUT":
                        System.out.println("PUT received");
                        if (request[1].equals("/")) {
                            filename = "ErrorsPages\\NoPostPage.html";
                        } else {
                            filename = (request[1].split("/"))[1];
                        }

                        // read the data sent. We basically ignore it,
                        // stop reading once a blank line is hit. This
                        // blank line signals the end of the client HTTP
                        // headers.
                        while (str != null && !str.equals("")) {
                            str = in.readLine();
                        }

                        str = ".";
                        body = "";
                        // Lecture du contenu de la requête, BIEN METTRE UN RETOUR A LA LIGNE APRES LA
                        // BALISE FERMANTE
                        while (str != null && !str.equals("</html>")) {
                            str = in.readLine();
                            System.out.println(str);
                            if (body.equals("")) {
                                body = str;
                            } else {
                                body += "\n" + str;
                            }

                        }

                        putRequest(out, body, filename);
                        break;

                    case "DELETE":
                        System.out.println("DELETE received");
                        if (request[1].equals("/")) {
                            filename = "ErrorsPages\\NoDeletePage.html";
                        } else {
                            filename = (request[1].split("/"))[1];
                        }
                        deleteRequest(out, filename);
                        break;

                    case "HEAD":

                        System.out.println("HEAD received");
                        // Tests sur la page web demandée dans un Get avant de passer
                        if (request[1].equals("/")) {
                            filename = "ErrorsPages\\NoPostPage.html";
                        } else {
                            filename = (request[1].split("/"))[1];
                        }
                        while (str != null && !str.equals("")) {
                            str = in.readLine();
                            System.out.println(str);
                        }

                        headRequest(out, str, filename);

                        break;

                    default:
                        System.out.println("Unknown request received");
                        out.println("HTTP/1.0 400 BAD REQUEST");
                        out.println("Content-Type: text/plain");
                        out.println("Server: Bot");
                        out.println("");
                        out.flush();
                        break;
                }
            }
            remote.close();

        } catch (Exception e) {
            System.out.println("Error in WebServer: " + e);
        }

    }

    private String readFile(String fileName) {
        BufferedReader lecteurAvecBuffer = null;
        String ligne;
        String total = "";
        try {
            lecteurAvecBuffer = new BufferedReader(new FileReader(fileName));
            while ((ligne = lecteurAvecBuffer.readLine()) != null) {
                if (total.equals("")) {
                    total = ligne;
                } else {
                    total += "\n" + ligne;
                }
            }
            lecteurAvecBuffer.close();
            return total;
        } catch (Exception exc) {
            System.out.println("Erreur d'ouverture : " + exc);
            return "";
        }
    }

    public void getRequest(PrintWriter out, String pageName) throws IOException {

        System.out.println(pageName);
        String extension = (pageName.split("\\."))[1];
        System.out.println(extension);

        BufferedOutputStream out2 = new BufferedOutputStream(remote.getOutputStream());

        switch (extension) {
            
            case "html":

                System.out.println("fichier html");

                // Send the response
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: text/html");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                // Send the HTML page

                String pageToReturn = readFile("http\\server\\" + pageName);
                if (pageToReturn.equals("")) {
                    out.println("File " + pageName + " could not be opened");
                    String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.flush();
                } else {
                    out.println(pageToReturn);
                }
                out.flush();
                break;

            case "txt":

                System.out.println("fichier txt");

                // Send the response
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: text/plain");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                // Send the HTML page

                pageToReturn = readFile("http\\server\\" + pageName);
                if (pageToReturn.equals("")) {
                    out.println("File " + pageName + " could not be opened");
                    String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.flush();
                } else {
                    out.println(pageToReturn);
                }
                out.flush();

                break;

            case "jpg":
                try
                {    
                    System.out.println("fichier jpg");

                    File file = new File("http\\server\\" + pageName);
                    FileInputStream imageFile = new FileInputStream(file);
                    byte[] imageData = new byte[(int)file.length()];
                    imageFile.read(imageData);
                    imageFile.close();
                    String Header = "HTTP/1.0 200 OK\r\ncontent-length:"+file.length()+"\r\ncontent-type: image/jpg\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.write(imageData);

                    out2.flush();
                }
                catch(Exception e)
                {
                    System.out.println("Exception in jpg GET : " + e);
                    String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.flush();
                }
                break;

            case "svg":
                try
                {    
                    System.out.println("fichier svg");

                    File file = new File("http\\server\\" + pageName);
                    FileInputStream imageFile = new FileInputStream(file);
                    byte[] imageData = new byte[(int)file.length()];
                    imageFile.read(imageData);
                    imageFile.close();
                    String Header = "HTTP/1.0 200 OK\r\ncontent-length:"+file.length()+"\r\ncontent-type: image/svg+xml\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.write(imageData);

                    out2.flush();
                }
                catch(Exception e)
                {
                    System.out.println("Exception in jpg GET : " + e);
                    String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.flush();
                }
                break;

            case "mp3":
                try
                {    
                    System.out.println("fichier mp3");

                    File file = new File("http\\server\\" + pageName);
                    FileInputStream songFile = new FileInputStream(file);
                    byte[] imageData = new byte[(int)file.length()];
                    songFile.read(imageData);
                    songFile.close();
                    String Header = "HTTP/1.0 200 OK\r\ncontent-length:"+file.length()+"\r\ncontent-type: audio/mpeg\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.write(imageData);

                    out2.flush();
                }
                catch(Exception e)
                {
                    System.out.println("Exception in mp3 GET : " + e);
                    String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.flush();
                }
                break;

            case "mp4":
                try
                {    
                    System.out.println("fichier mp4");

                    File file = new File("http\\server\\" + pageName);
                    FileInputStream songFile = new FileInputStream(file);
                    byte[] imageData = new byte[(int)file.length()];
                    songFile.read(imageData);
                    songFile.close();
                    String Header = "HTTP/1.0 200 OK\r\ncontent-length:"+file.length()+"\r\ncontent-type: video/mp4\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.write(imageData);

                    out2.flush();
                }
                catch(Exception e)
                {
                    System.out.println("Exception in mp4 GET : " + e);
                    String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                    out2.write(Header.getBytes());
                    out2.flush();
                }
                break;

            default:

            try
            {    
                System.out.println("fichier non reconnu");
                String Header = "HTTP/1.0 404 NOT FOUND\r\ncontent-type: text/plain\r\nServer:Bot\r\n\r\n";
                out2.write(Header.getBytes());
                out2.flush();
                }
                catch(Exception e)
                {
                    System.out.println("Exception in mp4 GET : " + e);
                }

                break;

        }
    }

    public void postRequest(PrintWriter out, String msg, String filename) throws IOException {

        // Mise à jour du fichier à POST
        Path path = FileSystems.getDefault().getPath("http/server/" + filename);

        Files.write(path, msg.getBytes(), StandardOpenOption.APPEND);
        String fullDoc = Files.readString(path);

            // Send the response
            // Send the headers
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");

            out.println(fullDoc);
            out.flush();

    }

    public void putRequest(PrintWriter out, String msg, String filename) throws IOException {

        // Mise à jour du fichier à PUT
        Path path = FileSystems.getDefault().getPath("http/server/" + filename);
        
        Files.write(path, msg.getBytes());
        String fullDoc = Files.readString(path);

            // Send the response
            // Send the headers
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");

            out.println(fullDoc);
            out.flush();

    }

    public void deleteRequest(PrintWriter out, String filename) throws IOException {

        File file = new File("http\\server\\" + filename);
        String confirmation;
        if (file.delete())
            confirmation = "Fichier supprime : " + filename;
        else
            confirmation = "Supression du fichier echoue : " + filename;
        // Send the response

        System.out.println(filename);
        String extension = (filename.split("\\."))[1];
        System.out.println(extension);

        switch (extension) {
        
            case "http" :
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: text/html");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                out.println(confirmation);
                out.flush();
                break;

            case "txt" :
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: text/plain");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                out.println(confirmation);
                out.flush();
                break;
            
            case "jpg" :
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: image/jpg");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                out.println(confirmation);
                out.flush();
                break;
            
            case "jpeg" :
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: image/jpeg");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                out.println(confirmation);
                out.flush();
                break;

            case "mp3" :
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: audio/mpeg");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                out.println(confirmation);
                out.flush();
                break;

            case "svg" :
                // Send the headers
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: svg/xml");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");

                out.println(confirmation);
                out.flush();
                break;

            default:

                out.println("Extension non reconnu");
                out.println("HTTP/1.0 404 NOT FOUND");
                out.println("Content-Type: text/plain");
                out.println("Server: Bot");

                break;
        }
    }

    public void headRequest(PrintWriter out, String headers, String pageName) throws IOException {
        Path path = FileSystems.getDefault().getPath("http/server/" + pageName);
        File file = new File("http\\server\\" + pageName);
        FileTime fileTime = Files.getLastModifiedTime(path);
        // Send the response
        // Send the headers
        out.println("HTTP/1.0 200 OK");
        out.println("Content-Type: text/html");
        out.println("Server: Bot");
        out.println("file-size : " + file.length());
        out.println("last-modification : " + fileTime);

        // Taille du fichier
        // Nom

        // this blank line signals the end of the headers
        out.println("");
        out.flush();
    }

}
