package server;
import model.*;
import java.util.*;
import java.io.*;

public class DataStore {
    private List<Product> products = Collections.synchronizedList(new ArrayList<>());//synchronized żeby było przyjazne wątkom
    private Set<User> users = new HashSet<>();
    private List<Order> orders = new ArrayList<>();
    private int productId = 1;  //licznik id produktów
    private final String FILE = "products.db"; //zapis listy produktów do "bazy danych"

    public void loadInitialData() { //ładuje wstępnych użytkowników i produktu
        users.add(new Admin("admin", "admin"));
        users.add(new NormalUser("user1", "user1"));
        users.add(new NormalUser("user2", "user2"));
        products.add(new Product(productId++, "Frez walcowy 10mm", 20, 55.99));
        products.add(new Product(productId++, "Wiertło 6mm", 50, 14.80));
        products.add(new Product(productId++, "Płytka skrawająca CNMG", 100, 8.40));
        loadFromFile(); //nadpisanie listy produktów z pliku (jeśli istnieje)
    }
    public synchronized List<Product> getProducts() { return new ArrayList<>(products); } //zwracamy kopię żeby nie zmienić jej z zewntąrz

    public synchronized void addProduct(Product p) {
        p = new Product(productId++, p.getName(), p.getQuantity(), p.getPrice());
        products.add(p);
        saveToFile(); //zapis do pliku
    }
    public synchronized void removeProduct(int id) {
        products.removeIf(p -> p.getId() == id);
        saveToFile();
    }
    public synchronized void editProduct(Product edited) {
        System.out.println("Edycja produktu id: " + edited.getId());
        for (Product p : products) {
            if (p.getId() == edited.getId()) {
                System.out.println("Zmieniam z " + p.getName() + " na " + edited.getName());
                p.setName(edited.getName());
                p.setQuantity(edited.getQuantity());
                p.setPrice(edited.getPrice());
            }
        }
        saveToFile();
    }
    public synchronized void saveOrder(Order o) {
        orders.add(o);
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(products);
        } catch (Exception e) { }
    }
    @SuppressWarnings("unchecked") //adnotacja do ignorowania ostrzeżen o niekontrolowanyh rzutowaniu typów
    private void loadFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE))) {
            products = (List<Product>) ois.readObject();
            if (!products.isEmpty())
                productId = products.get(products.size()-1).getId() + 1; //pobiera ID ostatniego elementu i zwiększa o 1
        } catch (Exception e) { System.out.println(e + "Błąd przy ładowaniu pliku z produktami"); }
    }
    public synchronized User authenticate(String username, String pw) { //log w konsoli o logowaniu
        System.out.println("PRÓBA LOGOWANIA: " + username + "/" + pw);
        for (User u : users) {
            System.out.println("U: " + u.getUsername() + "/" + u.getPassword());
            if (u.getUsername().equals(username) && u.checkPassword(pw)) {
                System.out.println("AUTORYZACJA UDANA");
                return u;
            }
        }
        System.out.println("AUTORYZACJA NIEUDANA");
        return null;
    }
}
