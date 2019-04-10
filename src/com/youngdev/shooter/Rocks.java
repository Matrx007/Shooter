package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Rocks extends WorldObject {

    private ArrayList<Piece> rocks;
    public final int Type = 9;

    public Rocks(int x, int y) {
        super(7, 7, 9);
        this.x = x;
        this.y = y;
        this.solid = true;

        rocks = new ArrayList<>();
        double maxDistance = random.nextInt(24)+8;
        int numRocks = (int)(maxDistance*1.5d);
        int smallestX=Integer.MAX_VALUE,
                smallestY=Integer.MAX_VALUE,
                largestX=Integer.MIN_VALUE,
                largestY=Integer.MIN_VALUE;
        for(int i = 0; i < numRocks; i++) {
            double gaussian = random.nextDouble();
            double angle = random.nextInt(359);
            double distance = Tree.calcGaussian(
                    gaussian, 8)*maxDistance;
            double addX = Math.cos(Math.toRadians(angle))*distance;
            double addY = Math.sin(Math.toRadians(angle))*distance;
            int xx = x + (int)Math.round(addX);
            int yy = y + (int)Math.round(addY);
            int size = (int)((random.nextInt(15)+15) / 24d * maxDistance);
            rocks.add(new Piece(xx, yy, size,
                    (int)(distance/maxDistance*20d)+
                    random.nextInt(5)+5));

            smallestX = Math.min(smallestX, (int)Math.floor(xx-size/2d));
            smallestY = Math.min(smallestY, (int)Math.floor(yy-size/2d));
            largestX = Math.max(largestX, (int)Math.ceil(xx+size/2d));
            largestY = Math.max(largestY, (int)Math.ceil(yy+size/2d));
        }

        this.mask = new Mask.Rectangle(smallestX, smallestY,
                largestX-smallestX, largestY-smallestY);
        this.aabbComponent = new AABBComponent(new Mask.Rectangle(
                smallestX, smallestY,
                largestX-smallestX,
                largestY-smallestY));
    }

    @Override
    public void update(Input input) {

    }

    @Override
    public void render(Renderer r) {
        for(Piece rock : rocks) {
            r.fillRectangle(rock.x-rock.size/2, rock.y-rock.size/2,
                    rock.size, rock.size, rock.getColor());
        }

        if(Main.main.showDebugInfo) {
            r.fillRectangle(aabbComponent.area.x, this.aabbComponent.area.y,
                    ((Mask.Rectangle) aabbComponent.area).w,
                    ((Mask.Rectangle) aabbComponent.area).w, Color.red);
        }
    }

    public class Piece {
        public int x, y, size, tone;
        public Color baseColor = new Color(70, 70, 70);

        public Piece(int x, int y, int size, int tone) {
            this.x = x;
            this.y = y;
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
}
