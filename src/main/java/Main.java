import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    HashSet<String> availableCommands = new HashSet<>();



    void main(String[] args) throws Exception {
        availableCommands.addAll(List.of(new String[]{"echo", "type", "exit"}));
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = in.nextLine();

            if (input.equals("exit")) {
                break;
            }
            String trimmed = input.trim();
            String[] command = trimmed.split(" ");
            if (command[0].equals("echo")) {
                System.out.println(input.substring(5));
            }
            else if (command[0].equals("type")) {
                type(command);

            }

            else {
                System.out.println(input + ": command not found");
            }



        }

    }

    int type(String[] commands) {
        if (availableCommands.contains(commands[1])) {
            System.out.println(commands[1] + " is a shell builtin");
        }
        else {
            String systemPATH = System.getenv("PATH");
            String[] paths = systemPATH != null ? systemPATH.split(File.pathSeparator) : new String[0];
            for (String path : paths) {
                File dir = new File(path);
                File commandFile = new File(dir, commands[1]);
                if (commandFile.exists() && commandFile.canExecute()) {
                    System.out.printf("%s is %s %n", commands[1], commandFile.getAbsolutePath());
                    return 0;
                }
            }
            System.out.println(commands[1] + ": not found");

        }
        return 0;
    }
}
