package model;
import java.io.Serializable;

public abstract class User implements Serializable { //interfejs do serializacji i deserializacji przesyłanych obiektów ObjectOutputStream
    private static final long serialVersionUID = 1L; //identyfikator wersji, jak nie podam to Java zrobi jakiś automatycznie, podczas deserializacji Java sprawdza czy ID się zgadza
    protected String username;
    protected String password;
    protected String role; // "ADMIN" lub "USER"

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean checkPassword(String pw) { return password.equals(pw); }
}