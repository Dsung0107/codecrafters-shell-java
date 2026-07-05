import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jline.reader.*;
import org.jline.terminal.*;
import org.jline.reader.impl.history.DefaultHistory;

public class Main {
    public static HashSet<String> availableCommands = new HashSet<>();
    public static Path dirOG = Paths.get("").toAbsolutePath();
    public static String redirectTarget = null;
    public static String redirectError = null;
    public static boolean redirectAppend = false;
    public static ArrayList<String> historyList = new ArrayList<>();
    public static int countSinceLastWrite = 0;
    
    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        History history = new DefaultHistory();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .history(history)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .build();

        availableCommands.addAll(List.of(new String[]{"echo", "type", "exit", "pwd", "cd", "history"}));
        PrintStream console = System.out;
        PrintStream error = System.err;
        
        while (true) {
            try {
                String input = reader.readLine("$ ");
                if (input.equals("exit")) {
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

            
            
        
            } catch (EndOfFileException e) {
                break;
            } catch (UserInterruptException e) {
                continue;
            }
            
        }
        terminal.close();
    }



    public static void executeCMD(String[] command, String input) throws IOException, InterruptedException {
        if (command.length == 0) {
            return;
        }
        countSinceLastWrite++;
        historyList.add(input);
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
        else if (command[0].equals("history")) {
            getHistory(command);
        }
        else if (findPATH(command) == true) {
            executeCommand(command);
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

    public static void getHistory(String[] command) {
        if (command.length == 1) {
            for (int i = 0; i < historyList.size(); i++) {
                System.out.printf(" %5d  %s%n", i + 1, historyList.get(i));
            }
        }
        else if (command.length > 1 && command[1].equals("-r")) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(command[2]));
                for (String line : lines) {
                    if (!line.trim().isBlank()) {
                        historyList.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("history: error reading history file");
            }
        }
        else if (command.length > 1 && command[1].equals("-w")) {
            try {
                Files.write(Paths.get(command[2]), historyList);
                countSinceLastWrite = 0;
            } catch (IOException e) {
                System.err.println("history: error writing history file");
            }
        }
        else if (command.length > 1 && command[1].equals("-a")) {
            try {
                File file = new File(command[2]);
                FileOutputStream fos = new FileOutputStream(file, true);
                int start = historyList.size() - countSinceLastWrite;
                for (int i = start; i < historyList.size(); i++) {
                    fos.write((historyList.get(i) + System.lineSeparator()).getBytes());
                }
                fos.close();
                countSinceLastWrite = 0;
            } catch (IOException e) {
                System.err.println("history: error writing history file");
            }
        }
        else {
             try {
                int n = Integer.parseInt(command[1]);
                int start = Math.max(0, historyList.size() - n);
                for (int i = start; i < historyList.size(); i++) {
                    System.out.printf(" %5d  %s%n", i + 1, historyList.get(i));
                }
            }
            catch (NumberFormatException e) {
                System.err.println("history: " + command[1] + ": numeric argument required");
            }
        }
        
    }

    


}
