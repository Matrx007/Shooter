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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.youngdev.shooter.EnemyBolt.calcColorParameter;
import static java.awt.event.KeyEvent.*;

public class Main extends Game {
    public static Main main;

    public static int chunkSize = 128;
    private int hoverChunkX, hoverChunkY;
    private static int chunkXTopLeft;
    private static int chunkYTopLeft;
    private static int chunkXBottomRight;
    private static int chunkYBottomRight;
    public static int width = 320;
    public static int height = 180;
    private int timer, time=120;
    private boolean found, findOnScreenBlocked, findOnScreenCalled;
    private boolean usesChunkRenderer;
    public boolean showDebugInfo;
    public static float slowMotionSpeed = 1f;
    public static  float slowMotionMultiplier = 1f;
    private Random random;
    public Camera camera;
    public static CollisionMap collisionMap;
    public static boolean startMenuMode = false;
    private BufferedImage gameLogo;
    private BufferedImage gameLogoMask;
    private int numButtons;
    private String[] buttonLabels;
    private int[] buttonStates;
    private BufferedImage[] buttonImages;
    private ArrayList<UniParticle> startMenuButtonParticles;

    // HERE: Server stuff
    private static boolean isServer;
    private static int port;
    private static String ip;
    private int clientId;
    private ArrayList<Socket> clients;
    private Map<Integer, Player> players;
    private Map<Integer, GameObject> objects;
    private Socket socket;
    private ServerSocket serverSocket;

    public Cursor cursor;

    public static Color grassColor = new Color(80, 140, 110);

    public Player player;

    List<GameObject> addEntities, entities, visibleChunkObjects, coins;
    private List<GameObject> structuralBlocks,visibleChunkObjectsTemp;
    List<Fly> flies;

    public static void main(String[] args) {
        if(args.length == 0) {
            isServer = false;
            port = 25664;
        } else {
            if(args.length > 1 && args[0].equals("port")) {
                port = Integer.parseInt(args[1]);
                if(port == -1) {
                    port = 25664;
                }
                isServer = true;
                if(args.length > 3 && args[2].equals("ip")) {
                    ip = args[3];
                    isServer = false;
                }
            }
        }
        new Main();
    }

    public Map<Point, CopyOnWriteArrayList<GameObject>> chunks;

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

