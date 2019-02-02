package com.youngdev.shooter;

import com.engine.Core;
import com.engine.Game;
import com.engine.libs.font.Alignment;
import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Filter;
import com.engine.libs.rendering.RenderUtils;
import com.engine.libs.rendering.Renderer;
import com.engine.libs.world.CollisionMap;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.youngdev.shooter.EnemyBolt.calcColorParameter;
import static java.awt.event.KeyEvent.*;

public class Main extends Game {
    public static Main main;

    public static int chunkSize = 128;
    public int flyCounter;
    private DataInputStream inputStreamReader;
    private DataOutputStream outputStreamWriter;
    private int hoverChunkX, hoverChunkY;
    private static int chunkXTopLeft;
    private static int chunkYTopLeft;
    private static int chunkXBottomRight;
    private static int chunkYBottomRight;
    static int width = 320;
    static int height = 180;
    private boolean findOnScreenBlocked, findOnScreenCalled;
    private boolean usesChunkRenderer;
    boolean showDebugInfo;
    static float slowMotionSpeed = 1f;
    private static  float slowMotionMultiplier = 1f;
    private Random random;
    public Camera camera;
    static CollisionMap collisionMap;
    static boolean startMenuMode = false;
    private BufferedImage gameLogo;
    private int numButtons;
    private String[] buttonLabels;
    private BufferedImage[] buttonImages;
    private ArrayList[] buttonHoverVectors;
    private boolean[] buttonPrevInside;
    private ArrayList<UniParticle> startMenuButtonParticles;
    public ArrayList<Player> playerEntities;
    private Cursor cursor;
    static Color grassColor = new Color(80, 140, 110);
    public Player player;
    public List<GameObject> addEntities;
    public List<GameObject> entities;
    List<GameObject> coins;
    private List<GameObject> structuralBlocks,visibleChunkObjectsTemp;
    public List<Fly> flies;
    public SoundManager soundManager;
    private boolean[] startMenuButtons_prevHover;
    private boolean startMenuModePrev;
    public int flySounds;
    private int tick;

    // *** SOUNDS ***
    private Clip noise;
    private Clip music;

    public static void main(String[] args) {
        new Main();
    }

    List<GameObject> visibleChunkObjects;
    List<WorldObject> visibleChunkEntities;
    private Map<Point, ArrayList<GameObject>> chunks;
    private Map<Point, HashSet<WorldObject>> chunkEntities;
    private ArrayDeque<MoveTo> moveChunkEntities;

    public Main() {
        main = this;
        int size = 25;
        width = 16*size;
        height = 9*size;

        e.width = width;
        e.height = height;
        e.scale = 3f;
//        e.startAsFullscreen = true;

        showDebugInfo = false;

        e.start();

        // HERE: Init

        buttonLabels = new String[] {
                "DuleKiva",
                "Kohandatud mäng",
                "Krediidid",
                "Seaded",
                "Lahku"
        };
        numButtons = 5;
        buttonImages = new BufferedImage[numButtons];
        buttonPrevInside = new boolean[numButtons];

        playerEntities = new ArrayList<>();

        startMenuMode = true;
        flySounds = 5;
        flyCounter = 0;

        soundManager = new SoundManager();
        loadSounds();
        startMenuButtons_prevHover = new boolean[numButtons];
        Arrays.fill(startMenuButtons_prevHover, false);

        AABBCollisionManager.MAX_UNSTUCK_TRIES = 32;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ArrayList<String> fonts = new ArrayList<>();
        fonts.add("press-start.regular.ttf");
        fonts.add("Nunito-Bold.ttf");
        fonts.add("Nunito-Light.ttf");

        for(String name : fonts) {
            try {
                ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream("/"+name)));
            } catch (Exception ignored1) {
                try {
                    ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getResourceAsStream("/"+name)));
                } catch (Exception ignored2) {
                    try {
                        ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream(name)));
                    } catch (Exception ignored3) {
                        try {
                            ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getResourceAsStream(name)));
                        } catch (Exception ignored4) {}
                    }
                }
            }
        }

        random = new Random();
        entities = Collections.synchronizedList(new ArrayList<>());
        cursor = new Cursor();
        chunks = new HashMap<>();
        usesChunkRenderer = true;
        moveChunkEntities = new ArrayDeque<>();
        chunkEntities = new HashMap<>();
        visibleChunkObjects = Collections.synchronizedList(new ArrayList<>());
        visibleChunkEntities = Collections.synchronizedList(new ArrayList<>());
        visibleChunkObjectsTemp = Collections.synchronizedList(new ArrayList<>());
        flies = Collections.synchronizedList(new ArrayList<>());
        structuralBlocks = Collections.synchronizedList(new ArrayList<>());
        addEntities = Collections.synchronizedList(new ArrayList<>());
        coins = Collections.synchronizedList(new ArrayList<>());
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

        // HERE: Pre-Render game logo
        gameLogo = RenderUtils.createImage(e.width, 48, false);
        Graphics2D g2 = (Graphics2D) gameLogo.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        Container contentPane = e.getWindow().getFrame().getContentPane();
        contentPane.setCursor(contentPane.getToolkit().createCustomCursor(
                new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ),
                new Point(),
                null ));

        String text = "DuleKiva";

