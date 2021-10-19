package http.server;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;

public class WebServerThread extends Thread {
    Socket remote;

    WebServerThread(Socket r) {
        this.remote = r;
    }

    public void run() {
        try {
            // remote is now the connected socket
            System.out.println("Connection, sending data.");

            BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
            PrintWriter out = new PrintWriter(remote.getOutputStream());

            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            String str = ".";
            /*
             * while (str != null && !str.equals("")) { str = in.readLine();
             * System.out.println(str); }
             */

            // Lecture de la requête
            str = in.readLine();
            if (str != null) {
                String[] request = str.split(" ");
                System.out.println(str);
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
                            filename = "ErrorsPages\\NoPostPage.html";
                        } else {
                            filename = (request[1].split("/"))[1];
                        }

                        // Lecture du header
                        while (str != null && !str.equals("")) {
                            str = in.readLine();
                        }

                        str = ".";
                        String body = "";
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

                        postRequest(out, body, filename);
                        break;

                    case "PUT":
                        System.out.println("PUT received");
                        if (request[1].equals("/")) {
                            filename = "ErrorsPages\\NoPostPage.html";
                        } else {
                            filename = (request[1].split("/"))[1];
                        }

                        // Lecture du header
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

    public void getRequest(PrintWriter out, String pageName) {
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
        } else {
            out.println(pageToReturn);
        }
        out.flush();
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
        // Send the headers
        out.println("HTTP/1.0 200 OK");
        out.println("Content-Type: text/html");
        out.println("Server: Bot");
        // this blank line signals the end of the headers
        out.println("");

        out.println(confirmation);
        out.flush();
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
