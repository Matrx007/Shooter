package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class StructuralBlock extends GameObject {
    public int type;
    public ArrayList<UniParticle> particles;
    public final int Type = 10;

    public static final int TYPE_ROCKS = 1, TYPE_WOOD = 2, TYPE_FIBER = 3, TYPE_WOOD_FLOORING = 4;

    public StructuralBlock(int x, int y, int type) {
        super(8, 8);
        this.type = type;
        this.x = x;
        this.y = y;

        Random random = new Random();
        particles = new ArrayList<>();

        if(type == TYPE_ROCKS) {
            this.solid = true;
            this.depth = 14;
            int numRocks = random.nextInt(10) + 20;
            for (int i = 0; i < numRocks; i++) {
                int xx = x + random.nextInt(12) + 3;
                int yy = y + random.nextInt(12) + 3;
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color color = new Color(80 + tone, 80 + tone, 80 + tone);
                particles.add(new UniParticle(xx, yy, random.nextInt(3) + 7, true, color));
            }
        } else if (type == TYPE_WOOD) {
            this.solid = true;
            this.depth = 7;
            int numPieces = random.nextInt(10) + 20;
            for (int i = 0; i < numPieces; i++) {
                int xx = x + random.nextInt(12) + 3;
                int yy = y + random.nextInt(12) + 3;
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color baseColor = new Color(158, 109, 80);
                Color color = new Color(
                        baseColor.getRed() + tone,
                        baseColor.getGreen() + tone,
                        baseColor.getBlue() + tone);
                particles.add(new UniParticle(xx, yy, random.nextInt(3) + 7, true, color));
            }
        } else if (type == TYPE_FIBER) {
            this.solid = true;
            this.depth = 7;
            int numPieces = random.nextInt(10) + 20;
            for (int i = 0; i < numPieces; i++) {
                int xx = x + random.nextInt(12) + 3;
                int yy = y + random.nextInt(12) + 3;
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color baseColor = new Color(128, 79, 50);
                Color color = new Color(
                        baseColor.getRed() + tone,
                        baseColor.getGreen() + tone,
                        baseColor.getBlue() + tone);
                particles.add(new UniParticle(xx, yy, random.nextInt(3) + 7, true, color));
            }
        } else if (type == TYPE_WOOD_FLOORING) {
            this.solid = false;
            this.depth = 7;
            int numPieces = random.nextInt(10) + 20;
            for (int i = 0; i < numPieces; i++) {
                int xx = x + random.nextInt(12) + 3;
                int yy = y + random.nextInt(12) + 3;
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color baseColor = new Color(178, 139, 100);
                Color color = new Color(
                        baseColor.getRed() + tone,
                        baseColor.getGreen() + tone,
                        baseColor.getBlue() + tone);
                particles.add(new UniParticle(xx, yy, random.nextInt(3) + 7, true, color));
            }
        }

        // HERE: Fix depth
        this.depth = random.nextInt(1023)+depth*1024;

        this.mask = new Mask.Rectangle(x, y, 16, 16);
        if(solid)
            this.aabbComponent = new AABBComponent(this.mask);
    }

    @Override
    public void update(Input input) {
        particles.removeIf(p -> {
            p.update();
            return p.dead;
        });
    }

    @Override
    public void render(Renderer renderer) {
        particles.forEach(p -> p.render(renderer));
    }

    public void render(Renderer r, int x, int y) {
        r.shift(x, y);
        particles.forEach(p -> p.render(r));
        r.shift(-x, -y);
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
