package client;
import java.net.*;
import java.io.*;
import java.util.*;
import model.*;
import server.Config;
//wszystkie części aplikacji mają wspólny zestaw danych dlatego Static, nie trzeba tworzyć obiektu, jest jedna sesja, bo loguje się tylko raz
public class ClientSession {
    public static ObjectOutputStream out;
    public static ObjectInputStream in;
    public static User loggedUser;
    public static List<Product> productCache = new ArrayList<>();//lista produktów przesłana z serwera

    public static void init(String login, String pass) throws Exception {
        Socket s = new Socket(Config.IP,Config.PORT);
        out = new ObjectOutputStream(s.getOutputStream());
        in  = new ObjectInputStream(s.getInputStream());
        out.writeObject(new String[]{login, pass});//wysyla na serwer tablice z loginem i haslem
        out.flush();

        String reply = (String) in.readObject();
        if (!"OK".equals(reply)) throw new Exception("Login failed");

        //Odbiór i deserializacja obiektu User
        try {
            loggedUser = (User) in.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Deserializacja użytkownika nie powiodła się!");
        }

        //Odbiór flagi "PRODUCTS"
        String productsFlag = (String) in.readObject();
        if (!"PRODUCTS".equals(productsFlag)) throw new Exception("Brak listy produktów!");

        //Odbiór listy produktów
        try {
            productCache = (List<Product>) in.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Deserializacja produktów nie powiodła się!");
        }

        //wątek nasłuchujący zmiany produktów
        new Thread(() -> {
            try {
                while(true) {
                    String typ = (String) in.readObject();
                    System.out.println("ODEBRANO TYP: " + typ);
                    if ("PRODUCTS".equals(typ)) {
                        productCache = (List<Product>) in.readObject();
                        System.out.println("ODEBRANO PRODUKTY:");
                        for(Product p : productCache) {
                            System.out.println("ID: " + p.getId() + ", Nazwa: " + p.getName());
                        }
                        if(MainWindow.instance != null) MainWindow.instance.reloadProducts();
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
