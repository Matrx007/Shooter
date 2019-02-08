package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ProjectTile extends WorldObject {
    private double direction;
    private static final double SPEED = 3f;
    private static final double SIZE = 2f;

    private double lifetime;

    private ArrayList<UniParticle> particles;

    private int wiggleX, wiggleY;

    public ProjectTile(int x, int y, double direction) {
        super(17, 14, 17);
        this.x = x;
        this.y = y;
        this.direction = direction;
        particles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            createRandomParticle();
        }
        lifetime = 1d;
        this.mask = new Mask.Rectangle(x-(int)SIZE, y-(int)SIZE,
                (int)SIZE*2, (int)SIZE*2);
    }

    private void createRandomParticle() {
        double direction = random.nextDouble()*360d;
        double xx = Math.cos(Math.toRadians(direction)) * SIZE;
        double yy = Math.sin(Math.toRadians(direction)) * SIZE;

        this.particles.add(new UniParticle((int)Math.round(x + xx),
                (int)Math.round(y + yy), 3,
                true, Color.BLACK,
                new UniParticle.ResizingProcess(
                        5d, 0d, 0.25d)));
    }

    @Override
    public void update(Input input) {
        lifetime -= 0.0025;
        this.x += Main.toSlowMotion(Math.cos(
                Math.toRadians(direction)) * SPEED);
        this.y += Main.toSlowMotion(Math.sin(
                Math.toRadians(direction)) * SPEED);

        if(lifetime > 0 && calculateProbability())
            createRandomParticle();

        int smallest_x = Integer.MAX_VALUE;
        int smallest_y = Integer.MAX_VALUE;
        int largest_x = Integer.MIN_VALUE;
        int largest_y = Integer.MIN_VALUE;

        Iterator<UniParticle> particleIterator;

        UniParticle particle;
        for(particleIterator = particles.iterator();
                particleIterator.hasNext();) {
            particle = particleIterator.next();
            particle.update();

            smallest_x = Math.min(smallest_x, particle.x);
            smallest_y = Math.min(smallest_y, particle.y);
            largest_x = Math.max(largest_x, particle.x);
            largest_y = Math.max(largest_y, particle.y);

            if(particle.dead)
                particleIterator.remove();
        }

        this.mask = new Mask.Rectangle(smallest_x, smallest_y,
                largest_x, largest_y);

        if(!Main.main.visibleAreaMask.
                isColliding(this.mask)) {
            dead = true;
        }

        wiggleX = random.nextInt(4)-2;
        wiggleY = random.nextInt(4)-2;

        if(Fly.distance(x, y, Main.main.player.x, Main.main.player.y) < 8d &&
                lifetime > 0 && !Main.main.player.isDead) {
            Main.main.player.health--;
            lifetime = 0d;
        }
    }

    private boolean calculateProbability() {
        return random.nextDouble() <= Main.slowMotionSpeed;
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(p -> p.render(r));
//        r.fillCircle(scoreX+wiggleX, scoreY+wiggleY, (int)SIZE*2, Color.black);
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }
}
