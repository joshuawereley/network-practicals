import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class Main {

    private static POP3Client pop3Client;
    private static ArrayList<Email> emails;
    private static JFrame frame;
    private static JPanel emailPanel;
    private static JTextField serverField;
    private static JTextField portField;
    private static JTextField usernameField;
    private static JPasswordField passwordField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        frame = new JFrame("POP3 Email Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("POP3 Server:"), gbc);

        gbc.gridx = 1;
        serverField = new JTextField("pop.gmail.com", 20);
        loginPanel.add(serverField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Port:"), gbc);

        gbc.gridx = 1;
        portField = new JTextField("995", 5);
        loginPanel.add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToPOP3Server());
        loginPanel.add(connectButton, gbc);

        emailPanel = new JPanel();
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(emailPanel);
        scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );

        JPanel bottomPanel = new JPanel();
        JButton deleteButton = new JButton("Delete Selected Messages");
        deleteButton.addActionListener(e -> deleteSelectedMessages());
        bottomPanel.add(deleteButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> connectToPOP3Server());
        bottomPanel.add(refreshButton);

        frame.setLayout(new BorderLayout());
        frame.add(loginPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void connectToPOP3Server() {
        String server = serverField.getText();
        int port = Integer.parseInt(portField.getText());
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            setCursor(true);

            if (server.equals("pop.gmail.com") && port == 995) {
                pop3Client = new SSLPop3Client(server, port);
            } else {
                pop3Client = new POP3Client(server, port);
            }

            boolean loginSuccess = pop3Client.login(username, password);

            if (!loginSuccess) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Login failed. Please check your credentials.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            emails = pop3Client.listEmails();

            updateEmailPanel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "Error connecting to POP3 server: " + ex.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        } finally {
            setCursor(false);
        }
    }

    private static void updateEmailPanel() {
        emailPanel.removeAll();

        JPanel headerPanel = new JPanel(new GridLayout(1, 4));
        headerPanel.add(new JLabel("Select"));
        headerPanel.add(new JLabel("From"));
        headerPanel.add(new JLabel("Subject"));
        headerPanel.add(new JLabel("Size (bytes)"));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(headerPanel, BorderLayout.NORTH);
        emailPanel.add(headerContainer);

        if (emails != null) {
            for (Email email : emails) {
                JPanel emailRow = new JPanel(new GridLayout(1, 4));

                JCheckBox checkBox = new JCheckBox();
                email.setCheckBox(checkBox);
                emailRow.add(checkBox);

                emailRow.add(new JLabel(email.getFrom()));
                emailRow.add(new JLabel(email.getSubject()));
                emailRow.add(new JLabel(String.valueOf(email.getSize())));

                emailRow.setBorder(BorderFactory.createEtchedBorder());
                emailPanel.add(emailRow);
            }
        }

        emailPanel.revalidate();
        emailPanel.repaint();
    }

    private static void deleteSelectedMessages() {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        try {
            setCursor(true);

            ArrayList<Email> toDelete = new ArrayList<>();
            for (Email email : emails) {
                if (email.isSelected()) {
                    toDelete.add(email);
                }
            }

            if (toDelete.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "No emails selected for deletion."
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete " +
                toDelete.size() +
                " message(s)? " +
                "This will permanently remove them from your Gmail account.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                System.out.println(
                    "Deleting " + toDelete.size() + " message(s)..."
                );

                for (Email email : toDelete) {
                    System.out.println(
                        "Marking message #" +
                        email.getMessageNumber() +
                        " for deletion"
                    );
                    pop3Client.deleteEmail(email.getMessageNumber());
                }

                System.out.println("Committing deletions with QUIT command");
                pop3Client.quit();

                System.out.println("Reconnecting to check results");
                connectToPOP3Server();

                JOptionPane.showMessageDialog(
                    frame,
                    toDelete.size() +
                    " message(s) deleted successfully.\n" +
                    "Note: Gmail may take a few minutes to sync these changes."
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "Error deleting messages: " + ex.getMessage(),
                "Deletion Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        } finally {
            setCursor(false);
        }
    }

    private static void setCursor(boolean waiting) {
        frame.setCursor(
            waiting
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor()
        );
    }
}
