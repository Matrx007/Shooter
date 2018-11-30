package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class UniParticle {
    public int x, y, size;
    public Color color;
    public ArrayList<Process> processes;
    public boolean dead, affectedBySlowMotion;

    public UniParticle(int x, int y, int size, boolean affectedBySlowMotion, Color color) {
        this.x = x;
        this.y = y;
        this.dead = false;
        this.size = size;
        this.affectedBySlowMotion = affectedBySlowMotion;
        this.color = color;
        processes = new ArrayList<>();
    }

    public UniParticle(int x, int y, int size, boolean affectedBySlowMotion, Color color, Process... processes) {
        this.x = x;
        this.y = y;
        this.dead = false;
        this.size = size;
        this.affectedBySlowMotion = affectedBySlowMotion;
        this.color = color;
        this.processes = new ArrayList<>(Arrays.asList(processes));
        this.processes.forEach(p -> {
            p.owner = this;
            p.init();
        });
    }

    public UniParticleInstance asInstance(int depth) {
        return new UniParticleInstance(this, depth);
    }

    public void render(Renderer r) {
        if(dead) return;
        r.fillRectangle(x-size/2, y-size/2, size, size, color);
        processes.forEach(p -> p.render(r));
    }

    public void update() {
        if(dead) return;
        processes.forEach(Process::update);
    }

    public static abstract class Process {
        UniParticle owner;
        public abstract void init();
        public abstract void render(Renderer r);
        public abstract void update();
    }

    public static class ColorChangeEffect extends Process {
        public Color from, to;
        public double step, stepSpeed;

        public ColorChangeEffect(Color from, Color to, double step, double stepSpeed) {
            this.from = from;
            this.to = to;
            this.step = step;
            this.stepSpeed = stepSpeed;
        }

        @Override
        public void init() {
            super.owner.color = from;
        }

        @Override
        public void render(Renderer r) {

        }

        @Override
        public void update() {
            if(owner.affectedBySlowMotion)
                step += Main.toSlowMotion(stepSpeed);
            else step += stepSpeed;
            if(step >= 255 || step <= 0) {
                super.owner.dead = true;
            }

            owner.color = new Color(
                    calcColorParameter(owner.color.getRed(), to.getRed(), 0f + (float)step / 255f),
                    calcColorParameter(owner.color.getGreen(), to.getGreen(), 0f + (float)step / 255f),
                    calcColorParameter(owner.color.getBlue(), to.getBlue(), 0f + (float)step / 255f)
            );
        }
    }

    public static class MovingProcess extends Process {
        public double speed, speedAdder;
        public double direction, xD, yD;
        public double speedX, speedY;

        private MovingProcess(double speedX, double speedY, double direction,
                              double speed, boolean useXY, double speedAdder) {
            this.speedX = speedX;
            this.speedY = speedY;
            this.direction = direction;
            this.speed = speed;
            calcSpeeds(useXY);
            this.speedAdder = speedAdder;
        }

        public static MovingProcess create(float speedX, float speedY) {
            return new MovingProcess(speedX, speedY, 0, 0, true, 0);
        }

        public static MovingProcess create(double direction, float speed) {
            return new MovingProcess(0, 0, direction, speed, false, 0);
        }

        public static MovingProcess create(float speedX, float speedY, double speedAdder) {
            return new MovingProcess(speedX, speedY, 0, 0, true, speedAdder);
        }

        public static MovingProcess create(double direction, float speed, double speedAdder) {
            return new MovingProcess(0, 0, direction, speed, false, speedAdder);
        }

        public void calcSpeeds(boolean fromXY) {
            if(fromXY) {
                direction = Fly.angle(0, 0, speedX, speedY);
                speed = (float) Fly.distance(0, 0, speedX, speedY);
            } else {
                speedX = Math.cos(Math.toRadians(direction)) * speed;
                speedY = Math.sin(Math.toRadians(direction)) * speed;
            }
        }

        @Override
        public void init() {
            xD = super.owner.x;
            yD = super.owner.y;
        }

        @Override
        public void render(Renderer r) {

        }

        @Override
        public void update() {
            if(owner.affectedBySlowMotion)
                speed += Main.toSlowMotion(speedAdder);
            else speed += speedAdder;

            if(owner.affectedBySlowMotion) {
                xD += Main.toSlowMotion(speedX);
                yD += Main.toSlowMotion(speedY);
            } else {
                xD += speedX;
                yD += speedY;
            }

            super.owner.x = (int)xD;
            super.owner.y = (int)yD;
        }
    }

    public static class FollowingProcess extends Process {
        private GameObject target;
        private int targetShiftX, targetShiftY;
        private float speed;
        private double xD, yD;

        public FollowingProcess(GameObject target, int targetShiftX, int targetShiftY, float speed) {
            this.target = target;
            this.targetShiftX = targetShiftX;
            this.targetShiftY = targetShiftY;
            this.speed = speed;
        }

        @Override
        public void init() {
            this.xD = owner.x;
            this.yD = owner.y;
        }

        @Override
        public void render(Renderer r) {

        }

        @Override
        public void update() {
            xD = owner.x;
            yD = owner.y;

            double dir = AdvancedMath.angle(owner.x, owner.y, (int)target.x+targetShiftX, (int)target.y+targetShiftY);

            double addX = Math.cos(Math.toRadians(dir)) * speed;
            double addY = Math.sin(Math.toRadians(dir)) * speed;

            if(owner.affectedBySlowMotion) {
                xD += Main.toSlowMotion(addX);
                yD += Main.toSlowMotion(addY);
            } else {
                xD += addX;
                yD += addY;
            }

            owner.x = (int)xD;
            owner.y = (int)yD;
        }
    }

    public static class FadingProcess extends Process {
        double alpha, alphaSpeed;
        boolean usesAlphaChannel;

        public FadingProcess(int alpha, int alphaSpeed) {
            this.alpha = alpha;
            this.alphaSpeed = alphaSpeed;
            this.usesAlphaChannel = false;
        }

        public FadingProcess(int alpha, int alphaSpeed, boolean usesAlphaChannel) {
            this.alpha = alpha;
            this.alphaSpeed = alphaSpeed;
            this.usesAlphaChannel = usesAlphaChannel;
        }

        @Override
        public void init() {

        }

        @Override
        public void render(Renderer r) {

        }

        @Override
        public void update() {

//            alpha -= alphaSpeed;
            if(owner.affectedBySlowMotion) alpha -= Main.toSlowMotion(alphaSpeed);
            else alpha -= alphaSpeed;

            if(alpha < 0d) {
                owner.dead = true;
                return;
            }

            if(usesAlphaChannel) {
                owner.color = new Color(owner.color.getRed(), owner.color.getGreen(), owner.color.getBlue(), ((int) alpha));
            } else {
                owner.color = new Color(
                        calcColorParameter(Main.grassColor.getRed(), owner.color.getRed(), (float) (alpha / 255f)),
                        calcColorParameter(Main.grassColor.getGreen(), owner.color.getGreen(), (float)(alpha / 255f)),
                        calcColorParameter(Main.grassColor.getBlue(), owner.color.getBlue(), (float)(alpha / 255f))
                );
            }
        }
    }

    public static int calcColorParameter(int colorBack, int colorFront, float alpha) {
        return (int)(alpha * (float)colorFront) + (int)((1f - alpha) * (float)colorBack);
    }

    public class UniParticleInstance extends GameObject {
        UniParticle owner;

        public UniParticleInstance(UniParticle owner, int depth) {
            super(12, depth);
            this.owner = owner;

            // HERE: Fix depth
            Random random = new Random();
            this.depth = random.nextInt(1023)+depth*1024;

            this.x = owner.x;
            this.y = owner.y;

            this.mask = new Mask.Rectangle((int)x-size/2, (int)y-size/2, size, size);
        }

        @Override
        public void update(Input input) {
            owner.update();
        }

        @Override
        public void render(Renderer r) {
            owner.render(r);
        }

        @Override
        public String shareSend() {
            return null;
        }

        @Override
        public void shareReceive(String s) {

        }
    }

}
