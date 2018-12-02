package com.youngdev.shooter;

import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.game.behaviors.AABBComponent;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.Random;

public class Bunny extends Healable {

    double speedTick;
    double direction;
    double directionTarget;
    double speed;
    double speedTarget;
    double maxSpeed;
    double movingTime;
    boolean escaping;
    private Random random;
    private boolean unStucking;

    private AABBCollisionManager cm;

    public static final boolean collideWithOthers = false;

    public Bunny(int x, int y) {
        super(x, y, 16, 16, 50, 13, 9, false, collideWithOthers);
        random = new Random();

        cm = new AABBCollisionManager(this, Main.collisionMap);
        if(collideWithOthers) aabbComponent = new AABBComponent(this.mask);

        escaping = false;
        unStucking = Main.collisionMap.collisionWithExcept(mask, aabbComponent);
        speedTick = 0;
        direction = random.nextInt(359);
        directionTarget = random.nextInt(359);
        speed = 0;
        maxSpeed = 2d+random.nextDouble()*2d;
        speedTarget = maxSpeed;
        movingTime = 0;
    }

    @Override
    public void update(Input i) {
        if (escaping) {
            directionTarget = Fly.angle(x, y, Main.main.player.x, Main.main.player.y) - 180;
            speedTarget = maxSpeed;
        } else {
            if(movingTime < 0) {
                directionTarget = random.nextInt(359);
                speedTarget = maxSpeed*0.5d + maxSpeed*random.nextDouble()*0.5d;
                movingTime = random.nextInt(60)+15;
            } else {
                movingTime--;
            }
//            System.out.println("movingTime = " + movingTime);
        }

        direction += (directionTarget - direction) * 0.25d;
        speed += (speedTarget - speed) * 0.1d;

        if(collideWithOthers) {
            unStucking = Main.collisionMap.collisionWithExcept(this.mask, aabbComponent);
            if (unStucking) {
                double amountX = Math.cos(Math.toRadians(direction)) * speed;
                double amountY = Math.sin(Math.toRadians(direction)) * speed;
                x += amountX;
                y += amountY;
                this.mask.move(amountX, amountY);
//            aabbComponent.area = this.mask;
            } else {
//            System.out.println("direction = " + direction);
//            System.out.println("directionTarget = " + directionTarget);
//            aabbComponent.area = this.mask;
                cm.move(Math.cos(Math.toRadians(direction)) * speed, Math.sin(Math.toRadians(direction)) * speed);
            }
        } else {
            cm.move(Math.cos(Math.toRadians(direction)) * speed, Math.sin(Math.toRadians(direction)) * speed);
        }
    }

