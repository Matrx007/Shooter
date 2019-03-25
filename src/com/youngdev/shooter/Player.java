package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;
import com.engine.libs.world.CollisionMap;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.util.*;

public class Player extends Healable {

    public ArrayList<UniParticle> particles;
    public AABBCollisionManager cm;

    boolean prevOnPlant, onPlant, prevOnPuddle, onPuddle;
    public int xx;
    public int yy;
    private int midX;
    private int midY;
    private int time=60;
    private int timer;
    public long money;
    int moveX;
    int moveY;

    double lastCoinX, lastCoinY, coinOverlayAlpha,
            coinOverlayX, coinOverlayY;
    private double slowMotionSpeedMultiplierTarget;

    private static Color baseColor = new Color(220, 212, 148);
    // 170, 32, 128
    private float speedX, targetSpeedX, speedY, targetSpeedY,
            speedStep, blinkingTimer;
    private ArrayList<Vector8> rays;
    private ShadowRenderer shadowRenderer;
    public final int Type = 7;
    private int step;
    public static int MaxHealth = 5;
    public int health;
    public boolean isDead;
    public static float MaxSpeed = 3f;

    public Player(int x, int y) {
        super(7, x, y, 4, 4, 200, 0, 10, false, false);
        this.depth = 10;
        this.random = new Random();
        particles = new ArrayList<>();

        // HERE: Fix depth
        depth = random.nextInt(1023)+depth*1024;

        onPlant = false;
        prevOnPlant = false;
        isDead = false;

        lastCoinX = x;
        lastCoinY = y-256;

        midX = Main.main.getE().getWidth()/2;
        midY = Main.main.getE().getHeight()/2;

        core = Main.main.getE();

        super.x = x;
        super.y = y;

        this.xx = x;
        this.yy = y;

        this.targetSpeedX = 0;
        this.targetSpeedY = 0;
        this.speedX = 0;
        this.speedY = 0;
        this.speedStep = 0.5f;
        this.blinkingTimer = 0;

        health = MaxHealth;

        slowMotionSpeedMultiplierTarget = 1d;

        rays = new ArrayList<>();
        shadowRenderer = new ShadowRenderer();
        CollisionMap collisionMap = Main.collisionMap;
        mask = new Mask.Rectangle(x-5, y-5, 10, 10);
        aabbComponent = new AABBComponent(this.mask);
        cm = new AABBCollisionManager(this, collisionMap);
    }

