
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Capitalize Client");
    private JTextField dataField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(40, 60);

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
                if (command.equals("Exit")) {
                    //TODO Afficher message Exit -> bloquer input? possibl reconnect?
                    out.println(command);

                    System.exit(0);
                }
                messageArea.append("\n" + command + "\n");
                testCommand(command);
                dataField.selectAll();

                    /*response = in.readLine();
                    if (response == null || response.equals("")) {
                        System.exit(0);
                    }

                    while(!response.equals("#End")) {
                        output += response + "\n";
                        response = in.readLine();
                    }*/
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
            command_ls(command);
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

    }

    private void command_download(String command) {

    }

    /**
     * Implements the connection logic by prompting the end user for
     * the server's IP address, connecting, setting up streams, and
     * consuming the welcome messages from the server.  The Capitalizer
     * protocol says that the server sends three lines of text to the
     * client immediately after establishing a connection.
     */
    @SuppressWarnings("resource")
    public void connectToServer() throws IOException {

        // Get the server address from a dialog box.
        String serverAddress = JOptionPane.showInputDialog(frame,"Enter IP Address of the Server:","Welcome to the Capitalization Program",JOptionPane.QUESTION_MESSAGE);
        int port = 5000;

        Socket socket;
        socket = new Socket(serverAddress, port);

        System.out.format("The Storage Drive server is running on %s:%d%n", serverAddress, port);

        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Consume the initial welcoming messages from the server
        //for (int i = 0; i < 3; i++) {
            messageArea.append(in.readLine() + "\n");
        //}
    }

    /**
     * Runs the client application.
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.pack();
        client.frame.setVisible(true);
        client.connectToServer();
    }
}
