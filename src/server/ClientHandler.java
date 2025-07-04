package server;
import java.io.*;
import java.net.*;
import model.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User loggedUser;

    public ClientHandler(Socket s) {
        this.socket = s;
    }
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            //Autoryzacja
            Object obj = in.readObject(); //wczytuje objekt z logowania
            if (obj instanceof String[]) { //sprawdzenie czy to tablica stringów
                String[] clientLoginNPassword = (String[])obj;
                User user = ServerMain.dataStore.authenticate(clientLoginNPassword[0], clientLoginNPassword[1]); //0 - login 1 - hasło
                if (user != null) {
                    loggedUser = user;
                    out.writeObject("OK"); //wysłanie potwierdzenia do ClientSession
                    out.writeObject(user);
                    sendProductsList();
                } else {
                    out.writeObject("ERROR");
                    socket.close();
                    return;
                }
            }

            while (true) {
                Object request = in.readObject(); //wczytanie requesta
                if (request instanceof String[]) {
                    String[] cmd = (String[]) request;
                    switch (cmd[0]) {
                        case "ADD":
                            if (loggedUser.getRole().equals("ADMIN")) {
                                Product p = (Product) in.readObject();
                                ServerMain.dataStore.addProduct(p);
                                ServerMain.logger.saveLog("Dodano produkt: " + p.getName());
                                ServerMain.broadcastUpdate(); //rozsyła aktualizację wszystkim klientom
                            }
                            break;
                        case "REMOVE":
                            if (loggedUser.getRole().equals("ADMIN")) {
                                int prodId = Integer.parseInt(cmd[1]);
                                ServerMain.dataStore.removeProduct(prodId);
                                ServerMain.logger.saveLog("Usunięto produkt id: " + prodId);
                                ServerMain.broadcastUpdate();
                            }
                            break;
                        case "EDIT":
                            if (loggedUser.getRole().equals("ADMIN")) {
                                Product p = (Product) in.readObject();
                                ServerMain.dataStore.editProduct(p);
                                ServerMain.logger.saveLog("Edytowano produkt: " + p.getName());
                                ServerMain.broadcastUpdate();
                            }
                            break;
                        case "ORDER":
                            Order order = (Order) in.readObject();
                            ServerMain.dataStore.saveOrder(order);
                            ServerMain.logger.saveLog("Zamówienie od: " + order.getUser().getUsername());
                            break;
                        case "DISCONNECT": //nie użyłem
                            socket.close(); return;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            ServerMain.clients.remove(this);
        }
    }
    public void sendProductsList() {
        try {
            out.reset(); //przez to ***** walczyłem kilka dni z odświeżaniem listy produktów - resetowanie strumienia żeby wysłać pełne obiekty
            out.writeObject("PRODUCTS"); //wysłanie flagi products do ClientSession
            out.writeObject(ServerMain.dataStore.getProducts());//wysłanie produktów
            out.flush();//flushuje dane
        } catch (Exception ex) {System.out.println(ex);}
    }
}
