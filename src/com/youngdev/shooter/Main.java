package com.youngdev.shooter;

import com.engine.Core;
import com.engine.Game;
import com.engine.libs.font.Alignment;
import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.rendering.Filter;
import com.engine.libs.rendering.Renderer;
import com.engine.libs.world.CollisionMap;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BinaryOperator;

import static java.awt.event.KeyEvent.*;

public class Main extends Game {
    public static Main main;

    public static int chunkSize = 128;
    private int hoverChunkX, hoverChunkY;
    private static int chunkXTopLeft;
    private static int chunkYTopLeft;
    private static int chunkXBottomRight;
    private static int chunkYBottomRight;
    private int timer, time=120;
    private boolean found, findOnScreenBlocked, findOnScreenCalled;
    private boolean usesChunkRenderer;
    public boolean showDebugInfo;
    public static float slowMotionSpeed = 1f;
    private Random random;
    public Camera camera;
    public static CollisionMap collisionMap;

    public Cursor cursor;

    public static Color grassColor = new Color(80, 140, 110);

    public Player player;

    List<GameObject> addEntities, entities, visibleChunkObjects;
    private List<GameObject> structuralBlocks,visibleChunkObjectsTemp;
    List<Fly> flies;

    public static void main(String[] args) {
        new Main();
    }

    public Map<Point, CopyOnWriteArrayList<GameObject>> chunks;

