import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    HashSet<String> availableCommands = new HashSet<>();



    void main(String[] args) throws Exception {
        availableCommands.addAll(List.of(new String[]{"echo", "type", "exit", "pwd"}));
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
            else if (command[0].equals("pwd")) {
                Path dirOG = Paths.get("").toAbsolutePath();
                System.out.println(dirOG);
            }
            else if (findPATH(command) == true) {
                executeCommand(command);
            }
            else {

                System.out.println(input + ": command not found");

            }



        }

    }

    int getType(String[] commands) {
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
            return 0;
        }
        return 0;
    }
    boolean findPATH(String[] commands) {
        String systemPATH = System.getenv("PATH");
        String[] paths = systemPATH != null ? systemPATH.split(File.pathSeparator) : new String[0];
        for (String path : paths) {
            File dir = new File(path);
            File commandFile = new File(dir, commands[0]);
            if (commandFile.exists() && commandFile.canExecute()) {
                return true;
            }
            else {
                return false;
            }

        }
        return false;
    }
    int executeCommand(String[] commands) throws IOException {
        String systemPATH = System.getenv("PATH");
        String[] paths = systemPATH != null ? systemPATH.split(File.pathSeparator) : new String[0];
        for (String path : paths) {
            File dir = new File(path);
            File commandFile = new File(dir, commands[0]);
            if (commandFile.exists() && commandFile.canExecute()) {
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.inheritIO();
                pb.start();
                return 0;

            }
            else {
                return -1;
            }

        }
        return -1;
    }
}
