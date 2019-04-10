package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;

public class Leaf extends WorldObject {
    public ArrayList<UniParticle> particles;
    public final int Type = 12;
    private int smallestX, smallestY, largestX, largestY;
    private Color baseColor0;

    public Leaf(int x, int y) {
        super(15, 2, 14);

        this.x = x;
        this.y = y;

        particles = new ArrayList<>();

        // HERE: Generate leaf
        baseColor0 = new Color(51, 120, 47);
        for(int i = random.nextInt(10)+5; i >= 0; i--) {
            double angle = random.nextDouble()*360d;
            double distance = calcGaussian(random.nextDouble(), 8)*40;

            double xx = Math.cos(Math.toRadians(angle))*distance;
            double yy = Math.sin(Math.toRadians(angle))*distance;

            particles.add(createParticle((int)xx, (int)yy));
        }
        findBounds();
    }

    public UniParticle createParticle(int addX, int addY) {
        int tone = random.nextInt(20)-15;
        Color c = new Color(
                baseColor0.getRed()+tone,
                baseColor0.getGreen()+tone,
                baseColor0.getBlue()+tone
        );

        UniParticle.Process stepOverProcess = new UniParticle.Process() {
            private double speedX, speedY, xx, yy, angle, targetAngle, size;
            private int x1, y1, x2, y2, x3, y3, x4, y4;
            private Color color;
            private boolean prevCollision;

            @Override
            public void init() {
                speedX = 0;
                speedY = 0;
                this.xx = owner.x;
                this.yy = owner.y;
                this.color = new Color(
                        baseColor0.getRed()+tone,
                        baseColor0.getGreen()+tone,
                        baseColor0.getBlue()+tone
                );
                this.angle = random.nextInt(359);
                targetAngle = angle;
                prevCollision = false;

                size = random.nextDouble()*3d+3d;

                x1 = (int)(Math.cos(Math.toRadians(angle))*size);
                y1 = (int)(Math.sin(Math.toRadians(angle))*size);
                x2 = (int)(Math.cos(Math.toRadians(angle-90))*size);
                y2 = (int)(Math.sin(Math.toRadians(angle-90))*size);
                x3 = (int)(Math.cos(Math.toRadians(angle-180))*size);
                y3 = (int)(Math.sin(Math.toRadians(angle-180))*size);
                x4 = (int)(Math.cos(Math.toRadians(angle-270))*size);
                y4 = (int)(Math.sin(Math.toRadians(angle-270))*size);
            }

            @Override
            public void render(Renderer r) {
                int xx = (int)Math.round(this.xx);
                int yy = (int)Math.round(this.yy);
                r.fillPolygon(
                        new int[]{xx+x1, xx+x2, xx+x3, xx+x4},
                        new int[]{yy+y1, yy+y2, yy+y3, yy+y4}, color);
            }

            @Override
            public void update() {
                double prevAngle = angle;
                speedX *= 0.85;
                speedY *= 0.85;

                ArrayList<GameObject> nearEntities = new ArrayList<>();
                for (GameObject entity : Main.main.visibleChunkEntities) {
                    if(entity.mask instanceof Mask.Rectangle &&
                            entity.mask.isColliding(mask)) {
                        nearEntities.add(entity);
                    }
                }

                boolean found = false;
                boolean move = random.nextBoolean();
                Player player = Main.main.player;
                if(Fly.distance(owner.x, owner.y, player.x, player.y) < 8d) {
                    if(move) {
                        speedX = Math.cos(Math.toRadians(
                                Fly.angle(player.x, player.y, owner.x, owner.y) - 180 +
                                random.nextInt(30)-15))*2d;
                        speedY = Math.sin(Math.toRadians(
                                Fly.angle(player.x, player.y, owner.x, owner.y) - 180 +
                                        random.nextInt(30)-15))*2d;
                    }
                    found = true;
                } else {
                    for (GameObject entity : nearEntities) {
                        if (Fly.distance(entity.x, entity.y, owner.x, owner.y) < 24d) {
                            if(move) {
                                speedX = Math.cos(Math.toRadians(
                                        Fly.angle(entity.x, entity.y, owner.x, owner.y) - 180));
                                speedY = Math.sin(Math.toRadians(
                                        Fly.angle(entity.x, entity.y, owner.x, owner.y) - 180));
                            }
                            found = true;
                            break;
                        }
                    }
                }

                if(found && !prevCollision) {
                    targetAngle += random.nextInt(90)-45;
                }

                prevCollision = found;

                angle += (targetAngle - angle) *0.1d;

                xx += Main.toSlowMotion(speedX);
                yy += Main.toSlowMotion(speedY);

                owner.x = (int)xx;
                owner.y = (int)yy;

                if(prevAngle != angle) {
                    x1 = (int)(Math.cos(Math.toRadians(angle))*size);
                    y1 = (int)(Math.sin(Math.toRadians(angle))*size);
                    x2 = (int)(Math.cos(Math.toRadians(angle-90))*size);
                    y2 = (int)(Math.sin(Math.toRadians(angle-90))*size);
                    x3 = (int)(Math.cos(Math.toRadians(angle-180))*size);
                    y3 = (int)(Math.sin(Math.toRadians(angle-180))*size);
                    x4 = (int)(Math.cos(Math.toRadians(angle-270))*size);
                    y4 = (int)(Math.sin(Math.toRadians(angle-270))*size);
                }
            }
        };

        return new UniParticle((int)x+addX, (int)y+addY, 0,
                true, c, stepOverProcess);
    }

    @Override
    public void update(Input i) {
        particles.forEach(UniParticle::update);
        findBounds();
    }

    private void findBounds() {
        smallestX=Integer.MAX_VALUE;
        smallestY=Integer.MAX_VALUE;
        largestX=Integer.MIN_VALUE;
        largestY=Integer.MIN_VALUE;

        particles.forEach((p) -> {
            smallestX = Math.min(smallestX, p.x);
            smallestY = Math.min(smallestY, p.y);
            largestX = Math.max(largestX, p.x);
            largestY = Math.max(largestY, p.y);
        });

        mask = new Mask.Rectangle(
                smallestX -10,
                smallestY -10,
                largestX -smallestX+20,
                largestY -smallestY+20);
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(o -> o.render(r));

        if(Main.main.showDebugInfo) {
            r.drawRectangle(mask.x, mask.y,
                    ((Mask.Rectangle) mask).w, ((Mask.Rectangle) mask).h,
                    Color.blue);
        }
    }

    public static double calcGaussian(double x, int pow) {
        return Math.pow(Math.sin(x+ Math.PI/2), pow);
    }
}
