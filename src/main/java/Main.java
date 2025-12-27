import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static HashSet<String> availableCommands = new HashSet<>();



    public static void main(String[] args) throws Exception {
        availableCommands.addAll(List.of(new String[]{"echo", "type", "exit", "pwd", "cd"}));
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = in.nextLine();

            if (input.equals("exit")) {
                break;
            }

            String trimmed = input.trim();
            String[] command = trimmed.split(" ");

            if ((command[0].equals("echo")) && (command.length > 1)) {
                System.out.println(input.substring(5));
            }
            else if ((command[0].equals("type")) && (command.length > 1)) {
                getType(command);
            }
            else if (command[0].equals("pwd")) {
                Path dirOG = Paths.get("").toAbsolutePath();
                System.out.println(dirOG);
            }
            else if ((command[0].equals("cd")) && (command.length > 1)) {
                changeDirectory(command);

            }
            else if (findPATH(command) == true) {
                executeCommand(command);
            }
            else {

                System.out.println(input + ": command not found");

            }



        }

    }

    public static int getType(String[] commands) {
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
    public static boolean findPATH(String[] commands) {
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
    public static int executeCommand(String[] commands) throws IOException, InterruptedException {
        String systemPATH = System.getenv("PATH");
        String[] paths = systemPATH != null ? systemPATH.split(File.pathSeparator) : new String[0];
        for (String path : paths) {
            File dir = new File(path);
            File commandFile = new File(dir, commands[0]);
            if (commandFile.exists() && commandFile.canExecute()) {
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.inheritIO();
                Process proc = pb.start();
                proc.waitFor();

                return 0;

            }
            else {
                return -1;
            }

        }
        return -1;
    }

    public static void changeDirectory(String[] path) throws IOException {
        Path target = Path.of(path[1]);
        Path dirOG = Paths.get("").toAbsolutePath();

        if (!Files.isDirectory(target)) {
            System.out.println("cd: " + path[1] + ": No such file or directory");
        }
        if (!target.isAbsolute()) {
            target = dirOG.resolve(target);

        }
        dirOG = target.normalize();

    }
}
