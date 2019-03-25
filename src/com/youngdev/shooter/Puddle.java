package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.sun.javafx.geom.Vec3d;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Random;

public class Puddle extends WorldObject {

    public int type;
    private Random random;
    ArrayList<UniParticle> particles;
    public final int Type = 12;
    private Color color;

    Area area;

    public Puddle(int x, int y, int type) {
        super(10, 1, 12);
        this.x = x;
        this.y = y;
        random = new Random();

//        this.type = random.nextInt(3)+1;
        this.type = type;

        particles = new ArrayList<>();

        int smallestX = Integer.MAX_VALUE;
        int smallestY = Integer.MAX_VALUE;
        int largestX = Integer.MIN_VALUE;
        int largestY = Integer.MIN_VALUE;

        area = new Area();
        color = new Color(150, 150, 230);
        // Make the puddle blend in with the grass
        color = new Color(
                UniParticle.calcColorParameter(Main.grassColor.getRed(), color.getRed(), 0.25f),
                UniParticle.calcColorParameter(Main.grassColor.getGreen(), color.getGreen(), 0.25f),
                UniParticle.calcColorParameter(Main.grassColor.getBlue(), color.getBlue(), 0.25f)
        );

        for(int i = 0; i < random.nextInt(10)+10; i++) {

            double direction = random.nextDouble() * 360;
            double distance = Tree.calcGaussian(random.nextDouble(),
                    5)*random.nextDouble() * 60;

            int xx = (int) (this.x+Math.cos(Math.toRadians(direction))*distance);
            int yy = (int) (this.y+Math.sin(Math.toRadians(direction))*distance);
            int size = random.nextInt(16)+32;

            area.add(new Area(new Ellipse2D.Double(
                    xx-size/2, yy-size/2, size, size)));

            particles.add(new UniParticle(xx, yy, size, true, color){
                @Override
                public void render(Renderer r) {
                    r.fillCircle(x, y, size, color);
                }
            });

            smallestX = Math.min(smallestX, xx-size/2);
            smallestY = Math.min(smallestY, yy-size/2);
            largestX = Math.max(largestX, xx+size/2);
            largestY = Math.max(largestY, yy+size/2);
        }
        this.mask = new Mask.Rectangle(smallestX, smallestY,
                largestX-smallestX, largestY-smallestY);
    }

    @Override
    public void update(Input input) {
        particles.forEach(UniParticle::update);
        Player player = Main.main.player;
        if(area.contains(player.x, player.y)) {
            player.onPuddle = true;
        }
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(o -> o.render(r));
    }
}
