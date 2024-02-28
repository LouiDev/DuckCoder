package duckcoder.managers;

import duckcoder.Exceptions;
import duckcoder.MainWindow;
import duckcoder.commandlogic.Executable;
import duckcoder.commandlogic.RegisterReturnable;
import duckcoder.commandlogic.Returnable;
import duckcoder.gameobjects.Displayable;
import duckcoder.gameobjects.Duck;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class CodeManager {
    private List<String> rawInput;          //Rohe Eingabe ohne Einrückungen
    private int commandPointer;
    private int lastExecuted;
    private int scriptSize;
    private boolean isComplete;
    private HashMap<String, Executable> actionCommands;
    private HashMap<String, Returnable<Boolean>> booleanReturnCommands;
    private HashMap<String, Returnable<Integer>> intReturnCommands;
    private HashMap<String, RegisterReturnable<?>> registerReturnCommands;
    private HashMap<String, Color> codeColors;
    private HashMap<Integer, Integer> register;
    private Displayable pointer;

    public CodeManager() {
        rawInput = new ArrayList<>();
        actionCommands = new HashMap<>();
        booleanReturnCommands = new HashMap<>();
        intReturnCommands = new HashMap<>();
        codeColors = new HashMap<>();
        register = new HashMap<>();
        registerReturnCommands = new HashMap<>();

        isComplete = false;

        pointer = new Displayable("pointer");
        pointer.setLocation(10, -100);

        init();
    }

    // Schreitet im Programmcode voran
    public void update() {
        if (!isComplete) {
            String raw = rawInput.get(commandPointer);
            String[] params = raw.split(" ");
            String command = params[0];

            lastExecuted = commandPointer;
            pointer.setLocation(10, 75 + lastExecuted * 21);

            if (raw.isBlank() || raw.isEmpty() || raw.stripLeading().contains("//") || raw.stripLeading().startsWith("(") && raw.endsWith(")")) {
                commandPointer++;
                return;
            }

            if (actionCommands.containsKey(command)) {
                if (actionCommands.get(command).execute(List.of(params))) commandPointer++;

                if (commandPointer == scriptSize) {
                    isComplete = true;
                    MainWindow.panel.stopTimer();
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXC_INVALID_COMMAND, commandPointer + 1);
                isComplete = true;
            }
        }
    }

    // Lädt die angegebene Datei und bereit alles für die Ausführung vor
    public List<String> load(File file) {
        rawInput = new ArrayList<>();
        List<String> display = new ArrayList<>();

        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();

            while (line != null) {
                display.add(line);

                if (line.contains(";")) line = line.replaceAll(";", "");
                line = line.stripLeading();
                rawInput.add(line);

                line = reader.readLine();
            }

            fr.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        commandPointer = 0;
        isComplete = false;

        pointer.setLocation(10, -100);

        scriptSize = rawInput.size();
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
        codeColors.put("springewennregister", Color.RED);

        codeColors.put("istrechtswand", Color.MAGENTA);
        codeColors.put("istlinkswand", Color.MAGENTA);
        codeColors.put("istobenwand", Color.MAGENTA);
        codeColors.put("istuntenwand", Color.MAGENTA);

        Color green = new Color(24, 161, 61);
        codeColors.put("registersetzen", green);
        codeColors.put("registererhöhen", green);
        codeColors.put("registerverringern", green);
        codeColors.put("register:1", green);
        codeColors.put("register:2", green);
        codeColors.put("register:3", green);
        codeColors.put("register:4", green);
        codeColors.put("register:5", green);
        codeColors.put("register:6", green);
        codeColors.put("register:7", green);
        codeColors.put("register:8", green);
        codeColors.put("register:9", green);
        codeColors.put("register:10", green);
        codeColors.put("register:11", green);
        codeColors.put("register:12", green);
        codeColors.put("register:13", green);
        codeColors.put("register:14", green);
        codeColors.put("register:15", green);
        codeColors.put("register:16", green);
        codeColors.put("register:17", green);
        codeColors.put("register:18", green);
        codeColors.put("register:19", green);
        codeColors.put("register:20", green);

        for (int i = 1; i < 21; i++) {
            register.put(i, 0);
        }

        //Beenden
        actionCommands.put("beenden", params -> {
            isComplete = true;
            MainWindow.panel.printConsoleMessage("Programm beendet", Color.BLUE, Font.PLAIN);
            return true;
        });

        //Forwärtslaufen
        actionCommands.put("vorwärtslaufen", params -> {
            int moveAmount = 1;

            if (params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    int reg = getRegisterIndex(params.get(1));
                    if (reg == -1) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                        isComplete = true;
                    } else {
                        moveAmount = getRegisterValue(reg);
                    }
                }
            }

            Duck duck = MainWindow.duck;
            if (duck.getX() + moveAmount > (MainWindow.gridSizeHorizontal - 1) || duck.getX() + moveAmount > 0) {
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

            if (params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    int reg = getRegisterIndex(params.get(1));
                    if (reg == -1) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                        isComplete = true;
                    } else {
                        moveAmount = getRegisterValue(reg);
                    }
                }
            }

            Duck duck = MainWindow.duck;
            if (duck.getX() - moveAmount < 0 || duck.getX() + moveAmount > (MainWindow.gridSizeHorizontal - 1)) {
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

            if (params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    int reg = getRegisterIndex(params.get(1));
                    if (reg == -1) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                        isComplete = true;
                    } else {
                        moveAmount = getRegisterValue(reg);
                    }
                }
            }

            Duck duck = MainWindow.duck;
            if (duck.getY() - moveAmount < 0 || duck.getY() - moveAmount < (MainWindow.gridSizeVertical - 1)) {
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

            if (params.size() > 1) {
                try {
                    moveAmount = Integer.parseInt(params.get(1));
                } catch (NumberFormatException ignored) {
                    int reg = getRegisterIndex(params.get(1));
                    if (reg == -1) {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                        isComplete = true;
                    } else {
                        moveAmount = getRegisterValue(reg);
                    }
                }
            }

            Duck duck = MainWindow.duck;
            if (duck.getY() + moveAmount > (MainWindow.gridSizeVertical - 1) || duck.getY() + moveAmount > 0) {
                MainWindow.exceptions.throwErr(Exceptions.EXC_CAN_NOT_LEAVE_GRID, commandPointer + 1);
                isComplete = true;
            } else {
                MainWindow.duck.movedown(moveAmount);
            }

            return true;
        });

        //Springen
        actionCommands.put("springen", params -> {
            if (params.size() > 1) {
                checkAndJump(params, 1);
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "Zeilenindex");
                isComplete = true;
            }

            return false;
        });

        //Springewenn
        actionCommands.put("springewenn", params -> {
            if (params.size() > 2) {
                boolean match = false;
                for (String key : booleanReturnCommands.keySet()) {
                    if (params.get(1).contains(key)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    if (booleanReturnCommands.get(params.get(1)).get()) {
                        checkAndJump(params, 2);
                        return false;
                    }
                } else {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOWN_ATTRIBUTE, commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "Attribut, Zeilenindex");
                isComplete = true;
            }
            return true;
        });

        //Springenichtwenn
        actionCommands.put("springenichtwenn", params -> {
            if (params.size() > 2) {
                boolean match = false;
                for (String key : booleanReturnCommands.keySet()) {
                    if (params.get(1).contains(key)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    if (!booleanReturnCommands.get(params.get(1)).get()) {
                        checkAndJump(params, 2);
                        return false;
                    }
                } else {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOWN_ATTRIBUTE, commandPointer + 1, params.get(1));
                    isComplete = true;
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "Attribut, Zeilenindex");
                isComplete = true;
            }
            return true;
        });

        actionCommands.put("teleportieren", params -> {
            if (params.size() > 2) {
                int xPos = -1;
                int yPos = -1;

                try {
                    xPos = Integer.parseInt(params.get(1));
                } catch (NumberFormatException e) {
                    if (params.get(1).startsWith("register:")) {
                        int reg = getRegisterIndex(params.get(1));
                        if (reg == -1) {
                            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                            isComplete = true;
                        } else {
                            xPos = (int) registerReturnCommands.get("register:").get(reg);
                        }
                    } else {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOWN_ATTRIBUTE, commandPointer + 1, params.get(1));
                        isComplete = true;
                    }
                }

                try {
                    yPos = Integer.parseInt(params.get(2));
                } catch (Exception e) {
                    if (params.get(2).startsWith("register:")) {
                        int reg = getRegisterIndex(params.get(2));
                        if (reg == -1) {
                            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(2));
                            isComplete = true;
                        } else {
                            yPos = (int) registerReturnCommands.get("register:").get(reg);
                        }
                    } else {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOWN_ATTRIBUTE, commandPointer + 1, params.get(1));
                        isComplete = true;
                    }
                }

                if (xPos < 1 || xPos > MainWindow.gridSizeHorizontal || yPos < 1 || yPos > MainWindow.gridSizeVertical) {
                    MainWindow.exceptions.throwErr(Exceptions.EXC_CAN_NOT_LEAVE_GRID, commandPointer + 1);
                    isComplete = true;
                } else {
                    MainWindow.duck.setLocation(xPos - 1, yPos - 1);
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "x Koordinate, y Koordinate");
                isComplete = true;
            }

            return true;
        });

        //Register setzen
        actionCommands.put("registersetzen", params -> {
            if (params.size() > 2) {
                if (params.get(1).startsWith("register:")) {
                    try {
                        int num = Integer.parseInt(params.get(2));
                        String[] regArgs = params.get(1).split(":");
                        try {
                            int regIndex = Integer.parseInt(regArgs[1]);
                            if (regIndex < 1 || regIndex > 20) {
                                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                                isComplete = true;
                            } else {
                                register.put(regIndex, num);
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(1));
                            isComplete = true;
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
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "Register, Integer");
                isComplete = true;
            }
            return true;
        });

        //Registererhöhen
        actionCommands.put("registererhöhen", params -> {
            if (params.size() > 2) {
                int reg = getRegisterIndex(params.get(1));
                if (reg == -1) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                    isComplete = true;
                } else {
                    int increase = 0;
                    try {
                        increase = Integer.parseInt(params.get(2));
                    } catch (NumberFormatException e) {
                        int increaseReg = getRegisterIndex(params.get(2));
                        if (increaseReg == -1) {
                            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(2));
                            isComplete = true;
                        } else {
                            increase = getRegisterValue(increaseReg);
                        }
                    }

                    int value = getRegisterValue(reg);
                    value += increase;
                    register.put(reg, value);
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "Register, Integer ODER Register");
                isComplete = true;
            }
            return true;
        });

        //Registererhöhen
        actionCommands.put("registerverringern", params -> {
            if (params.size() > 2) {
                int reg = getRegisterIndex(params.get(1));
                if (reg == -1) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(1));
                    isComplete = true;
                } else {
                    int increase = 0;
                    try {
                        increase = Integer.parseInt(params.get(2));
                    } catch (NumberFormatException e) {
                        int increaseReg = getRegisterIndex(params.get(2));
                        if (increaseReg == -1) {
                            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, params.get(2));
                            isComplete = true;
                        } else {
                            increase = getRegisterValue(increaseReg);
                        }
                    }

                    int value = getRegisterValue(reg);
                    value -= increase;
                    register.put(reg, value);
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "Register, Integer ODER Register");
                isComplete = true;
            }
            return true;
        });

        // Springewennregister
        actionCommands.put("springewennregister", params -> {
            if (params.size() > 4) {
                String param1 = params.get(1);
                String param2 = params.get(2);
                String param3 = params.get(3);
                int reg = getRegisterIndex(param1);
                if (reg == -1) {
                    MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, param1);
                    isComplete = true;
                } else {
                    int num = getRegisterValue(reg);
                    if (param2.equals("<") || param2.equals("<=") || param2.equals("==") || param2.equals(">=") || param2.equals(">")) {
                        int toCompare;
                        if(param3.startsWith("register:")) {
                            int reg2 = getRegisterIndex(param3);
                            if (reg2 == -1) {
                                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOW_REGISTER, commandPointer + 1, param1);
                                isComplete = true;
                                return true;
                            } else {
                                toCompare = getRegisterValue(reg2);
                            }
                        } else {
                            try {
                                toCompare = Integer.parseInt(param3);
                            } catch (NumberFormatException e) {
                                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, param3);
                                isComplete = true;
                                return true;
                            }
                        }

                        switch (param2) {
                            case "<" -> {
                                if (num < toCompare) {
                                    checkAndJump(params, 4);
                                    return false;
                                }
                            }
                            case "<=" -> {
                                if (num <= toCompare) {
                                    checkAndJump(params, 4);
                                    return false;
                                }
                            }
                            case "==" -> {
                                if (num == toCompare) {
                                    checkAndJump(params, 4);
                                    return false;
                                }
                            }
                            case ">=" -> {
                                if (num >= toCompare) {
                                    checkAndJump(params, 4);
                                    return false;
                                }
                            }
                            case ">" -> {
                                if (num > toCompare) {
                                    checkAndJump(params, 4);
                                    return false;
                                }
                            }
                        }
                    } else {
                        MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_OPERANT, commandPointer + 1, param2);
                        isComplete = true;
                    }
                }
            } else {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_PARAM_REQUIRED, commandPointer + 1, "Register, Operant, Register ODER Integer, Zeilenindex");
                isComplete = true;
            }

            return true;
        });

        intReturnCommands.put("xposition", () -> MainWindow.duck.getX());
        intReturnCommands.put("yposition", () -> MainWindow.duck.getY());

        booleanReturnCommands.put("istrechtswand", () -> MainWindow.duck.getX() == MainWindow.gridSizeHorizontal - 1);
        booleanReturnCommands.put("istlinkswand", () -> MainWindow.duck.getX() == 0);
        booleanReturnCommands.put("istobenwand", () -> MainWindow.duck.getY() == 0);
        booleanReturnCommands.put("istuntenwand", () -> MainWindow.duck.getY() == MainWindow.gridSizeVertical - 1);

        registerReturnCommands.put("register:", (RegisterReturnable<Integer>) this::getRegisterValue);

        commandPointer = 0;
    }

    private void checkAndJump(List<String> params, int paramIndex) {
        try {
            int index = Integer.parseInt(params.get(paramIndex));
            if (index < 1 || index > scriptSize) {
                MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_UNKNOWN_LINE_INDEX, commandPointer + 1, params.get(paramIndex));
                isComplete = true;
            } else {
                commandPointer = index - 1;
            }
        } catch (NumberFormatException e) {
            MainWindow.exceptions.throwErr(Exceptions.EXCPARAM_INVALID_PARAMETER, commandPointer + 1, params.get(paramIndex));
            isComplete = true;
        }
    }

    private int getRegisterIndex(String input) {
        String[] args = input.split(":");
        try {
            int reg = Integer.parseInt(args[1]);
            if (reg > 0 && reg < 21) {
                return reg;
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    public int getRegisterValue(int reg) {
        return register.get(reg);
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
