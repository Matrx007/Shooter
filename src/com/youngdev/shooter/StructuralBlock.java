package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class StructuralBlock extends WorldObject {
    public int type;
    public ArrayList<UniParticle> particles;
    public final int Type = 10;
    public static final int SIZE = 32;

    public static final int TYPE_ROCKS = 1, TYPE_WOOD = 2, TYPE_FIBER = 3, TYPE_WOOD_FLOORING = 4,
            TYPE_STONE_FLOORING = 5;

    public StructuralBlock(int x, int y, int type) {
        super(8, 8, 10);
        this.type = type;
        this.x = x;
        this.y = y;

        particles = new ArrayList<>();

        double _depth = 0;
        if(type == TYPE_STONE_FLOORING) {
            this.solid = false;
            _depth = 0;
            int numRocks = random.nextInt(5) + 5;
            for (int i = 0; i < numRocks; i++) {
                int xx = x + random.nextInt(SIZE);
                int yy = y + random.nextInt(SIZE);
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color color = new Color(120 + tone, 120 + tone, 120 + tone);
                particles.add(new UniParticle(xx, yy,
                        random.nextInt(3) + 4,
                        true, color));
            }
        } else if(type == TYPE_ROCKS) {
            this.solid = true;
            _depth = 1;
            int numRocks = random.nextInt(10) + 20;
            for (int i = 0; i < numRocks; i++) {
                int xx = x + random.nextInt(SIZE);
                int yy = y + random.nextInt(SIZE);
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color color = new Color(80 + tone, 80 + tone, 80 + tone);
                particles.add(new UniParticle(xx, yy,
                        random.nextInt(3) + 7,
                        true, color));
            }
        } else if (type == TYPE_WOOD) {
            this.solid = true;
            _depth = 0;
            int numPieces = random.nextInt(10) + 20;
            for (int i = 0; i < numPieces; i++) {
                int xx = x + random.nextInt(SIZE);
                int yy = y + random.nextInt(SIZE);
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color baseColor = new Color(158, 109, 80);
                Color color = new Color(
                        baseColor.getRed() + tone,
                        baseColor.getGreen() + tone,
                        baseColor.getBlue() + tone);
                particles.add(new UniParticle(xx, yy,
                        random.nextInt(3) + 7,
                        true, color));
            }
        } else if (type == TYPE_FIBER) {
            this.solid = true;
            _depth = 0;
            int numPieces = random.nextInt(10) + 20;
            for (int i = 0; i < numPieces; i++) {
                int xx = x + random.nextInt(SIZE);
                int yy = y + random.nextInt(SIZE);
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color baseColor = new Color(128, 79, 50);
                Color color = new Color(
                        baseColor.getRed() + tone,
                        baseColor.getGreen() + tone,
                        baseColor.getBlue() + tone);
                particles.add(new UniParticle(xx, yy,
                        random.nextInt(3) + 7,
                        true, color));
            }
        } else if (type == TYPE_WOOD_FLOORING) {
            this.solid = false;
            _depth = 0;
            int numPieces = random.nextInt(10) + 20;
            for (int i = 0; i < numPieces; i++) {
                int xx = x + random.nextInt(SIZE);
                int yy = y + random.nextInt(SIZE);
                int tone = random.nextInt(3) - 5;
                tone *= 5;
                Color baseColor = new Color(178, 139, 100);
                Color color = new Color(
                        baseColor.getRed() + tone,
                        baseColor.getGreen() + tone,
                        baseColor.getBlue() + tone);
                particles.add(new UniParticle(xx, yy,
                        random.nextInt(3) + 7,
                        true, color));
            }
        }
        this.depth = 8 * 1024 + random.nextInt(
                1+(int)(1023d*_depth));

        this.mask = new Mask.Rectangle(x, y, SIZE, SIZE);
        if(solid)
            this.aabbComponent = new AABBComponent(this.mask);
    }

    @Override
    public void update(Input input) {
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(p -> p.render(r));
    }

    public void render(Renderer r, int x, int y) {
        r.shift(x, y);
        particles.forEach(p -> p.render(r));
        r.shift(-x, -y);
    }
}
