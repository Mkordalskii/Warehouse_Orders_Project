package server;
import java.net.*;
import java.util.*;
import model.*;

public class ServerMain {
    public static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());//synchronizedSet bezpieczne wątkowo, żeby kilku klientów mogło działać na raz
    public static DataStore dataStore = new DataStore(); //produkty i użytkownicy
    public static Logger logger = new Logger("server.log"); //zapis zdarzeń do pliku

    public static void main(String[] args) {
        try (ServerSocket ss = new ServerSocket(Config.PORT)) {
            System.out.println("Serwer wystartował na porcie " + ss.getLocalPort());
            dataStore.loadInitialData(); //ładowanie wstępnych danych - użytkownicy + towary
            while (true) {
                Socket socket = ss.accept();
                ClientHandler ch = new ClientHandler(socket);
                clients.add(ch);
                ch.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //wysyła listę produktów do wszystkich klientów
    public static void broadcastUpdate() {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.sendProductsList();
            }
        }
    }
}
