import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage

        Scanner in = new Scanner(System.in);
        while (true) {

            System.out.print("$ ");
            String input = in.nextLine();

            if ((input.length() > 5) && (input.substring(0, 5).equals("echo "))) {
                System.out.println(input.substring(5));
            }
            else if (input.equals("exit")) {
                break;
            }
            else {
                System.out.println(input + ": command not found");
            }



        }
    }
}
