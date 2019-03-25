package com.youngdev.shooter;

import com.engine.Core;
import com.engine.Game;
import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBCollisionManager;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.rendering.Filter;
import com.engine.libs.rendering.Image;
import com.engine.libs.rendering.RenderUtils;
import com.engine.libs.rendering.Renderer;
import com.engine.libs.world.CollisionMap;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static java.awt.event.KeyEvent.*;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class Main extends Game {
    public static Main main;

    static int chunkSize = 128;
    FloatControl noiseVolume;
    int flySoundCounter;
    int coinSoundCounter;
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
    List<GameObject> entities;
    Mask.Rectangle visibleAreaMask;
    List<GameObject> coins;
    private List<GameObject> structuralBlocks,visibleChunkObjectsTemp;
    List<Fly> flies;
    SoundManager soundManager;
    private boolean[] startMenuButtons_prevHover;
    private boolean startMenuModePrev;
    int flySounds;
    private int tick;
    private Area onScreenPuddles;
    private ArrayList<Vec3d> puddleWaves;
    UI ui;
    ArrayList<Vec2d> entitiesOnScreen;
    public static int DuleKivaHaruldus = 10;
    private Button[] mainMenuButtons;

    // *** SOUNDS ***
    private Clip noise;
    private Clip music;
    private PuddleRenderer puddleRenderer;

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

        width = 16 * size;
        height = 9 * size;

        e.width = width;
        e.height = height;
        e.scale = 3f;
//        e.startAsFullscreen = true;

        showDebugInfo = false;

        e.start();

        // HERE: Init
        e.getWindow().getFrame().setTitle("DuleKiva");

        mainMenuButtons = new Button[]{
                new Button(64, "Klassikaline"),
                new Button(96, "Muuda raskusastet"),
                new Button(128, "Lahku mängust")
        };

        e.getWindow().getFrame().setIconImage(
                new Image("/icon.png").getImage());

        playerEntities = new ArrayList<>();
        entitiesOnScreen = new ArrayList<>();
        visibleAreaMask = null;
        onScreenPuddles = new Area();
        coinSoundCounter = 0;

        startMenuMode = true;
        flySounds = 5;
        flySoundCounter = 0;

        puddleWaves = new ArrayList<>();
        puddleRenderer = new PuddleRenderer();

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
        fonts.add("Romantiques.ttf");
        fonts.add("shanghai.ttf");

        for (String name : fonts) {
            try {
                ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream("/" + name)));
            } catch (Exception ignored1) {
                try {
                    ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getResourceAsStream("/" + name)));
                } catch (Exception ignored2) {
                    try {
                        ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream(name)));
                    } catch (Exception ignored3) {
                        try {
                            ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getResourceAsStream(name)));
                        } catch (Exception ignored4) {
                        }
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
        List<GameObject> addEntities = Collections.synchronizedList(new ArrayList<>());
        coins = Collections.synchronizedList(new ArrayList<>());
        findOnScreenBlocked = false;
        findOnScreenCalled = false;

        collisionMap = new CollisionMap();
        e.getRenderer().setCamX(Integer.MAX_VALUE / 2 - e.width / 2);
        e.getRenderer().setCamY(Integer.MAX_VALUE / 2 - e.height / 2);
        player = new Player(e.getRenderer().getCamX() + e.width / 2,
                e.getRenderer().getCamY() + e.height / 2);

        camera = new Camera(e.width, e.height, player);

        Graphics2D g2d = (Graphics2D) getE().getRenderer().getG();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Container contentPane = e.getWindow().getFrame().getContentPane();
        contentPane.setCursor(contentPane.getToolkit().createCustomCursor(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                new Point(),
                null));

        // HERE: Pre-Render game logo

        // Old text based logo
        BufferedImage loadedLogo = new Image("/dulekiva.png").getImage();
        double newHeight = 64;
        double newWidth = (double) loadedLogo.getWidth() /
                (double) loadedLogo.getHeight() * newHeight;
        java.awt.Image logo = loadedLogo.getScaledInstance((int) newWidth,
                (int) newHeight, java.awt.Image.SCALE_FAST);
        gameLogo = new BufferedImage((int) newWidth, (int) newHeight,
                BufferedImage.TYPE_INT_ARGB);
        gameLogo.getGraphics().drawImage(logo, 0, 0, null);

        ui = new UI();

        music = soundManager.playSound("startMenuMusic", Clip.LOOP_CONTINUOUSLY);
        noise = soundManager.playSound("noise", Clip.LOOP_CONTINUOUSLY);
        noise.stop();
        music.loop(Clip.LOOP_CONTINUOUSLY);
        noise.loop(Clip.LOOP_CONTINUOUSLY);
        noiseVolume = (FloatControl)
                noise.getControl(FloatControl.Type.MASTER_GAIN);

        restart();

        e.run();
    }

    public void move(WorldObject obj, int fromX, int fromY,
                     int toX, int toY) {
        moveChunkEntities.offerLast(new MoveTo(obj,
                fromX, fromY, toX, toY));
    }

    private void loadSounds() {
        soundManager.addClip("sounds/backgroundMusic.wav", "startMenuMusic", -2f);
        soundManager.addClip("sounds/noiseLow.wav", "noise", 5f);
        soundManager.addClip("sounds/buttonHoverBass.wav", "buttonHover", 5f);
        soundManager.addClip("sounds/buttonPressedBass.wav", "buttonPress", 5f);
        soundManager.addClip("sounds/beeFlyingAway.wav", "flyAway", 5f);

        soundManager.addClip("sounds/footsteps/dirt1.wav", "dirt0", -0.5f);
        soundManager.addClip("sounds/footsteps/dirt2.wav", "dirt1", -0.5f);
        soundManager.addClip("sounds/footsteps/dirt3.wav", "dirt2", -0.5f);
        soundManager.addClip("sounds/footsteps/dirt4.wav", "dirt3", -0.5f);
        soundManager.addClip("sounds/footsteps/dirt5.wav", "dirt4", -0.5f);

        soundManager.addClip("sounds/footsteps/grass0.wav", "grass0", -4f);
        soundManager.addClip("sounds/footsteps/grass1.wav", "grass1", -4f);

        soundManager.addClip("sounds/bee.wav", "bee", 0f);
        soundManager.addClip("sounds/beeFlyingAway.wav", "beeFlyingAway", 2.5f);

        soundManager.addClip("sounds/tapping.wav", "branchesTouched", 0f);

        soundManager.addClip("sounds/open.wav", "open", -1f);

        soundManager.addClip("sounds/footsteps/puddle1.wav", "puddle0", 1f);
        soundManager.addClip("sounds/footsteps/puddle2.wav", "puddle1", 1f);
        soundManager.addClip("sounds/footsteps/puddle3.wav", "puddle2", 1f);
        soundManager.addClip("sounds/footsteps/puddle4.wav", "puddle3", 1f);
        soundManager.addClip("sounds/footsteps/puddle5.wav", "puddle4", 1f);

        soundManager.addClip("sounds/pickup.wav", "pickup", 6f);
        soundManager.addClip("sounds/nextWave.wav", "nextWave", 0f);
        soundManager.addClip("sounds/gameover.wav", "gameOver", 1f);
    }

    private void createSpawn() {
        if(player != null && !startMenuMode) {
            int playerLocX = getChunkLocation(
                    (int)player.x, (int)player.y).x;
            int playerLocY = getChunkLocation(
                    (int)player.x, (int)player.y).y;

            double midX = player.x;
            double midY = player.y;

            for(int i = -2; i < 3; i++) {
                for (int j = -2; j < 3; j++) {
                    int cX = playerLocX+i;
                    int cY = playerLocY+j;

                    ArrayList<GameObject> chunk = new ArrayList<>();
                    chunks.put(new Point(cX, cY), chunk);
                    if(random.nextBoolean())
                        chunk.add(new Terrain(
                                cX *chunkSize+random.nextInt(
                                        chunkSize),
                                cY *chunkSize+random.nextInt(
                                        chunkSize), random.nextBoolean() ?
                                Terrain.TYPE_DIRT_PATCH :
                                Terrain.TYPE_SMALL_ROCKS));
                }
            }


            getAndGenerateChunk(playerLocX, playerLocY).
                    add(new TutorialDialog(player.x, player.y));
        }

        /*getChunkArray(cX, cY).removeIf((o) ->
                            o instanceof Tree ||
                            o instanceof Rabbit ||
                            o instanceof DuleKiva ||
                            o instanceof Puddle);*/
    }

    public void deleteChunk(int x, int y) {
        chunks.remove(new Point(x, y));
    }

    private void generateChunk(int x, int y) {
        ArrayList<GameObject> chunk = new ArrayList<>();
        HashSet<WorldObject> chunkEntities = new HashSet<>();

        SplittableRandom random = new SplittableRandom();

        int minX = x*chunkSize;
        int minY = y*chunkSize;

        if(DuleKivaHaruldus == 0 ||
                random.nextInt(DuleKivaHaruldus) == 0) {
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
            chunk.add(new Puddle(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY, random.nextInt(2)+2));
        }

        if(random.nextInt(8)==1) {
            chunk.add(new Branches(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        if(random.nextInt(8)==1) {
//            chunkEntities.add(new FlyGroup(minX+chunkSize/2,
//                    minY+chunkSize/2,
//                    random.nextInt(15)+10));
            int xx = random.nextInt(chunkSize);
            int yy = random.nextInt(chunkSize);
            for(int i = 0; i < random.nextInt(15)+15; i++) {
                double angle = random.nextInt(359);
                double distance = random.nextInt(chunkSize/4)+chunkSize/4f;
                int xxx = minX + xx + (int) (Math.cos(Math.toRadians(angle))*distance);
                int yyy = minY + yy + (int) (Math.sin(Math.toRadians(angle))*distance);
                flies.add(new Fly(xxx, yyy));
//                System.out.println("Spawned a fly at ("+xxx+","+yyy+")");
            }
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

    public HashSet<WorldObject> getChunkEntitiesArray(int x, int y) {
        Point loc = new Point(x, y);
        if(!chunkEntities.containsKey(loc)) {
            chunkEntities.put(loc, new HashSet<>());
        }
        return chunkEntities.get(loc);
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
        getChunkEntitiesArray(cX, cY).add(entity);
    }

    void findOnScreenObjects() {
        int chunkXTopLeft = Math.floorDiv(e.getRenderer().getCamX(), chunkSize);
        int chunkYTopLeft = Math.floorDiv(e.getRenderer().getCamY(), chunkSize);
        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        int chunkXBottomRight = (int)Math.ceil(chunkXTopLeft +e.width/ chunkSize);
        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        int chunkYBottomRight = (int)Math.ceil(chunkYTopLeft +e.height/ chunkSize);
        /*
        Should work aswell
        int chunkXTopLeft = (int)Math.floor(camera.cX/(double)chunkSize);
        int chunkYTopLeft = (int)Math.floor(camera.cY/(double)chunkSize);
        int chunkXBottomRight = (int)Math.ceil((e.width+camera.cX)/(double)chunkSize);
        int chunkYBottomRight = (int)Math.ceil((e.height+camera.cY)/(double)chunkSize);
        */

        Mask.Rectangle visibleAreaMask_BufferZone = new Mask.Rectangle(
                e.getRenderer().getCamX()-chunkSize,
                e.getRenderer().getCamY()-chunkSize,
                e.getRenderer().getCamX()+e.width+chunkSize,
                e.getRenderer().getCamY()+e.height+chunkSize);

        collisionMap.empty();
        ArrayList<GameObject> visibleChunksObjectsTemporary = new ArrayList<>();
        ArrayList<GameObject> addQueue = new ArrayList<>();
        visibleChunkEntities.clear();
        entitiesOnScreen.clear();
        if(!startMenuMode) {
            addQueue.add(player);
        }
//        addQueue.add(puddleRenderer);

        // HERE: Add additional ones that are partly on screen
        for(int xx = chunkXTopLeft-4; xx < chunkXBottomRight+4; xx++) {
            for(int yy = chunkYTopLeft-4; yy < chunkYBottomRight+4; yy++) {
                ArrayList<GameObject> chunk = getChunkArray(xx, yy);
                GameObject obj;
                for(int j = chunk.size()-1; j >= 0; j--) {
                    obj = chunk.get(j);
                    if(obj.mask instanceof Mask.Rectangle &&
                            visibleAreaMask.isColliding(obj.mask)) {
                        addQueue.add(obj);
                    }
                    if(obj.aabbComponent != null) {
                        if(obj.aabbComponent.area instanceof Mask.Rectangle &&
                                obj.aabbComponent.area.isColliding(
                                visibleAreaMask_BufferZone)) {
                            collisionMap.add(obj.aabbComponent);
                        }
                    }
                    /*if((obj.mask instanceof Mask.Rectangle &&
                            visibleAreaMask.isColliding(obj.mask)) ||
                            (obj.aabbComponent != null &&
                            obj.aabbComponent.area instanceof
                            Mask.Rectangle && visibleAreaMask.
                            isColliding(obj.aabbComponent.area))) {
                        addQueue.add(obj);
                        if(obj.aabbComponent != null) {
                            collisionMap.add(obj.aabbComponent);
                        }
                    }*/
                }
            }
        }
        for(int xx = chunkXTopLeft-4; xx < chunkXBottomRight+4; xx++) {
            for(int yy = chunkYTopLeft-4; yy < chunkYBottomRight+4; yy++) {
                HashSet<WorldObject> chunk = chunkEntities.
                        get(new Point(xx, yy));
                if(chunk != null) chunk.removeIf(obj -> {
                    if((obj.mask instanceof Mask.Rectangle &&
                            visibleAreaMask.isColliding(obj.mask)) ||
                            (obj.aabbComponent != null &&
                            obj.aabbComponent.area instanceof
                            Mask.Rectangle && obj.aabbComponent.area.
                                    isColliding(visibleAreaMask))) {
                        addQueue.add(obj);
                        visibleChunkEntities.add(obj);
                        entitiesOnScreen.add(new Vec2d(obj.x, obj.y));
                    }
                    return obj.dead;
                });
            }
        }

        Iterator<GameObject> iterator = coins.iterator();
        for (;iterator.hasNext();) {
            GameObject obj = iterator.next();
            Point chunkLoc = getChunkLocation((int) obj.x, (int) obj.y);
            if (visibleAreaMask.isColliding((int)obj.x, (int)obj.y)) {
                addQueue.add(obj);
//                if(obj instanceof WorldObject)
//                    visibleChunkEntities.add((WorldObject) obj);
            } else {
                iterator.remove();
            }
        }

        structuralBlocks.forEach(o -> {
            if(o.mask instanceof Mask.Rectangle &&
                    o.mask.isColliding(visibleAreaMask)) {
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

    public void createWave(double x, double y) {
        puddleWaves.add(new Vec3d(x, y, 0));
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void update(Core core) {
        Input i = core.getInput();

        SpeedController.resetMultipliers();

        tick++;
        if(tick > 59) {
            tick = 0;
            flySoundCounter = 0;
        }

        if(startMenuMode && !ui.settings) {
            for (Button button : mainMenuButtons) {
                button.update(i);
            }

            if(mainMenuButtons[0].pressed) {
                DuleKiva.SpawnSpeed = 120d;
                Player.MaxSpeed = 3f;
                Player.MaxHealth = 5;
                Main.DuleKivaHaruldus = 10;
                ui.speedMultiplier = 1f;
                SpeedController.resetMultipliers();
                startMenuMode = false;
            }

            if(mainMenuButtons[1].pressed) {
                ui.settings = true;
            }

            if(mainMenuButtons[2].pressed) {
                System.exit(0);
            }
        } else {
            ui.update(i);
        }

        // ### Calculate visible chunks' boundaries ###
        chunkXTopLeft = (int)Math.floor((int)camera.cX/ chunkSize);
        chunkYTopLeft = (int)Math.floor((int)camera.cY/ chunkSize);
        chunkXBottomRight = (int)Math.ceil((int)(e.width+camera.cX)/ chunkSize);
        chunkYBottomRight = (int)Math.ceil((int)(e.height+camera.cY)/ chunkSize);

        // ### Create mask, generate & load terrain ###
        visibleAreaMask = new Mask.Rectangle(
                e.getRenderer().getCamX(),
                e.getRenderer().getCamY(),
                e.getRenderer().getCamX()+e.width,
                e.getRenderer().getCamY()+e.height);
        for (int xx = chunkXTopLeft - 4; xx < chunkXBottomRight + 4; xx++) {
            for (int yy = chunkYTopLeft - 4; yy < chunkYBottomRight + 4; yy++) {
                if (!chunks.containsKey(new Point(xx, yy))) {
                    generateChunk(xx, yy);
                }
            }
        }
        findOnScreenObjects();

        // ### Update world ###
        while(!moveChunkEntities.isEmpty()) {
            MoveTo move = moveChunkEntities.pollFirst();
            if(move != null) {
                getChunkEntitiesArray(move.fromX, move.fromY).
                        remove(move.targetObject);
                getChunkEntitiesArray(move.toX, move.toY).
                        add(move.targetObject);
            }
        }

        coins.removeIf(o -> {
            if(!o.dead) o.update(i);
            return o.dead;
        });

        Iterator<Player> it3 = playerEntities.iterator();
        while(it3.hasNext()) {
            Player player = it3.next();
            if(player.dead) {
                it3.remove();
            } else {
                player.update(core.getInput());
            }
        }

        puddleWaves.removeIf(wave -> {
            wave.z += 0.0125;
            return wave.z > 1;
        });

//        onScreenPuddles.reset();
        if(!startMenuMode)
            player.update(core.getInput());

        // ### Update Objects ###
        Iterator<GameObject> it4;
        for(it4 = Main.main.visibleChunkObjects.iterator(); it4.hasNext();) {
            GameObject obj = it4.next();
            if(obj == player) continue;
            if(obj instanceof WorldObject) {
                if(obj instanceof Puddle) {
                    obj.update(core.getInput());
                    /*Area puddleArea = ((Puddle) obj).area;
                    Rectangle bounds = puddleArea.getBounds();
                    if(new Mask.Rectangle(bounds.getX(),
                            bounds.getY(), (int)bounds.getWidth()+1,
                            (int)bounds.getHeight()+1).isColliding(
                                    visibleAreaMask)) {
                        onScreenPuddles.add(puddleArea);
                    }*/ // Puddle effects
                } else if (!(obj instanceof Tree) && !(obj instanceof Rocks)) {
                    double prevX = obj.x;
                    double prevY = obj.y;
                    obj.update(core.getInput());
                    ((WorldObject) obj).checkLocation(prevX, prevY);
                }
            }
        }

        // ### Find entities on puddles ###
        /*Iterator<WorldObject> it5;
        for(it5 = Main.main.visibleChunkEntities.iterator(); it5.hasNext();) {
            WorldObject obj = it5.next();
            if(obj instanceof Rabbit || obj instanceof Coin) {
                if(onScreenPuddles.contains(obj.x, obj.y)) {
                    createWave(obj.x, obj.y);
                }
            }
        }*/

        cursor.update(i);
        camera.update();
        camera.apply(core.getRenderer());


        if(i.isKeyDown(VK_F2)) {
            showDebugInfo = !showDebugInfo;
        }

        if(i.isKeyDown(VK_F5)) {
            takeMegaScreenshot();
        }

        if(i.isKeyDown(VK_F3) && showDebugInfo) {
            usesChunkRenderer = !usesChunkRenderer;
        }

        hoverChunkX=i.getRelativeMouseX()/chunkSize;
        hoverChunkY=i.getRelativeMouseY()/chunkSize;

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
                camera.bitCrushEffect = 0.5f;
                cursor.visible = true;
                chunks.clear();
                visibleChunkObjects.clear();
                entities.clear();
                if(music.isRunning()) music.stop();
                if(!noise.isRunning()) noise.start();
                noise.loop(Clip.LOOP_CONTINUOUSLY);
                camera.cX = player.x-width/2d;
                camera.cY = player.y-height/2d;
//                restart();
                createSpawn();
            }
        }
        startMenuModePrev = startMenuMode;
    }

    private void takeMegaScreenshot() {
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

    public static int toSlowMotion(int amount) {
        return (int)(amount*SpeedController.calcSpeed());
    }

    public static double toSlowMotion(double amount) {
        return amount*SpeedController.calcSpeed();
    }

    @Override
    public void render(Core core) {
        Renderer r = core.getRenderer();

        r.absolute();

        Filter duleKivaEffect = (newC, oldC) -> {
            if (camera.bluishEffect == 1f &&
                    camera.bitCrushEffect == 0f &&
                    camera.blackAndWhiteEffect == 0f) return newC;

            Color result = newC;

            if (camera.bluishEffect < 1f) {
                double resultRed;
                double resultGrn;
                double resultBlu;

                double newRed = newC.getRed() / 255d;
                double newGrn = newC.getGreen() / 255d;
                double newBlu = newC.getBlue() / 255d;

                resultRed = AdvancedMath.setRange(
                        newRed * camera.bluishEffect, 0d, 1d);
                resultGrn = AdvancedMath.setRange(
                        newGrn * camera.bluishEffect, 0d, 1d);
                resultBlu = AdvancedMath.setRange(
                        newBlu, 0d, 1d);

                result = new Color((int) (resultRed * 255d),
                        (int) (resultGrn * 255d),
                        (int) (resultBlu * 255d), newC.getAlpha());
            }

            if ((int) (camera.bitCrushEffect * 255d) > 0) {
                int divider = (int) (camera.bitCrushEffect * 255d);
                double newRed = (newC.getRed() / divider);
                double newGrn = (newC.getGreen() / divider);
                double newBlu = (newC.getBlue() / divider);

                result = new Color((int) (newRed * divider * (1 - camera.bitCrushEffect)),
                        (int) (newGrn * divider * (1 - camera.bitCrushEffect)),
                        (int) (newBlu * divider * (1 - camera.bitCrushEffect)), newC.getAlpha());
            }

            if (camera.blackAndWhiteEffect > 0) {
                double red = result.getRed();
                double grn = result.getGreen();
                double blu = result.getBlue();
                double blcAndWhtRslt = (red + grn + blu) / 3d;

                double oldRed = result.getRed();
                double oldGrn = result.getGreen();
                double oldBlu = result.getBlue();

                double fxInverted = 1d - camera.blackAndWhiteEffect;
                double fx = camera.blackAndWhiteEffect;

                int rsltRed = (int) StrictMath.round((fxInverted * oldRed) +
                        StrictMath.min(fx * blcAndWhtRslt * 0.9d, 255d));
                int rsltGrn = (int) StrictMath.round((fxInverted * oldGrn) +
                        StrictMath.min(fx * blcAndWhtRslt, 255d));
                int rsltBlu = (int) StrictMath.round((fxInverted * oldBlu) +
                        StrictMath.min(fx * blcAndWhtRslt * 0.8d, 255d));

                result = new Color(rsltRed, rsltGrn, rsltBlu, newC.getAlpha());
            }

            return result;
        };

        r.setFilter(0, duleKivaEffect);

        r.setClip(0, 0, e.width, e.height);

        r.fillRectangle(0, 0, e.width, e.height, grassColor);

        r.relative();
        if (showDebugInfo)
            r.fillRectangle(hoverChunkX * chunkSize, hoverChunkY * chunkSize, chunkSize, chunkSize, new Color(40, 100, 70));

        // ╔════════════════════════════╗
        // ║ R E N D E R    C H U N K S ║
        // ╚════════════════════════════╝
        long startingTime = System.nanoTime();
        if (usesChunkRenderer) {
            // HERE: Render only visible chunks
            GameObject obj;
            Mask.Rectangle screen = new Mask.Rectangle(
                    r.getCamX(), r.getCamY(), e.width, e.height);
            int size = visibleChunkObjects.size();
            for (int i = 0; i < size; i++) {
                obj = visibleChunkObjects.get(i);
                if(obj.mask instanceof Mask.Rectangle &&
                        obj.mask.isColliding(screen)) obj.render(r);
            }
//            Iterator<GameObject> it1;
//            for (it1 = Main.main.visibleChunkObjects.iterator(); it1.hasNext(); ) {
//                it1.next().render(r);
//            }
        } else {
            // HERE: Render all chunks, temporally
            chunks.forEach((loc, chunk) -> chunk.forEach(obj -> obj.render(r)));
//            flies.forEach(f -> f.render(r));
        }
//        System.out.println(System.nanoTime()-startingTime);

//        r.setFont(new Font("Press Start Regular", Font.BOLD, 16));
//        r.drawText(String.valueOf((int) player.money), player.coinOverlayX, player.coinOverlayY - 32, 16,
//                Alignment.MIDDLE_CENTER, new Color(40, 250, 140, (int) player.coinOverlayAlpha));

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
            y += addY;
            r.drawText("Game Speed: " + SpeedController.calcSpeed()+
                    " ("+SpeedController.getSpeed()+" x "+
                    SpeedController.getMultiplier()+")",
                    8, y, 10, Color.black);
            y += addY;
            r.drawText("Visible chunk objects: " + visibleChunkObjects.size(), 8, y, 10, Color.black);
        }
        r.relative();
        r.clearFilters();
//        r.setFilter(0, duleKivaEffect);


        // ╔═══════════════════════════════════╗
        // ║ R E N D E R    M A I N    M E N U ║
        // ╚═══════════════════════════════════╝
        if(startMenuMode && !ui.settings) {
            r.absolute();
            r.drawImage((e.width-gameLogo.getWidth())/2d,
                    0, gameLogo);

            // HERE: Render buttons
            for (Button button : mainMenuButtons) {
                button.render(r);
            }
        } else {
            r.absolute();
            ui.render(r);
        }

        r.relative();
        cursor.render(r);

    }

    public void restart() {
        chunks.clear();
        chunkEntities.clear();
        moveChunkEntities.clear();
        visibleChunkEntities.clear();
        visibleChunkObjects.clear();
//        player = new Player(e.getRenderer().getCamX()+e.width/2,
//                e.getRenderer().getCamY()+e.height/2);
        player = new Player(0xffff, 0xffff);
        camera.target = player;
        camera.bitCrushEffect = 0f;
        random.setSeed(random.nextLong());
        camera.blackAndWhiteEffect = 0f;
        SpeedController.resetMultipliers();
        SpeedController.setSpeed(1f);
        camera.bluishEffect = 1f;
        createSpawn();
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
    private class PuddleRenderer extends WorldObject {
        private Color color;
        public PuddleRenderer() {
            super(100, 0, 100);
            this.depth = 2048;
            color = new Color(150, 150, 230);
            color = new Color(
                    UniParticle.calcColorParameter(Main.grassColor.getRed(), color.getRed(), 0.25f),
                    UniParticle.calcColorParameter(Main.grassColor.getGreen(), color.getGreen(), 0.25f),
                    UniParticle.calcColorParameter(Main.grassColor.getBlue(), color.getBlue(), 0.25f)
            );
        }

        @Override
        public void update(Input input) {

        }

        @Override
        public void render(Renderer r) {
            // Backup and set new properties
            Graphics2D g2d = (Graphics2D)r.getG();
            Shape oldClip = g2d.getClip();
            g2d.translate(-r.getCamX(), -r.getCamY());
            g2d.setClip(onScreenPuddles);
            g2d.translate(r.getCamX(), r.getCamY());

            // Draw waves
            puddleWaves.forEach(wave -> {
                Color waveColor = Color.white;
//            Color waveColor = new Color(192, 192, 255);
                r.fillCircle(wave.x, wave.y, (int)(wave.z*96), new Color(
                        UniParticle.calcColorParameter(color.getRed(), waveColor.getRed(), 0.1f*(1f-(float) wave.z)),
                        UniParticle.calcColorParameter(color.getGreen(), waveColor.getGreen(), 0.1f*(1f-(float) wave.z)),
                        UniParticle.calcColorParameter(color.getBlue(), waveColor.getBlue(), 0.1f*(1f-(float) wave.z))
                ));
            });

            // Restore the previous properties
            g2d.setClip(oldClip);
        }
    }
}
