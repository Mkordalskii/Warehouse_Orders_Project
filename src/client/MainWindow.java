package client;

import model.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {
    public static MainWindow instance = null; //statyczna referencja do aktualnego okna zeby wątek w ClienSession mógł wykonać reloadProducts()
    private JTable table;
    private DefaultTableModel model;
    private JButton addButton = new JButton("Dodaj");
    private JButton removeButton = new JButton("Usuń");
    private JButton editButton = new JButton("Edytuj");
    private JButton orderButton = new JButton("Zamów");
    private JButton refreshButton = new JButton("Odśwież");
    private JTextArea logTextArea = new JTextArea(10, 25);

    public MainWindow() {
        instance = this;
        setTitle("Magazyn CNC - użytkownik: " + ClientSession.loggedUser.getUsername() + " [" + ClientSession.loggedUser.getRole() + "]");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        model = new DefaultTableModel(new Object[]{"ID", "Nazwa", "Ilość", "Cena"}, 0) { //0 bo tabela jest na początku pusta
            public boolean isCellEditable(int r, int c) { //domyslnie mozna edytowac komórki tabeli po kliknięciu, dlatego ustawiam na false
                return false;
            }
        };
        table = new JTable(model);
        reloadProducts();

        //Pnael logów
        logTextArea.setEditable(false); //żeby nie można było edytować tekstu w okienku
        JScrollPane logPane = new JScrollPane(logTextArea);

        //Panel środkowy z podziałem na tabele i logi
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), logPane);
        splitPane.setResizeWeight(0.75); //tabela zajmuje 75% szerokości

        add(splitPane, BorderLayout.CENTER);

        JPanel p = new JPanel();
        if (ClientSession.loggedUser.getRole().equals("ADMIN")) { //przyciski tylko dla admina
            p.add(addButton);
            p.add(removeButton);
            p.add(editButton);
            p.add(refreshButton);
        }
        p.add(orderButton);
        add(p, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            showProductDialog(null);//null bo dodajemy nowy produkt, a nie edytujemy istniejący
            dodajLog("Dodano nowy produkt.");
        });
        removeButton.addActionListener(e -> {
            Product prod = getSelectedProduct();
            if (prod != null) dodajLog("Usunięto produkt: " + prod.getName());
            removeProduct();
        });
        editButton.addActionListener(e -> {
            Product prod = getSelectedProduct();
            if (prod != null) dodajLog("Edytowano produkt: " + prod.getName());
            showProductDialog(getSelectedProduct());
        });
        orderButton.addActionListener(e -> {
            Product prod = getSelectedProduct();
            dodajLog("Rozpoczęto zamawianie produktu: " + (prod != null ? prod.getName() : "(brak wyboru)"));
            new OrderWindow(this, getSelectedProduct());
        });
        refreshButton.addActionListener(e -> {
            reloadProducts();
        });

        table.addMouseListener(new MouseAdapter() {//podwójne kliknięcie to zamówienie produktu
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    orderButton.doClick();
                }
            }
        });

        setSize(700, 400);
        setLocationRelativeTo(null); //wycentrowanie okna
        setVisible(true);
    }

    public void reloadProducts() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (Product p : ClientSession.productCache) {
                model.addRow(new Object[]{p.getId(), p.getName(), p.getQuantity(), p.getPrice()});
            }
            table.revalidate();//ponowne przeliczenie rozmiarow tabeli
            table.repaint();//rysowanie tabeli od nowa
        });
        dodajLog("Odświeżono listę produktów.");
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (Integer) model.getValueAt(row, 0);
        for (Product p : ClientSession.productCache) if (p.getId() == id) return p;
        return null;
    }

    private void removeProduct() {
        Product p = getSelectedProduct();
        if (p == null) return;
        if (JOptionPane.showConfirmDialog(this, "Usunąć " + p.getName() + "?", "Potwierdź", JOptionPane.YES_NO_OPTION) != 0)
            return;
        try {
            ClientSession.out.writeObject(new String[]{"REMOVE", "" + p.getId()});
            ClientSession.out.flush();
            dodajLog("Produkt usunięty z magazynu: " + p.getName());
        } catch (Exception ex) {
            dodajLog("Błąd przy usuwaniu produktu.");
        }
    }

    private void showProductDialog(Product p) {
        JTextField nameTextField = new JTextField(p == null ? "" : p.getName());
        JTextField quantityTextField = new JTextField(p == null ? "" : "" + ((p == null) ? 0 : p.getQuantity()));
        JTextField priceTextField = new JTextField(p == null ? "" : "" + ((p == null) ? 0 : p.getPrice()));
        Object[] msg = {"Nazwa:", nameTextField, "Ilość:", quantityTextField, "Cena:", priceTextField}; //tablica etykiet do pokazania w oknie
        if (JOptionPane.showConfirmDialog(this, msg, (p == null ? "Dodaj" : "Edytuj"), JOptionPane.OK_CANCEL_OPTION) != 0)//jesli nowy produkt to Dodaj, jesli nie to Edytuj
            return;
        try {//pobranie danych z pól tekstowych
            String name = nameTextField.getText();
            int quantity = Integer.parseInt(quantityTextField.getText());
            double price = Double.parseDouble(priceTextField.getText());
            if (p == null) {
                ClientSession.out.writeObject(new String[]{"ADD"});
                ClientSession.out.writeObject(new Product(0, name, quantity, price));
                dodajLog("Dodano produkt: " + name + ", ilość: " + quantity + ", cena: " + price);
            } else {
                ClientSession.out.writeObject(new String[]{"EDIT"});
                ClientSession.out.writeObject(new Product(p.getId(), name, quantity, price));
                dodajLog("Zmieniono produkt: " + name + ", ilość: " + quantity + ", cena: " + price);
            }
            ClientSession.out.flush();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błędne dane!");
            dodajLog("Błąd danych przy edycji/dodawaniu produktu.");
        }
    }

    //dodawanie wpisu do loga
    public void dodajLog(String msg) {
        logTextArea.append(msg + "\n");
    }
}