    @Override
    public void render(Renderer r) {
        double[][] points = new double[][]{
                rotatePoint(x-8, y-12, x, y, direction+90),
                rotatePoint(x+8, y-12, x, y, direction+90),
                rotatePoint(x+8, y+12, x, y, direction+90),
                rotatePoint(x-8, y+12, x, y, direction+90)
        };
//        Color c = Main.collisionMap.collisionWithExcept(mask, aabbComponent) ? Color.black : new Color(240, 240,240);
        fillPoly(points, new Color(240, 240,240), r);

        double[][] headPoints = new double[][]{
                rotatePoint(-5, -5, 0, 0, directionTarget-direction),
                rotatePoint(5, -5, 0, 0, directionTarget-direction),
                rotatePoint(5, 5, 0, 0, directionTarget-direction),
                rotatePoint(-5, 5, 0, 0, directionTarget-direction)
        };

        points = new double[][]{
                rotatePoint(x+headPoints[0][0], y+headPoints[0][1]-14, x, y, direction+90),
                rotatePoint(x+headPoints[1][0], y+headPoints[1][1]-14, x, y, direction+90),
                rotatePoint(x+headPoints[2][0], y+headPoints[2][1]-14, x, y, direction+90),
                rotatePoint(x+headPoints[3][0], y+headPoints[3][1]-14, x, y, direction+90)
        };
        fillPoly(points, new Color(220, 220,220), r);

        double[][] nosePoints = new double[][]{
                rotatePoint(-2, -6, 0, 0, directionTarget-direction),
                rotatePoint(2, -6, 0, 0, directionTarget-direction),
                rotatePoint(2, -4, 0, 0, directionTarget-direction),
                rotatePoint(-2, -4, 0, 0, directionTarget-direction)
        };

        points = new double[][]{
                rotatePoint(x+nosePoints[0][0], y+nosePoints[0][1]-14, x, y, direction+90),
                rotatePoint(x+nosePoints[1][0], y+nosePoints[1][1]-14, x, y, direction+90),
                rotatePoint(x+nosePoints[2][0], y+nosePoints[2][1]-14, x, y, direction+90),
                rotatePoint(x+nosePoints[3][0], y+nosePoints[3][1]-14, x, y, direction+90)
        };
        fillPoly(points, new Color(200, 200,200), r);

        int eyeXOffset = 3;
        int eyeYOffset = -4;

        double[][] eye1Points = new double[][]{
                rotatePoint(eyeXOffset, eyeYOffset, 0, 0, directionTarget-direction),
                rotatePoint(eyeXOffset, eyeYOffset, 0, 0, directionTarget-direction)
        };

        points = new double[][]{
                rotatePoint(x+eye1Points[0][0], y+eye1Points[0][1]-14, x, y, direction+90),
                rotatePoint(x+eye1Points[1][0], y+eye1Points[1][1]-14, x, y, direction+90)
        };
        int[] xPoints = new int[]{
                (int)points[0][0],
                (int)points[1][0]
        };
        int[] yPoints = new int[]{
                (int)points[0][1],
                (int)points[1][1]
        };

        r.fillCircle(xPoints[0], yPoints[0], 3, new Color(70, 70, 70));

        double[][] eye2Points = new double[][]{
                rotatePoint(-eyeXOffset, eyeYOffset, 0, 0, directionTarget-direction),
                rotatePoint(-eyeXOffset, eyeYOffset, 0, 0, directionTarget-direction)
        };

        points = new double[][]{
                rotatePoint(x+eye2Points[0][0], y+eye2Points[0][1]-14, x, y, direction+90),
                rotatePoint(x+eye2Points[1][0], y+eye2Points[1][1]-14, x, y, direction+90)
        };
        xPoints = new int[]{
                (int)points[0][0],
                (int)points[1][0]
        };
        yPoints = new int[]{
                (int)points[0][1],
                (int)points[1][1]
        };

        r.fillCircle(xPoints[0], yPoints[0], 3, new Color(70, 70, 70));

        double[][] rightEarPoints = new double[][]{
                rotatePoint(5, -2, 0, 0, directionTarget-direction+45),
                rotatePoint(13, -2, 0, 0, directionTarget-direction+45),
                rotatePoint(13, 2, 0, 0, directionTarget-direction+45),
                rotatePoint(5, 2, 0, 0, directionTarget-direction+45)
        };

        points = new double[][]{
                rotatePoint(x+rightEarPoints[0][0], y+rightEarPoints[0][1]-14, x, y, direction+90),
                rotatePoint(x+rightEarPoints[1][0], y+rightEarPoints[1][1]-14, x, y, direction+90),
                rotatePoint(x+rightEarPoints[2][0], y+rightEarPoints[2][1]-14, x, y, direction+90),
                rotatePoint(x+rightEarPoints[3][0], y+rightEarPoints[3][1]-14, x, y, direction+90)
        };
        fillPoly(points, new Color(190, 190,190), r);

        double[][] leftEarPoints = new double[][]{
                rotatePoint(5, -2, 0, 0, directionTarget-direction+135),
                rotatePoint(13, -2, 0, 0, directionTarget-direction+135),
                rotatePoint(13, 2, 0, 0, directionTarget-direction+135),
                rotatePoint(5, 2, 0, 0, directionTarget-direction+135)
        };

        points = new double[][]{
                rotatePoint(x+leftEarPoints[0][0], y+leftEarPoints[0][1]-14, x, y, direction+90),
                rotatePoint(x+leftEarPoints[1][0], y+leftEarPoints[1][1]-14, x, y, direction+90),
                rotatePoint(x+leftEarPoints[2][0], y+leftEarPoints[2][1]-14, x, y, direction+90),
                rotatePoint(x+leftEarPoints[3][0], y+leftEarPoints[3][1]-14, x, y, direction+90)
        };
        fillPoly(points, new Color(190, 190,190), r);

        points = new double[][]{
                rotatePoint(x-4, y+12-4, x, y, direction+90),
                rotatePoint(x+4, y+12-4, x, y, direction+90),
                rotatePoint(x+4, y+12+4, x, y, direction+90),
                rotatePoint(x-4, y+13+4, x, y, direction+90)
        };
        fillPoly(points, new Color(220, 220,220), r);

        if(Main.main.showDebugInfo) {
            r.fillRectangle(mask.x, mask.y, ((Mask.Rectangle)mask).w, ((Mask.Rectangle)mask).h, Color.red);

            int w = ((Mask.Rectangle) aabbComponent.area).w;
            int h = ((Mask.Rectangle) aabbComponent.area).h;
            r.fillCircle(aabbComponent.area.x, aabbComponent.area.y, 3, Color.blue);
            r.fillCircle(aabbComponent.area.x+w, aabbComponent.area.y, 3, Color.blue);
            r.fillCircle(aabbComponent.area.x+w, aabbComponent.area.y+h, 3, Color.blue);
            r.fillCircle(aabbComponent.area.x, aabbComponent.area.y+h, 3, Color.blue);
        }
    }

    private void fillPoly(double[][] points, Color color, Renderer r) {
        double[] xPoints = new double[points.length]/*{
                points[0][0],
                points[1][0],
                points[2][0],
                points[3][0]
        }*/;
        double[] yPoints = new double[points.length]/*{
            points[0][1],
                    points[1][1],
                    points[2][1],
                    points[3][1]
        }*/;

        for(int i = 0; i < points.length; i++) {
            xPoints[i] = points[i][0];
            yPoints[i] = points[i][1];
        }
        r.fillPolygon(xPoints, yPoints, points.length, color);
    }

    public double[] rotatePoint(double x, double y, double anchorX, double anchorY, double degrees) {
        double xx = (x - anchorX) * Math.cos(degrees * Math.PI / 180) - (y - anchorY) * Math.sin(degrees * Math.PI / 180) + anchorX;
        double yy = (x - anchorX) * Math.sin(degrees * Math.PI / 180) + (y - anchorY) * Math.cos(degrees * Math.PI / 180) + anchorY;
        return new double[]{xx, yy};
    }
}