//        g2.setColor(new Color());
//        g2.fill;
        GradientPaint gradientPaint = new GradientPaint(0, 0, grassColor,
                0, gameLogo.getHeight(), new Color(
                        grassColor.getRed(),
                        grassColor.getGreen(),
                        grassColor.getBlue(),
                        0
        ));
        g2.setPaint(gradientPaint);
        g2.fillRect(0, 0, gameLogo.getWidth(), gameLogo.getHeight());
        g2.setPaint(null);
        g2.setStroke(new BasicStroke(4f));
        g2.setFont(new Font("Nunito Bold", Font.PLAIN, 24));

        FontMetrics fontMetrics = g2.getFontMetrics();
        Rectangle2D textBounds = fontMetrics.getStringBounds(text, g2);

        g2.setColor(Color.black);
        g2.drawString(text, (int)(gameLogo.getWidth()-textBounds.getWidth())/2,
                32);

        startMenuButtonParticles = new ArrayList<>();

        buttonHoverVectors = new ArrayList[numButtons];

        for(int i = 0; i < numButtons; i++) {
            BufferedImage image = RenderUtils.createImage(192, 24);
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            g.setStroke(new BasicStroke(2f));
            g.setColor(Color.black);
            g.setFont(new Font("Nunito Bold", Font.PLAIN, 16));
            g.drawString(buttonLabels[i], 8, 16);

            buttonHoverVectors[i] = new ArrayList<>();

            buttonImages[i] = image;
        }

        music = soundManager.playSound("startMenuMusic", Clip.LOOP_CONTINUOUSLY, -15f);
        noise = soundManager.playSound("noise", Clip.LOOP_CONTINUOUSLY, 0f);
        noise.stop();
        music.loop(Clip.LOOP_CONTINUOUSLY);
        noise.loop(Clip.LOOP_CONTINUOUSLY);

        e.run();
    }

    public void move(WorldObject obj, int fromX, int fromY,
                     int toX, int toY) {
        moveChunkEntities.offerLast(new MoveTo(obj,
                fromX, fromY, toX, toY));
    }

    private void loadSounds() {
        soundManager.addClip("sounds/backgroundMusic.wav", "startMenuMusic");
        soundManager.addClip("sounds/noiseLow.wav", "noise");
        soundManager.addClip("sounds/buttonHoverBass.wav", "buttonHover");
        soundManager.addClip("sounds/buttonPressedBass.wav", "buttonPress");
        soundManager.addClip("sounds/flyAway.wav", "flyAway");

        soundManager.addClip("sounds/footsteps/dirt1.wav", "dirt0");
        soundManager.addClip("sounds/footsteps/dirt2.wav", "dirt1");
        soundManager.addClip("sounds/footsteps/dirt3.wav", "dirt2");
        soundManager.addClip("sounds/footsteps/dirt4.wav", "dirt3");
        soundManager.addClip("sounds/footsteps/dirt5.wav", "dirt4");

        soundManager.addClip("sounds/footsteps/grass0.wav", "grass0");
        soundManager.addClip("sounds/footsteps/grass1.wav", "grass1");

        soundManager.addClip("sounds/bee.wav", "bee");
        soundManager.addClip("sounds/beeFlyingAway.wav", "beeFlyingAway");

        soundManager.addClip("sounds/tapping.wav", "branchesTouched");

        soundManager.addClip("sounds/explosion.wav", "explosion");
    }

    public void deleteChunk(int x, int y) {
        chunks.remove(new Point(x, y));
    }

    private void generateChunk(int x, int y) {
        ArrayList<GameObject> chunk = new ArrayList<>();
        HashSet<WorldObject> chunkEntities = new HashSet<>();

        Random random = new Random();

        int minX = x*chunkSize;
        int minY = y*chunkSize;

        if(random.nextInt(10) == 0 || true) {
            chunkEntities.add(new DuleKiva(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        if(random.nextBoolean()) {
            chunk.add(new Bush(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        if(random.nextBoolean()) {
            chunk.add(new Rocks(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        for(int i = 0; i < random.nextInt(7); i++) {
            Color clr = new Color(random.nextInt(135)+100,random.nextInt(135)+100,random.nextInt(135)+100);
//            Color clr = new Color(Color.HSBtoRGB(random.nextFloat(), 1f, 1f));
//            System.out.println(clr);
            chunk.add(new Plant(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY, clr));
        }

        for(int i = 0; i < random.nextInt(4); i++) {
            chunk.add(new Terrain(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY, random.nextInt(2)+1));
        }

        if(random.nextInt(4)==1) {
            chunk.add(new Trash(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY, random.nextInt(2)+2));
        }

        if(random.nextInt(8)==1) {
            chunk.add(new Branches(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        if(random.nextInt(8)==1) {
            chunkEntities.add(new FlyGroup(minX+chunkSize/2,
                    minY+chunkSize/2,
                    random.nextInt(15)+10));
            /*int xx = random.nextInt(chunkSize);
            int yy = random.nextInt(chunkSize);
            for(int i = 0; i < random.nextInt(15)+15; i++) {
                double angle = random.nextInt(359);
                double distance = random.nextInt(chunkSize/4)+chunkSize/4f;
                int xxx = minX + xx + (int) (Math.cos(Math.toRadians(angle))*distance);
                int yyy = minY + yy + (int) (Math.sin(Math.toRadians(angle))*distance);
                flies.add(new Fly(xxx, yyy));
//                System.out.println("Spawned a fly at ("+xxx+","+yyy+")");
            }*/
        }

        if(random.nextInt(4)==1) {
            chunkEntities.add(new Rabbit(minX+chunkSize/2, minY+chunkSize/2));
        }

        /*if(random.nextInt(7)==3) {
            int xx = random.nextInt(chunkSize);
            int yy = random.nextInt(chunkSize);
            for(int i = 0; i < random.nextInt(4)+3; i++) {
                double angle = random.nextInt(359);
                double distance = random.nextInt(chunkSize/4)+chunkSize/4f;
                int xxx = minX + xx + (int) (Math.cos(Math.toRadians(angle))*distance);
                int yyy = minY + yy + (int) (Math.sin(Math.toRadians(angle))*distance);
//                entities.add(new EnemyBolt(xxx, yyy));
//                flies.add(new Fly(xxx, yyy));
//                System.out.println("Spawned a fly at ("+xxx+","+yyy+")");
            }
        }*/

        if(random.nextInt(10)==1) {
            chunk.add(new Tree(minX + random.nextInt(chunkSize),
                    minY + random.nextInt(chunkSize), random.nextBoolean() ?
                    Tree.TYPE_SAVANNA : Tree.TYPE_OAK));
        }

        Point loc = new Point(x, y);

        if(!chunks.containsKey(loc)) {
            chunks.put(loc, chunk);
        } else getChunkArray(x, y).addAll(chunk);

        if(!this.chunkEntities.containsKey(loc)) {
            this.chunkEntities.put(loc, chunkEntities);
        } else this.chunkEntities.get(loc).addAll(chunkEntities);
    }

    public ArrayList<GameObject> getChunkArray(int x, int y) {
        Point loc = new Point(x, y);
        if(!chunks.containsKey(loc)) {
            chunks.put(loc, new ArrayList<>());
        }
        return chunks.get(loc);
    }

    public ArrayList<GameObject> getAndGenerateChunk(int x, int y) {
        Point loc = new Point(x, y);
        if(!chunks.containsKey(loc)) {
            generateChunk(loc.x, loc.y);
        }
        ArrayList<GameObject> chunk = chunks.get(loc);
        return chunk == null ? new ArrayList<>() :
                chunk;
    }

    public boolean isOnScreen(int x, int y) {
        return (x>=chunkXTopLeft) && (x<=chunkXBottomRight) &&
                (y>=chunkYTopLeft) && (y<=chunkYBottomRight);
    }

    public boolean isPixelOnScreen(int x, int y) {
        return (x>=chunkXTopLeft*chunkSize) && (x<=chunkXBottomRight*chunkSize) &&
                (y>=chunkYTopLeft*chunkSize) && (y<=chunkYBottomRight*chunkSize);
    }

    private boolean isPixelOnScreen(int x1, int y1, int x2, int y2, int x, int y) {
        return (x>=x1*chunkSize) && (x<=x2*chunkSize) &&
                (y>=y1*chunkSize) && (y<=y2*chunkSize);
    }

    public boolean isPixelOnScreen(int x1, int y1, int x2, int y2, int x, int y, int bufferZoneSize) {
        return (x>=(x1-bufferZoneSize)*chunkSize) && (x<=(x2+bufferZoneSize)*chunkSize) &&
                (y>=(y1-bufferZoneSize)*chunkSize) && (y<=(y2+bufferZoneSize)*chunkSize);
    }

    private static boolean isOnScreen(int x, int y, int bufferZoneSize) {
        return (x>=chunkXTopLeft-bufferZoneSize) && (x<=chunkXBottomRight+bufferZoneSize) &&
                (y>=chunkYTopLeft-bufferZoneSize) && (y<=chunkYBottomRight+bufferZoneSize);
    }

    private static boolean isOnScreen(int x1, int y1, int x2, int y2,
                                      int x, int y, int bufferZoneSize) {
        return (x>=x1-bufferZoneSize) && (x<=x2+bufferZoneSize) &&
                (y>=y1-bufferZoneSize) && (y<=y2+bufferZoneSize);
    }

    static boolean isPixelOnScreen(int x, int y, int bufferZoneSize) {
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

    public void addEntity(WorldObject entity) {
        int cX = Math.floorDiv((int)entity.x, chunkSize);
        int cY = Math.floorDiv((int)entity.y, chunkSize);
        chunkEntities.get(new Point(cX, cY)).add(entity);
    }

    void findOnScreenObjects() {
        int chunkXTopLeft = (int)Math.floor(camera.cX/(double)chunkSize);
        int chunkYTopLeft = (int)Math.floor(camera.cY/(double)chunkSize);
        int chunkXBottomRight = (int)Math.ceil((e.width+camera.cX)/(double)chunkSize);
        int chunkYBottomRight = (int)Math.ceil((e.height+camera.cY)/(double)chunkSize);

        collisionMap.empty();
        ArrayList<GameObject> visibleChunksObjectsTemporary = new ArrayList<>();
        ArrayList<GameObject> addQueue = new ArrayList<>();
        if(!startMenuMode) {
            addQueue.add(player);
        }

        // HERE: Add additional ones that are partly on screen
        Mask.Rectangle visibleAreaMask = new Mask.Rectangle(
                e.getRenderer().getCamX(),
                e.getRenderer().getCamY(),
                e.getRenderer().getCamX()+e.width,
                e.getRenderer().getCamY()+e.height);
        for(int xx = chunkXTopLeft-4; xx < chunkXBottomRight+4; xx++) {
            for(int yy = chunkYTopLeft-4; yy < chunkYBottomRight+4; yy++) {
                ArrayList<GameObject> chunk = getChunkArray(xx, yy);
                GameObject obj;
                for(int j = chunk.size()-1; j >= 0; j--) {
                    obj = chunk.get(j);
                    if(visibleAreaMask.isColliding(obj.mask)) {
                        addQueue.add(obj);
                        if(obj.aabbComponent != null) {
                            collisionMap.add(obj.aabbComponent);
                        }
                    }
                }
            }
        }
        for(int xx = chunkXTopLeft-4; xx < chunkXBottomRight+4; xx++) {
            for(int yy = chunkYTopLeft-4; yy < chunkYBottomRight+4; yy++) {
                HashSet<WorldObject> chunk = chunkEntities.
                        get(new Point(xx, yy));
                chunk.removeIf(obj -> {
                    if((obj.mask != null && isPixelOnScreen(
                            (int)obj.x, (int)obj.y, 4)) ||
                            (obj.mask != null && obj.mask instanceof
                                    Mask.Rectangle && visibleAreaMask.
                                    isColliding(obj.mask))) {
                        addQueue.add(obj);
                    }
                    return obj.dead;
                });
            }
        }

        for (GameObject obj : coins) {
            Point chunkLoc = getChunkLocation((int) obj.x, (int) obj.y);
            if (isOnScreen(chunkLoc.x, chunkLoc.y, 1)) {
                addQueue.add(obj);
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
        Input i = core.getInput();

        tick++;
        if(tick > 59) {
            tick = 0;

            // TODO: Do something every 1 second
            flyCounter = 0;
        }

        if(startMenuMode) {
            camera.bluishEffect = 0f;
            int x = core.getInput().getMouseX();
            int y = core.getInput().getMouseY();
//            int alphaSpeed = 12;
            for(int k = 0; k < numButtons; k++) {
                int xx = 16;
                int yy = 64+32*k;
                boolean inside = false;
//                if(AdvancedMath.inRange(x, y, xx, yy, 192, 24)) {
                if(AdvancedMath.inRange(x, y, 0, yy, width, 24)) {
                    inside = true;

                    if(!startMenuButtons_prevHover[k])
                        buttonHoverVectors[k].add(new Vec3d(
                                i.getMouseX(), 1d, 1d));

                    if(!startMenuButtons_prevHover[k]) {
                        soundManager.playSound("buttonHover", 0.5f);
                    }

                    if(i.isButtonDown(1)) {
                        if(k == 0) {
                            // TODO: Start the game
                            startMenuMode = false;
                        }
                        soundManager.playSound("buttonPress", 0.5f);
                    }
                    startMenuButtons_prevHover[k] = true;
                } else {
                    startMenuButtons_prevHover[k] = false;
                }
//                cursor.visible = !found;
                Iterator iterator = buttonHoverVectors[k].iterator();
                for(;iterator.hasNext();) {
                    Object obj = iterator.next();
                    if(obj instanceof Vec3d) {
                        if(!inside || ((Vec3d)obj).y > 0.4d)
                            ((Vec3d) obj).y /= 1.05;
                        ((Vec3d) obj).z -= 0.05;
//                            ((Vec3d) obj).z -= 0.01;
                        if(((Vec3d) obj).y <= 0.003) {
                            iterator.remove();
                        }
                    }
                }
//                System.out.println("size = " + buttonHoverVectors[k].size());
            }

            startMenuButtonParticles.removeIf(p -> {
                if(p.dead) {
                    return true;
                } else {
                    p.update();
                    return false;
                }
            });
        }

        while(!moveChunkEntities.isEmpty()) {
            MoveTo move = moveChunkEntities.pollFirst();
            if(move != null) {
                chunkEntities.get(new Point(move.fromX, move.fromY)).
                        remove(move.targetObject);
                chunkEntities.get(new Point(move.toX, move.toY)).
                        add(move.targetObject);
            }
        }

//        entities.addAll(addEntities);
//        addEntities.clear();

        findOnScreenBlocked = true;

        int prevCamX = core.getRenderer().getCamX();
        int prevCamY = core.getRenderer().getCamY();

        coins.removeIf(o -> {
            if(!o.dead) o.update(i);
            return o.dead;
        });

        /*Iterator<Fly> it = flies.iterator();
        while(it.hasNext()) {
            Fly fly = it.next();

            if(!isPixelOnScreen((int)fly.x, (int)fly.y, 4)) {
                fly.dead = true;
            }

            if(fly.dead) {
                it.remove();
            } else {
                fly.update(core.getInput());
            }
        }*/

        /*Iterator<GameObject> it2 = entities.iterator();
        while(it2.hasNext()) {
            GameObject entity = it2.next();
            entity.dead = entity.dead ||
                    !isPixelOnScreen((int)entity.x,
                            (int)entity.y, 10);
            if(entity.dead) {
                it2.remove();
            } else {
                entity.update(core.getInput());
            }
        }*/

        Iterator<Player> it3 = playerEntities.iterator();
        while(it3.hasNext()) {
            Player player = it3.next();
            if(player.dead) {
                it3.remove();
            } else {
                player.update(core.getInput());
            }
        }

        if(!startMenuMode)
            player.update(core.getInput());
        Iterator<GameObject> it4;
        for(it4 = Main.main.visibleChunkObjects.iterator(); it4.hasNext();) {
            GameObject obj = it4.next();
            if(obj == player) continue;
            if(obj instanceof WorldObject) {
                if (!(obj instanceof Tree) && !(obj instanceof Rocks)) {
                    obj.update(core.getInput());
                }
            }
        }
        Iterator<WorldObject> it5;
        for(it5 = Main.main.visibleChunkEntities.iterator(); it5.hasNext();) {
            WorldObject obj = it5.next();
            if(obj == player) continue;
            double prevX = obj.x;
            double prevY = obj.y;
            obj.update(core.getInput());
            obj.checkLocation(prevX, prevY);
        }
        cursor.update(i);

        e.getWindow().getFrame().setTitle("Codename SHOOTER - FPS: "+e.getFps());

//        player.update(i);

        camera.update();
        camera.apply(core.getRenderer());
        chunkXTopLeft = (int)Math.floor(camera.cX/(double)chunkSize);
        chunkYTopLeft = (int)Math.floor(camera.cY/(double)chunkSize);
        chunkXBottomRight = (int)Math.ceil((e.width+camera.cX)/(double)chunkSize);
        chunkYBottomRight = (int)Math.ceil((e.height+camera.cY)/(double)chunkSize);

        // HERE: Gather together all chunks that are visible
        // HERE: and put them to visibleChunkObjects

        if(i.isKeyDown(VK_F2)) {
            showDebugInfo = !showDebugInfo;
        }

        if(i.isKeyDown(VK_F1)) {
            camera.target = player;
        }

        if(i.isKeyDown(VK_F6)) {
            int centerX = e.getRenderer().getCamX()+width/2;
            int centerY = e.getRenderer().getCamY()+height/2;

            int chunkX1 = (centerX-8192)/chunkSize;
            int chunkY1 = (centerY-8192)/chunkSize;
            int chunkX2 = (centerX+8192)/chunkSize;
            int chunkY2 = (centerY+8192)/chunkSize;

            for(int j = chunkY1; j < chunkY2; j++) {
                for(int k = chunkX1; k < chunkX2; k++) {
                    generateChunk(k, j);
                }
            }
        }

        if(i.isKeyDown(VK_F5)) {
            // HERE: Take a "Mega" screenshot
            BufferedImage image = new BufferedImage(24*chunkSize,
                    16*chunkSize, BufferedImage.TYPE_INT_RGB);
            Renderer r = new Renderer(image.getGraphics(), image.getWidth(), image.getHeight());
            r.absolute();
            r.fillRectangle(0, 0, image.getWidth(), image.getHeight(), grassColor);

            int centerX = e.getRenderer().getCamX()+width/2;
            int centerY = e.getRenderer().getCamY()+height/2;

            int chunkX1 = (centerX)/chunkSize-12;
            int chunkY1 = (centerY)/chunkSize-8;
            int chunkX2 = (centerX)/chunkSize+12;
            int chunkY2 = (centerY)/chunkSize+8;

            r.relative();
            r.setCamX( chunkX1*chunkSize);
            r.setCamY( chunkY1*chunkSize);
            r.relative();

            ArrayList<GameObject> objects = new ArrayList<>();

            for(int j = chunkY1; j < chunkY2; j++) {
                for(int k = chunkX1; k < chunkX2; k++) {
                    objects.addAll(getAndGenerateChunk(k, j));
//                    .forEach(o -> o.render(r));
                }
            }

            Mask.Rectangle area = new Mask.Rectangle(chunkX1*chunkSize, chunkY1*chunkSize,
                    (chunkX2-chunkX1)*chunkSize, (chunkY2-chunkY1)*chunkSize);

            for(GameObject entity : entities) {
                if(entity.mask.isColliding(area)) {
                    objects.add(entity);
//                    entity.render(r);
                }
            }


            objects.sort(Comparator.comparingInt((o) -> o.depth));
            objects.forEach(o -> o.render(r));

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".png");
                }

                @Override
                public String getDescription() {
                    return "PNG";
                }
            });
            fileChooser.showSaveDialog(e.getWindow().getFrame());
            File file = fileChooser.getSelectedFile();
            if(file != null) {
                try {
                    if(!file.getName().contains(".")) {
                        file = new File(file.getAbsolutePath()+".png");
                    }
                    ImageIO.write(image, "PNG", file);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(e.getWindow().getFrame(), "Image saved");
        }

        if(i.isKeyDown(VK_F3)) {
            for(GameObject obj : entities) {
                if(obj.mask.isColliding(i.getRelativeMouseX(), i.getRelativeMouseY())) {
                    camera.target = obj;
                }
            }
        }

        if(i.isKeyDown(VK_F4)) {
            entities.clear();
        }

        if(i.isButtonDown(2)) {
//            Arrow arrow = new Arrow(i.getRelativeMouseX(), i.getRelativeMouseY(),
//                    (int)Fly.angle(i.getRelativeMouseX(), i.getRelativeMouseY(), player.x, player.y)-180);
//            arrow.shotByFriendly = false;
            GameObject bunny = new Rabbit(i.getRelativeMouseX(), i.getRelativeMouseY());
            entities.add(bunny);
//            camera.target = bunny;
        }


        if(i.isKeyDown(VK_F3) && showDebugInfo) {
            usesChunkRenderer = !usesChunkRenderer;
        }

        hoverChunkX=i.getRelativeMouseX()/chunkSize;
        hoverChunkY=i.getRelativeMouseY()/chunkSize;

        /*if(i.isButtonDown(1) && showDebugInfo && (isServer || startMenuMode)) {
            // HERE: Generate selected chunk
            deleteChunk(hoverChunkX, hoverChunkY);
            generateChunk(hoverChunkX, hoverChunkY);
        }*/

//        findOnScreenObjects();
//        int moveX = (i.isKey(VK_D) ? 1 : 0)*8 - (i.isKey(VK_A) ? 1 : 0)*8;
//        int moveY = (i.isKey(VK_S) ? 1 : 0)*8 - (i.isKey(VK_W) ? 1 : 0)*8;
//
//        core.getRenderer().setCamX(core.getRenderer().getCamX()+moveX);
//        core.getRenderer().setCamY(core.getRenderer().getCamY()+moveY);


        // HERE: Generate new chunks
        if(prevCamX != core.getRenderer().getCamX() || prevCamY != core.getRenderer().getCamY()) {
//            findOnScreenObjects();
            for (int xx = chunkXTopLeft - 4; xx < chunkXBottomRight + 4; xx++) {
                for (int yy = chunkYTopLeft - 4; yy < chunkYBottomRight + 4; yy++) {
                    if (getChunkArray(xx, yy).size() == 0) {
                        generateChunk(xx, yy);
                    }
                }
            }
        }

//        findOnScreenBlocked = false;
//        if(findOnScreenCalled)
        findOnScreenObjects();

        if(!startMenuMode) {
            player.castRays();
        }


        /*
            if(!startMenuModePrev) {
                if(!music.isRunning()) music.start();
                if(noise.isRunning()) noise.stop();
            }*/
        if(startMenuModePrev != startMenuMode) {
            if(startMenuMode) {
                // HERE: Go to start menu mode
                camera.cX =(int)(Integer.MAX_VALUE/8d*7d);
                camera.cY =(int)(Integer.MAX_VALUE/8d*7d);
                chunks.clear();
                visibleChunkObjects.clear();
                entities.clear();
                if(!music.isRunning()) music.start();
                if(noise.isRunning()) noise.stop();
                music.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                // HERE: Exit start menu mode
                cursor.visible = true;
                chunks.clear();
                visibleChunkObjects.clear();
                entities.clear();
                if(music.isRunning()) music.stop();
                if(!noise.isRunning()) noise.start();
                noise.loop(Clip.LOOP_CONTINUOUSLY);
                camera.cX = player.x-width/2d;
                camera.cY = player.y-height/2d;
            }
        }
        startMenuModePrev = startMenuMode;

        if(entities.size() > 0) {
            System.out.println(entities.size());
        }
    }

    public static int toSlowMotion(int amount) {
        return (int)(amount*slowMotionSpeed* slowMotionMultiplier);
    }

    public static double toSlowMotion(double amount) {
        return amount*slowMotionSpeed* slowMotionMultiplier;
    }

    @Override
    public void render(Core core) {
        Renderer r = core.getRenderer();

        r.absolute();

        Filter passThrough = (newC, oldC) -> newC;

        r.setFilter(0, passThrough);

        r.setClip(0, 0, e.width, e.height);

        r.fillRectangle(0, 0, e.width, e.height, grassColor);

        r.relative();
        if (showDebugInfo)
            r.fillRectangle(hoverChunkX * chunkSize, hoverChunkY * chunkSize, chunkSize, chunkSize, new Color(40, 100, 70));

        // ╔════════════════════════════╗
        // ║ R E N D E R    C H U N K S ║
        // ╚════════════════════════════╝
        if (usesChunkRenderer) {
            // HERE: Render only visible chunks
            Iterator<GameObject> it1;
            for (it1 = Main.main.visibleChunkObjects.iterator(); it1.hasNext(); ) {
                it1.next().render(r);
            }
        } else {
            // HERE: Render all chunks, temporally
            chunks.forEach((loc, chunk) -> chunk.forEach(obj -> obj.render(r)));
//            flies.forEach(f -> f.render(r));
        }

        r.setFont(new Font("Press Start Regular", Font.BOLD, 16));
        r.drawText(String.valueOf((int) player.money), player.coinOverlayX, player.coinOverlayY - 32, 16,
                Alignment.MIDDLE_CENTER, new Color(40, 250, 140, (int) player.coinOverlayAlpha));

        r.absolute();

        // ╔═════════════════════════════════════╗
        // ║ R E N D E R    D E B U G    I N F O ║
        // ╚═════════════════════════════════════╝
        if (showDebugInfo) {
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
            r.drawText("Entities: " + entities.size(), 8, y, 10, Color.black);

            y += addY;
            r.drawText("PlayerX: " + player.xx, 8, y, 10, Color.black);
            y += addY;
            r.drawText("PlayerY: " + player.yy, 8, y, 10, Color.black);

            y += addY;
            r.drawText("Generated chunks: " + chunks.size(), 8, y, 10, Color.black);
            y += addY;
            r.drawText("Flies: " + flies.size(), 8, y, 10, Color.black);
            y += addY;
            r.drawText("Visible chunk objects: " + visibleChunkObjects.size(), 8, y, 10, Color.black);
        }
        r.relative();
        r.clearFilters();


        // ╔═══════════════════════════════════╗
        // ║ R E N D E R    M A I N    M E N U ║
        // ╚═══════════════════════════════════╝
        if(startMenuMode) {
            r.absolute();
            r.drawImage(0, 0, gameLogo);

            // HERE: Render buttons
            int y = 64;
            int x = 16;
            Shape clip = r.getG().getClip();
            for (int i = 0; i < numButtons; i++) {
                r.setClip(0, y, width,
                        buttonImages[i].getHeight());
                for(Object obj : buttonHoverVectors[i]) {
                    if(obj instanceof Vec3d) {
                        int w = (int)((1 - ((Vec3d) obj).z)*width*2d);
                        r.fillRectangle((int)((Vec3d) obj).x-w/2d, y, w,
                                buttonImages[i].getHeight(),
                                new Color(255, 255, 255,
                                        (int)(((Vec3d) obj).y*128d)));
                    }
                }
                r.drawImage(x, y, buttonImages[i]);
                y += 32;
            }
            r.getG().setClip(clip);
            startMenuButtonParticles.forEach(p -> p.render(r));
        }

        r.relative();
        cursor.render(r);
    }

    private class MoveTo {
        public WorldObject targetObject;
        public int fromX, fromY, toX, toY;

        public MoveTo(WorldObject targetObject, int fromX, int fromY, int toX, int toY) {
            this.targetObject = targetObject;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }
}
