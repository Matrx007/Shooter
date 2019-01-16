package com.youngdev.shooter.modules.premade;

import com.engine.libs.game.GameObject;
import com.youngdev.shooter.Fly;
import com.youngdev.shooter.Main;
import com.youngdev.shooter.Player;
import com.youngdev.shooter.modules.Module;
import com.youngdev.shooter.multiPlayerManagement.WorldObject;
import com.youngdev.shooter.multiPlayerManagement.WorldObjectData;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.youngdev.shooter.Main.*;

public class WorldReceiver extends Module {


    public WorldReceiver(Main main) {
        super(main, 0);
    }

    @Override
    public void init() {

    }

    @Override
    public void read(byte[] data) {
        byte[] extractedData = extractData(data);
        int[] extractedInfo = extractSignInfo(data);
        switch (extractedInfo[1]) {
            case 1: // Chunk Object
                int x = fetchInt(extractedData, 8);
                int y = fetchInt(extractedData, 12);

                int chunkX = getChunkLocation(x, y).x;
                int chunkY = getChunkLocation(x, y).y;

                int targetHashcode = fetchInt(extractedData, 4);

                CopyOnWriteArrayList<GameObject> allObjects =
                        main.getChunkArray(chunkX, chunkY);

                boolean found = false;
                Iterator<GameObject> iterator;
                for (iterator = allObjects.iterator();
                     iterator.hasNext(); ) {
                    GameObject obj = iterator.next();
                    if (obj instanceof WorldObject) {
                        // Ladies and gentlemen, we found it...
                        if (((WorldObject) obj).virtualHashcode ==
                                targetHashcode && ((WorldObject) obj).getType()
                                == fetchInt(extractedData, 0)) {
                            WorldObjectData objectData = WorldObject.getObjectData(
                                    ((WorldObject) obj).getType());
                            objectData.getFactory().modify((WorldObject) obj,
                                    objectData, extractedData);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    WorldObjectData objectData = WorldObject.getObjectData(fetchInt(
                            extractedData, 0));
//                    System.out.println(objectData);
                    main.getChunkArray(chunkX, chunkY).add(
                            objectData.getFactory().construct(objectData,
                                    extractedData)
                    );
//                    System.out.println("New object@" + chunkX + "," + chunkY);
                }
                break;
            case 3: // Entity

                targetHashcode = fetchInt(extractedData, 4);

                GameObject entity = null;
                boolean found1 = false;
                Iterator<GameObject> iterator1;
                for(iterator1 = main.entities.iterator(); iterator1.hasNext();) {
                    GameObject next = iterator1.next();
                    if(next instanceof WorldObject) {
                        if(((WorldObject) next).virtualHashcode
                                == targetHashcode) {
                            found1 = true;
                            entity = next;
                            break;
                        }
                    }
                }

                int targetType = fetchInt(extractedData, 0);
                WorldObjectData data1 =
                        WorldObject.getObjectData(targetType);

                if(!found1) {
                    main.entities.add(data1.getFactory().
                            construct(data1, extractedData));
                } else {
                    data1.getFactory().modify((WorldObject) entity, data1,
                            extractedData);
                }
                break;
            case 4: // Player
                double xx = main.player.x;
                double yy = main.player.y;
                targetHashcode = fetchInt(extractedData, 4);
                //                    int targetClientId = fetchInt(extractedData, 24);
                int objectType = fetchInt(extractedData, 0);

                WorldObjectData data2 =
                        WorldObject.getObjectData(objectType);

                        /*if(player.clientId == targetClientId ||
                                player.virtualHashcode == targetHashcode) {
                            data2.getFactory().modify(player, data2,
                                    extractedData);
                        }*/

                boolean found2 = false;
                Player foundPlayer = null;
                Iterator<Player> iterator2;
                for(iterator2 = main.playerEntities.iterator();
                    iterator2.hasNext();) {
                    Player player = iterator2.next();
                    if(player.virtualHashcode ==
                            targetHashcode) {
                        foundPlayer = player;
                        found2 = true;
                    }
                }

                if(found2) {
                    data2.getFactory().modify(foundPlayer, data2,
                            extractedData);
                    if(foundPlayer.clientId == clientId) {
                        if(main.waitingForPlayerReference) {
                            main.player = foundPlayer;
                            main.waitingForPlayerReference = false;
                            main.camera.target = main.player;
                        }
                    }
                } else {
                    main.playerEntities.add((Player)data2.getFactory().
                            construct(data2, extractedData));
                }

                if(main.player.x != xx ||
                        main.player.y != yy) {
                    System.out.println("x = " + main.player.x);
                    System.out.println("y = " + main.player.y);
                }

                break;
            case 5: // Fly
                targetHashcode = fetchInt(extractedData, 4);
                objectType = fetchInt(extractedData, 0);

                WorldObjectData data3 =
                        WorldObject.getObjectData(objectType);

                boolean found3 = false;
                Iterator<Fly> iterator3;
                for(iterator3 = main.flies.iterator();
                    iterator3.hasNext();) {
                    Fly fly = iterator3.next();
                    if(fly.virtualHashcode ==
                            targetHashcode) {
                        data3.getFactory().modify(fly, data3,
                                extractedData);
                        found3 = true;
                    }
                }

                if(!found3) {
                    main.flies.add((Fly)data3.getFactory().
                            construct(data3, extractedData));
                }
                break;

        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void draw() {

    }
}
