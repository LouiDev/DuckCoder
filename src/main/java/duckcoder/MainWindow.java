package duckcoder;

import duckcoder.gameobjects.Displayable;
import duckcoder.gameobjects.Duck;
import duckcoder.managers.CodeManager;
import duckcoder.managers.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainWindow {
    private JFrame frame;
    private List<Displayable> objects;
    private int gridOriginX;
    private int gridOriginY;
    private CodeManager codeManager;

    public static ResourceManager rsManager;
    public static Exceptions exceptions;
    public static Duck duck;
    public static int gridSizeHorizontal = 12;
    public static int gridSizeVertical = 12;
    public static WindowPanel panel;

    public MainWindow() {
        rsManager = new ResourceManager();

        gridOriginX = 782;
        gridOriginY = 30;

        objects = new ArrayList<Displayable>();

        panel = new WindowPanel(this);
        frame = new JFrame("DuckCoder");
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1600, 1000);
        frame.setResizable(false);
        frame.setVisible(true);

        duck = new Duck();
        exceptions = new Exceptions(panel);

        drawGrid();
        renderDisplayable(duck);

        codeManager = new CodeManager();

        panel.repaint();
    }

    private void drawGrid() {
        for(int i = 0; i < gridSizeVertical; i++) {
            for(int j = 0; j < gridSizeHorizontal; j++) {
                Displayable caste = new Displayable("frame");
                caste.setLocation(i, j);
                renderDisplayable(caste);
            }
        }
    }

    public void renderDisplayable(Displayable d) {
        objects.add(d);
    }

    public void destroyDisplayable(Displayable d) {
        objects.remove(d);
    }

    public List<Displayable> getObjects() {
        return objects;
    }

    public int getGridOriginX() {
        return gridOriginX;
    }

    public int getGridOriginY() {
        return gridOriginY;
    }
    public boolean isRendered(Displayable d) {
        return objects.contains(d);
    }

    public class WindowPanel extends JPanel {
        private MainWindow window;
        private JTextField textField;
        private JButton loadFilebtn;
        private JButton stepBtn;
        private JButton playBtn;
        private JButton stopBtn;
        private String consoleMessage;
        private Color consoleColor;
        private int consoleStyle;
        private List<String> codeDisplay;
        private int codeDisplayOriginX;
        private int codeDisplayOriginY;
        private Timer timer;

        public WindowPanel(MainWindow window) {
            this.window = window;

            textField = new JTextField();
            textField.setLocation(20, 30);
            textField.setSize(700, 30);
            add(textField);

            loadFilebtn = new JButton("Datei laden");
            loadFilebtn.setLocation(782, 818);
            loadFilebtn.setSize(100, 30);
            loadFilebtn.addActionListener(e -> {
                String input = getTextInput();
                if(input.isEmpty() || input.isBlank()) {
                    printConsoleMessage("Bitte gebe einen Dateipfad an!", Color.RED, Font.BOLD);
                } else {
                    loadFile(input);
                    MainWindow.duck.setLocation(0, 0);
                }
            });
            add(loadFilebtn);

            playBtn = new JButton("Abspielen");
            playBtn.setLocation(1000, 818);
            playBtn.setSize(100, 30);
            playBtn.addActionListener(e -> {
                codeManager.update();
                repaint();
                timer = new Timer(50, e1 -> {
                    codeManager.update();
                    repaint();
                });
                timer.start();
            });
            add(playBtn);

            stepBtn = new JButton("Schritt");
            stepBtn.setLocation(1120, 818);
            stepBtn.setSize(100, 30);
            stepBtn.addActionListener(e -> {
                codeManager.update();
                repaint();
            });
            add(stepBtn);

            stopBtn = new JButton("Stop");
            stopBtn.setLocation(1240, 818);
            stopBtn.setSize(100, 30);
            stopBtn.addActionListener(e -> {
                stopTimer();
            });
            add(stopBtn);

            consoleMessage = "";
            codeDisplayOriginX = 60;
            codeDisplayOriginY = 95;

            codeDisplay = new ArrayList<>();

            setLayout(null);
            setVisible(true);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            //Displayables
            List<Displayable> objects = window.getObjects();
            for(Displayable d : objects) {
                int x = d.getX();
                int y = d.getY();
                BufferedImage img = d.getImg();
                g.drawImage(img, gridOriginX + (x * 64), gridOriginY + (y * 64), null);
            }

            //Console
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(consoleColor);
            g2d.setFont(new Font("Arial", consoleStyle, 22));
            g2d.drawString(consoleMessage, 782, 910);

            //Code
            drawCode(g);

            //Code pointer
            Displayable pointer = codeManager.getPointer();
            g.drawImage(pointer.getImg(), pointer.getX(), pointer.getY(), null);
        }

        public void stopTimer() {
            if(timer != null) timer.stop();
        }

        public void startTimer() {
            if(timer != null) timer.start();
        }

        public void printConsoleMessage(String message, Color color, int style) {
            consoleMessage = message;
            consoleColor = color;
            consoleStyle = style;
            repaint();
        }

        private void drawCode(Graphics g) {
            int offsetX = 0;

            for(int i = 0; i < codeDisplay.size(); i++) {
                Graphics2D code = (Graphics2D) g;
                String text =  codeDisplay.get(i);
                offsetX = 0;

                int whitespaces = 0;
                for(int j = 0; j < text.length(); j++) {
                    char c = text.charAt(j);
                    if(c == ' ') {
                        whitespaces++;
                    } else {
                        break;
                    }
                }

                String[] content = text.split("\\s+");
                for(String toDisplay : content) {
                    toDisplay = toDisplay.stripLeading();
                    toDisplay = toDisplay.replaceAll(";", "");

                    Color color = Color.BLACK;
                    if (codeManager.getCodeColor(toDisplay) != null) {
                        color = codeManager.getCodeColor(toDisplay);
                    }
                    code.setColor(color);
                    code.setFont(new Font("Arial", Font.PLAIN, 16));

                    StringBuilder sb = new StringBuilder();
                    while(whitespaces > 0) {
                        sb.append(" ");
                        whitespaces--;
                    }
                    toDisplay = sb.toString() + " " + toDisplay;
                    code.drawString(toDisplay, codeDisplayOriginX + offsetX, codeDisplayOriginY + i * 21);

                    offsetX += g.getFontMetrics().stringWidth(toDisplay);
                }
                g.setColor(Color.BLACK);
                code.drawString(";", codeDisplayOriginX + offsetX, codeDisplayOriginY + i * 21);
            }
        }

        private void loadFile(String path) {
            File file = new File(path);

            if(file.exists()) {
                codeDisplay = codeManager.load(file);
                printConsoleMessage("Datei geladen!", Color.GREEN, Font.PLAIN);
            } else {
                printConsoleMessage("Bitte gebe einen validen Dateipfad an!", Color.RED, Font.PLAIN);
            }
        }

        public String getTextInput() {
            return textField.getText();
        }
    }
}
