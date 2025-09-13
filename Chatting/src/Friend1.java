import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Friend1 {
    private static final int PORT = 4244;
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    // GUI components
    private JFrame frame = new JFrame("Chat");
    private JTextArea chatArea = new JTextArea(20, 40);
    private JTextField inputField = new JTextField(40);
    private JButton sendButton = new JButton("Send");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Friend1().startServer());
    }

    private void startServer() {
        // Setup GUI
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

        // Actions
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Start server thread
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                chatArea.append("");

                while (true) {
                    Socket socket = serverSocket.accept();
                    chatArea.append("");
                    new ClientHandler(socket).start();
                }
            } catch (IOException e) {
                chatArea.append("Server error: " + e.getMessage() + "\n");
            }
        }).start();
    }

    // Send message from server GUI
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.append("Frd1: " + message + "\n");
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println("Frd1: " + message);
                }
            }
            inputField.setText("");
        }
    }

    // Handles client connections
    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                clientWriters.add(out);

                String message;
                while ((message = in.readLine()) != null) {
                    chatArea.append("Frd2: " + message + "\n");
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println("Frd2: " + message);
                        }
                    }
                }
            } catch (IOException e) {
                chatArea.append("Client disconnected\n");
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                clientWriters.remove(out);
            }
        }
    }
}
