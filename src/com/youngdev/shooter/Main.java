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
import com.youngdev.shooter.gamemodes.GameMode;
import com.youngdev.shooter.gamemodes.hideandseek.HideAndSeek;
import com.youngdev.shooter.modules.Module;
import com.youngdev.shooter.modules.premade.WorldReceiver;
import com.youngdev.shooter.modules.premade.WorldSender;
import com.youngdev.shooter.multiPlayerManagement.WorldObject;
import com.youngdev.shooter.multiPlayerManagement.WorldObjectData;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.youngdev.shooter.EnemyBolt.calcColorParameter;
import static java.awt.event.KeyEvent.*;

public class Main extends Game {
    public static Main main;

    public static int chunkSize = 128;
    private DataInputStream inputStreamReader;
    private DataOutputStream outputStreamWriter;
    private int hoverChunkX, hoverChunkY;
    private static int chunkXTopLeft;
    private static int chunkYTopLeft;
    private static int chunkXBottomRight;
    private static int chunkYBottomRight;
    static int width = 320;
    static int height = 180;
    private int timer, time=120;
    private boolean found, findOnScreenBlocked, findOnScreenCalled;
    private boolean usesChunkRenderer;
    boolean showDebugInfo;
    static float slowMotionSpeed = 1f;
    private static  float slowMotionMultiplier = 1f;
    private Random random;
    public Camera camera;
    static CollisionMap collisionMap;
    static boolean startMenuMode = false;
    private BufferedImage gameLogo;
    private BufferedImage gameLogoMask;
    private int numButtons;
    private String[] buttonLabels;
    private int[] buttonStates;
    private BufferedImage[] buttonImages;
    private ArrayList<UniParticle> startMenuButtonParticles;
    public ArrayList<Player> playerEntities;

    // HERE: Server stuff
    private boolean movingStopped;
    private Module[] modulesServerSide;
    private Module[] modulesClientSide;
    public static boolean isServer, isClient, isLocal;
    private static int port;
    public static int clientId;
    private static String ip;
    private ArrayList<Socket> clients;
    private Map<Integer, DataOutputStream> clientStreams;
    public Map<Integer, Player> players;
    public Map<Integer, HashSet<GameObject>> objects;
    public Map<Integer, UserData> data;
    public Map<Integer, ConcurrentLinkedDeque<byte[]>> packetSendQueue_serverSide;
    public Map<Integer, ConcurrentLinkedDeque<byte[]>> packetReceiveQueue_serverSide;
    public ConcurrentLinkedDeque<byte[]> packetSendQueue_clientSide;
    public ConcurrentLinkedDeque<byte[]> packetReceiveQueue_clientSide;
    private Socket socket;
    private ServerSocket serverSocket;
    private GameMode currentGameMode;
    private boolean acceptingClients;
    public boolean waitingForPlayerReference;
    private Deque<Integer> stats_sendQueueLengthValues;
    private Deque<Integer> stats_sendQueueBytesValues;

    private Cursor cursor;

    static Color grassColor = new Color(80, 140, 110);

    public Player player;

    public List<GameObject> addEntities;
    public List<GameObject> entities;
    protected List<GameObject> visibleChunkObjects;
    List<GameObject> coins;
    private List<GameObject> structuralBlocks,visibleChunkObjectsTemp;
    public List<Fly> flies;

    public static void main(String[] args) {
        startMenuMode = false;
        isClient = false;
        isLocal = true;
        new Main();
        System.exit(-2);
        isClient = false;
        port = 12884;
        ip = null;
        if(args.length > 2) {
            isServer = args[0].equals("server");
            isClient = args[0].equals("client");
            isLocal = args[0].equals("local");
            switch (args[1]) {
                case "port":
                    port = Integer.parseInt(args[2]);
                    break;
                case "ip":
                    ip = args[2];
                    break;
            }
            if(args.length > 4) {
                switch (args[3]) {
                    case "port":
                        port = Integer.parseInt(args[4]);
                        break;
                    case "ip":
                        ip = args[4];
                        break;
                }
            }
        } else {
            System.exit(-1);
        }
        new Main();
    }

