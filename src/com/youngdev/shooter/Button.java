package com.youngdev.shooter;

import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;
import com.sun.javafx.geom.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Button {
    private int y;
    private String text;
    private int width;
    public boolean pressed;
    private boolean mouseHover;
    private ArrayList<Vec3d> hoverVectors;

    public Button(int y, String text) {
        this.y = y;
        this.text = text;
        width = 192;
        hoverVectors = new ArrayList<>();
    }

    public void update(Input i) {
        pressed = false;
        boolean prevHover = mouseHover;
        mouseHover = AdvancedMath.inRange(i.getMouseX(), i.getMouseY(),
                0, y, width, 24);
        if(mouseHover && !prevHover) {
            Main.main.soundManager.playSound("buttonHover", -3f);
            Main.main.ui.hoverTargetY = y;
        }
        Main.main.ui.hovering =
                Main.main.ui.hovering || mouseHover;
        if(mouseHover && i.isButtonDown(1)) {
            Main.main.soundManager.playSound("buttonPress", -3f);
            pressed = true;
        }

        Iterator<Vec3d> iterator = hoverVectors.iterator();
        for(;iterator.hasNext();) {
            Vec3d vec = iterator.next();
            vec.y /= 1.05;
            vec.z -= 0.1;
            if(vec.y <= 0.003) {
                iterator.remove();
            }
        }
    }

    public void renderText(Renderer r) {
        Graphics g = r.getG();
        g.setColor(UI.UI_BACK.darker().darker().darker());
        g.setFont(new Font("Nunito Bold", Font.PLAIN, 16));
        g.drawString(text, 8, y+16);
    }

    public void render(Renderer r) {
        renderText(r);
    }
}
