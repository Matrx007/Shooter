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

        // HERE: Fix depth
        Random random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        rocks = new ArrayList<>();
        int numRocks = random.nextInt(7)+10;
        int smallestX=Integer.MAX_VALUE, smallestY=Integer.MAX_VALUE, largestX=Integer.MIN_VALUE, largestY=Integer.MIN_VALUE;
        for(int i = 0; i < numRocks; i++) {
            // HERE: Create a leave
            int xx = x-random.nextInt(24)+12;
            int yy = y-random.nextInt(24)+12;
            int size = random.nextInt(20)+4;
            rocks.add(new Piece(xx, yy, size, random.nextInt(8)*5));

            smallestX = Math.min(smallestX, (int)Math.floor(xx-size/2d));
            smallestY = Math.min(smallestY, (int)Math.floor(yy-size/2d));
            largestX = Math.max(largestX, (int)Math.ceil(xx+size/2d));
            largestY = Math.max(largestY, (int)Math.ceil(yy+size/2d));
        }

        this.mask = new Mask.Rectangle(smallestX, smallestY,
                largestX-smallestX, largestY-smallestY);
        this.aabbComponent = new AABBComponent(new Mask.Rectangle(
                smallestX, smallestY, largestX-smallestX, largestY-smallestY));
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
            r.fillRectangle(aabbComponent.area.x, this.aabbComponent.area.y, ((Mask.Rectangle) aabbComponent.area).w, ((Mask.Rectangle) aabbComponent.area).w, Color.red);
        }
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

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
