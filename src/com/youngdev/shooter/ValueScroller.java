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
        width = Main.main.getE().getWidth();
        hoverVectors = new ArrayList<>();
        uiText = text.replace("$", getUIValue());
    }

    public void update(Input i) {
        boolean prevHover = mouseHover;
        mouseHover = AdvancedMath.inRange(i.getMouseX(), i.getMouseY(),
                0, y, width, 24);
        if(mouseHover && !prevHover) {
            Main.main.soundManager.playSound("buttonHover", -5f);
            hoverVectors.add(new Vec3d(i.getMouseX(), 1d, 1d));
        }
        if(i.isButtonDown(1)) {
            if (mouseHover) {
                Main.main.soundManager.playSound("buttonPress", -5f);
                selected = true;
            } else {
                selected = false;
            }
        }
        if(selected) {
            if (i.isKeyDown(KeyEvent.VK_LEFT)) {
                value -= step;
                value = Math.max(minValue, value);
                Main.main.soundManager.playSound("buttonPress", -5f);
            }
            if (i.isKeyDown(KeyEvent.VK_RIGHT)) {
                value += step;
                value = Math.min(maxValue, value);
                Main.main.soundManager.playSound("buttonPress", -5f);
            }
        }

        Iterator<Vec3d> iterator = hoverVectors.iterator();
        for(;iterator.hasNext();) {
            Vec3d vec = iterator.next();
            if(!mouseHover || (vec).y > 0.2d)
                vec.y /= 1.05;
            vec.z -= 0.05;
            if(vec.y <= 0.003) {
                iterator.remove();
            }
        }

        uiText = text.replace("$", getUIValue());
    }

    public String getUIValue() {
        return value+"";
    }

    public void renderText(Renderer r) {
        Graphics g = r.getG();
        g.setColor(Color.black);
        g.setFont(new Font("Nunito Bold", Font.PLAIN, 16));
        g.drawString(uiText, 8, y+16);
    }

    public void render(Renderer r) {
        // ### BACKGROUND ###
        r.absolute();
        for(Vec3d vec : hoverVectors) {
            int w = (int)((1 - vec.z)*width*2d);
            r.fillRectangle((int)vec.x-w/2d, y, w, 24,
                    new Color(255, 255, 255,
                            (int)(vec.y*128d)));
        }
        if(selected)
        r.fillRectangle(0, y, width, 24,
                new Color(255, 255, 255,
                        32));

        // ### VALUE INDICATOR ###
        int width = (int)Math.round((value - minValue)/(maxValue-minValue)*
                this.width);
        Color color1 = new Color(0, 0, 0, 0);
        Color color2 = new Color(16, 16, 16,
                selected ? 128 : 64);
        GradientPaint gradient =
                new GradientPaint(0, 0, color1,
                        width, 0, color2);
        Graphics2D g2d = (Graphics2D) r.getG();
        Paint oldPaint = g2d.getPaint();
        g2d.setPaint(gradient);
        g2d.fillRect(0, y, width, 24);
        g2d.setPaint(oldPaint);
        renderText(r);
    }
}
