package duckcoder.managers;

import duckcoder.Exceptions;
import duckcoder.MainWindow;
import duckcoder.commandlogic.Executable;
import duckcoder.commandlogic.Returnable;
import duckcoder.gameobjects.Displayable;
import duckcoder.gameobjects.Duck;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class CodeManager {
    private List<String> rawInput;          //Rohe Eingabe ohne Einrückungen
    private int commandPointer;
    private int lastExecuted;
    private int scriptSize;
    private boolean isComplete;
    private HashMap<String, Executable> actionCommands;
    private HashMap<String, Returnable<?>> returnCommands;
    private HashMap<String, Color> codeColors;
    private Displayable pointer;

    public CodeManager() {
        rawInput = new ArrayList<>();
        actionCommands = new HashMap<>();
        returnCommands = new HashMap<>();
        codeColors = new HashMap<>();

        isComplete = false;

        pointer = new Displayable("pointer");
        pointer.setLocation(10, -100);

        init();
    }

    // Schreitet im Programmcode voran
    public void update() {
        if(!isComplete) {
            String raw = rawInput.get(commandPointer);
            String[] params = raw.split(" ");
            String command = params[0];

            if(raw.startsWith("/") || (raw.startsWith("(") && raw.endsWith(")"))) {
                lastExecuted = commandPointer;
                commandPointer++;
                return;
            }

            if(actionCommands.containsKey(command)) {
                lastExecuted = commandPointer;
                pointer.setLocation(10, 75 + lastExecuted * 21);

                if(actionCommands.get(command).execute(List.of(params))) commandPointer++;

                if(commandPointer == scriptSize) {
                    isComplete = true;
                    MainWindow.panel.stopTimer();
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXC_INVALID_COMMAND, commandPointer + 1);
                pointer.setLocation(10, 75 + lastExecuted * 21);
                isComplete = true;
            }
        }
    }

    // Lädt die angegeben Datei und bereit alles für die Ausführung vor
    public List<String> load(File file) {
        rawInput = new ArrayList<>();
        List<String> display;
        try {
            String content = new String(Files.readAllBytes(Path.of(file.getAbsolutePath())));
            display = List.of(content.split("\n"));

            content = content.replaceAll("\n", "");
            String[] inputs = content.split(";");
            for(String s : inputs) {
                rawInput.add(s.stripLeading());
            }

            scriptSize = rawInput.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        commandPointer = 0;
        isComplete = false;

        pointer.setLocation(10, -100);

        return display;
    }

    private void init() {
        codeColors.put("vorwärtslaufen", Color.BLUE);
        codeColors.put("rückwärtslaufen", Color.BLUE);
        codeColors.put("hochlaufen", Color.BLUE);
        codeColors.put("runterlaufen", Color.BLUE);
        codeColors.put("teleportieren", Color.BLUE);

        codeColors.put("beenden", Color.RED);
        codeColors.put("springen", Color.RED);
        codeColors.put("springewenn", Color.RED);
        codeColors.put("springenichtwenn", Color.RED);

        codeColors.put("istrechtswand", Color.MAGENTA);
        codeColors.put("istlinkswand", Color.MAGENTA);
        codeColors.put("istobenwand", Color.MAGENTA);
        codeColors.put("istuntenwand", Color.MAGENTA);

        //Beenden
        actionCommands.put("beenden", params -> {
           isComplete = true;
           MainWindow.panel.printConsoleMessage("Programm beendet", Color.BLUE, Font.PLAIN);
           return true;
        });

        //Forwärtslaufen
        actionCommands.put("vorwärtslaufen", params -> {
            int moveAmount = 1;

            if(params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER,commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            }

            Duck duck = MainWindow.duck;
            if(duck.getX() + moveAmount > (MainWindow.gridSizeHorizontal - 1)) {
                MainWindow.exceptions.throwErr(Exceptions.EXC_CAN_NOT_LEAVE_GRID, commandPointer + 1);
                isComplete = true;
            } else {
                MainWindow.duck.movefoward(moveAmount);
            }

            return true;
        });

        //Rückwärtslaufen
        actionCommands.put("rückwärtslaufen", params -> {
            int moveAmount = 1;

            if(params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER,commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            }

            Duck duck = MainWindow.duck;
            if(duck.getX() - moveAmount < 0) {
                MainWindow.exceptions.throwErr(Exceptions.EXC_CAN_NOT_LEAVE_GRID, commandPointer + 1);
                isComplete = true;
            } else {
                MainWindow.duck.movebackwards(moveAmount);
            }

            return true;
        });

        //Hochlaufen
        actionCommands.put("hochlaufen", params -> {
            int moveAmount = 1;

            if(params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            }

            Duck duck = MainWindow.duck;
            if(duck.getY() - moveAmount < 0) {
                MainWindow.exceptions.throwErr(Exceptions.EXC_CAN_NOT_LEAVE_GRID, commandPointer + 1);
                isComplete = true;
            } else {
                MainWindow.duck.moveup(moveAmount);
            }

            return true;
        });

        //Runterlaufen
        actionCommands.put("runterlaufen", params -> {
            int moveAmount = 1;

            if(params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            }

            Duck duck = MainWindow.duck;
            if(duck.getY() + moveAmount > (MainWindow.gridSizeVertical - 1)) {
                MainWindow.exceptions.throwErr(Exceptions.EXC_CAN_NOT_LEAVE_GRID, commandPointer + 1);
                isComplete = true;
            } else {
                MainWindow.duck.movedown(moveAmount);
            }

            return true;
        });

        //Springen
        actionCommands.put("springen", params -> {
            if(params.size() > 1) {
                try {
                    int index = Integer.parseInt(params.get(1));
                    if(index < 1 || index > scriptSize - 1) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(1));
                        isComplete = true;
                    } else {
                        commandPointer = index - 1;
                    }
                } catch (NumberFormatException e) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1,"Zeilenindex");
                isComplete = true;
            }

            return false;
        });

        //Springewenn
        actionCommands.put("springewenn", params -> {
            if(params.size() > 2) {
                boolean match = false;
                for(String key : returnCommands.keySet()) {
                    if (params.get(1).contains(key)) {
                        match = true;
                        break;
                    }
                }

                if(match) {
                    try {
                        int index = Integer.parseInt(params.get(2));

                        if(index < 1 || index > scriptSize) {
                            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(2));
                            isComplete = true;
                        } else {
                            boolean result = (boolean) returnCommands.get(params.get(1)).get();
                            if(result) {
                                commandPointer = index - 1;
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(2));
                        isComplete = true;
                    }
                } else {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1,"Attribut, Zeilenindex");
                isComplete = true;
            }
            return true;
        });

        //Springenichtwenn
        actionCommands.put("springenichtwenn", params -> {
            if(params.size() > 2) {
                boolean match = false;
                for(String key : returnCommands.keySet()) {
                    if (params.get(1).contains(key)) {
                        match = true;
                        break;
                    }
                }

                if(match) {
                    try {
                        int index = Integer.parseInt(params.get(2));

                        if(index < 1 || index > scriptSize) {
                            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(2));
                            isComplete = true;
                        } else {
                            boolean result = (boolean) returnCommands.get(params.get(1)).get();
                            if(!result) {
                                commandPointer = index - 1;
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(2));
                        isComplete = true;
                    }
                } else {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1,"Attribut, Zeilenindex");
                isComplete = true;
            }
            return true;
        });

        actionCommands.put("teleportieren", params -> {
            if(params.size() > 2) {
                try {
                    int xPos = Integer.parseInt(params.get(1));
                    try {
                        int yPos = Integer.parseInt(params.get(2));

                        if(xPos < 1 || xPos > MainWindow.gridSizeHorizontal || yPos < 1 || yPos > MainWindow.gridSizeVertical) {
                            MainWindow.exceptions.throwErr(Exceptions.EXC_CAN_NOT_LEAVE_GRID, commandPointer + 1);
                            isComplete = true;
                        } else {
                            MainWindow.duck.setLocation(xPos - 1, yPos - 1);
                        }
                    } catch (Exception e) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1,"y Koordinate");
                        isComplete = true;
                    }
                } catch (NumberFormatException e) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1,"x Koordinate");
                    isComplete = true;
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1,"x Koordinate, y Koordinate");
                isComplete = true;
            }

            return true;
        });

        returnCommands.put("xposition", (Returnable<Integer>) () -> MainWindow.duck.getX());
        returnCommands.put("yposition", (Returnable<Integer>) () -> MainWindow.duck.getY());
        returnCommands.put("istrechtswand", (Returnable<Boolean>) () -> MainWindow.duck.getX() == MainWindow.gridSizeHorizontal - 1);
        returnCommands.put("istlinkswand", (Returnable<Boolean>) () -> MainWindow.duck.getX() == 0);
        returnCommands.put("istobenwand", (Returnable<Boolean>) () -> MainWindow.duck.getY() == 0);
        returnCommands.put("istuntenwand", (Returnable<Boolean>) () -> MainWindow.duck.getY() == MainWindow.gridSizeVertical - 1);

        commandPointer = 0;
    }

    public List<String> getRawInput() {
        return rawInput;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public Displayable getPointer() {
        return pointer;
    }

    public HashMap<String, Color> getCodeColors() {
        return codeColors;
    }

    public Color getCodeColor(String command) {
        return codeColors.get(command);
    }
}
