package javaapplication2;

import javax.swing.SwingUtilities;

public class JavaApplication2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
