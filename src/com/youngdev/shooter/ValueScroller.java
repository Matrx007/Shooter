package com.youngdev.shooter;

import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;
import com.sun.javafx.geom.Vec3d;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class ValueScroller {
    private int y;
    private String text, uiText;
    private double minValue;
    private double maxValue;
    private double step;
    public double value;
    private int width;
    private boolean mouseHover, selected;
    private ArrayList<Vec3d> hoverVectors;

    public ValueScroller(int y, String text, double minValue,
                         double maxValue, double step,
                         double defaultValue) {
        this.value = defaultValue;
        this.y = y;
        this.text = text;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        width = 192;
        hoverVectors = new ArrayList<>();
        uiText = text.replace("$", getUIValue());
    }

    private double map(double n,
                       double start1, double end1,
                       double start2, double end2) {
        return ((n-start1)/(end1-start1))*(end2-start2)+start2;
    }

    public void update(Input i) {
        // ### OTHER ###
        boolean prevHover = mouseHover;
        mouseHover = AdvancedMath.inRange(i.getMouseX(), i.getMouseY(),
                0, y, width, 24);
        width = (int)Math.round(Main.main.ui.backX);

        // ### ON HOVER ###
        if(mouseHover && !prevHover && !i.isButton(1)) {
            Main.main.soundManager.playSound("buttonHover");
            Main.main.ui.hoverTargetY = y;
        }
        Main.main.ui.hovering =
                Main.main.ui.hovering || mouseHover;
        // ### ON CLICK / START HOLDING ###
        if(i.isButtonDown(1)) {
            if (mouseHover) {
                Main.main.soundManager.playSound("buttonPress");
                selected = true;
            }
        }

        // ### VALUE CHANGING / WHILE HOLDING ###
        if(i.isButton(1)) {
            if (selected) {
                value = AdvancedMath.setRange(
                        map(i.getMouseX(),
                        0, width, minValue, maxValue),
                        minValue, maxValue);
            }
        } else selected = false;

        // ### TEXT FORMATION ###
        uiText = text.replace("$", getUIValue());
    }

    public String getUIValue() {
        return value+"";
    }

    public void renderText(Renderer r) {
        Graphics g = r.getG();
        g.setColor(new Color(0, 0, 0, selected ? 192 : 128));
        g.setFont(new Font("Nunito Bold", Font.PLAIN, 16));
        g.drawString(uiText, 8, y+16);
    }

    public void render(Renderer r) {
        // ### VALUE INDICATOR ###
        int width = (int)Math.round((value - minValue)/(maxValue-minValue)*
                this.width);
        r.fillRectangle(0, y, width, 24,
                new Color(0, 0, 0, selected ? 64 : 32));

        // ### RENDER TEXT ###
        renderText(r);
    }
}
