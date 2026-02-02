public class Commands {
    interface Command(String[] args) {
        void execute(String[] args);
    }
    public static Command get(String name) {
        return switch(name) {
            case "exit" -> new Exit();
            case "echo" -> new Echo();
            case "type" -> new Type();
            case "pwd" -> new Pwd();
            case "cd" -> new Cd();
            default -> null;
        }


    }
    public
}
