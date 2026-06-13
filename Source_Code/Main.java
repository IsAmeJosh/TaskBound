import ui_and_ux.TaskManagerGUI;

// Just kicks off the GUI. Kept separate from TaskManagerGUI so the entry
// point class has no package, matching how the project is run/compiled.
public class Main {
    public static void main(String[] args) throws Exception {
        TaskManagerGUI.main(args);
    }
}