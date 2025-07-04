package client;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderWindow extends JDialog {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea powiadomieniaTextArea;
    private JButton addButton, removeButton, editButton, sendButton;
    private JFrame parent; //referencja do okna rodzica MainWindow

    public OrderWindow(JFrame parent, Product p) {
        super(parent, "Twoje zamówienie", true);//okno modalne blokuje okno rodzica
        this.parent = parent;
        setLayout(new BorderLayout(8, 8));

        //Model tabeli zamówień
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nazwa", "Ilość", "Cena"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; } //nie mozna edytowac komorki po kliknieciu
            public Class getColumnClass(int col) { //ustawia odpowiednie typy kolumn jako klasy kopertowe
                return switch (col) {
                    case 0, 2 -> Integer.class;
                    case 3 -> Double.class;
                    default -> String.class;
                };
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//tylko jedno zaznaczenie na raz

        if (p != null)
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getQuantity(), p.getPrice()}); //jesli przekazano produkt to dodaje go do zamówienia

        //Powiadomienia
        powiadomieniaTextArea = new JTextArea(7, 18);
        powiadomieniaTextArea.setEditable(false);

        //Przyciski
        addButton = new JButton("Dodaj");
        removeButton = new JButton("Usuń");
        editButton = new JButton("Edytuj ilość");
        sendButton = new JButton("Wyślij zamówienie");

        addButton.addActionListener(e -> addProduct());
        removeButton.addActionListener(e -> removeProduct());
        editButton.addActionListener(e -> editProduct());
        sendButton.addActionListener(e -> sendOrder());

        JPanel btnPanel = new JPanel();
        btnPanel.add(addButton);
        btnPanel.add(removeButton);
        btnPanel.add(editButton);
        btnPanel.add(sendButton);

        //Panel po lewej (tabela + label + przyciski)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(new JLabel("Produkty w zamówieniu:"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        //Panel po prawej (powiadomienia)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Powiadomienia:"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(powiadomieniaTextArea), BorderLayout.CENTER);

        //Główny layout
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.7);
        add(split, BorderLayout.CENTER);

        setSize(650, 350);
        setLocationRelativeTo(parent); //centrowanie okienka do okienka rodzica
        setVisible(true);
    }

    private void addProduct() {
        Product[] arr = ClientSession.productCache.toArray(new Product[0]);
        Product sel = (Product) JOptionPane.showInputDialog(this, "Wybierz produkt", "Dodaj", JOptionPane.PLAIN_MESSAGE, null, arr, arr.length > 0 ? arr[0] : null); // zaznaczona pierwsza pozycja jesli lista nie jest pusta
        if (sel != null) {
            String ileStr = JOptionPane.showInputDialog(this, "Podaj ilość:", "1");
            try {
                int ile = Integer.parseInt(ileStr);
                if (ile <= 0) throw new NumberFormatException();
                tableModel.addRow(new Object[]{sel.getId(), sel.getName(), ile, sel.getPrice()});
                powiadomieniaTextArea.append("Dodano: " + sel.getName() + " x" + ile + "\n");
            } catch (Exception ex) {
                powiadomieniaTextArea.append("Błędna ilość!\n");
            }
        }
    }

    private void removeProduct() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String nazwa = (String) tableModel.getValueAt(row, 1);
            tableModel.removeRow(row);
            powiadomieniaTextArea.append("Usunięto: " + nazwa + "\n");
        } else {
            powiadomieniaTextArea.append("Nie wybrano produktu do usunięcia.\n");
        }
    }

    private void editProduct() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int aktualnaIlosc = (Integer) tableModel.getValueAt(row, 2);
            String ileStr = JOptionPane.showInputDialog(this, "Nowa ilość:", aktualnaIlosc);
            try {
                int nowaIlosc = Integer.parseInt(ileStr);
                if (nowaIlosc <= 0) throw new NumberFormatException();
                tableModel.setValueAt(nowaIlosc, row, 2);
                powiadomieniaTextArea.append("Zmieniono ilość produktu na: " + nowaIlosc + "\n");
            } catch (Exception ex) {
                powiadomieniaTextArea.append("Błędna ilość!\n");
            }
        } else {
            powiadomieniaTextArea.append("Nie wybrano produktu do edycji.\n");
        }
    }

    private void sendOrder() { //wysłanie zamówienia do serwera
        if (tableModel.getRowCount() == 0) {
            powiadomieniaTextArea.append("Dodaj produkty do zamówienia!\n");
            JOptionPane.showMessageDialog(this, "Dodaj produkty!");
            return;
        }
        Order order = new Order(ClientSession.loggedUser);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int id = (Integer) tableModel.getValueAt(i, 0);
            String nazwa = (String) tableModel.getValueAt(i, 1);
            int ilosc = (Integer) tableModel.getValueAt(i, 2);
            double cena = (Double) tableModel.getValueAt(i, 3);
            order.addProduct(new Product(id, nazwa, ilosc, cena));
        }
        try {
            ClientSession.out.writeObject(new String[]{"ORDER"});
            ClientSession.out.writeObject(order);
            ClientSession.out.flush();
            powiadomieniaTextArea.append("Zamówienie wysłane!\n");
            JOptionPane.showMessageDialog(this, "Zamówienie wysłane!");
            dispose();
        } catch (Exception ex) {
            powiadomieniaTextArea.append("Błąd przy wysyłaniu zamówienia!\n");
        }
    }
}