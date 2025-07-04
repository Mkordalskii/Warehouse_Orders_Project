package model;

public class NormalUser extends User {
    private static final long serialVersionUID = 1L;
    public NormalUser(String username, String password) {
        super(username, password, "USER");
    }
}
