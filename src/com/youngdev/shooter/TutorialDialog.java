package com.youngdev.shooter;

import com.engine.libs.font.Alignment;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.RenderUtils;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TutorialDialog extends WorldObject {
    private BufferedImage preRendered;

    public TutorialDialog(double x, double y) {
        super(1000, 1000, 1000);
        this.x = x;
        this.y = y;
        this.mask = new Mask.Rectangle(x, y, 64*3+8*2, 64*2+8+64);
        preRender();
    }

    private void preRender() {
        preRendered = RenderUtils.makeCompatible(new BufferedImage(
                64*3+8*2, 64*2+8+64, BufferedImage.TYPE_INT_ARGB));

        Graphics g = preRendered.getGraphics();

        g.setColor(new Color(0, 0, 64));
        g.setFont(new Font("Nunito Bold", Font.PLAIN, 32));
        g.drawString("Liikumine", 8, 40);

        drawButton(g, "W", 64+8, 64);
        drawButton(g, "A", 0, 64+64+8);
        drawButton(g, "S",64+8, 64+64+8);
        drawButton(g, "D",64*2+8*2, 64+64+8);
    }

    private void drawButton(Graphics g, String name,
                            int x, int y) {
        // Outline
        g.setColor(new Color(64, 64, 64));
        g.fillRoundRect(x, y, 64, 64,
                16, 16);

        // Fill
        g.setColor(new Color(48, 48, 48));
        g.fillRoundRect(x+4, y+4, 64-8,
                64-8, 16, 16);

        // Label
        Renderer r = new Renderer(g, x+64, y+64);
        r.setFont(new Font("Shanghai", Font.BOLD, 0));
        r.drawText(name, x+32, y+32, 40,
                new Alignment(Alignment.HOR_CENTER, Alignment.VER_MIDDLE),
                new Color(96, 96, 96));
    }

    @Override
    public void update(Input input) {

    }

    @Override
    public void render(Renderer r) {
        r.drawImage(x, y, preRendered);
    }
}
