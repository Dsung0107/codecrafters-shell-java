import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static HashSet<String> availableCommands = new HashSet<>();
    public static Path dirOG = Paths.get("").toAbsolutePath();
    public static String redirectTarget = null;
    public static String redirectError = null;
    public static boolean redirectAppend = false;
    public static ArrayList<String> history = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        availableCommands.addAll(List.of(new String[]{"echo", "type", "exit", "pwd", "cd", "history"}));
        Scanner in = new Scanner(System.in);
        PrintStream console = System.out;
        PrintStream error = System.err;
        while (true) {
            System.out.print("$ ");
            String input = in.nextLine();
            if (input.equals("exit")) {
                in.close();
                break;
                
            }
            ArrayList<String> response = new ArrayList<>();
            StringBuilder echoReturn = new StringBuilder();
            boolean inSingle = false;
            boolean inDouble = false;
            boolean backslashed = false;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == '"' && !inSingle && !backslashed) {
                    inDouble = !inDouble;
                    continue;
                }
                if (c == '\'' && !inDouble && !backslashed) {
                    inSingle = !inSingle;
                    continue;
                }
                if (c == '\\' && !inSingle && !backslashed) {
                    if (inDouble && input.charAt(i+1) != '\\' && input.charAt(i+1) != '"' ) {
                        backslashed = false;
                    }
                    else {
                        backslashed = true;
                        continue;
                    }
                }
                if (c == ' ' && !inSingle && !inDouble && !backslashed) {
                    if (echoReturn.length() > 0) {
                        response.add(echoReturn.toString());
                        echoReturn.setLength(0);
                    }
                }
                else {
                    echoReturn.append(c);
                    backslashed = false;
                }
            }
            if (echoReturn.length() > 0) {
                response.add(echoReturn.toString());
            }
            String[] command = response.toArray(new String[0]);
            command = redirectOutput(command);
            history.add(command[0]);
            PrintStream fileOut = null;
            if (redirectTarget != null) {
                fileOut = new PrintStream(new FileOutputStream (redirectTarget, redirectAppend));
                System.setOut(fileOut);
            }
            else if (redirectError != null) {
                fileOut = new PrintStream(new FileOutputStream(redirectError, redirectAppend));
                System.setErr(fileOut);
            }
            executeCMD(command, input);
            System.setOut(console);
            System.setErr(error);
            if (fileOut != null) {
                fileOut.close();            
            }
            redirectTarget = null;
            redirectError = null;
            redirectAppend = false;
        }
    }



    public static void executeCMD(String[] command, String input) throws IOException, InterruptedException {
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
        else if (command[0].equals("history")) {
            for (int i = 0; i < history.size(); i++) {
                System.out.printf(" %5d  %s%n", i + 1, history.get(i));
            }   
        }
        else {
            System.err.println(input + ": command not found");
        }
    }



    public static String[] redirectOutput(String[] arguments) throws FileNotFoundException {
        if (arguments.length > 2) {
            if (arguments[arguments.length-2].equals(">") ||
                    arguments[arguments.length-2].equals("1>")) {
                redirectTarget = arguments[arguments.length-1];
                arguments = Arrays.copyOfRange(arguments, 0, arguments.length - 2);
            }
            else if (arguments[arguments.length-2].equals("2>")) {
                redirectError = arguments[arguments.length-1];
                arguments = Arrays.copyOfRange(arguments, 0, arguments.length - 2);
            }
            else if (arguments[arguments.length-2].equals(">>") ||
                    arguments[arguments.length-2].equals("1>>")) {
                redirectTarget = arguments[arguments.length-1];
                arguments = Arrays.copyOfRange(arguments, 0, arguments.length - 2);
                redirectAppend = true;
            }
            else if (arguments[arguments.length-2].equals("2>>")) {
                redirectError = arguments[arguments.length-1];
                arguments = Arrays.copyOfRange(arguments, 0, arguments.length - 2);
                redirectAppend = true;
            }
        }
        return arguments;
    }


    public static int getType(String[] commands) {
        if (availableCommands.contains(commands[1])) {
            System.out.println(commands[1] + " is a shell builtin");
            return 0;
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
            System.err.println(commands[1] + ": not found");
            return -1;
        }
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
                if (redirectTarget != null) {
                    if (redirectAppend) {
                        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(redirectTarget)));
                    }
                    else {
                        pb.redirectOutput(new File(redirectTarget));
                    }
                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                }
                else if (redirectError != null) {
                    if (redirectAppend) {
                        pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(redirectError)));
                    }
                    else {
                        pb.redirectError(new File(redirectError));
                    }
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                } 
                else {
                    pb.inheritIO();
                }
                Process proc = pb.start();
                proc.waitFor();
                return 0;
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
            System.err.println("cd: " + command[1] + ": No such file or directory");
        }
    }



}
