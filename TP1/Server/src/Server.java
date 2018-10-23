
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Path;

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
            serverAddress = JOptionPane.showInputDialog(frame, "Enter IP Address and Port of the Server:\n Format (Address:Port)\n" + message, "Welcome to the Storage Drive", JOptionPane.QUESTION_MESSAGE);
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

        public StorageDrive(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            this.path = Paths.get("");
            this.path = this.path.toAbsolutePath();
            System.out.println("New connection with client# " + clientNumber + " at " + socket);
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Welcome to the Storage Drive. Enter \"Exit\" to leave\n");
                System.out.println("Path: " + this.path);
                String returnMessage = "";

                while(true) {
                    String input = in.readLine();
                    if (input == "Exit") {
                        break;
                    }
                    returnMessage = testCommand(input);
                    out.println(returnMessage);
                }

            } catch (IOException e) {
                System.out.println("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Couldn't close a socket, what's going on?");
                }
                System.out.println("Connection with client# " + clientNumber + " closed");
            }
        }
    }

    private static String testCommand(String command) {
        String[] parsedCommand = command.split(" ");

        if (parsedCommand.length > 2) {
            return "Command format with too many arguments";
        } else if (parsedCommand.length < 2) {
            return "Command format with too few arguments";
        }

        if (parsedCommand[0] == "cd") {
            return command_cd(parsedCommand[1]);
        } else if (parsedCommand[0] == "ls") {
            return command_ls(parsedCommand[1]);
        } else if (parsedCommand[0] == "mkdir") {
            return commmand_mkdir(parsedCommand[1]);
        } else if (parsedCommand[0] == "upload") {
            return command_upload(parsedCommand[1]);
        } else if (parsedCommand[0] == "download") {
            return command_download(parsedCommand[1]);
        } else {
            return "Command not recognized";
        }
    }

    private static String command_cd(String command) {
        return "cd";
    }

    private static String command_ls(String command) {
        return "ls";
    }

    private static String commmand_mkdir(String command) {
        return "mkdir";
    }

    private static String command_upload(String command) {
        return "upload";
    }

    private static String command_download(String command) {
        return "download";
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

    private static class Capitalizer extends Thread {

    }



}
