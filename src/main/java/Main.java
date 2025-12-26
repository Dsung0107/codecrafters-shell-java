import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage

        Scanner in = new Scanner(System.in);
        while (true) {

            System.out.print("$ ");
            String input = in.next();
            if (!input.equals("exit")) {
                System.out.println(input + ": command not found");
            }
            else if (input.equals("exit")) {
                break;
            }



        }
    }
}
