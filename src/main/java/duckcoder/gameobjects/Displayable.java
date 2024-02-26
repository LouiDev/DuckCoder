package duckcoder.gameobjects;

import duckcoder.MainWindow;

import java.awt.image.BufferedImage;

public class Displayable {
    protected int x;
    protected int y;
    protected BufferedImage img;

    public Displayable(String imgName) {
        x = 0;
        y = 0;
        img = MainWindow.rsManager.get(imgName);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public BufferedImage getImg() {
        return img;
    }
}