    @Override
    public void update(Input i) {
        // ###### MOVEMENT AND SOUNDS ########
        blinkingTimer += 1d/Main.toSlowMotion(1d);

        if(!isDead) {
            // HERE: Use keyboard to move
            moveX = (i.isKey(KeyEvent.VK_D) ? 1 : 0) - (i.isKey(KeyEvent.VK_A) ? 1 : 0);
            moveY = (i.isKey(KeyEvent.VK_S) ? 1 : 0) - (i.isKey(KeyEvent.VK_W) ? 1 : 0);

            if ((moveX != 0 || moveY != 0)) {
                timer = time;
                step+=SpeedController.calcSpeed();
                if (step % 20 == 1) {
                    // TODO: Play sound depending on player's location
                    if (onPuddle) {
                        Main.main.soundManager.playSound("puddle" +
                                random.nextInt(5));
//                        Main.main.createWave(x, y);
                    } else if (onPlant) {
                        Main.main.soundManager.playSound("grass" +
                                ((step / 20 % 2 == 0) ? 1 : 0));
                    } else {
                        Main.main.soundManager.playSound("dirt" +
                                (random.nextInt(4)));
                    }
                } else if (!prevOnPuddle && onPuddle) {
                    Main.main.soundManager.playSound("puddle" +
                            random.nextInt(5));
                } else if (!prevOnPlant && onPlant) {
                    Main.main.soundManager.playSound("grass" +
                            (random.nextBoolean() ? 1 : 0));
                }
            } else {
                step = 0;
            }
            prevOnPuddle = onPuddle;
            onPuddle = false;
            prevOnPlant = onPlant;
            onPlant = false;

            targetSpeedX = moveX * MaxSpeed;
            targetSpeedY = moveY * MaxSpeed;
        } else {
            targetSpeedX = 0;
            targetSpeedY = 0;
        }

        speedX += Math.signum(targetSpeedX - speedX)*speedStep;
        speedY += Math.signum(targetSpeedY - speedY)*speedStep;

        speedX = Math.max(-MaxSpeed, Math.min(MaxSpeed, speedX));
        speedY = Math.max(-MaxSpeed, Math.min(MaxSpeed, speedY));

        cm.move(Main.toSlowMotion(speedX), Main.toSlowMotion(speedY));

        if(Main.collisionMap.collisionWithWhoExcept(
                mask, aabbComponent).size() > 0) {
            cm.unstuck();
        }

        // ###### PARTICLES ######

        this.xx = (int) x;
        this.yy = (int) y;

        if(!isDead)
            if(random.nextDouble() <= SpeedController.calcSpeed())
                for (int j = 0; j < 2; j++) {
                    int tone = random.nextInt(30)-15;
                    int sDir = random.nextInt(359);
                    int sDistance = random.nextInt(10);
                    int xx = this.xx + (int) (Math.cos(Math.toRadians(sDir)) * sDistance);
                    int yy = this.yy + (int) (Math.sin(Math.toRadians(sDir)) * sDistance);
                    int fadingSpeed = random.nextInt(8)+8;
                    int size = random.nextInt(4)+2;
                    Color color = new Color(baseColor.getRed()+tone, baseColor.getGreen()+tone, baseColor.getBlue()+tone);
                    UniParticle.FadingProcess fadingProcess = new UniParticle.FadingProcess(255, fadingSpeed, true);
                    particles.add(new UniParticle(xx, yy, size, true, color, fadingProcess));
                }

        particles.forEach(UniParticle::update);
        particles.removeIf(particle -> particle.dead);

        this.mask.x = x;
        this.mask.y = y;

        this.aabbComponent.area.x = x;
        this.aabbComponent.area.y = y;

        this.coinOverlayX += (lastCoinX -= coinOverlayX) * 0.1d;
        this.coinOverlayY += (lastCoinY -= coinOverlayY) * 0.1d;

        coinOverlayAlpha = Math.max(0, Math.min(1, coinOverlayAlpha-0.025));
    }

    public void castRays() {
        // ---- Shadow Caster V 0.2 ----
        rays.clear();
        int iterations = 0;
        int raysCast = 0;
        for(GameObject obj : Main.main.visibleChunkObjects) {
            if(obj.mask == null) continue;
            else if(!(obj.mask instanceof Mask.Rectangle)) continue;
            else if(obj instanceof Healable) continue;
            else if(!obj.solid) continue;
            iterations++;

            Mask.Rectangle mask = (Mask.Rectangle) obj.aabbComponent.area;
            double minAngle = Double.MAX_VALUE;
            Point minPoint = null;
            double maxAngle = Double.MIN_VALUE;
            Point maxPoint = null;
            double currentDistance;

            rays.add(new Vector8((int)mask.x, (int)mask.y,
                    (int)(mask.x+mask.w), (int)mask.y,
                    (int)(mask.x+mask.w), (int)(mask.y+mask.h),
                    (int)mask.x, (int)(mask.y+mask.h)));
            for(int j = 0; j < 4; j++) {
//                Point current = new Point(mask.scoreX + (((j+1) % 2 == 0) ? mask.w : 0), mask.scoreY + ((j % 2 == 0) ? mask.h : 0));
//                Point next = new Point(mask.scoreX + (((j+2) % 2 == 0) ? mask.w : 0), mask.scoreY + (((j+1) % 2 == 0) ? mask.h : 0));

                Point current = findCorner(j, mask);
                Point next = findCorner(j+1, mask);

                double disCurrent = Fly.angle(x, y, current.x, current.y);
                double disNext = Fly.angle(x, y, next.x, next.y);

                double size = Fly.distance(0, 0, core.width/2d, core.height/2d);

                rays.add(new Vector8((int)current.x, (int)current.y, (int)(current.x+ Math.cos(Math.toRadians(disCurrent-180))*size),
                                (int)(current.y+ Math.sin(Math.toRadians(disCurrent-180))*size), (int)(next.x+Math.cos(Math.toRadians(disNext-180))*size),
                        (int)(next.y+ Math.sin(Math.toRadians(disNext-180))*size), (int)next.x, (int)next.y));
                raysCast++;
            }
        }
    }

