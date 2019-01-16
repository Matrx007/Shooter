package com.youngdev.shooter.modules.premade;

import com.engine.libs.game.GameObject;
import com.youngdev.shooter.Fly;
import com.youngdev.shooter.Main;
import com.youngdev.shooter.Player;
import com.youngdev.shooter.UserData;
import com.youngdev.shooter.modules.Module;
import com.youngdev.shooter.multiPlayerManagement.WorldObject;
import com.youngdev.shooter.multiPlayerManagement.WorldObjectData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.youngdev.shooter.Main.addSignInfo;

public class WorldSender extends Module {

    public WorldSender(Main main) {
        super(main, 0);
    }

    @Override
    public void init() {
        
    }

    @Override
    public void read(byte[] bytes) {
        // WorldSender will not receive any bytes
    }

    @Override
    public void tick() {
        long start = System.nanoTime();
        int num = 0;
        ConcurrentLinkedDeque<byte[]> deque = null;
        for(int clientId = 0; clientId < main.data.size(); clientId++) {
//            System.out.println("tick");
            UserData data = main.data.get(clientId);

            ArrayList<byte[]> dataToSend = new ArrayList<>();

            data.calcVisibleArea();
            int chunkXTopLeft = data.chunkXTopLeft;
            int chunkYTopLeft = data.chunkYTopLeft;
            int chunkXBottomRight = data.chunkXBottomRight;
            int chunkYBottomRight = data.chunkYBottomRight;

            ArrayList<GameObject> visibleObjects = new ArrayList<>();

            for (int xx = chunkXTopLeft - 2; xx < chunkXBottomRight + 2; xx++) {
                for (int yy = chunkYTopLeft - 2; yy < chunkYBottomRight + 2; yy++) {
                    visibleObjects.addAll(main.getAndGenerateChunk(xx, yy));
                }
            }

            HashSet<GameObject> objList = main.objects.get(clientId);

            int c5 = 0;
            for (GameObject obj : visibleObjects) {
                if (obj instanceof WorldObject) {
                    if (!objList.contains(obj)) {
                        WorldObjectData objectData =
                                WorldObject.getObjectData(((WorldObject) obj).getType());
                        byte[] toSend = addSignInfo(objectData.getFactory().
                                deconstruct((WorldObject) obj, objectData),
                                0, 1);
                        dataToSend.add(toSend);
//                        System.out.println("adding wo");
                        objList.add(obj);
                        c5++;
                    }
                }
            }

            System.out.println("Chunk objects: "+c5);

            int c4 = 0;
            for (GameObject obj : visibleObjects) {
                if (main.needsToBeModified(obj)) {
                    WorldObjectData objectData =
                            WorldObject.getObjectData(((WorldObject) obj).getType());
                    byte[] toSend = addSignInfo(objectData.getFactory().
                            deconstruct((WorldObject) obj, objectData),
                            0, 1);
                    dataToSend.add(toSend);
                    objList.add(obj);
                    c4++;
                }
            }

            System.out.println("Chunk object updates: "+c4);

            int c3 = 0;
            for (GameObject obj : main.entities) {
                if (obj instanceof WorldObject) {
                    if (main.isPixelOnScreen(chunkXTopLeft,
                            chunkYTopLeft, chunkXBottomRight,
                            chunkYBottomRight, (int) obj.x, (int) obj.y,
                            2)) {
                        WorldObjectData objectData =
                                WorldObject.getObjectData(((WorldObject) obj).getType());
                        byte[] toSend = addSignInfo(objectData.getFactory().
                                deconstruct((WorldObject) obj, objectData),
                                0, 3);
                        dataToSend.add(toSend);
                        c3++;
//                        objList.add(obj);
                    }
                }
            }

            System.out.println("Entities: "+c3);

            int c2 = 0;
            for (GameObject obj : main.players.values()) {
                if (main.isPixelOnScreen(chunkXTopLeft,
                        chunkYTopLeft, chunkXBottomRight,
                        chunkYBottomRight, (int) obj.x, (int) obj.y,
                        2) || (obj instanceof Player &&
                        ((Player) obj).clientId == clientId) &&
                        main.needsToBeModified(obj)) {
                    WorldObjectData objectData =
                            WorldObject.getObjectData(((WorldObject) obj).getType());
                    byte[] toSend = addSignInfo(objectData.getFactory().
                            deconstruct((WorldObject) obj, objectData),
                            0, 4);
                    dataToSend.add(toSend);
                    c2++;
//                    objList.add(obj);
                }
            }
            System.out.println("Players: "+c2);

            int c1 = 0;
            for (Fly fly : main.flies) {
                if (main.isPixelOnScreen(chunkXTopLeft,
                        chunkYTopLeft, chunkXBottomRight,
                        chunkYBottomRight, (int) fly.x, (int) fly.y,
                        2)) {
                    WorldObjectData objectData =
                            WorldObject.getObjectData(fly.getType());
                    byte[] toSend = addSignInfo(objectData.getFactory().
                            deconstruct(fly, objectData), 0, 5);
                    dataToSend.add(toSend);
                    c1++;
//                    objList.add(fly);
                }
            }

            System.out.println("Flies: "+c1);

            deque = main.packetSendQueue_serverSide.get(clientId);
            for (int i = dataToSend.size() - 1; i > 0; i--) {
                deque.addFirst(dataToSend.get(i));
            }
            num += dataToSend.size();
        }
        long end = System.nanoTime();
        end -= start;
        if(deque != null)
            System.out.println("took "+end+" ns to add "+num+" to queue");
    }

    @Override
    public void draw() {
        
    }
}
