package duckcoder.managers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

public class ResourceManager {
    private HashMap<String, BufferedImage> images;

    public ResourceManager() {
        images = new HashMap<>();

        images.put("duck", load("/images/duck.png"));
        images.put("frame", load("/images/frame.png"));
        images.put("pointer", load("/images/pointer.png"));
    }

    private BufferedImage load(String path) {
        try {
            return ImageIO.read(ResourceManager.class.getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BufferedImage get(String name) {
        return images.get(name);
    }
}
