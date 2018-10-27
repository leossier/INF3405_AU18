
import java.io.*;
/*import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;*/
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.text.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Server {

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("TP1: Storage Drive");
        Boolean CorrectAddress = false;
        String serverAddress = "";
        String[] parsedAddress;
        String message = "";
        int clientNumber = 0;

        while (!CorrectAddress) {
            serverAddress = JOptionPane.showInputDialog(frame, "Entrer l'adresse IP et le Port du serveur:\n Format (Addresse:Port)\n" + message, "Welcome to the Storage Drive", JOptionPane.QUESTION_MESSAGE);
            message = testAddress(serverAddress);
            if (message == "") {
                CorrectAddress = true;
            }
        }

        parsedAddress = serverAddress.split(":");

        ServerSocket listener;
        InetAddress locIP = InetAddress.getByName(parsedAddress[0]);
        int port = Integer.parseInt(parsedAddress[1]);

        listener = new ServerSocket();
        listener.setReuseAddress(true);
        listener.bind(new InetSocketAddress(locIP, port));

        System.out.println("Storage Drive Server connected on " + serverAddress);

        try {
            while (true) {
                new StorageDrive(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }

    }

    private static class StorageDrive extends Thread {
        private Socket socket;
        private int clientNumber;
        private Path path;
        private BufferedReader in;
        private PrintWriter out;
        private OutputStream outStream;
        private InputStream inStream;

        public StorageDrive(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            this.path = Paths.get("").toAbsolutePath();

            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.outStream = socket.getOutputStream();
                this.inStream = socket.getInputStream();
            } catch (IOException e) {
                System.out.println("Error setting up in/out: " + e);
            }

            System.out.println(this.path.toString());
            System.out.println("New connection with client# " + clientNumber + " at " + socket);
        }

        public void run() {
            try {
                //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Bienvenue sur le Storage Drive. Entrez \"Exit\" pour deconnecter");

                while(true) {
                    String input = in.readLine();
                    if (input == null || input.equals("Exit")) {
                        break;
                    }
                    testCommand(input);
                }

            } catch (IOException e) {
                System.out.println("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error: Couldn't close a socket");
                }
                System.out.println("Connection with client# " + clientNumber + " closed");
            }
        }

        private void testCommand(String command) {
            String[] parsedCommand = command.split(" ");
            Date now = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd '@' HH:mm:ss");
            String client = socket.getRemoteSocketAddress().toString();
            System.out.println("[" + client + " - " + ft.format(now) + "]:" + command + "\n");

            if (parsedCommand[0].equals("cd")) {
                command_cd(parsedCommand[1]);
            } else if (parsedCommand[0].equals("ls")) {
                command_ls();
            } else if (parsedCommand[0].equals("mkdir")) {
                commmand_mkdir(parsedCommand[1]);
            } else if (parsedCommand[0].equals("upload")) {
                command_upload(parsedCommand[1]);
            } else if (parsedCommand[0].equals("download")) {
                command_download(parsedCommand[1]);
            } else {
                System.out.println("Error: Received an unrecognized command: " + command);
            }
        }

        private void command_cd(String argument) {
            String oldPath = path.toString();
            //Temporaray values
            String newPath = oldPath;
            Path testPath = path;

            if (argument.equals("..")) {
                int endIndex = oldPath.lastIndexOf("\\");
                if (endIndex == 2) {
                    out.println("Impossible de remonter plus loins dans les dossiers");
                    return;
                } else {
                    newPath = oldPath.substring(0, endIndex);
                    testPath = Paths.get(newPath);
                }
            } else {
                newPath = oldPath + "\\" + argument;
                testPath = Paths.get(newPath);
            }

            if (Files.exists(testPath)) {
                path = testPath;
                out.println("Vous etes dans le dossier " + newPath);
            } else {
                out.println("Dossier inexistant");
            }
        }

        private void command_ls() {
            File directory = new File(path.toString());
            File[] fileList = directory.listFiles();

            for (File file: fileList) {
                if (file.isDirectory()) {
                    out.println("[Folder] " + file.getName());
                }
            }

            for (File file: fileList) {
                if (file.isFile()) {
                    out.println("[File] " + file.getName());
                }
            }

            out.println("#End");
        }

        private void commmand_mkdir(String argument) {
            Path newFolder = Paths.get(path.toString() + "\\" + argument);
            if (Files.exists(newFolder)) {
                out.println("Dossier deja existant");
            } else {
                try {
                    Files.createDirectory(newFolder);
                    out.println("Le dossier " + argument + " a ete cree.");
                } catch (IOException ex) {
                    System.out.println("Error creating directory: " + ex);
                    out.println("Erreur de creation de dossier");
                }
            }
        }

        private void command_upload(String argument) {
            int fileSize = 16777216;    //Arbitrary number. Passing the size caused problems

            int bytesRead;
            int current = 0;    //TODO Test remove
            FileOutputStream fOutput = null;
            BufferedOutputStream bOutput = null;

            String filePath = path.toString() + "\\" + argument;

            try {
                byte[] myByteArray = new byte[fileSize];

                fOutput = new FileOutputStream(filePath);
                bOutput = new BufferedOutputStream(fOutput);

                bytesRead = inStream.read(myByteArray, 0, myByteArray.length);
                current = bytesRead;

                bOutput.write(myByteArray, 0, current);
                bOutput.flush();

            } catch (IOException ex) {
                System.out.println("Exception IO upload: " + ex);
            } finally {
                try {
                    if (fOutput != null)   fOutput.close();
                    if (bOutput != null)   bOutput.close();
                } catch (IOException ex) {
                    System.out.println("Exception IO close upload: " + ex);
                }
            }
            out.println("Le fichier " + argument + " a bien ete televerse.");
        }

        private void command_download(String argument) {
            Path filePath = Paths.get(path + "\\" + argument);
            if (!Files.exists(filePath)) {
                //Contact the Client ton inform no file is comming
                out.println("NotFound");
                return;
            }
            //Inform the client the file is comming
            out.println("Found");

            FileInputStream fInput = null;
            BufferedInputStream bInput = null;

            try {
                File file = new File(filePath.toString());
                byte[] myByteArray = new byte[(int)file.length()];
                fInput = new FileInputStream(file);
                bInput = new BufferedInputStream(fInput);
                bInput.read(myByteArray, 0, myByteArray.length);
                outStream.write(myByteArray, 0, myByteArray.length);
                outStream.flush();

            } catch (IOException ex) {
                System.out.println("Exception IO download: " + ex);
            } finally {
                try {
                    if (bInput != null) bInput.close();
                } catch (IOException ex) {
                    System.out.println("Exception IO close download: " + ex);
                }
            }
        }
    }

    private static String testAddress(String serverAddress) {
        String[] addressPort;
        String[] parsedAddress;
        int temp = 0;

        addressPort = serverAddress.split(":");
        parsedAddress = addressPort[0].split("\\.");

        if(addressPort.length != 2) {
            return "Error: No identified port (:)";
        } else if(parsedAddress.length != 4) {
            return "Error: Wrong format address";
        } else {
            try{
                for (int i = 0; i < 4; i++) {
                    temp = Integer.parseInt(parsedAddress[i]);
                    if (temp < 0 || temp > 255) {
                        return "Error: Byte #" + (i + 1) + "Ouside limits";
                    }
                }

                temp = Integer.parseInt(addressPort[1]);
                if (temp < 5000 || temp > 5050) {
                    return "Error: Port outside limit";
                }

            } catch(Exception e) {
                return "Invalid character in the address";
            }

            return "";
        }
    }
}
