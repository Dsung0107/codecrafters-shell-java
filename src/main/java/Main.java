import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage

        Scanner in = new Scanner(System.in);
        while (true) {

            System.out.print("$ ");
            String input = in.nextLine();

            if ((input.length() > 4) && (input.substring(0, 5).equals("echo "))) {
                System.out.println(input.substring(5));
            }
            else if (input.equals("exit")) {
                break;
            }
            else if ((input.length() > 4) && (input.substring(0,5).equals("type "))) {
                String after = input.substring(5);
                if ((after.equals("echo")) || (after.equals("exit")) || (after.equals("type"))) {
                    System.out.println(after + " is a shell builtin");
                }
                else {
                    System.out.println(after + ": not found");
                }
            }



            else {
                System.out.println(input + ": command not found");
            }



        }
    }
}
