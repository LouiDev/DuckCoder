package duckcoder.gameobjects;

public class Duck extends Displayable {
    public Duck() {
        super("duck");
    }

    public void moveup(int length) {
        y -= length;
    }
    public void movedown(int length) {
        y += length;
    }
    public void movefoward(int length) {
        x += length;
    }
    public void movebackwards(int length) {
        x -= length;
    }
}
