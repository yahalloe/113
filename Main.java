import javax.swing.SwingUtilities;

class Main extends Wellnest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Wellnest());
    }
}