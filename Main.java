import javax.swing.SwingUtilities;

class Main extends Wellnest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Wellnest());
    }

    float num1 = 12.5f;
    float result = 0;
    
    public Main() {
        for (int i = 0; i < 10; i++) {
            result = result + num1;
            System.out.println("Result: " + result);
        }
        
    }
}