    public Main() {
        main = this;

        e.width = 320;
        e.height = 240;
        e.scale = 4f;

        showDebugInfo = false;

        e.start();

        // HERE: Init

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        try {
            ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream("/RETRO.ttf")));
        } catch (Exception ignored1) {
            try {
                ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getResourceAsStream("/RETRO.ttf")));
            } catch (Exception ignored2) {
                try {
                    ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream("RETRO.ttf")));
                } catch (Exception ignored3) {
                    try {
                        ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getResourceAsStream("RETRO.ttf")));
                    } catch (Exception ignored4) {}
                }
            }
        }


        random = new Random();
        entities = Collections.synchronizedList(new ArrayList<>());
        cursor = new Cursor();
        chunks = new HashMap<>();
        usesChunkRenderer = false;
        visibleChunkObjects = Collections.synchronizedList(new ArrayList<>());
        visibleChunkObjectsTemp = Collections.synchronizedList(new ArrayList<>());
        flies = Collections.synchronizedList(new ArrayList<>());
        structuralBlocks = Collections.synchronizedList(new ArrayList<>());
        addEntities = Collections.synchronizedList(new ArrayList<>());
        findOnScreenBlocked = false;
        findOnScreenCalled = false;

        collisionMap = new CollisionMap();
        e.getRenderer().setCamX(Integer.MAX_VALUE/2-e.width/2);
        e.getRenderer().setCamY(Integer.MAX_VALUE/2-e.height/2);
        player = new Player(e.getRenderer().getCamX()+e.width/2,
                e.getRenderer().getCamY()+e.height/2);

        camera = new Camera(e.width, e.height, player);

        Graphics2D g2d = (Graphics2D) getE().getRenderer().getG();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        e.run();
    }

    public void deleteChunk(int x, int y){
        chunks.remove(new Point(x, y));
    }

    public void generateChunk(int x, int y) {
        ArrayList<GameObject> chunk = new ArrayList<>();


        Random random = new Random();

        int minX = x*chunkSize;
        int minY = y*chunkSize;

        for(int i = 0; i < random.nextInt(4); i++) {
            chunk.add(new Tree(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        for(int i = 0; i < random.nextInt(3); i++) {
            chunk.add(new Rocks(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        for(int i = 0; i < random.nextInt(3); i++) {
            Color clr = new Color(random.nextInt(135)+100,random.nextInt(135)+100,random.nextInt(135)+100);
//            Color clr = new Color(Color.HSBtoRGB(random.nextFloat(), 1f, 1f));
//            System.out.println(clr);
            chunk.add(new Plant(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY, clr));
        }

        for(int i = 0; i < random.nextInt(2); i++) {
            chunk.add(new Terrain(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY, random.nextInt(2)+1));
        }

        if(random.nextInt(4)==1) {
            chunk.add(new Trash(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        if(random.nextInt(5)==3) {
            int xx = random.nextInt(chunkSize);
            int yy = random.nextInt(chunkSize);
            for(int i = 0; i < random.nextInt(4)+3; i++) {
                double angle = random.nextInt(359);
                double distance = random.nextInt(chunkSize/4)+chunkSize/4f;
                int xxx = minX + xx + (int) (Math.cos(Math.toRadians(angle))*distance);
                int yyy = minY + yy + (int) (Math.sin(Math.toRadians(angle))*distance);
                flies.add(new Fly(xxx, yyy));
//                System.out.println("Spawned a fly at ("+xxx+","+yyy+")");
            }
        }

        if(random.nextInt(7)==3) {
            int xx = random.nextInt(chunkSize);
            int yy = random.nextInt(chunkSize);
            for(int i = 0; i < random.nextInt(4)+3; i++) {
                double angle = random.nextInt(359);
                double distance = random.nextInt(chunkSize/4)+chunkSize/4f;
                int xxx = minX + xx + (int) (Math.cos(Math.toRadians(angle))*distance);
                int yyy = minY + yy + (int) (Math.sin(Math.toRadians(angle))*distance);
                entities.add(new EnemyBolt(xxx, yyy));
//                flies.add(new Fly(xxx, yyy));
//                System.out.println("Spawned a fly at ("+xxx+","+yyy+")");
            }
        }

        getChunkArray(x, y).addAll(chunk);

        if(isOnScreen(x, y, 2))
            findOnScreenObjects();
    }

    public CopyOnWriteArrayList<GameObject> getChunkArray(int x, int y) {
        Point loc = new Point(x, y);
        if(!chunks.containsKey(loc)) {
            chunks.put(loc, new CopyOnWriteArrayList<>());
        }
        return chunks.get(loc);
    }

    public boolean isOnScreen(int x, int y) {
        return (x>=chunkXTopLeft) && (x<=chunkXBottomRight) &&
                (y>=chunkYTopLeft) && (y<=chunkYBottomRight);
    }

    public boolean isPixelOnScreen(int x, int y) {
        return (x>=chunkXTopLeft*chunkSize) && (x<=chunkXBottomRight*chunkSize) &&
                (y>=chunkYTopLeft*chunkSize) && (y<=chunkYBottomRight*chunkSize);
    }

    public static boolean isOnScreen(int x, int y, int bufferZoneSize) {
        return (x>=chunkXTopLeft-bufferZoneSize) && (x<=chunkXBottomRight+bufferZoneSize) &&
                (y>=chunkYTopLeft-bufferZoneSize) && (y<=chunkYBottomRight+bufferZoneSize);
    }

    public static boolean isPixelOnScreen(int x, int y, int bufferZoneSize) {
        return (x>=(chunkXTopLeft-bufferZoneSize)*chunkSize) && (x<=(chunkXBottomRight+bufferZoneSize)*chunkSize) &&
                (y>=(chunkYTopLeft-bufferZoneSize)*chunkSize) && (y<=(chunkYBottomRight+bufferZoneSize)*chunkSize);
    }

    public boolean isFree(int x, int y) {
        for (GameObject obj : structuralBlocks) {
            if (obj.mask.isColliding(x, y)) {
                return false;
            }
        }
        return true;
    }

    public GameObject find(int x, int y) {
        for (GameObject obj : structuralBlocks) {
            if (obj.mask.isColliding(x, y)) {
                return obj;
            }
        }
        return null;
    }

    public void remove(int x, int y) {
        structuralBlocks.removeIf(obj -> obj.mask.isColliding(x, y));
    }

    void findOnScreenObjects() {
        findOnScreenCalled = true;
        if(findOnScreenBlocked) return;

        collisionMap.empty();
        ArrayList<GameObject> visibleChunksObjectsTemporary = new ArrayList<>();
        ArrayList<GameObject> addQueue = new ArrayList<>();
        addQueue.add(player);

        // HERE: Add chunks that are inside screen to visibleChunkObjects list
        for(int xx = chunkXTopLeft; xx < chunkXBottomRight; xx++) {
            for(int yy = chunkYTopLeft; yy < chunkYBottomRight; yy++) {
                for(GameObject obj : getChunkArray(xx, yy)) {
                    addQueue.add(obj);
                    if(obj.aabbComponent != null) {
                        collisionMap.add(obj.aabbComponent);
                    }
                }
//                visibleChunkObjects.addAll(getChunkArray(xx, yy));
            }
        }

        // HERE: Add additional ones that are partly on screen
        Mask.Rectangle visibleAreaMask = new Mask.Rectangle(
                e.getRenderer().getCamX(),
                e.getRenderer().getCamY(),
                e.getRenderer().getCamX()+e.width,
                e.getRenderer().getCamY()+e.height);
        for(int xx = chunkXTopLeft-2; xx < chunkXBottomRight+2; xx++) {
            for(int yy = chunkYTopLeft-2; yy < chunkYBottomRight+2; yy++) {
                if(xx > chunkXTopLeft && yy > chunkYTopLeft &&
                        xx < chunkXBottomRight && yy < chunkYBottomRight) {
                    continue;
                }

                getChunkArray(xx, yy).forEach(obj -> {
                    if(obj.mask.isColliding(visibleAreaMask)) {
                        addQueue.add(obj);
                        if(obj.aabbComponent != null) {
                            collisionMap.add(obj.aabbComponent);
                        }
                    }
                });
            }
        }


        int camX = e.getRenderer().getCamX();
        int camY = e.getRenderer().getCamY();
        // HERE: Also add entities
        Iterator<GameObject> iterator = entities.iterator();
        while(iterator.hasNext()) {
            GameObject obj = iterator.next();
            Point chunkLoc = getChunkLocation((int)obj.x, (int)obj.y);
            if(isOnScreen(chunkLoc.x, chunkLoc.y, 2)) {
                addQueue.add(obj);
            } else if(!(obj instanceof Fly)) {
                iterator.remove();
            }
        }

        structuralBlocks.forEach(o -> {
            if(o.mask.isColliding(visibleAreaMask)) {
                addQueue.add(o);
                if(o.aabbComponent != null)
                    collisionMap.add(o.aabbComponent);
            }
        });
        collisionMap.refresh();
        flies.forEach(f -> {
            if(isPixelOnScreen((int)f.x, (int)f.y, 1)) {
                addQueue.add(f);
            }
        });
        visibleChunksObjectsTemporary.addAll(addQueue);
        visibleChunksObjectsTemporary.sort(Comparator.comparingInt((o) -> o.depth));
        visibleChunkObjects = visibleChunksObjectsTemporary;
        {
            visibleChunkObjectsTemp.clear();
            visibleChunkObjectsTemp.addAll(visibleChunksObjectsTemporary);
        }
    }

    public static Point getChunkLocation(int x, int y) {
        return new Point(x/chunkSize, y/chunkSize);
    }

    @Override
    public void update(Core core) {
        entities.addAll(addEntities);
        addEntities.clear();

        findOnScreenBlocked = true;
        Input i = core.getInput();

        int prevCamX = core.getRenderer().getCamX();
        int prevCamY = core.getRenderer().getCamY();

        timer++;
        if(timer >= time) {
            timer = 0;
            int x = prevCamX - 2*chunkSize + random.nextInt(e.width+4*chunkSize);
            int y = prevCamY - 2*chunkSize + random.nextInt(e.height+4*chunkSize);
//            entities.add(new Ghost(xx, yy));
//            System.out.println("Ghost spawner");
        }

        Iterator<Fly> it = flies.iterator();
        while(it.hasNext()) {
            Fly fly = it.next();
            if(fly.dead) {
                it.remove();
            } else {
                fly.update(core.getInput());
            }
        }

        Iterator<GameObject> it2 = entities.iterator();
        while(it2.hasNext()) {
            GameObject entity = it2.next();
            if(entity.dead) {
                it2.remove();
            } else {
                entity.update(core.getInput());
            }
        }

        player.update(core.getInput());
        Iterator<GameObject> it3;
        for(it3 = Main.main.visibleChunkObjects.iterator(); it3.hasNext();) {
            GameObject obj = it3.next();
            if(obj instanceof Tree || obj instanceof Plant || obj instanceof Trash) {
                obj.update(core.getInput());
            }
        };
        cursor.update(i);

        e.getWindow().getFrame().setTitle("Codename SHOOTER - FPS: "+e.getFps());

//        player.update(i);

        camera.update();
        camera.apply(core.getRenderer());

        chunkXTopLeft = (int)Math.floor(e.getRenderer().getCamX()/(double)chunkSize);
        chunkYTopLeft = (int)Math.floor(e.getRenderer().getCamY()/(double)chunkSize);
        chunkXBottomRight = (int)Math.floor((e.width+e.getRenderer().getCamX())/(double)chunkSize);
        chunkYBottomRight = (int)Math.floor((e.height+e.getRenderer().getCamY())/(double)chunkSize);

        /*chunkXTopLeft = (int)Math.floor(camera.cX/(double)chunkSize);
        chunkYTopLeft = (int)Math.floor(camera.cY/(double)chunkSize);
        chunkXBottomRight = (int)Math.floor((e.width+camera.cX)/(double)chunkSize);
        chunkYBottomRight = (int)Math.floor((e.height+camera.cY)/(double)chunkSize);*/

        // HERE: Gather together all chunks that are visible
        // HERE: and put them to visibleChunkObjects

        if(i.isKeyDown(VK_F2)) {
            showDebugInfo = !showDebugInfo;
        }

        if(i.isKeyDown(VK_F4)) {
            entities.clear();
        }

        if(i.isButtonDown(2)) {
            Arrow arrow = new Arrow(i.getRelativeMouseX(), i.getRelativeMouseY(),
                    (int)Fly.angle(i.getRelativeMouseX(), i.getRelativeMouseY(), player.x, player.y)-180);
            arrow.shotByFriendly = false;
            entities.add(arrow);
        }


        if(i.isKeyDown(VK_F3) && showDebugInfo) {
            usesChunkRenderer = !usesChunkRenderer;
        }

        hoverChunkX=i.getRelativeMouseX()/chunkSize;
        hoverChunkY=i.getRelativeMouseY()/chunkSize;

        if(i.isButton(1) && showDebugInfo) {
//            // Place new tree, debug only!
//            world.add(new Tree(i.getMouseX(), i.getMouseY()));

            // HERE: Generate selected chunk
            deleteChunk(hoverChunkX, hoverChunkY);
            generateChunk(hoverChunkX, hoverChunkY);
        }

//        findOnScreenObjects();
//        int moveX = (i.isKey(VK_D) ? 1 : 0)*8 - (i.isKey(VK_A) ? 1 : 0)*8;
//        int moveY = (i.isKey(VK_S) ? 1 : 0)*8 - (i.isKey(VK_W) ? 1 : 0)*8;
//
//        core.getRenderer().setCamX(core.getRenderer().getCamX()+moveX);
//        core.getRenderer().setCamY(core.getRenderer().getCamY()+moveY);


        // HERE: Generate new chunks
        if(prevCamX != core.getRenderer().getCamX() || prevCamY != core.getRenderer().getCamY()) {
            findOnScreenObjects();
            for(int xx = chunkXTopLeft-2; xx < chunkXBottomRight+2; xx++) {
                for(int yy = chunkYTopLeft-2; yy < chunkYBottomRight+2; yy++) {
                    if(getChunkArray(xx, yy).size() == 0) {
                        generateChunk(xx, yy);
                    }
                }
            }
        }

        findOnScreenBlocked = false;
        if(findOnScreenCalled) findOnScreenObjects();
    }

    public static int toSlowMotion(int amount) {
        return (int)(amount*slowMotionSpeed);
    }

    public static double toSlowMotion(double amount) {
        return amount*slowMotionSpeed;
    }

    @Override
    public void render(Core core) {
        Renderer r = core.getRenderer();

        r.absolute();

        Filter grayScale = (newC, oldC) -> {
            int cr = newC.getRed();
            int cg = newC.getGreen();
            int cb = newC.getBlue();

            int grayScaleColor = (int)(((double)(cr+cg+cb))/3d);

            cr = grayScaleColor;
            cg = grayScaleColor;
            cb = grayScaleColor;

            return new Color(cr, cg, cb);
        };


        Filter slowMotionOverlay = (newC, oldC) -> {
            float amount = camera.bluishEffect;
            double cr = newC.getRed() * (1 - 0.45 * amount);
            double cg = newC.getGreen() * (1 - 0.45 * amount);
            double cb = newC.getBlue() * (1 - 0.45 * amount);

            double grayScaleColor = (cr+cg+cb) / 3d;

            cr = cr*(1-amount)+grayScaleColor*amount;
            cg = cg*(1-(amount*0.25))+grayScaleColor*(amount*0.25);
            cb = cb*(1-amount)+grayScaleColor*amount;

            cr = Math.max(0, Math.min(255, cr*1.25));
            cg = Math.max(0, Math.min(255, cg));
            cb = Math.max(0, Math.min(255, cb));

            return new Color((int)cr, (int)cg, (int)cb, newC.getAlpha());
        };

        r.setFilter(0, slowMotionOverlay);

        /*r.setFilter(0, (newC, oldC) -> {
            newC = new Color(64, 64, 64, 64);

            return new Color(
                    oldC.getRed(), oldC.getGreen(), oldC.getBlue()
//                255,
//                UniParticle.calcColorParameter(oldC.getRed(), newC.getRed(), newC.getAlpha()/255f),
//                UniParticle.calcColorParameter(oldC.getGreen(), newC.getGreen(), newC.getAlpha()/255f),
//                UniParticle.calcColorParameter(oldC.getBlue(), newC.getBlue(), newC.getAlpha()/255f)
            );

        });*/

        r.fillRectangle(0, 0, e.width, e.height, grassColor);

        r.relative();
        if(showDebugInfo)
            r.fillRectangle(hoverChunkX*chunkSize, hoverChunkY*chunkSize, chunkSize, chunkSize, new Color(40, 100, 70));

        if(usesChunkRenderer) {
            // HERE: Render only visible chunks

            Iterator<GameObject> it;
            for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
                it.next().render(r);
            }
//            visibleChunkObjects.forEach(obj -> obj.render(r));
//            flies.forEach(f -> {
//                if(isPixelOnScreen((int)f.xx, (int)f.yy, 1)) {
//                    f.render(r);
//                }
//            });
        } else {
            // HERE: Render all chunks, temporally
            chunks.forEach((loc, chunk) -> chunk.forEach(obj -> obj.render(r)));
//            flies.forEach(f -> f.render(r));
        }

        // HERE: Render nearby bullet warnings
        Filter backup = r.getFilter(0);
        r.removeFilter(0);
        if(player.blinkingON) {
            Iterator<GameObject> it;
            for(it = Main.main.visibleChunkObjects.iterator(); it.hasNext();) {
                GameObject obj = it.next();
                if (obj instanceof Arrow)
                    if (!((Arrow) obj).shotByFriendly)
                        if (Fly.distance(obj.x, obj.y, player.x, player.y) < 160) {
                            r.drawRectangle(obj.x - 8, obj.y - 8, 16, 16, Color.red);
                        }
            }
        }
        r.setFilter(0, backup);

        r.setFont(new Font("RETRO", Font.BOLD, 16));
        r.drawText(String.valueOf((int)player.money), player.coinOverlayX, player.coinOverlayY-32, 16,
                Alignment.MIDDLE_CENTER, new Color(40, 250, 140, (int)player.coinOverlayAlpha));


//        double mouseAngle = Fly.angle(player.x, player.y, e.getInput().getRelativeMouseX(), e.getInput().getRelativeMouseY());
        if(player.clipOverlayAlpha != 0) {
            double xx = e.getInput().getRelativeMouseX();
            double yy = e.getInput().getRelativeMouseY();
            double step1 = 360d / player.maxClip;
            double addAngle = player.clipOverlayRotation;
            for (int i = 0; i < player.clip; i++) {
                double angle = step1 * i + addAngle;
//            System.out.println("XX: "+xx);
//            System.out.println("YY: "+yy);
//            System.out.println("Angle: "+angle);
                double sizeOuter = 24;
                double sizeInner = 12;
                double x1 = xx + Math.cos(Math.toRadians(angle + 3)) * sizeOuter;
                double y1 = yy + Math.sin(Math.toRadians(angle + 3)) * sizeOuter;
                double x2 = xx + Math.cos(Math.toRadians(angle + step1 - 3)) * sizeOuter;
                double y2 = yy + Math.sin(Math.toRadians(angle + step1 - 3)) * sizeOuter;
                double x3 = xx + Math.cos(Math.toRadians(angle + step1 - 3)) * sizeInner;
                double y3 = yy + Math.sin(Math.toRadians(angle + step1 - 3)) * sizeInner;
                double x4 = xx + Math.cos(Math.toRadians(angle + 3)) * sizeInner;
                double y4 = yy + Math.sin(Math.toRadians(angle + 3)) * sizeInner;

                r.fillPolygon(new double[]{x1, x2, x3, x4}, new double[]{y1, y2, y3, y4}, new Color(64, 64, 64, (int) player.clipOverlayAlpha));
            }
            double step2 = 360d/player.maxAmmo;
            for (int i = 0; i < player.ammo; i++) {
                double angle = step2 * i + addAngle / 1.5d;
//            System.out.println("XX: "+xx);
//            System.out.println("YY: "+yy);
//            System.out.println("Angle: "+angle);
                double sizeOuter = 32;
                double sizeInner = 28;
                double x1 = xx + Math.cos(Math.toRadians(angle + 3)) * sizeInner;
                double y1 = yy + Math.sin(Math.toRadians(angle + 3)) * sizeInner;
                double x2 = xx + Math.cos(Math.toRadians(angle + 3)) * sizeOuter;
                double y2 = yy + Math.sin(Math.toRadians(angle + 3)) * sizeOuter;

                r.drawLineWidth(x1, y1, x2, y2, 1, new Color(80, 80, 64, (int) player.clipOverlayAlpha));
            }
        }
        r.absolute();
//        r.fillRectangle(0, 0, e.width, e.height, new Color(64, 64, 64, 64));

        if(showDebugInfo) {
            int addY = 16;
            int y = 8;
            r.setFont(new Font("Arial", Font.PLAIN, 10));
            r.drawText("FPS: " + core.getFps(), 8, y, 10, Color.black);

            y += addY;
            r.drawText("X: " + core.getInput().getRelativeMouseX(), 8, y, 10, Color.black);
            y += addY;
            r.drawText("Y: " + core.getInput().getRelativeMouseY(), 8, y, 10, Color.black);

            y += addY;
            r.drawText("Chunk X: " + hoverChunkX, 8, y, 10, Color.black);
            y += addY;
            r.drawText("Chunk Y: " + hoverChunkY, 8, y, 10, Color.black);

            y += addY;
            r.drawText("Uses Chunk Renderer: " + (usesChunkRenderer ? "true" : "false"), 8, y, 10, Color.black);

            y += addY;
            r.drawText("Entities: " + entities.size(), 8, y, 10, Color.black);

            y += addY;
            r.drawText("CamX: " + camera.cX, 8, y, 10, Color.black);
            y += addY;
            r.drawText("CamY: " + camera.cY, 8, y, 10, Color.black);
            y += addY;
            r.drawText("PlayerX: " + player.xx, 8, y, 10, Color.black);
            y += addY;
            r.drawText("PlayerY: " + player.yy, 8, y, 10, Color.black);
        }
        r.relative();

        cursor.render(r);

        /*r.removeFilter(0);

        r.absolute();

        if(player.inventoryOpen)
            player.renderInventory(r);

        r.relative();*/

    }
}
