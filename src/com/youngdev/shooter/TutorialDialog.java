package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Image;
import com.engine.libs.rendering.RenderUtils;
import com.engine.libs.rendering.Renderer;

import java.awt.image.BufferedImage;

public class TutorialDialog extends WorldObject {
    private BufferedImage image;

    public TutorialDialog(double x, double y) {
        super(1000, 1.5, 1000);
        this.x = x;
        this.y = y;
        this.image = new Image("/help.png").getImage();
        this.mask = new Mask.Rectangle(x, y,
                image.getWidth(), image.getHeight());
    }

    @Override
    public void update(Input input) {

    }

    @Override
    public void render(Renderer r) {
        r.drawImage(x, y, image);
    }
}
