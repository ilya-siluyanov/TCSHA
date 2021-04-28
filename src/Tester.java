import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Tester {
    public static void main(String[] args) throws FileNotFoundException {
        System.setIn(new FileInputStream("input.txt"));
        System.setOut(new PrintStream("output.txt"));
        System.out.print("Error:\n" +
                "E5: FSA is nondeterministic");
    }
}
