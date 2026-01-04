import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static HashSet<String> availableCommands = new HashSet<>();
    public static Path dirOG = Paths.get("").toAbsolutePath();


    public static void main(String[] args) throws Exception {
        availableCommands.addAll(List.of(new String[]{"echo", "type", "exit", "pwd", "cd"}));
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = in.nextLine();

            if (input.equals("exit")) {
                break;
            }

            ArrayList<String> response = new ArrayList<>();
            StringBuilder echoReturn = new StringBuilder();
            boolean inSingle = false;
            boolean inDouble = false;

            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == '"' && !inSingle) {
                    inDouble = !inDouble;
                    continue;
                }
                if (c == '\'' && !inDouble) {
                    inSingle = !inSingle;
                    continue;
                }
                if (c == ' ' && !inSingle && !inDouble) {
                    if (echoReturn.length() > 0) {
                        response.add(echoReturn.toString());
                        echoReturn.setLength(0);
                    }
                }
                else {
                    echoReturn.append(c);
                }
            }
            if (echoReturn.length() > 0) {
                response.add(echoReturn.toString());
            }
            String[] command = response.toArray(new String[0]);

            if ((command[0].equals("echo")) && (command.length > 1)) {

                for (int i = 1; i < command.length; i++) {
                    System.out.print(command[i] + " ");
                }
                System.out.printf("\n");

            }
            else if ((command[0].equals("type")) && (command.length > 1)) {
                getType(command);
            }
            else if (command[0].equals("pwd")) {

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
    public static void changeDirectory(String[] command) {
        Path target = dirOG.resolve(command[1]).toAbsolutePath().normalize();

        if (command[1].equals("~")) {
            command[1] = System.getenv("HOME");
            target = dirOG.resolve(command[1]).toAbsolutePath().normalize();
        }
        if (Files.isDirectory(target)) {
            dirOG = target;
        }
        else {
            System.out.println("cd: " + command[1] + ": No such file or directory");
        }
    }

}
