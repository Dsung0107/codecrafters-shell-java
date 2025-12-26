import java.io.IOException;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
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
                getType(command);
            }

            else {
                executeCommand(command);

                System.out.println(input + ": command not found");
            }



        }

    }

    void getType(String[] commands) {
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
                }
            }
            System.out.println(commands[1] + ": not found");

        }
    }
    void executeCommand(String[] commands) throws IOException {
        String systemPATH = System.getenv("PATH");
        String[] paths = systemPATH != null ? systemPATH.split(File.pathSeparator) : new String[0];
        for (String path : paths) {
            File dir = new File(path);
            File commandFile = new File(dir, commands[0]);
            if (commandFile.exists() && commandFile.canExecute()) {
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.inheritIO();
                pb.start();


            }
        }
    }
}