    private Map<Point, CopyOnWriteArrayList<GameObject>> chunks;

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
        acceptingClients = false;
        waitingForPlayerReference = true;

        e.start();

        // HERE: Init
        playerEntities = new ArrayList<>();

        currentGameMode = new HideAndSeek();

        modulesClientSide = new Module[]{
                new WorldReceiver(this)
        };
        modulesServerSide = new Module[]{
                new WorldSender(this)
        };

//        startMenuMode = true;
        if(isServer) {
            acceptingClients = true;
            clients = new ArrayList<>();
            clientStreams = new HashMap<>();
            players = new HashMap<>();
            data = new HashMap<>();
            objects = new HashMap<>();
            packetSendQueue_serverSide = new HashMap<>();
            packetReceiveQueue_serverSide = new HashMap<>();
            try {
                InetAddress address = InetAddress.getByName(ip);
                serverSocket = new ServerSocket(port, 50, address);
                System.out.println("ServerSocket created");
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(-2);
            }
            startMenuMode = false;
            new Thread(() -> {
                try {
                    while(acceptingClients) {
                        System.out.println("Waiting for connection...");
                        Socket acceptedSocket = serverSocket.accept();
                        // --- New for new client/connection ---

                        // HERE: Client communication logic
                        int id = clients.size();
                        System.out.println("Connection established! ["+id+"]");
                        DataInputStream inputStreamReader = new
                                DataInputStream(acceptedSocket.getInputStream());
                        DataOutputStream outputStreamWriter = new
                                DataOutputStream(acceptedSocket.getOutputStream());

                        // HERE: Finally add client to clients list
                        packetReceiveQueue_serverSide.put(id, new ConcurrentLinkedDeque<>());
                        packetSendQueue_serverSide.put(id, new ConcurrentLinkedDeque<>());
                        clients.add(clientId, acceptedSocket);
                        System.out.println("Client added");
                        objects.put(id, new HashSet<>());
                        UserData data = new UserData();
                        this.data.put(id, data);
                        Point startPoint = currentGameMode.getPlayerJoinPosition(data);
                        players.put(id, new Player(startPoint.x, startPoint.y));
                        data.player = players.get(id);
                        data.clientId = id;
                        clientStreams.put(id, outputStreamWriter);

                        new Thread(() -> {
                            try {
                                while(this.data.size() > id) {
                                    byte[] toWrite = packetSendQueue_serverSide.get(id).pollLast();
                                    if(toWrite != null) {
                                        outputStreamWriter.writeInt(toWrite.length);
//                                        System.out.println(">"+Arrays.toString(toWrite));
                                        outputStreamWriter.write(toWrite);
                                    }
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                disconnectClient(id);
                            }
                            disconnectClient(id);
                        }).start();

                        new Thread(() -> {
                            while(players.containsKey(id)) {
                                try {
                                    byte[] receivedBytes = new byte[
                                            inputStreamReader.readInt()];
                                    if(receivedBytes.length > 0) {
                                        inputStreamReader.readFully(receivedBytes);
                                        packetReceiveQueue_serverSide.get(id).offerFirst(receivedBytes);
                                    }
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    disconnectClient(id);
                                }
                            }
                            disconnectClient(id);
                        }).start();
                    }
                } catch (IOException ignored) {}
            }).start();
        }

         if (isClient) {
             // HERE: Is client
             packetReceiveQueue_clientSide = new ConcurrentLinkedDeque<>();
             packetSendQueue_clientSide = new ConcurrentLinkedDeque<>();
             clientId = -1;

             new Thread(() -> {
                 try {
                     socket = new Socket(ip, port);
                     System.out.println("Connection established!");
                     startMenuMode = false;

                     inputStreamReader =
                             new DataInputStream(socket.getInputStream());
                     outputStreamWriter =
                             new DataOutputStream(socket.getOutputStream());

                     new Thread(() -> {
                         try {
                             while(true) {
                                 byte[] toWrite = packetSendQueue_clientSide.pollLast();
                                 if (toWrite != null) {
                                     outputStreamWriter.writeInt(toWrite.length);
                                     outputStreamWriter.write(toWrite);
                                 }
                             }
                         } catch (IOException e1) {
                             // TODO: Disconnect
                             e1.printStackTrace();
                         }
                     }).start();

                     new Thread(() -> {
                         try {
                             while(true) {
                                 byte[] receivedBytes = new byte[
                                         inputStreamReader.readInt()];
                                 if (receivedBytes.length > 0) {
                                     inputStreamReader.readFully(receivedBytes);
                                     packetReceiveQueue_clientSide.addFirst(receivedBytes);
                                 }
                             }
                         } catch (IOException e1) {
                             // TODO: Disconnect
                             e1.printStackTrace();
                         }
                     }).start();
                 } catch (IOException e1) {
                     e1.printStackTrace();
//                System.exit(-2);
                 }
             }).start();
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

        stats_sendQueueLengthValues = new ArrayDeque<>();

        stats_sendQueueBytesValues = new ArrayDeque<>();

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
            BufferedImage image = RenderUtils.createImage(192, 24);
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

    private void generateChunk(int x, int y) {
//        System.out.println("cX:"+x);
//        System.out.println("cY:"+y);
//        System.out.println("mX:"+currentGameMode.getMiddleX());
//        System.out.println("mY:"+currentGameMode.getMiddleY());
        /*if(!AdvancedMath.inRange(x, y,
                currentGameMode.getMiddleX()-
                currentGameMode.getWorldWidth()/2,
                currentGameMode.getMiddleY()-
                currentGameMode.getWorldHeight()/2,
                currentGameMode.getWorldWidth(),
                currentGameMode.getWorldHeight())) return;*/
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

        if(random.nextInt(4)==1) {
            chunk.add(new Trash(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY, random.nextInt(2)+2));
        }

        if(random.nextInt(4)==1) {
            chunk.add(new Branches(random.nextInt(chunkSize)+minX, random.nextInt(chunkSize)+minY));
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
                    minY + random.nextInt(chunkSize), random.nextBoolean() ?
                    Tree.TYPE_SAVANNA : Tree.TYPE_OAK));
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
        if(!chunks.containsKey(loc) && (isServer || startMenuMode)) {
            generateChunk(loc.x, loc.y);
        }
        CopyOnWriteArrayList<GameObject> chunk = chunks.get(loc);
        return chunk == null ? new CopyOnWriteArrayList<>() :
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

    void findOnScreenObjects() {
        findOnScreenCalled = true;
        if(findOnScreenBlocked) return;

        collisionMap.empty();
        ArrayList<GameObject> visibleChunksObjectsTemporary = new ArrayList<>();
        ArrayList<GameObject> addQueue = new ArrayList<>();
        if(!startMenuMode) {
            if(isLocal) {
                addQueue.add(player);
                addQueue.add(player.shadowRenderer);
            } else {
                addQueue.add(player.shadowRenderer);
            }
        }

        // HERE: Add other stuff
        if(!isServer)
            addQueue.addAll(playerEntities);
        else
            addQueue.addAll(players.values());

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

    public ArrayList<GameObject> findOnScreenObjects(int x1, int y1, int x2, int y2) {
        ArrayList<GameObject> queue = new ArrayList<>();

        // HERE: Add other stuff
        if(!isServer)
            queue.addAll(playerEntities);
        else
            queue.addAll(players.values());

        // HERE: Add chunks that are inside screen to visibleChunkObjects list
        for(int xx = x1; xx < x2; xx++) {
            for(int yy = y1; yy < y2; yy++) {
                queue.addAll(getChunkArray(xx, yy));
            }
        }

        // HERE: Add additional ones that are partly on screen
        Mask.Rectangle visibleAreaMask = new Mask.Rectangle(
                x1*chunkSize, y1*chunkSize,
                x2*chunkSize+chunkSize, y2*chunkSize+chunkSize
        );
        for(int xx = x1-2; xx < x2; xx++) {
            for(int yy = y1-2; yy < y2; yy++) {
                if(xx > x1 && yy > y1) {
                    continue;
                }

                getChunkArray(xx, yy).forEach(obj -> {
                    if(obj.mask.isColliding(visibleAreaMask)) {
                        queue.add(obj);
                    }
                });
            }
        }

        // HERE: Also add entities
        Iterator<GameObject> iterator = entities.iterator();
        while(iterator.hasNext()) {
            GameObject obj = iterator.next();
            Point chunkLoc = getChunkLocation((int)obj.x, (int)obj.y);
            if(isOnScreen(x1, y1, x2, y2, chunkLoc.x, chunkLoc.y, 2)) {
                queue.add(obj);
            }
        }

        // HERE: Add coins
        Iterator<GameObject> iterator1 = coins.iterator();
        while(iterator1.hasNext()) {
            GameObject obj = iterator1.next();
            Point chunkLoc = getChunkLocation((int)obj.x, (int)obj.y);
            if(isOnScreen(x1, y1, x2, y2, chunkLoc.x, chunkLoc.y, 1)) {
                queue.add(obj);
            }
        }


        structuralBlocks.forEach(o -> {
            if(o.mask.isColliding(visibleAreaMask)) {
                queue.add(o);
            }
        });
        flies.forEach(f -> {
            if(isPixelOnScreen(x1, y1, x2, y2, (int)f.x, (int)f.y)) {
                queue.add(f);
            }
        });
        return queue;
    }

    private CollisionMap findOnScreenCollisions(int x1, int y1, int x2, int y2) {
        CollisionMap result = new CollisionMap();

        // HERE: Add chunks that are inside screen to visibleChunkObjects list
        for(int xx = x1; xx < x2; xx++) {
            for(int yy = y1; yy < y2; yy++) {
                getChunkArray(xx, yy).forEach(c -> {
                    if(c.aabbComponent!=null) {
                        result.add(c.aabbComponent);
                    }
                });
            }
        }

        // HERE: Add additional ones that are partly on screen
        Mask.Rectangle visibleAreaMask = new Mask.Rectangle(
                x1*chunkSize, y1*chunkSize,
                x2*chunkSize+chunkSize, y2*chunkSize+chunkSize
        );
        for(int xx = x1-2; xx < x2; xx++) {
            for(int yy = y1-2; yy < y2; yy++) {
                if(xx > x1 && yy > y1) {
                    continue;
                }

                getChunkArray(xx, yy).forEach(o -> {
                    if(o.mask.isColliding(visibleAreaMask)) {
                        if (o.aabbComponent != null) {
                            result.add(o.aabbComponent);
                        }
                    }
                });
            }
        }

        structuralBlocks.forEach(o -> {
            if(o.mask.isColliding(visibleAreaMask)) {
                if (o.aabbComponent != null) {
                    result.add(o.aabbComponent);
                }
            }
        });

        result.refresh();

        return result;
    }

    public static Point getChunkLocation(int x, int y) {
        return new Point(x/chunkSize, y/chunkSize);
    }

    @Override
    public void update(Core core) {
        Input i = core.getInput();
        boolean startMenuModePrev = startMenuMode;

        if(startMenuMode) {
            boolean found = false;
            camera.bluishEffect = 0f;
            int x = core.getInput().getMouseX();
            int y = core.getInput().getMouseY();
            int alphaSpeed = 32;
            for(int k = 0; k < numButtons; k++) {
                int xx = 16;
                int yy = 64+32*k;
                if(AdvancedMath.inRange(x, y, xx, yy, 192, 24)) {
                    found = true;
                    for(int t0 = 0; t0 < 8; t0++) {
                        int x1 = random.nextInt(192);
                        int x2 = random.nextInt(192);

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
                        startMenuButtonParticles.add(new UniParticle(xx+192, yy+y2, random.nextBoolean() ? 2 : 4, false,
                                color, fadingProcess));
                    }

                    if(i.isButtonDown(1)) {
                        if(k == 0) {
                            // HERE: Start the game
                            if(!isServer) {
                                try {
                                    clientId = -1;
                                    if (socket != null) {
                                        socket.close();
                                    }
                                    socket = new Socket(ip, port);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
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



        if(!isLocal)
            if(!isServer) {
                for (Module module : modulesClientSide) {
                    module.tick();
                }
                processSnd_clientSide();
                processRcv_clientSide();
            } else if(isClient) {
                for (Module module : modulesServerSide) {
                    module.tick();
                }
                int value = 0;
                for(int k = 0; k < clients.size(); k++) {
                    UserData data = this.data.get(k);
                    int j1 = 64;
                    while(j1 > 0 && packetReceiveQueue_serverSide.size() > 0) {
                        processRcv_serverSide(k);
                        j1--;
                    }
    //                data.calcVisibleArea();
                    Player player = players.get(k);
                    data.player = player;

                    processSnd_ServerSide(k);

                    value += packetSendQueue_serverSide.get(k).size();
                }

                stats_sendQueueLengthValues.addLast(value);
            }
        if(isClient) {

            if (stats_sendQueueLengthValues.size() > Main.width) {
                while (stats_sendQueueLengthValues.size() > Main.width) {
                    stats_sendQueueLengthValues.removeFirst();
                }
            }

            if (stats_sendQueueBytesValues.size() > Main.width) {
                while (stats_sendQueueBytesValues.size() > Main.width) {
                    stats_sendQueueBytesValues.removeFirst();
                }
            }
        }

        player.collisionMap = findOnScreenCollisions(
                chunkXTopLeft, chunkYTopLeft,
                chunkXBottomRight, chunkYBottomRight);
        player.cm = new AABBCollisionManager(player,
                player.collisionMap);
        player.update(i);

        entities.addAll(addEntities);
        addEntities.clear();

        findOnScreenBlocked = true;

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
                if(isServer)
                    sendObject(fly, 5);
                it.remove();
            } else {
                fly.update(core.getInput());
            }
        }

        Iterator<GameObject> it2 = entities.iterator();
        while(it2.hasNext()) {
            GameObject entity = it2.next();
            if(entity.dead) {
                if(isServer)
                    sendObject(entity, 3);
                it2.remove();
            } else {
                entity.update(core.getInput());
            }
        }

        Iterator<Player> it3 = playerEntities.iterator();
        while(it3.hasNext()) {
            Player player = it3.next();
            if(player.dead) {
                if(isServer)
                    sendObject(player,4);
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
        long startingTime = System.nanoTime();
        if(prevCamX != core.getRenderer().getCamX() || prevCamY != core.getRenderer().getCamY()) {
//            findOnScreenObjects();
            if(isServer || startMenuMode || isLocal) {
                for (int xx = chunkXTopLeft - 2; xx < chunkXBottomRight + 2; xx++) {
                    for (int yy = chunkYTopLeft - 2; yy < chunkYBottomRight + 2; yy++) {
                        if (getChunkArray(xx, yy).size() == 0) {
                            generateChunk(xx, yy);
                        }
                    }
                }
            }
        }
        System.out.println(System.nanoTime()-startingTime);


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
                chunks.clear();
                visibleChunkObjects.clear();
                entities.clear();
            } else {
                // HERE: Exit start menu mode
                cursor.visible = true;
                chunks.clear();
                visibleChunkObjects.clear();
                entities.clear();
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
            if (!isLocal) {
                int rcv = 0;
                int snd = 0;
                if (isServer) {
                    for (int i = 0; i < clients.size(); i++) {
                        rcv += packetReceiveQueue_serverSide.
                                get(i).size();
                        snd += packetSendQueue_serverSide.
                                get(i).size();
                    }
                }
                int amount = isServer ? snd :
                        packetSendQueue_clientSide.size();
                r.drawText("Send packets queue: " + amount, 8, y, 10, Color.black);
                y += addY;
                amount = isServer ? rcv :
                        packetReceiveQueue_clientSide.size();
                r.drawText("Received packets queue: " + amount, 8, y, 10, Color.black);
            }
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

    private void sendObject(GameObject obj) {
        for(int i = 0; i < clients.size(); i++) {
            WorldObjectData objectData =
                    WorldObject.getObjectData(((WorldObject) obj).getType());
            packetSendQueue_serverSide.get(i).offerFirst(addSignInfo(
                    objectData.getFactory().deconstruct((WorldObject)
                            obj, objectData), 1));
        }
    }

    private void sendObject(GameObject obj, int type) {
        for(int i = 0; i < clients.size(); i++) {
            WorldObjectData objectData =
                    WorldObject.getObjectData(((WorldObject) obj).getType());
            packetSendQueue_serverSide.get(i).offerFirst(addSignInfo(
                    objectData.getFactory().deconstruct((WorldObject)
                            obj, objectData), type));
        }
    }

    private void processSnd_ServerSide(int clientId) {
        // HERE: Choose data to send
        ArrayList<byte[]> dataToSend = new ArrayList<>();
        UserData data = this.data.get(clientId);

        // HERE: Send additional info
        if(!data.clientIdSent) {
            byte[] toSend = addSignInfo(convertToBytes(clientId), 2);
            dataToSend.add(toSend);
            stats_sendQueueBytesValues.addLast(toSend.length);

        }

        ConcurrentLinkedDeque<byte[]> deque =
                packetSendQueue_serverSide.get(clientId);
        System.out.println(deque.size());
        for (int i = dataToSend.size() - 1; i > 0; i--) {
            deque.addFirst(dataToSend.get(i));
        }

        try {
            clientStreams.get(clientId).flush();
        } catch (IOException e1) {
//            e1.printStackTrace();
        }
    }

    private void processRcv_serverSide(int clientId) {
        byte[] raw = packetReceiveQueue_serverSide.
                get(clientId).pollLast();

        if(raw == null) return;

        // HERE: Again... the logic
        int[] signData = extractSignInfo(raw);
        byte[] data = extractData(raw);

        switch (signData[0]) {
            case 1: // HERE: Movement
                int moveX = fetchInt(data, 0);
                int moveY = fetchInt(data, 4);
                if(moveX != 0 || moveY != 0)
                    System.out.println("Moving da player");
                players.get(clientId).moveX = moveX;
                players.get(clientId).moveY = moveY;
                break;
            case 2: // HERE: Name
                char[] chrs = new char[data.length/4];
                for(int i = 0; i < chrs.length; i++) {
                    chrs[i] = (char) fetchInt(data, i*4);
                }
                players.get(clientId).name = new String(chrs);
                this.data.get(clientId).playerName = new String(chrs);
        }

    }

    private void processSnd_clientSide() {
        ArrayList<byte[]> dataToSend = new ArrayList<>();

        // HERE: Name 'n' stuff
        byte[] nameAsCharacters = new byte[4*player.name.length()];
        char[] chars = player.name.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            System.arraycopy(convertToBytes((int)chars[i]), 0,
                    nameAsCharacters, i*4, 4);
        }
        byte[] toSend = addSignInfo(nameAsCharacters, 2);
        dataToSend.add(toSend);
        stats_sendQueueBytesValues.addLast(toSend.length);

        // HERE: Player movement
        if(player.moveX != 0 || player.moveY != 0 || !movingStopped) {
            byte[] movementInfo = new byte[8];
            System.arraycopy(convertToBytes(player.moveX), 0,
                    movementInfo, 0, 4);
            System.arraycopy(convertToBytes(player.moveY), 0,
                    movementInfo, 4, 4);
            byte[] toSend2 = addSignInfo(movementInfo, 1);
            dataToSend.add(toSend2);
            stats_sendQueueBytesValues.addLast(toSend2.length);
            movingStopped = false;
            if(player.moveX == 0 && player.moveY == 0) {
                movingStopped = true;
            }
        }

        if(isClient)
            for (int i = dataToSend.size() - 1; i > 0; i--) {
                packetSendQueue_clientSide.addFirst(dataToSend.get(i));
            }

        try {
            if(isClient || isServer)
                outputStreamWriter.flush();
        } catch (IOException e1) {
            // TODO: Server closed
            e1.printStackTrace();
        }
    }

    private void processRcv_clientSide() {
        while(packetReceiveQueue_clientSide.size() > 0) {
            byte[] data = packetReceiveQueue_clientSide.
                    pollLast();
            if (data == null) return;

            // HERE: Actual logic... FINALLY
            byte[] extractedData = extractData(data);
            int[] extractedInfo = extractSignInfo(data);
            System.out.println("===");
            System.out.println(Arrays.toString(extractedInfo));
            System.out.println("===");

            boolean found = false;
            for (Module module : modulesClientSide) {
                if (module.dataIndex ==
                        extractedInfo[0]) {
//                    System.out.println("yup..........");
                    module.read(data);
                    found = true;
                    break;
                }
            }

            if(!found) {
                switch (extractedInfo[0]) {
                    case 1: // ClientID
                        clientId = fetchInt(extractedData, 0);
                        player.clientId = clientId;
                        break;
                }
            }
        }
    }

    private void disconnectClient(int id) {
        clients.remove(id);
        players.remove(id);
        data.remove(id);
        packetSendQueue_serverSide.remove(id);
        packetReceiveQueue_serverSide.remove(id);
    }

    private static byte[] convertToBytes(int... args) {
        byte[] bytes = new byte[args.length*4];

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

    public static byte[] convertToBytes(int a) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (a >> 24);
        bytes[1] = (byte) (a >> 16);
        bytes[2] = (byte) (a >> 8);
        bytes[3] = (byte) (a);
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
        System.arraycopy(convertToBytes(bytes.length), 0, newBytes, 0, 4);
        return newBytes;
    }

    public byte[] sign(byte[] bytes, byte... args) {
        byte[] newBytes = new byte[bytes.length+args.length+4*args.length];
        System.arraycopy(args, 0, newBytes, 0, args.length);
        int i = 0;
        for(byte arg : args) {
            System.arraycopy(convertToBytes(args[i]), 0, newBytes, i*4, 4);
            i++;
        }
        return newBytes;
    }

    public byte[] addData(byte[] bytes, byte... args) {
        byte[] newBytes = new byte[bytes.length+args.length];
        System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
        System.arraycopy(args, 0, newBytes, bytes.length, args.length);
        return newBytes;
    }

    public static int fetchInt(byte[] array, int offset) {
        return toInt(array[offset], array[offset+1], array[offset+2], array[offset+3]);
    }

    private static int toInt(byte b1, byte b2, byte b3, byte b4) {
        return  ((0xFF & b1) << 24) | ((0xFF & b2) << 16) |
                ((0xFF & b3) << 8) | (0xFF & b4);
    }

    public static void addAllBytes(ArrayList<Byte> target, byte[] bytes) {
        for(byte byt : bytes) {
            target.add(byt);
        }
    }

    public static byte[] toByteArray(Byte[] array) {
        byte[] result = new byte[array.length];
        int i = 0;
        for(Byte bytee : array) {
            result[i] = bytee;
            i++;
        }
        return result;
    }

    public UserData getData(int clientId) {
        if(!data.containsKey(clientId)) {
            data.put(clientId, new UserData());
        }
        return data.get(clientId);
    }

    public static byte[] toByteArray(ArrayList<Byte> array) {
        byte[] result = new byte[array.size()];
        int i = 0;
        for(Byte bytee : array) {
            result[i] = bytee;
            i++;
        }
        return result;
    }

    public static byte[] addSignInfo(byte[] data, int... info) {
        byte[] result = new byte[data.length+info.length*4+4];
        System.arraycopy(convertToBytes(info.length), 0,
                result, 0, 4);
        System.arraycopy(convertToBytes(info),
                0, result, 4, info.length*4);
        System.arraycopy(data, 0, result, info.length*4+4,
                data.length);
        return result;
    }

    public static int[] extractSignInfo(byte[] data) {
        int infoAmount = fetchInt(data, 0);
        int[] signInfo = new int[infoAmount];
        for(int i = 0; i < infoAmount; i++) {
            signInfo[i] = fetchInt(data, i*4+4);
        }
        return signInfo;
    }

    public static byte[] extractData(byte[] data) {
        int infoAmount = fetchInt(data, 0)*4;
        int dataAmount = data.length - infoAmount - 4;
        byte[] result = new byte[dataAmount];
        System.arraycopy(data, 4+infoAmount, result,
                0, dataAmount);
        return result;
    }

    public boolean needsToBeModified(GameObject obj) {
        if(obj instanceof WorldObject) {
            boolean returnValue = ((WorldObject) obj).
                    needsUpdate;
            ((WorldObject) obj).needsUpdate = false;
            return returnValue;
        }
        return false;
    }

}