        if(isServer) {
            clients = new ArrayList<>();
            try {
                serverSocket = new ServerSocket(25664);
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(-2);
            }
            new Thread(() -> {
                try {
                    Socket acceptedSocket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            // New thread for new client/connection
                            // HERE: Client communication logic
                            DataInputStream inputStreamReader = new DataInputStream(acceptedSocket.getInputStream());

                            while (acceptedSocket.isConnected()) {
                                int dataSize = inputStreamReader.readInt();
                                byte[] data = new byte[dataSize];
                                inputStreamReader.readFully(data);

                                switch (fetchInt(data, 0)) {
                                    case 127:
                                        // HERE: Player
                                        Player target = players.get(fetchInt(data, 4));
                                        applyChanges(target, data);
                                        break;
                                    case 128:
                                        // HERE:
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            try {
                                clients.remove(acceptedSocket);
                                acceptedSocket.close();
                            } catch (IOException ignored) {}
                        }
                    }).start();
                    clients.add(acceptedSocket);
                } catch (IOException ignored) {}
            }).start();
        } else {
            try {
                socket = new Socket("localhost", 25664);
            } catch (IOException e1) {
                e1.printStackTrace();
//                System.exit(-2);
            }
        }

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
        visibleChunkObjects = Collections.synchronizedList(new ArrayList<>());
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

        String text = "Jookse peida";

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

        buttonLabels = new String[] {
                "Ühenda serveriga",
                "Alusta server",
                "Mängi üksi",
                "Krediidid",
                "Lahku"
        };
        numButtons = 3;
        buttonImages = new BufferedImage[numButtons];

        startMenuButtonParticles = new ArrayList<>();

        for(int i = 0; i < numButtons; i++) {
            BufferedImage image = RenderUtils.createImage(96, 24);
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            g.setStroke(new BasicStroke(2f));
            g.setColor(Color.black);
            g.setFont(new Font("Nunito Bold", Font.PLAIN, 16));
            g.drawString(buttonLabels[i], 8, 16);

            buttonImages[i] = image;
        }

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
            chunk.add(new Bush(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        for(int i = 0; i < random.nextInt(3); i++) {
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

        if(random.nextInt(3)==1) {
            chunk.add(new Trash(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        if(random.nextInt(5)==3) {
            int xx = random.nextInt(chunkSize);
            int yy = random.nextInt(chunkSize);
            for(int i = 0; i < random.nextInt(18)+3; i++) {
                double angle = random.nextInt(359);
                double distance = random.nextInt(chunkSize/4)+chunkSize/4f;
                int xxx = minX + xx + (int) (Math.cos(Math.toRadians(angle))*distance);
                int yyy = minY + yy + (int) (Math.sin(Math.toRadians(angle))*distance);
                flies.add(new Fly(xxx, yyy));
//                System.out.println("Spawned a fly at ("+xxx+","+yyy+")");
            }
        }

        if(random.nextInt(6)==2) {
            entities.add(new Rabbit(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
        }

        if(random.nextInt(7)==3) {
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
        }

        if(random.nextInt(6)==1) {
            chunk.add(new Tree(minX + random.nextInt(chunkSize),
                    minY + random.nextInt(chunkSize)));
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

    public CopyOnWriteArrayList<GameObject> getAndGenerateChunk(int x, int y) {
        Point loc = new Point(x, y);
        if(!chunks.containsKey(loc)) {
            generateChunk(loc.x, loc.y);
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
        if(!startMenuMode) {
            addQueue.add(player);
            addQueue.add(player.shadowRenderer);
        }

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

                if(obj instanceof Healable){
                    if(((Healable) obj).hasCollision) {
                        collisionMap.add(obj.aabbComponent);
                    }
                }
            } else {
                iterator.remove();
            }
        }

        // HERE: Add coins
        Iterator<GameObject> iterator1 = coins.iterator();
        while(iterator1.hasNext()) {
            GameObject obj = iterator1.next();
            Point chunkLoc = getChunkLocation((int)obj.x, (int)obj.y);
            if(isOnScreen(chunkLoc.x, chunkLoc.y, 1)) {
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
        boolean startMenuModePrev = startMenuMode;

        if(startMenuMode) {
            boolean found = false;
            camera.bluishEffect = 0f;
            int x = core.getInput().getMouseX();
            int y = core.getInput().getMouseY();
            int alphaSpeed = 32;
            for(int i = 0; i < numButtons; i++) {
                int xx = 16;
                int yy = 64+32*i;
                if(AdvancedMath.inRange(x, y, xx, yy, 96, 24)) {
                    found = true;
                    for(int t0 = 0; t0 < 4; t0++) {
                        int x1 = random.nextInt(96);
                        int x2 = random.nextInt(96);

                        {
                            // HERE: Top side
                            Color color = new Color(
                                    cursor.cursorColor.getRed()+random.nextInt(20),
                                    cursor.cursorColor.getGreen()+random.nextInt(20),
                                    cursor.cursorColor.getBlue()+random.nextInt(20)
                            );
                            UniParticle.FadingProcess fadingProcess =
                                    new UniParticle.FadingProcess(255, alphaSpeed, true);
                            startMenuButtonParticles.add(new UniParticle(xx+x1, yy, random.nextBoolean() ? 2 : 4, false,
                                    color, fadingProcess));
                        }
                        {
                            // HERE: Bottom side
                            Color color = new Color(
                                    cursor.cursorColor.getRed()+random.nextInt(20),
                                    cursor.cursorColor.getGreen()+random.nextInt(20),
                                    cursor.cursorColor.getBlue()+random.nextInt(20)
                            );
                            UniParticle.FadingProcess fadingProcess =
                                    new UniParticle.FadingProcess(255, alphaSpeed, true);
                            startMenuButtonParticles.add(new UniParticle(xx+x2, yy+24, random.nextBoolean() ? 2 : 4, false,
                                    color, fadingProcess));
                        }
                    }
                    int y1 = random.nextInt(24);
                    int y2 = random.nextInt(24);
                    {
                        // HERE: Left side
                        Color color = new Color(
                                cursor.cursorColor.getRed()+random.nextInt(20),
                                cursor.cursorColor.getGreen()+random.nextInt(20),
                                cursor.cursorColor.getBlue()+random.nextInt(20)
                        );
                        UniParticle.FadingProcess fadingProcess =
                                new UniParticle.FadingProcess(255, alphaSpeed, true);
                        startMenuButtonParticles.add(new UniParticle(xx, yy+y1, random.nextBoolean() ? 2 : 4, false,
                                color, fadingProcess));
                    }
                    {
                        // HERE: Right side
                        Color color = new Color(
                                cursor.cursorColor.getRed()+random.nextInt(20),
                                cursor.cursorColor.getGreen()+random.nextInt(20),
                                cursor.cursorColor.getBlue()+random.nextInt(20)
                        );
                        UniParticle.FadingProcess fadingProcess =
                                new UniParticle.FadingProcess(255, alphaSpeed, true);
                        startMenuButtonParticles.add(new UniParticle(xx+96, yy+y2, random.nextBoolean() ? 2 : 4, false,
                                color, fadingProcess));
                    }
                }
                cursor.visible = !found;
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

        coins.removeIf(o -> {
            if(!o.dead) o.update(i);
            return o.dead;
        });

        Iterator<Fly> it = flies.iterator();
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


        if(!startMenuMode)
            player.update(core.getInput());
        Iterator<GameObject> it3;
        for(it3 = Main.main.visibleChunkObjects.iterator(); it3.hasNext();) {
            GameObject obj = it3.next();
            if(obj instanceof Bush || obj instanceof Plant || obj instanceof Trash) {
                obj.update(core.getInput());
            }
        };
        cursor.update(i);

        e.getWindow().getFrame().setTitle("Codename SHOOTER - FPS: "+e.getFps());

//        player.update(i);

        camera.update();
        camera.apply(core.getRenderer());
        chunkXTopLeft = (int)Math.floor(camera.cX/(double)chunkSize);
        chunkYTopLeft = (int)Math.floor(camera.cY/(double)chunkSize);
        chunkXBottomRight = (int)Math.floor((e.width+camera.cX)/(double)chunkSize);
        chunkYBottomRight = (int)Math.floor((e.height+camera.cY)/(double)chunkSize);

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
                    getAndGenerateChunk(k, j);
                }
            }
        }

        if(i.isKeyDown(VK_F5)) {
            // HERE: Take a "Mega" screenshot
            BufferedImage image = new BufferedImage(16384, 16384, BufferedImage.TYPE_INT_RGB);
            Renderer r = new Renderer(image.getGraphics(), image.getWidth(), image.getHeight());
            r.absolute();
            r.fillRectangle(0, 0, image.getWidth(), image.getHeight(), grassColor);

            int centerX = e.getRenderer().getCamX()+width/2;
            int centerY = e.getRenderer().getCamY()+height/2;

            int chunkX1 = (centerX-8192)/chunkSize;
            int chunkY1 = (centerY-8192)/chunkSize;
            int chunkX2 = (centerX+8192)/chunkSize;
            int chunkY2 = (centerY+8192)/chunkSize;

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

        if(i.isButtonDown(1) && showDebugInfo) {
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

        if(!startMenuMode) {
            player.castRays();
        }

        if(startMenuModePrev != startMenuMode) {
            if(startMenuMode) {
                // HERE: Go to start menu mode
                camera.cX =(int)(Integer.MAX_VALUE/8d*7d);
                camera.cY =(int)(Integer.MAX_VALUE/8d*7d);
            } else {
                // HERE: Exit start menu mode
                cursor.visible = true;
            }
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

        Filter grayScale = (newC, oldC) -> {
            int cr = newC.getRed();
            int cg = newC.getGreen();
            int cb = newC.getBlue();

            int grayScaleColor = (int) (((double) (cr + cg + cb)) / 3d);

            cr = grayScaleColor;
            cg = grayScaleColor;
            cb = grayScaleColor;

            return new Color(cr, cg, cb);
        };

        Filter passThrough = (newC, oldC) -> newC;

        Filter slowMotionOverlay = (newC, oldC) -> {
            float amount = camera.bluishEffect;
            double cr = newC.getRed() * (1 - 0.45 * amount);
            double cg = newC.getGreen() * (1 - 0.45 * amount);
            double cb = newC.getBlue() * (1 - 0.45 * amount);

            double grayScaleColor = (cr + cg + cb) / 3d;

            cr = cr * (1 - amount) + grayScaleColor * amount;
            cg = cg * (1 - amount) + grayScaleColor * amount;
            cb = cb * (1 - (amount * 0.25)) + grayScaleColor * (amount * 0.25);

            cg = Math.max(0, Math.min(255, cg));
            cr = Math.max(0, Math.min(255, cr * (1d+0.25d*amount)));
            cb = Math.max(0, Math.min(255, cb));

            return new Color((int) cr, (int) cg, (int) cb, newC.getAlpha());
        };

        r.setFilter(0, passThrough);

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
        if (showDebugInfo)
            r.fillRectangle(hoverChunkX * chunkSize, hoverChunkY * chunkSize, chunkSize, chunkSize, new Color(40, 100, 70));

        if (usesChunkRenderer) {
            // HERE: Render only visible chunks

            Iterator<GameObject> it;
            for (it = Main.main.visibleChunkObjects.iterator(); it.hasNext(); ) {
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
        if (player.blinkingON) {
            Iterator<GameObject> it;
            for (it = Main.main.visibleChunkObjects.iterator(); it.hasNext(); ) {
                GameObject obj = it.next();
                if (obj instanceof Arrow)
                    if (!((Arrow) obj).shotByFriendly)
                        if (Fly.distance(obj.x, obj.y, player.x, player.y) < 160) {
                            r.drawRectangle(obj.x - 8, obj.y - 8, 16, 16, Color.red);
                        }
            }
        }
        r.setFilter(0, backup);

        r.setFont(new Font("Press Start Regular", Font.BOLD, 16));
        r.drawText(String.valueOf((int) player.money), player.coinOverlayX, player.coinOverlayY - 32, 16,
                Alignment.MIDDLE_CENTER, new Color(40, 250, 140, (int) player.coinOverlayAlpha));


//        double mouseAngle = Fly.angle(player.x, player.y, e.getInput().getRelativeMouseX(), e.getInput().getRelativeMouseY());
        backup = r.getFilter(0);
        r.removeFilter(0);
        if (player.clipOverlayAlpha != 0) {
            double xx = e.getInput().getRelativeMouseX();
            double yy = e.getInput().getRelativeMouseY();
            double step1 = 360d / 10d;
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
            double step2 = 360d / player.maxAmmo;
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

        int xx = e.getInput().getMouseX();
        int yy = e.getInput().getMouseY();
        if (player.statsOverlayAlpha > 0) {
            int radius = 40;
            double angle = player.health / player.healthMax * 359d;

            r.setColor(new Color(200, 30, 90, (int) player.statsOverlayAlpha));

            Stroke previous = ((Graphics2D) r.getG()).getStroke();
            ((Graphics2D) r.getG()).setStroke(new BasicStroke(12f));

            r.getG().drawArc(xx - radius - r.getCamX(), yy - radius - r.getCamY(), radius * 2, radius * 2,
                    90 - (int) player.statsOverlayRotation, (int) angle);


            r.setColor(new Color(60, 200, 90, (int) player.statsOverlayAlpha));
            radius = 56;
            angle = player.hunger / player.hungerMax * 359d;
            r.getG().drawArc(xx - radius - r.getCamX(), yy - radius - r.getCamY(), radius * 2, radius * 2,
                    90 - (int) player.statsOverlayRotation, (int) angle);

            ((Graphics2D) r.getG()).setStroke(previous);
        }

        if (player.autoReloadTimer != 0) {
            double w = player.autoReloadTimer / player.autoReloadTime * 62d;
            float alpha = (float) player.autoReloadBlinkingTimer /
                    (float) player.autoReloadBlinkingTime;
            Color color = new Color(
                    calcColorParameter(128, 190, alpha),
                    calcColorParameter(128, 210, alpha),
                    calcColorParameter(128, 40, alpha)
            );

            r.fillRectangle(xx - 32, yy + player.autoReloadY, 64, 8,
                    Color.gray);

            r.fillRectangle(xx - 32 + 1, yy + player.autoReloadY + 1, (int) w, 6, color);
        }

        r.setFilter(0, backup);

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
            r.drawText("Uses Chunk Renderer: " + (usesChunkRenderer ? "true" : "false"), 8, y, 10, Color.black);

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
            y += addY;
            r.drawText("Visible chunk objects contains Player: " + visibleChunkObjects.contains(player), 8, y, 10, Color.black);
        }
        r.relative();
        r.clearFilters();

        if(startMenuMode) {
            r.absolute();
            r.drawImage(0, 0, gameLogo);

            // HERE: Render buttons
            int y = 64;
            int x = 16;
            for (int i = 0; i < numButtons; i++) {
                r.drawImage(x, y, buttonImages[i]);
                y += 32;
            }
            startMenuButtonParticles.forEach(p -> p.render(r));
        }

        r.relative();
        cursor.render(r);
    }


    // HERE: -===- Multiplayer Stuff -===-

    public byte[] convertToBytes(int... args) {
        byte[] bytes = new byte[args.length];

        int i = 0;
        for(int j : args) {

            bytes[i] = (byte) (j >> 24);
            bytes[i+1] = (byte) (j >> 16);
            bytes[i+2] = (byte) (j >> 8);
            bytes[i+3] = (byte) (j);

            i += 4;
        }

        return bytes;
    }

    public byte[] convertToByte(int a) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (a >> 24);
        bytes[1] = (byte) (a >> 16);
        bytes[2] = (byte) (a >> 8);
        bytes[3] = (byte) (a);
        return bytes;
    }

    public void applyChanges(GameObject obj, byte[] data) {
        if(obj instanceof Player) {
            obj.x = fetchInt(data,4);
            obj.y = fetchInt(data,8);
        } else if(obj instanceof Fly) {
            obj.x = fetchInt(data,4);
            obj.y = fetchInt(data,8);
        } else if(obj instanceof Tree) {
            obj.x = fetchInt(data,4);
            obj.y = fetchInt(data,8);
        } else if(obj instanceof Bush) {
            obj.x = fetchInt(data,4);
            obj.y = fetchInt(data,8);
            ((Mask.Rectangle)obj.mask).w = fetchInt(data, 12);
            ((Mask.Rectangle)obj.mask).h = fetchInt(data, 16);
        } else if(obj instanceof Rabbit) {
            obj.x = fetchInt(data,4);
            obj.y = fetchInt(data,8);
            ((Rabbit) obj).direction = fetchInt(data, 12);
            ((Rabbit) obj).directionTarget = fetchInt(data, 16);
        } else if(obj instanceof Trash) {
            if(((Trash) obj).type == Trash.TYPE_MUD ||
                    ((Trash) obj).type == Trash.TYPE_WATER) {
                obj.x = fetchInt(data, 4);
                obj.y = fetchInt(data, 8);
            } else if(((Trash) obj).type == Trash.TYPE_BRANCHES) {
                obj.x = fetchInt(data, 4);
                obj.y = fetchInt(data, 8);
                int i = 0;
                for(UniParticle particle : ((Trash) obj).particles) {
                    particle.x = fetchInt(data, 8+i*8);
                    particle.y = fetchInt(data, 8+i*8+4);
                    i += 1;
                }
            }
        }
    }

    public byte[] convertFromObject(GameObject obj) {
        byte[] bytes = null;
        if(obj instanceof Player) {
            bytes = new byte[12];
            System.arraycopy(convertToByte(clientId), 0, bytes, 0, 4);
            System.arraycopy(convertToByte((int)obj.x), 0, bytes, 4, 4);
            System.arraycopy(convertToByte((int)obj.y), 0, bytes, 8, 4);
        } else if(obj instanceof Fly) {
            bytes = new byte[12];
            System.arraycopy(convertToByte(obj.hashCode()), 0, bytes, 0, 4);
            System.arraycopy(convertToByte((int)obj.x), 0, bytes, 4, 4);
            System.arraycopy(convertToByte((int)obj.y), 0, bytes, 8, 4);
        } else if(obj instanceof Tree) {
            bytes = new byte[12];
            System.arraycopy(convertToByte(obj.hashCode()), 0, bytes, 0, 4);
            System.arraycopy(convertToByte((int)obj.x), 0, bytes, 4, 4);
            System.arraycopy(convertToByte((int)obj.y), 0, bytes, 8, 4);
        } else if(obj instanceof Bush) {
            bytes = new byte[20];
            System.arraycopy(convertToByte(obj.hashCode()), 0, bytes, 0, 4);
            System.arraycopy(convertToByte((int)obj.x), 0, bytes, 4, 4);
            System.arraycopy(convertToByte((int)obj.y), 0, bytes, 8, 4);
            System.arraycopy(convertToByte(((Mask.Rectangle)obj.mask).w), 0, bytes, 12, 4);
            System.arraycopy(convertToByte(((Mask.Rectangle)obj.mask).h), 0, bytes, 16, 4);
        } else if(obj instanceof Rabbit) {
            bytes = new byte[20];
            System.arraycopy(convertToByte(obj.hashCode()), 0, bytes, 0, 4);
            System.arraycopy(convertToByte((int)obj.x), 0, bytes, 4, 4);
            System.arraycopy(convertToByte((int)obj.y), 0, bytes, 8, 4);
            System.arraycopy(convertToByte((int)(((Rabbit) obj).direction)), 0, bytes, 12, 4);
            System.arraycopy(convertToByte((int)(((Rabbit) obj).directionTarget)), 0, bytes, 16, 4);
        } else if(obj instanceof Trash) {
            if(((Trash) obj).type == Trash.TYPE_MUD ||
                    ((Trash) obj).type == Trash.TYPE_WATER) {
                bytes = new byte[12];
                System.arraycopy(convertToByte(obj.hashCode()), 0, bytes, 0, 4);
                System.arraycopy(convertToByte((int)obj.x), 0, bytes, 4, 4);
                System.arraycopy(convertToByte((int)obj.y), 0, bytes, 8, 4);
            } else if(((Trash)obj).type == Trash.TYPE_BRANCHES) {
                bytes = new byte[12 + ((Trash) obj).particles.size() * 8];
                System.arraycopy(convertToByte(obj.hashCode()), 0, bytes, 0, 4);
                System.arraycopy(convertToByte((int) obj.x), 0, bytes, 4, 4);
                System.arraycopy(convertToByte((int) obj.y), 0, bytes, 8, 4);
                int i = 0;
                for (UniParticle particle : ((Trash) obj).particles) {
                    System.arraycopy(convertToByte(particle.x), 0, bytes, 12 + i * 8, 4);
                    System.arraycopy(convertToByte(particle.y), 0, bytes, 12 + i * 8 + 4, 4);
                    i++;
                }
            }
        }
        return bytes;
    }

    public byte[] unsign(byte[] bytes) {
        byte[] newArray = new byte[bytes.length-1];
        System.arraycopy(bytes, 0, newArray, 0, bytes.length-1);
        return newArray;
    }

    public byte[] sign(byte[] bytes) {
        byte[] newBytes = new byte[bytes.length+4];
        System.arraycopy(bytes, 0, newBytes, 4, bytes.length);
        System.arraycopy(convertToByte(bytes.length), 0, newBytes, 0, 4);
        return newBytes;
    }

    public byte[] sign(byte[] bytes, byte... args) {
        byte[] newBytes = new byte[bytes.length+args.length+4*args.length];
        System.arraycopy(args, 0, newBytes, 0, args.length);
        int i = 0;
        for(byte arg : args) {
            System.arraycopy(convertToByte(args[i]), 0, newBytes, i*4, 4);
            i++;
        }
        return newBytes;
    }

    public int fetchInt(byte[] array, int offset) {
        return toInt(array[offset], array[offset+1], array[offset+2], array[offset+3]);
    }

    public int toInt(byte b1, byte b2, byte b3, byte b4) {
        return  ((0xFF & b1) << 24) | ((0xFF & b2) << 16) |
                ((0xFF & b3) << 8) | (0xFF & b4);
    }

}
