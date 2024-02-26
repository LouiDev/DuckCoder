package duckcoder;

import java.awt.*;

public class Exceptions {
    private MainWindow.WindowPanel panel;

    public static String EXC_CAN_NOT_LEAVE_GRID = "Ente kann das Spielfeld nicht verlassen";
    public static String EXC_INVALID_COMMAND = "Unbekannter Befehl";
    public static String EXCPARAM_INVALID_PARAMETER = "Ungültiger Parameter: ";
    public static String EXCPARAM_PARAM_REQUIRED = "Parameter erwartet: ";

    public Exceptions(MainWindow.WindowPanel panel) {
        this.panel = panel;
    }

    public void throwErr(String exception, int lineNumber) {
        panel.printConsoleMessage("Fehler in Zeile " + lineNumber + ": " + exception, Color.RED, Font.PLAIN);
    }

    public void throwErr(String exception, int lineNumber, String param) {
        panel.printConsoleMessage("Fehler in Zeile " + lineNumber + ": " + exception + "'" + param + "'", Color.RED, Font.PLAIN);
    }

}
