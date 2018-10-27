
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
/*import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;*/
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.nio.file.*;
import java.util.*;

public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private OutputStream outStream;
    private InputStream inStream;
    private JFrame frame = new JFrame("Capitalize Client");
    private JTextField dataField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(40, 60);
    private Boolean disconnected = false;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Enter in the
     * listener sends the textfield contents to the server.
     */
    public Client() {

        // Layout GUI
        messageArea.setEditable(false);
        frame.getContentPane().add(dataField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");

        // Add Listeners
        dataField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield
             * by sending the contents of the text field to the
             * server and displaying the response from the server
             * in the text area.  If the response is "." we exit
             * the whole application, which closes all sockets,
             * streams and windows.
             */
            public void actionPerformed(ActionEvent e) {
                String command = dataField.getText();
                if (disconnected) {
                    messageArea.append("\nVous etes deconnecte du serveur\n");
                    dataField.selectAll();
                } else {
                    if (command.equals("Exit")) {
                        out.println(command);
                        disconnected = true;
                        messageArea.append("\nExit\nVous avez ete deconnecte avec succes.\n");
                        dataField.selectAll();
                    } else {
                        messageArea.append("\n" + command + "\n");
                        testCommand(command);
                        dataField.selectAll();
                    }
                }
            }
        });
    }

    private void testCommand(String command) {
        String[] parsedCommand = command.split(" ");

        if (parsedCommand.length > 2) {
            messageArea.append("Command format with too many arguments\n");
            return;
        }

        if (parsedCommand[0].equals("ls")) {
            if (parsedCommand.length == 2) {
                messageArea.append("La commande \"ls\" ne prends pas de parametre\n");
            } else {
                command_ls(command);
            }
        } else if (parsedCommand[0].equals("cd")) {
            command_cd(command);
        } else if (parsedCommand[0].equals("mkdir")) {
            command_mkdir(command);
        } else if (parsedCommand[0].equals("upload")) {
            command_upload(command);
        } else if (parsedCommand[0].equals("download")) {
            command_download(command);
        } else {
            messageArea.append("Command not recognized\n");
            return;
        }
    }

    private void command_cd(String command) {
        try {
            out.println(command);
            String response = in.readLine();
            messageArea.append(response + "\n");
        } catch (IOException ex) {
            messageArea.append("Error: " + ex);
        }
    }

    private void command_ls(String command) {
        try {
            out.println(command);
            String response = in.readLine();
            while(!response.equals("#End")) {
                messageArea.append(response + "\n");
                response = in.readLine();
            }
        } catch (IOException ex) {
            messageArea.append("Error: " + ex);
        }
    }

    private void command_mkdir(String command) {
        try {
            out.println(command);
            String response = in.readLine();
            messageArea.append(response + "\n");
        } catch (IOException ex) {
            messageArea.append("Error: " + ex);
        }
    }

    private void command_upload(String command) {
        String[] parsedCommand = command.split(" ");
        Path path = Paths.get("").toAbsolutePath();
        path = Paths.get(path.toString() + "\\" + parsedCommand[1]);

        if (!Files.exists(path)) {
            messageArea.append("Le fichier n'a pas ete trouve\n");
            return;
        }

        out.println(command);

        FileInputStream fInput = null;
        BufferedInputStream bInput = null;

        try {
            File file = new File (path.toString());
            byte[] myByteArray  = new byte[(int)file.length()];
            fInput = new FileInputStream(file);
            bInput = new BufferedInputStream(fInput);
            bInput.read(myByteArray, 0, myByteArray.length);
            outStream.write(myByteArray, 0, myByteArray.length);
            outStream.flush();

        } catch (IOException ex) {
            System.out.println("Exception IO upload: " + ex);
        } finally {
            try {
                if (bInput != null) bInput.close();
            } catch (IOException ex) {
                System.out.println("Exception IO close upload: " + ex);
            }
        }

        try {
            String response = in.readLine();
            messageArea.append(response + "\n");
        } catch (IOException ex) {
            System.out.println("Exception read Upload: " + ex);
        }
    }

    private void command_download(String command) {
        int fileSize = 16777216;
        String[] parsedCommand = command.split(" ");
        int bytesRead;
        int current = 0;
        FileOutputStream fOutput = null;
        BufferedOutputStream bOutput = null;

        out.println(command);

        try {
            String response = in.readLine();
            if (response.equals("NotFound")) {
                messageArea.append("File not found on the server.\n");
                return;
            }


            Path path = Paths.get("").toAbsolutePath();
            String filePath = path.toString() + "\\" + parsedCommand[1];

            byte[] myByteArray = new byte[fileSize];

            fOutput = new FileOutputStream(filePath);
            bOutput = new BufferedOutputStream(fOutput);

            bytesRead = inStream.read(myByteArray, 0, myByteArray.length);
            current = bytesRead;

            bOutput.write(myByteArray, 0, current);
            bOutput.flush();

        } catch (IOException ex) {
            System.out.println("Excdption IO download: " + ex);
        } finally {
            try {
                if (fOutput != null)    fOutput.close();
                if (bOutput != null)    bOutput.close();
            } catch (IOException ex) {
                System.out.println("Exception IO close download: " + ex);
            }
        }

        messageArea.append("Le fichier " + parsedCommand[1] + " a bien ete telecharge\n");
    }

    @SuppressWarnings("resource")
    public void connectToServer() throws IOException {
        String message = "";
        Boolean CorrectAddress = false;
        String fullAddress = "";
        String[] parsedAddress;
        //String serverAddress = JOptionPane.showInputDialog(frame,"Enter IP Address of the Server:","Welcome to the Capitalization Program",JOptionPane.QUESTION_MESSAGE);
        //int port = 5000;

        while (!CorrectAddress) {
            fullAddress = JOptionPane.showInputDialog(frame, "Entrer l'adresse IP et le Port du serveur:\n Format (Addresse:Port)\n" + message, "Welcome to the Storage Drive", JOptionPane.QUESTION_MESSAGE);
            message = testAddress(fullAddress);
            if (message == "") {
                CorrectAddress = true;
            }
        }

        parsedAddress = fullAddress.split(":");
        String serverAddress = parsedAddress[0];
        int port = Integer.parseInt(parsedAddress[1]);


        Socket socket;
        socket = new Socket(serverAddress, port);

        System.out.format("The Storage Drive server is running on %s:%d%n", serverAddress, port);

        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        outStream = socket.getOutputStream();
        inStream = socket.getInputStream();

        messageArea.append(in.readLine() + "\n");

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

     // Runs the client application.
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.pack();
        client.frame.setVisible(true);
        client.connectToServer();
    }
}