    @Override
    public void render(Renderer r) {
        particles.forEach(p -> p.render(r));
        if(Main.main.showDebugInfo)
            r.fillRectangle(xx, yy, 8, 8, Color.red);
    }

    public void renderRays(Renderer r) {
        r.setColor(new Color(
                Main.grassColor.getRed()-15,
                Main.grassColor.getGreen()-15,
                Main.grassColor.getBlue()-15
        ));
        int addX = (int)-Main.main.camera.cX;
        int addY = (int)-Main.main.camera.cY;
        Graphics2D g2d = (Graphics2D) r.getG();
        g2d.setStroke(new BasicStroke(1));
//        for(Vector4 vec : rays)
//            r.getG().drawLine(vec.x1+addX, vec.y1+addY, vec.x2+addX, vec.y2+addY);
//        g2d.translate((double)addX, (double)addY);
        for(Vector8 vec : rays) {
            if(Main.main.showDebugInfo)
                g2d.drawPolygon(new int[]{vec.x1+addX, vec.x2+addX, vec.x3+addX, vec.x4+addX},
                        new int[]{vec.y1+addY, vec.y2+addY, vec.y3+addY, vec.y4+addY}, 4);
            else {
                g2d.fill(new Polygon(new int[]{vec.x1+addX, vec.x2+addX, vec.x3+addX, vec.x4+addX},
                        new int[]{vec.y1+addY, vec.y2+addY, vec.y3+addY, vec.y4+addY}, 4));
//                g2d.fillPolygon(new int[]{vec.x1, vec.x4, vec.x3},
//                        new int[]{vec.y1, vec.y4, vec.y3}, 3);
//                g2d.fillPolygon(new int[]{vec.x1, vec.x2, vec.x3},
//                        new int[]{vec.y1, vec.y2, vec.y3}, 3);
            }
//            g2d.fillOval(vec.x1-4, vec.y1-4, 8, 8);
//            g2d.fillOval(vec.x2-4, vec.y2-4, 8, 8);
//            g2d.fillOval(vec.x3-4, vec.y3-4, 8, 8);
//            g2d.fillOval(vec.x4-4, vec.y4-4, 8, 8);
        }
//        g2d.translate((double)-addX, (double)-addY);
    }

    public static class Vector4 {
        public int x1, y1, x2, y2;

        public Vector4(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static class Vector8 {
        public int x1, y1, x2, y2, x3, y3, x4, y4;

        public Vector8(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x3 = x3;
            this.y3 = y3;
            this.x4 = x4;
            this.y4 = y4;
        }
    }

    static Point getIntersectionPoint(Point A, Point B, Point P) {
        Point abDir = B.minus(A);
        Point prepareDir = new Point(-abDir.x, abDir.y);
        Point apDir = P.minus(A);
        double s = (prepareDir.y * apDir.x - prepareDir.x * apDir.y)
                / (abDir.x * prepareDir.y - abDir.y * prepareDir.x);
        return A.plus(abDir.scale(s));
    }

    public static class Point {
        public double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Point scale(double k) {
            return new Point(x * k, y * k);
        }

        Point plus(Point add) {
            return new Point(x + add.x, y + add.y);
        }

        Point minus(Point minus) {
            return new Point(x - minus.x, y - minus.y);
        }
    }

    public Point findCorner(int number, Mask.Rectangle mask) {
        switch (number%4) {
            case 0:
                return new Point(mask.x, mask.y);
            case 1:
                return new Point(mask.x+mask.w, mask.y);
            case 2:
                return new Point(mask.x+mask.w, mask.y+mask.h);
            case 3:
                return new Point(mask.x, mask.y+mask.h);
            default:
                return null;
        }
    }

    public class ShadowRenderer extends GameObject {

        public ShadowRenderer() {
            super(14, 0);
        }

        @Override
        public void update(Input input) {

        }

        @Override
        public void render(Renderer r) {
            renderRays(r);
        }
    }

}
