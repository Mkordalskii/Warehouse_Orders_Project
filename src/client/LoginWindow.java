package client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginWindow extends JFrame {
    private JTextField loginTextField = new JTextField(10); //10znakow
    private JPasswordField passwordField = new JPasswordField(10);
    private JButton loginButton = new JButton("Zaloguj");

    public LoginWindow() {
        setTitle("Logowanie");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel p = new JPanel(new GridLayout(3,2));
        p.add(new JLabel("Login:"));
        p.add(loginTextField);
        p.add(new JLabel("Hasło:"));
        p.add(passwordField);
        p.add(new JLabel());//pusty JLabel żeby przesunąć przycisk
        p.add(loginButton);
        add(p);
        pack();//dopasowanie okna do zawartosci
        setLocationRelativeTo(null); //centrowanie okna na ekranie
        setVisible(true);

        loginButton.addActionListener(e -> login());
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) login(); //jesli enter to login()
            }
        });
    }
    private void login() {
        String login = loginTextField.getText().trim(); //pobranie loginu bez bialych znakow z początku i końca
        String pass = new String(passwordField.getPassword());
        try {
            ClientSession.init(login, pass);
            dispose();//zamkniecie okienka logowania
            new MainWindow();//utworzenie okna głównego
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błędny login lub hasło!", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}
