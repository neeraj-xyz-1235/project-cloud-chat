package Services;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class User_auth extends JFrame {

    PrintWriter pw; // Declared PrintWriter to send messages to the server
    BufferedReader br; // Declared BufferedReader to read messages from the server
    static String current_user; // Variable to store the username of the currently logged-in user
    static int current_user_id; // Variable to store the user_id of the currently logged-in user

    private CardLayout cardLayout;
    private JPanel cardPanel;

    // Login Components
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // Registration Components
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;

    public User_auth(PrintWriter pw, BufferedReader br) {
        this.pw = pw;
        this.br = br;

        setTitle("User Authentication");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(400, 350);
        setLocationRelativeTo(null); // Centres the window

        // Setup CardLayout to switch between panels
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add both panels to the card manager
        cardPanel.add(createLoginPanel(), "Login");
        cardPanel.add(createRegistrationPanel(), "Register");

        add(cardPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        panel.add(new JLabel("Username:"));
        loginUsernameField = new JTextField();
        panel.add(loginUsernameField);

        panel.add(new JLabel("Password:"));
        loginPasswordField = new JPasswordField();
        panel.add(loginPasswordField);

        JButton switchToRegButton = new JButton("Register Instead");
        switchToRegButton.addActionListener(e -> cardLayout.show(cardPanel, "Register"));

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());

        panel.add(switchToRegButton);
        panel.add(loginButton);

        return panel;
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        regUsernameField = new JTextField();
        panel.add(regUsernameField);

        panel.add(new JLabel("Password:"));
        regPasswordField = new JPasswordField();
        panel.add(regPasswordField);

        panel.add(new JLabel("Confirm Password:"));
        regConfirmPasswordField = new JPasswordField();
        panel.add(regConfirmPasswordField);

        JButton switchToLoginButton = new JButton("Back to Login");
        switchToLoginButton.addActionListener(e -> cardLayout.show(cardPanel, "Login"));

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> handleRegistration());

        panel.add(switchToLoginButton);
        panel.add(registerButton);

        return panel;
    }

    // ########## HANDLING METHODS ##########

    private void handleLogin() {
        String enteredUsername = loginUsernameField.getText();
        char[] enteredPassword = loginPasswordField.getPassword();

        if (enteredUsername.isEmpty() || enteredPassword.length == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> { // lambda expression to create a new thread for handling login, so that the GUI remains responsive while waiting for server response
            try {
                pw.println("LOGIN::" + enteredUsername + "::" + new String(enteredPassword)); // sends the login request to the server with the entered username and password
                String login_response;
                while ((login_response = br.readLine()) != null) {
                    String[] login_response_parts = login_response.split("::");
                    if (login_response_parts[0].equals("LOGIN")) {
                        final String responseType = login_response_parts[1];
                        SwingUtilities.invokeLater(() -> { // invokelater is used to ensure that the code runs while the GUI remains responsive. It schedules the code to be executed on the Event Dispatch Thread (EDT), which isresponsible for handling GUI updates in Swing.
                            if (responseType.equals("SUCCESS")) {
                                JOptionPane.showMessageDialog(User_auth.this, "Login successful!", "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                                current_user = enteredUsername; // Store the logged-in username in the current_user variable
                                current_user_id = Integer.parseInt(login_response_parts[2]); // Store the user ID in thecurrent_user_id variable
                                
                                // Store the session key for encrypted communication
                                if (login_response_parts.length > 3) {
                                    CryptUtil.sessionKey = CryptUtil.stringToKey(login_response_parts[3]);
                                }

                                // redirecting user to messenger window after successful login
                                Messenger messengerWindow = new Messenger(pw, br);
                                messengerWindow.setVisible(true);
                                User_auth.this.dispose(); // Close the login window after successful login

                            } else if (responseType.equals("INVALID_CREDENTIALS")) {
                                JOptionPane.showMessageDialog(User_auth.this, "Invalid username or password!", "Error",
                                        JOptionPane.ERROR_MESSAGE);

                            } else {
                                JOptionPane.showMessageDialog(User_auth.this, "Login failed due to server error!",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        break;
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(User_auth.this,
                "An error occurred while communicating with the server!","Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void handleRegistration() {
        String enteredUsername = regUsernameField.getText();
        char[] enteredPassword = regPasswordField.getPassword();
        char[] confirmPassword = regConfirmPasswordField.getPassword();

        if (enteredUsername.isEmpty() || enteredPassword.length == 0 || confirmPassword.length == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!new String(enteredPassword).equals(new String(confirmPassword))) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                pw.println("REGISTER::" + enteredUsername + "::" + new String(enteredPassword));
                String reg_response;
                while ((reg_response = br.readLine()) != null) {
                    String[] reg_response_parts = reg_response.split("::");

                    if (reg_response_parts[0].equals("REGISTER")) {
                        final String responseType = reg_response_parts[1];
                        SwingUtilities.invokeLater(() -> {
                            if (responseType.equals("SUCCESS")) {
                                JOptionPane.showMessageDialog(User_auth.this,
                                        "Registration successful! You can now log in.", "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                                // redirecting user to messenger window after successful registration
                                cardLayout.show(cardPanel, "Login");
                            } else {
                                JOptionPane.showMessageDialog(User_auth.this,
                                        "Registration failed! Username might already be taken.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        break;
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(User_auth.this,
                        "An error occurred while communicating with the server!", "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    public static String getCurrentUser() {
        return current_user;
    }

    public static int getCurrentUserId() {
        return current_user_id;
    }
}
