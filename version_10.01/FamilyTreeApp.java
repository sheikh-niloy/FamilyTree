import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Person implements Serializable {
    private String name;
    private List<Person> children;

    public Person(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Person> getChildren() {
        return children;
    }

    public void addChild(Person child) {
        children.add(child);
    }
}

class FamilyTreePanel extends JPanel {
    private Map<String, Person> peopleMap;
    private static final int CIRCLE_DIAMETER = 40;
    private static final int X_OFFSET = 100;
    private static final int Y_OFFSET = 80;
    private static final String TREE_DATA_FILE = "familyTreeData.ser";

    public FamilyTreePanel() {
        this.peopleMap = loadFamilyTreeData();
        setBackground(new Color(34, 34, 34)); // Dark background color
    }

    public void saveFamilyTreeData() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(TREE_DATA_FILE))) {
            outputStream.writeObject(peopleMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Person> loadFamilyTreeData() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(TREE_DATA_FILE))) {
            return (Map<String, Person>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void reset() {
        peopleMap.clear();
        saveFamilyTreeData();
        repaint();
    }

    public void addPerson(String name, String parentName) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        name = name.trim();
        parentName = parentName.trim();

        Person person = new Person(name);
        if (!parentName.isEmpty() && peopleMap.containsKey(parentName)) {
            peopleMap.get(parentName).addChild(person);
        } else {
            peopleMap.put(name, person);
        }
        saveFamilyTreeData();
        repaint();
    }

    public void deletePerson(String name) {
        if (name == null || name.trim().isEmpty() || !peopleMap.containsKey(name)) {
            JOptionPane.showMessageDialog(this, "Person not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + name + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            peopleMap.remove(name);
            for (Person p : peopleMap.values()) {
                p.getChildren().removeIf(child -> child.getName().equals(name));
            }
            saveFamilyTreeData();
            repaint();
        }
    }

    public void editPerson(String oldName, String newName) {
        if (oldName == null || oldName.trim().isEmpty() || !peopleMap.containsKey(oldName)) {
            JOptionPane.showMessageDialog(this, "Person not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        newName = newName.trim();
        Person person = peopleMap.get(oldName);
        person.setName(newName);
        peopleMap.remove(oldName);
        peopleMap.put(newName, person);
        for (Person p : peopleMap.values()) {
            List<Person> children = p.getChildren();
            for (Person child : children) {
                if (child.getName().equals(oldName)) {
                    child.setName(newName);
                }
            }
        }
        saveFamilyTreeData();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!peopleMap.isEmpty()) {
            drawTree(g, getWidth() / 2, 30, peopleMap.values().iterator().next(), getWidth() / 4);
        }
    }

    private void drawTree(Graphics g, int x, int y, Person person, int xOffset) {
        if (person == null) return;

        g.setColor(Color.WHITE); // White text color
        g.drawOval(x - CIRCLE_DIAMETER / 2, y - CIRCLE_DIAMETER / 2, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        g.drawString(person.getName(), x - CIRCLE_DIAMETER / 4, y + CIRCLE_DIAMETER / 2);

        List<Person> children = person.getChildren();
        int numChildren = children.size();
        int startX = x - xOffset * numChildren / 2;
        int startY = y + Y_OFFSET;

        for (int i = 0; i < numChildren; i++) {
            int childX = startX + xOffset * i;
            int childY = startY + Y_OFFSET;
            g.drawLine(x, y + CIRCLE_DIAMETER / 2, childX + CIRCLE_DIAMETER / 2, childY - CIRCLE_DIAMETER / 2);
            drawTree(g, childX, childY, children.get(i), xOffset / 2);
        }
    }
}

public class FamilyTreeApp extends JFrame {
    private FamilyTreePanel treePanel;
    private JButton signInButton;
    private JButton signUpButton;
    private JButton signOutButton;
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;
    private Map<String, String> userCredentials;
    private static final String USER_CREDENTIALS_FILE = "userCredentials.ser";

    public FamilyTreeApp() {
        setTitle("Family Tree");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(34, 34, 34)); // Dark background color

        treePanel = new FamilyTreePanel();
        add(treePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(34, 34, 34)); // Dark control panel background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField nameField = new JTextField(20);
        nameField.setBackground(new Color(64, 64, 64)); // Dark text field background
        nameField.setForeground(Color.WHITE); // White text color
        JTextField parentField = new JTextField(20);
        parentField.setBackground(new Color(64, 64, 64)); // Dark text field background
        parentField.setForeground(Color.WHITE); // White text color
        addButton = new JButton("Add Person");
        deleteButton = new JButton("Delete Person");
        editButton = new JButton("Edit Person");
        signInButton = new JButton("Sign In");
        signUpButton = new JButton("Sign Up");
        signOutButton = new JButton("Sign Out");
        userCredentials = loadUserCredentials();

        addButton.setToolTipText("Add a new person to the family tree");
        deleteButton.setToolTipText("Delete a person from the family tree");
        editButton.setToolTipText("Edit the name of a person in the family tree");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String parentName = parentField.getText();
                treePanel.addPerson(name, parentName);
                nameField.setText("");
                parentField.setText("");
                nameField.requestFocus();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter the name of the person to delete:");
                if (name != null) {
                    treePanel.deletePerson(name.trim());
                }
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String oldName = JOptionPane.showInputDialog("Enter the current name of the person to edit:");
                if (oldName != null) {
                    String newName = JOptionPane.showInputDialog("Enter the new name:");
                    if (newName != null) {
                        treePanel.editPerson(oldName.trim(), newName.trim());
                    }
                }
            }
        });

        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = JOptionPane.showInputDialog("Enter your username:");
                String password = JOptionPane.showInputDialog("Enter your password:");
                if (verifyCredentials(username, password)) {
                    JOptionPane.showMessageDialog(null, "Welcome, " + username + "!");
                    enableButtons(true);
                    treePanel.reset(); // Reset the family tree
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newUsername = JOptionPane.showInputDialog("Enter a new username:");
                String newPassword = JOptionPane.showInputDialog("Enter a password:");
                if (newUsername != null && newPassword != null) {
                    userCredentials.put(newUsername, newPassword);
                    saveUserCredentials();
                    JOptionPane.showMessageDialog(null, "Sign up successful! You can now sign in.");
                }
            }
        });

        signOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Signed out successfully.");
                enableButtons(false);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Enter Name:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        controlPanel.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Enter Parent's Name (optional):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        controlPanel.add(parentField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlPanel.add(addButton, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        controlPanel.add(deleteButton, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        controlPanel.add(editButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        controlPanel.add(signInButton, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        controlPanel.add(signUpButton, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        controlPanel.add(signOutButton, gbc);
        add(controlPanel, BorderLayout.SOUTH);

        setButtonStyles();
        enableButtons(false); // Initially disable all buttons besides Sign Up and Sign In
        setSize(1000, 800); // Set initial size
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setButtonStyles() {
        JButton[] buttons = {signInButton, signUpButton, signOutButton, addButton, deleteButton, editButton};
        for (JButton button : buttons) {
            button.setBackground(new Color(59, 89, 182));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        signOutButton.setBackground(Color.RED);
    }

    private boolean verifyCredentials(String username, String password) {
        return userCredentials.containsKey(username) && userCredentials.get(username).equals(password);
    }

    private Map<String, String> loadUserCredentials() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(USER_CREDENTIALS_FILE))) {
            return (Map<String, String>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void saveUserCredentials() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(USER_CREDENTIALS_FILE))) {
            outputStream.writeObject(userCredentials);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableButtons(boolean enabled) {
        JButton[] buttons = {addButton, deleteButton, editButton};
        for (JButton button : buttons) {
            button.setEnabled(enabled);
        }
        signInButton.setEnabled(!enabled);
        signUpButton.setEnabled(!enabled);
        signOutButton.setEnabled(enabled);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FamilyTreeApp();
            }
        });
    }
}
