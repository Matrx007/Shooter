package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class Tree extends WorldObject {

    private ArrayList<Leaf> leaf;
    private ArrayList<Point[]> brunches;
    private Random random;
    private ArrayList<Leaf2> leaf2s;
    private boolean prevCollision, fliesInside;
    public boolean collision;
    public int type;
    public static final int TYPE_SAVANNA = 0, TYPE_OAK = 1;
    public final int Type = 13;

    public static final Color BASE_COLOR_OAK = new Color(29, 87, 18);
    public static final Color BASE_COLOR_SAVANNA = new Color(59, 111, 44);

    public Tree(int x, int y, int type) {
        super(11, 20, 13);
//        this.depth = 20;
        this.x = x;
        this.y = y;
        this.fliesInside = true;

        collision = false;
        leaf2s = new ArrayList<>();

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+20*1024;

        leaf = new ArrayList<>();
        brunches = new ArrayList<>();
//        type = random.nextBoolean() ? TYPE_SAVANNA : TYPE_OAK;
        this.type = TYPE_SAVANNA;
        solid = true;

        // HERE: Bush gen V 1.0
        Rectangle bounds = null;
        switch (this.type) {
            case TYPE_OAK:
                int smallestX=Integer.MAX_VALUE,
                        smallestY=Integer.MAX_VALUE,
                        largestX=Integer.MIN_VALUE,
                        largestY=Integer.MIN_VALUE;

                int numLeaf = random.nextInt(25)+50;
                int range = 128;
                for (int i = 0; i < numLeaf; i++) {
                    int[] xpoints = new int[3];
                    int[] ypoints = new int[3];
                    for (int o = 0; o < 3; o++) {
                        double angle = random.nextDouble()*360;
                        double distance = random.nextInt(range);

                        xpoints[o] = (int)(x+Math.cos(Math.toRadians(angle))*distance);
                        ypoints[o] = (int)(y+Math.sin(Math.toRadians(angle))*distance);
                    }
                    Color clr = new Color(
                            BASE_COLOR_OAK.getRed()+random.nextInt(20)-10,
                            BASE_COLOR_OAK.getGreen()+random.nextInt(20)-10,
                            BASE_COLOR_OAK.getBlue()+random.nextInt(20)-10
                    );
                    leaf2s.add(new Leaf2(new Polygon(xpoints, ypoints,
                            3), clr));
                    for(int j = 0; j < 3; j++) {
                        smallestX = Math.min(smallestX, xpoints[j]);
                        smallestY = Math.min(smallestY, ypoints[j]);
                        largestX = Math.max(largestX, xpoints[j]);
                        largestY = Math.max(largestY, ypoints[j]);
                    }

                    bounds = new Rectangle(smallestX, smallestY, largestX-smallestX,
                            largestY-smallestY);
                }

                break;
            case TYPE_SAVANNA:
                int leafGroups = random.nextInt(7)+7;
                smallestX=Integer.MAX_VALUE;
                smallestY=Integer.MAX_VALUE;
                largestX=Integer.MIN_VALUE;
                largestY=Integer.MIN_VALUE;
                for(int i = 0; i < leafGroups; i++) {
                    int angle = random.nextInt(359);
//                    int distance = (int)(Math.min(1, Math.abs(random.nextGaussian()))*50);
                    int distance = random.nextInt(90)+30;
                    int xx = (int)(Math.cos(Math.toRadians(angle))*distance);
                    int yy = (int)(Math.sin(Math.toRadians(angle))*distance);

                    Point[] brunch = new Point[] {
                            new Point(x, y),
                            new Point(x+xx, y+yy)
                    };
                    brunches.add(brunch);

                    int colorTone = random.nextInt(20)-10;
                    Color baseColor = new Color(59+colorTone,
                            111+colorTone,
                            44+colorTone);

                    Rectangle tempBounds = spawnLeaf(36, 48, 55,
                            10, 16, xx, yy, baseColor, 4);

                    smallestX = Math.min(smallestX, tempBounds.x);
                    smallestY = Math.min(smallestY, tempBounds.y);
                    largestX = Math.max(largestX, tempBounds.x+tempBounds.width);
                    largestY = Math.max(largestY, tempBounds.y+tempBounds.height);
                }

                bounds = new Rectangle(smallestX, smallestY, largestX-smallestX,
                        largestY-smallestY);

                break;
        }

//        System.out.println("x = " + x);
//        System.out.println("y = " + y);
//        System.out.println("type = " + type);
//        System.out.println("bounds = " + bounds);

        if(bounds != null) {
            mask = new Mask.Rectangle((double) bounds.x, (double) bounds.y,
                    bounds.width, bounds.height);
            aabbComponent = new AABBComponent(new Mask.Rectangle(x-8, y-8, 16, 16));
//            mask = new Mask.Rectangle(x-8, y-8, 16, 16);
        }
    }

    @Override
    public void update(Input input) {
//        collision = false;

        /*Iterator<GameObject> it;
        for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
            GameObject obj = it.next();
            if (obj instanceof Arrow || (obj instanceof Healable && obj.depth > this.depth))
                if(obj.mask != null)
                    if (obj.mask.isColliding(mask) ||
                            Fly.distance(x, y, obj.x, obj.y) < 64) {
                        collision = true;
                        break;
                    }
        }

        boolean collision_EffectivelyFinal = collision;
        boolean spawn = random.nextInt(16) == 3;*/

        /*leaf.forEach(leave -> {
            if(collision_EffectivelyFinal && !prevCollision) {*//* && !this.prevCollision) {*//*
                leave.speed = 24d;
                if(fliesInside) {
                    if (spawn) {
                        for (int i = 0; i < random.nextInt(5) + 3; i++) {
                            int xx = (int) x + random.nextInt(24) - 12;
                            int yy = (int) y + random.nextInt(24) - 12;
                            Main.main.flies.add(new Fly(xx, yy, false));
                        }
                        fliesInside = false;
                    }
                }
            }
            leave.step+=Main.toSlowMotion(leave.speed);
            leave.speed=Math.max(1, leave.speed-0.25);
            leave.addX = (int)(Math.cos(Math.toRadians(leave.step))*2d);
            leave.addY = (int)(Math.sin(Math.toRadians(leave.step))*2d);
        });*/

//        this.prevCollision = collision_EffectivelyFinal;
    }

    @Override
    public void render(Renderer r) {
        ((Graphics2D)r.getG()).setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        brunches.forEach(b -> r.drawLine(b[0].x, b[0].y, b[1].x, b[1].y,
                new Color(80, 40, 10)));
        leaf.forEach(leave -> r.fillRectangle(leave.x-leave.size/2+leave.addX, leave.y-leave.size/2+leave.addY,
                leave.size, leave.size, leave.getColor()));
        if(i != null) {
            r.fillPolygon(new int[]{
                    (int) x - 16, i.getRelativeMouseX(), (int) x + 16
            }, new int[]{
                    (int) y - 16, i.getRelativeMouseY(), (int) y + 16
            }, 3, Color.black);
        }
        ((Graphics2D) r.getG()).setStroke(new BasicStroke(1f));
        leaf2s.forEach(leave -> {
            int[] pointsx = new int[leave.polygon.xpoints.length];
            int[] pointsy = new int[leave.polygon.ypoints.length];

            for (int k = 0; k < pointsx.length; k++) {
                pointsx[k] = leave.polygon.xpoints[k]-r.getCamX();
                pointsy[k] = leave.polygon.ypoints[k]-r.getCamY();
            }

            r.getG().setColor(leave.color);
            r.getG().fillPolygon(pointsx, pointsy, leave.polygon.npoints);
        });

        if(Main.main.showDebugInfo) {
            if(mask instanceof Mask.Rectangle)
            r.drawRectangle(mask.x, mask.y,
                    ((Mask.Rectangle) mask).w, ((Mask.Rectangle) mask).h,
                    Color.red);
        }
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    private Rectangle spawnLeaf(int minLeaf, int maxLeaf, int distanceLimit,
                                int minSize, int maxSize, int offX, int offY,
                                Color baseColor, int pow) {
        int numLeaf = random.nextInt(maxLeaf-minLeaf) + minLeaf;
        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;
        for (int i = 0; i < numLeaf; i++) {
            // HERE: Create a leave
//            double distance = generateNonLinearNumber1(random.nextFloat()*10d-5d)*distanceLimit;
//            double distance = Math.abs(random.nextGaussian()/2.3d)*distanceLimit*2;
//            double distance = random.nextDouble()*distanceLimit;
            double gaussian = calcGaussian(random.nextDouble(), pow);
            double distanceGaussian = calcGaussian(random.nextDouble(), pow);
            double distance = distanceGaussian*distanceLimit;
//            double distance = (1-Math.sqrt(random.nextDouble()))*distanceLimit;
            double angle = random.nextDouble()*360;
            int xx = (int)(offX+x+Math.cos(Math.toRadians(angle))*distance);
            int yy = (int)(offY+y+Math.sin(Math.toRadians(angle))*distance);
//            int xx = (int)x - random.nextInt(distanceLimit*2) + distanceLimit + offX;
//            int yy = (int)y - random.nextInt(distanceLimit*2) + distanceLimit + offY;
            int size = (int)(minSize+(1-gaussian)*(maxSize-minSize));
//            int size = minSize+random.nextInt(maxSize-minSize);
            Leaf leave = new Leaf(xx, yy, size, random.nextInt(15), random.nextInt(359));
            leave.baseColor = baseColor;
            leaf.add(leave);

            smallestX = Math.min(smallestX, xx-size/2);
            smallestY = Math.min(smallestY, yy-size/2);
            largestX = Math.max(largestX, (int)Math.ceil(xx+size/2d));
            largestY = Math.max(largestY, (int)Math.ceil(yy+size/2d));
        }

        return new Rectangle(smallestX, smallestY, largestX-smallestX, largestY-smallestY);
    }

    public class Leaf {
        public int x, y, addX, addY, size, tone;
        public double speed, step;
        public Color baseColor = new Color(49, 71, 38);

        public Leaf(int x, int y, int size, int tone, int step) {
            this.x = x;
            this.y = y;
            this.step = step;
            this.addX = 0;
            this.addY = 0;
            this.speed = 1d;
            this.size = size;
            this.tone = tone;
        }

        public Color getColor() {
            return new Color(
                    baseColor.getRed()+tone,
                    baseColor.getGreen()+tone,
                    baseColor.getBlue()+tone);
        }
    }

    public static double calcGaussian(double x) {
        return Math.pow(Math.sin(x+ Math.PI/2), 10);
    }

    public static double calcGaussian(double x, int pow) {
        return Math.pow(Math.sin(x+ Math.PI/2), pow);
    }

    public double generateNonLinearNumber1(double x) {
        return random.nextGaussian();
    }

    public static double generateNonLinearNumber2(double x) {
        return Math.max(0d, 1d-Math.max(0d, Math.sqrt(Math.abs(x*1d))));
    }

    private class Leaf2 {
        public Polygon polygon;
        public Color color;

        public Leaf2(Polygon polygon, Color color) {
            this.polygon = polygon;
            this.color = color;
        }

        @Override
        public String toString() {
            return "Leaf2{" +
                    "polygonXpoints=" + Arrays.toString(polygon.xpoints) +
                    "polygonYpoints=" + Arrays.toString(polygon.ypoints) +
                    '}';
        }
    }
}
