package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Tree extends WorldObject {

    private ArrayList<Leaf> leaf;
    private ArrayList<Point[]> branches;
    private boolean hide;
    public boolean collision;
    public int type;
    public final int Type = 13;
    private Rectangle bounds;
    private float alpha, alphaTarget;
    private int returnTimer;
    private static final int returnTime = 60;

    public Tree(int x, int y) {
        super(11, 20, 13);
        this.x = x;
        this.y = y;

        collision = false;
        leaf = new ArrayList<>();
        branches = new ArrayList<>();
        solid = true;

        Rectangle bounds = createLeaf();

        alpha = 1f;
        alphaTarget = 1f;

        // Masks and collision
        mask = new Mask.Rectangle((double) bounds.x, (double) bounds.y,
                bounds.width, bounds.height);
        this.bounds = new Rectangle(
                bounds.x-x, bounds.y-y,
                bounds.width, bounds.height);
        aabbComponent = new AABBComponent(new Mask.Rectangle(x-8, y-8, 16, 16));
    }

    private Rectangle createLeaf() {
        int smallestX,
                smallestY,
                largestX,
                largestY;
        int leafGroups = random.nextInt(7)+7;
        smallestX=Integer.MAX_VALUE;
        smallestY=Integer.MAX_VALUE;
        largestX=Integer.MIN_VALUE;
        largestY=Integer.MIN_VALUE;
        for(int i = 0; i < leafGroups; i++) {
            int angle = random.nextInt(359);
            int distance = random.nextInt(90)+30;
            int xx = (int)(Math.cos(Math.toRadians(angle))*distance);
            int yy = (int)(Math.sin(Math.toRadians(angle))*distance);

            Point[] brunch = new Point[] {
                    new Point((int)x, (int)y),
                    new Point((int)x+xx, (int)y+yy)
            };
            branches.add(brunch);

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

        return new Rectangle(smallestX, smallestY, largestX-smallestX,
                largestY-smallestY);
    }

    @Override
    public void update(Input input) {
        boolean prevHide = hide;

        hide = Main.main.player.mask.isColliding(this.mask);

        if(!prevHide && hide) {
            returnTimer = returnTime;
        }

        alphaTarget = 0.4f;
        if(returnTimer < 0)
            alphaTarget = 1f;
        else if(!hide) {
            returnTimer--;
        }

        alpha += (alphaTarget - alpha) * 0.1f;
    }

    @Override
    public void render(Renderer r) {
        // ----------- PRE RENDERING LEAVES --------
        BufferedImage preRender = new BufferedImage(
                bounds.width, bounds.height,
                BufferedImage.TYPE_INT_ARGB);
        Renderer renderer = new Renderer(
                preRender.getGraphics(),
                bounds.width, bounds.height);

        renderer.setFilters(r.getFilters());

        // Drawing branches
        ((Graphics2D)renderer.getG()).setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        branches.forEach(b -> renderer.drawLine(
                b[0].x-this.x-bounds.x,
                b[0].y-this.y-bounds.y,
                b[1].x-this.x-bounds.x,
                b[1].y-this.y-bounds.y,
                new Color(80, 40, 10)));

        ((Graphics2D)renderer.getG()).setStroke(new BasicStroke(1f));
        // Drawing leaves
        leaf.forEach(leave -> {
            Color c = leave.getColor();
            renderer.fillRectangle(
                    leave.x - leave.size / 2d + leave.addX - x - bounds.x,
                    leave.y - leave.size / 2d + leave.addY - y - bounds.y,
                    leave.size, leave.size, c);
        });

        // ------- DRAWING PRE-RENDERED IMAGE -------
        Graphics2D g2d = (Graphics2D) r.getG();

        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER,
                (float)((int)(alpha*1000f))/1000f));

        r.drawImage(x + bounds.x, y + bounds.y, preRender);

        g2d.setComposite(old);


        if(Main.main.showDebugInfo) {
            if(mask instanceof Mask.Rectangle)
            r.drawRectangle(mask.x, mask.y,
                    ((Mask.Rectangle) mask).w, ((Mask.Rectangle) mask).h,
                    Color.red);
        }
    }

    private Rectangle spawnLeaf(int minLeaf, int maxLeaf, int distanceLimit,
                                int minSize, int maxSize, int offX, int offY,
                                Color baseColor, int pow) {
        int numLeaf = random.nextInt(maxLeaf-minLeaf) + minLeaf;
        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;
        for (int i = 0; i < numLeaf; i++) {
            // HERE: Create one leave
            double gaussian = calcGaussian(random.nextDouble(), pow);
            double distanceGaussian = calcGaussian(random.nextDouble(), pow);
            double distance = distanceGaussian*distanceLimit;
            double angle = random.nextDouble()*360;
            int xx = (int)(offX+x+Math.cos(Math.toRadians(angle))*distance);
            int yy = (int)(offY+y+Math.sin(Math.toRadians(angle))*distance);
            int size = (int)(minSize+(1-gaussian)*(maxSize-minSize));
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

    public static double calcGaussian(double x, int pow) {
        return Math.pow(Math.sin(x+ Math.PI/2), pow);
    }
}
