import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Friend2 {
    private static final String SERVER_IP = "localhost"; // Change if server is remote
    private static final int SERVER_PORT = 4244;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JFrame frame = new JFrame("chat");
    private JTextArea chatArea = new JTextArea(20, 40);
    private JTextField inputField = new JTextField(30);
    private JButton sendButton = new JButton("Send");

    public Friend2() {
        try {
            // Connect to server
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // --- GUI Setup ---
            chatArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(chatArea);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(inputField, BorderLayout.CENTER);
            panel.add(sendButton, BorderLayout.EAST);

            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            frame.getContentPane().add(panel, BorderLayout.SOUTH);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // --- Actions ---
            sendButton.addActionListener(e -> sendMessage());
            inputField.addActionListener(e -> sendMessage());

            // --- Thread for receiving messages ---
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        chatArea.append(response + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append("Connection closed.\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Friend2::new);
    }
}
