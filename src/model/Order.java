package model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private User user;
    private List<Product> products = new ArrayList<>();
    public Order(User user) {
        this.user = user;
    }
    public void addProduct(Product p) { products.add(p); }
    public void removeProduct(Product p) { products.remove(p); }
    public List<Product> getProducts() { return products; }
    public User getUser() { return user; }
}
