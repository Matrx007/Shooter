package com.youngdev.shooter.multiPlayerManagement;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.game.behaviors.AABBComponent;
import com.sun.istack.internal.NotNull;
import com.youngdev.shooter.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class WorldObject extends GameObject {
    private Random random;
    private int type;
    public boolean needsUpdate = false;
    public int virtualHashcode = 0;
    private static final Map<Integer, WorldObjectData> typeData =
            new HashMap<>();

    static {
        WorldObjectData defaultType = new WorldObjectData(
            new Variable("type", new Pointer(0, Pointer.SIZE_INT)),
            new Variable("hashcode", new Pointer(4, Pointer.SIZE_INT)),
            new Variable("x", new Pointer(8, Pointer.SIZE_INT)),
            new Variable("y", new Pointer(12, Pointer.SIZE_INT)),
            new Variable("depth", new Pointer(16, Pointer.SIZE_INT)),
            new Variable("dead", new Pointer(20, Pointer.SIZE_INT))
        );
        WorldObjectData defaultTypeSolid = new WorldObjectData(
                new Variable("type", new Pointer(0, Pointer.SIZE_INT)),
                new Variable("hashcode", new Pointer(4, Pointer.SIZE_INT)),
                new Variable("x", new Pointer(8, Pointer.SIZE_INT)),
                new Variable("y", new Pointer(12, Pointer.SIZE_INT)),
                new Variable("depth", new Pointer(16, Pointer.SIZE_INT)),
                new Variable("dead", new Pointer(20, Pointer.SIZE_INT)),
                new Variable("maskx", new Pointer(24, Pointer.SIZE_INT)),
                new Variable("masky", new Pointer(28, Pointer.SIZE_INT)),
                new Variable("maskw", new Pointer(32, Pointer.SIZE_INT)),
                new Variable("maskh", new Pointer(36, Pointer.SIZE_INT))
        );

        // --- Arrow --- < 0 >
        typeData.put(0, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Arrow arrow = new Arrow(
                        info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        info.fetchInt(data, "dir")
                );
                arrow.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                arrow.depth = info.fetchInt(data, "depth");
                arrow.dead = info.fetchInt(data, "dead") == 1;
                return arrow;
            }

            @Override
            public void modify(@NotNull WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Fly)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(@NotNull WorldObject target, WorldObjectData info) {
                if(target instanceof Arrow) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "dir", ((Arrow) target).dir
                            );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("dir", new Pointer(24, Pointer.SIZE_INT))));

        // --- Bush --- < 1 >
        typeData.put(1, defaultTypeSolid.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Bush bush = new Bush(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"));
                bush.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                bush.depth = info.fetchInt(data, "depth");
                bush.dead = info.fetchInt(data, "dead") == 1;
                bush.aabbComponent = new AABBComponent(new Mask.Rectangle(
                        info.fetchInt(data, "maskx"),
                        info.fetchInt(data, "masky"),
                        info.fetchInt(data, "maskw"),
                        info.fetchInt(data, "maskh")
                ));
                bush.fliesInside = false;
                return bush;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Fly)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Bush) {
                    byte[] result = new byte[40];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "maskx", target.mask.x,
                            "masky", target.mask.y,
                            "maskw", ((Mask.Rectangle) target.mask).w,
                            "maskh", ((Mask.Rectangle) target.mask).h
                    );
                    return result;
                }
                return new byte[0];
            }
        }));

        // --- Coin --- < 2 >
        typeData.put(2, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Coin coin = new Coin(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        info.fetchInt(data, "angle"));
                coin.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                coin.depth = info.fetchInt(data, "depth");
                coin.dead = info.fetchInt(data, "dead") == 1;
                return coin;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Fly)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Coin) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "angle", ((Coin) target).angle
                    );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("angle", new Pointer(24, Pointer.SIZE_INT))));

        // --- EnemyBolt --- < 3 >
        typeData.put(3, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                EnemyBolt enemy = new EnemyBolt(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"));
                enemy.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                enemy.depth = info.fetchInt(data, "depth");
                enemy.dead = info.fetchInt(data, "dead") == 1;
                return enemy;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Fly)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof EnemyBolt) {
                    byte[] result = new byte[24];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0
                    );
                    return result;
                }
                return new byte[0];
            }
        }));


        // --- Fly --- < 5 >
        typeData.put(5, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Fly fly = new Fly(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        info.fetchInt(data, "state") == 1);
                fly.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                fly.depth = info.fetchInt(data, "depth");
                fly.dead = info.fetchInt(data, "dead") == 1;
                return fly;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Fly)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
                ((Fly) target).state = info.fetchInt(data, "state")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Fly) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "state", ((Fly) target).state
                    );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("state", new Pointer(24, Pointer.SIZE_INT))));

        // --- Plant --- < 6 >
        typeData.put(6, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Random random = new Random();
                Plant plant = new Plant(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        new Color(random.nextInt(235),
                                random.nextInt(235),
                                random.nextInt(235)));
                plant.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                plant.depth = info.fetchInt(data, "depth");
                plant.dead = info.fetchInt(data, "dead") == 1;
                return plant;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Plant)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
                ((Plant) target).collision = info.fetchInt(data, "collision")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Plant) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "collision", ((Plant) target).collision
                    );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("collision", new Pointer(24, Pointer.SIZE_INT))));

        // --- Player --- < 7 >
        typeData.put(7, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Player player = new Player(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"));
                player.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                player.depth = info.fetchInt(data, "depth");
                player.dead = info.fetchInt(data, "dead") == 1;
                player.clientId = info.fetchInt(data, "clientId");
                return player;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Player)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Player) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "clientId", ((Player) target).clientId
                    );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("clientId", new Pointer(24, Pointer.SIZE_INT))));

        // --- Rabbit --- < 8 >
        typeData.put(8, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Rabbit rabbit = new Rabbit(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"));
                rabbit.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                rabbit.depth = info.fetchInt(data, "depth");
                rabbit.dead = info.fetchInt(data, "dead") == 1;
                rabbit.escaping = info.fetchInt(data, "escaping") == 1;
                rabbit.directionTarget = info.fetchInt(data, "headDir");
                rabbit.direction = info.fetchInt(data, "movingDir");
                return rabbit;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Rabbit)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
                ((Rabbit) target).escaping = info.fetchInt(data, "escaping") == 1;
                ((Rabbit) target).directionTarget = info.fetchInt(data, "headDir");
                ((Rabbit) target).direction = info.fetchInt(data, "movingDir");
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Rabbit) {
                    byte[] result = new byte[36];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "escaping", ((Rabbit) target).escaping ? 1 : 0,
                            "movingDir", ((Rabbit) target).direction,
                            "headDir", ((Rabbit) target).directionTarget
                            );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("escaping", new Pointer(24, Pointer.SIZE_INT)),
                new Variable("movingDir", new Pointer(28, Pointer.SIZE_INT)),
                new Variable("headDir", new Pointer(32, Pointer.SIZE_INT))));

        // --- Rocks --- < 9 >
        typeData.put(9, defaultTypeSolid.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Rocks rocks = new Rocks(info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"));
                rocks.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                rocks.depth = info.fetchInt(data, "depth");
                rocks.dead = info.fetchInt(data, "dead") == 1;
                rocks.aabbComponent = new AABBComponent(new Mask.Rectangle(
                        info.fetchInt(data, " maskx"),
                        info.fetchInt(data, " masky"),
                        info.fetchInt(data, " maskw"),
                        info.fetchInt(data, " maskh")
                ));
                return rocks;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Rocks)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Rocks) {
                    byte[] result = new byte[40];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "maskx", ((Mask.Rectangle)
                                    target.aabbComponent.area).x,
                            "masky", ((Mask.Rectangle)
                                    target.aabbComponent.area).y,
                            "maskw", ((Mask.Rectangle)
                                    target.aabbComponent.area).w,
                            "maskh", ((Mask.Rectangle)
                                    target.aabbComponent.area).h
                    );
                    return result;
                }
                return new byte[0];
            }
        }));

        // --- StructuralBlock --- < 10 >
        typeData.put(10, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                StructuralBlock structuralBlock = new StructuralBlock(
                        info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        info.fetchInt(data, "sType"));
                structuralBlock.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                structuralBlock.depth = info.fetchInt(data, "depth");
                structuralBlock.dead = info.fetchInt(data, "dead") == 1;
                return structuralBlock;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof StructuralBlock)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof StructuralBlock) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "sType", ((StructuralBlock) target).type
                    );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("sType", new Pointer(24, Pointer.SIZE_INT))));

        // --- Terrain --- < 11 >
        typeData.put(11, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Terrain terrain = new Terrain(
                        info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        info.fetchInt(data, "sType"));
                terrain.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                terrain.depth = info.fetchInt(data, "depth");
                terrain.dead = info.fetchInt(data, "dead") == 1;
                terrain.type = info.fetchInt(data, "sType");
                return terrain;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Terrain)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Terrain) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "sType", ((Terrain) target).type
                    );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("sType", new Pointer(24, Pointer.SIZE_INT))));

        // --- Trash --- < 12 >
        typeData.put(12, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Trash trash = new Trash(
                        info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        info.fetchInt(data, "sType"));
                trash.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                trash.depth = info.fetchInt(data, "depth");
                trash.dead = info.fetchInt(data, "dead") == 1;
                trash.type = info.fetchInt(data, "sType");
                return trash;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Trash)) return;
                target.x = info.fetchInt(data, "x");
                target.y = info.fetchInt(data, "y");
                target.dead = info.fetchInt(data, "dead")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Trash) {
                    byte[] result = new byte[28];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "sType", ((Trash) target).type
                    );
                    return result;
                }
                return new byte[0];
            }
        }, new Variable("sType", new Pointer(24, Pointer.SIZE_INT))));

        // --- Tree --- < 13 >
        typeData.put(13, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Tree trash = new Tree(
                        info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"),
                        info.fetchInt(data, "sType"));
                trash.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                trash.depth = info.fetchInt(data, "depth");
                trash.dead = info.fetchInt(data, "dead") == 1;
                trash.type = info.fetchInt(data, "sType");
                trash.collision = info.fetchInt(data,
                        "collision") == 1;
                return trash;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Tree)) return;
                target.dead = info.fetchInt(data, "dead")==1;
                ((Tree) target).collision = info.fetchInt(data, "collision")==1;
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Tree) {
                    byte[] result = new byte[40];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "collision", ((Tree) target).collision ? 1 : 0,
                            "sType", ((Tree) target).type
                    );
                    return result;
                }
                return new byte[0];
            }
        },
            new Variable("sType", new Pointer(24, Pointer.SIZE_INT)),
                    new Variable("collision", new Pointer(28, Pointer.SIZE_INT))));

        // --- Branches --- < 14 >
        typeData.put(14, defaultType.addFactoryAndData(new Factory(){
            @Override
            public GameObject construct(WorldObjectData info, byte[] data) {
                Branches trash = new Branches(
                        info.fetchInt(data, "x"),
                        info.fetchInt(data, "y"));
                trash.virtualHashcode = info.fetchInt(data,
                        "hashcode");
                trash.depth = info.fetchInt(data, "depth");
                trash.dead = info.fetchInt(data, "dead") == 1;

                for(int i = 0; i < (data.length-40)/8; i++) {
                    trash.particles.add(trash.createParticle(
                            Main.fetchInt(data, i*8),
                            Main.fetchInt(data, i*8+4)));
                }
                return trash;
            }

            @Override
            public void modify(WorldObject target, WorldObjectData info, byte[] data) {
                if(!(target instanceof Branches)) return;
                target.dead = info.fetchInt(data, "dead")==1;
                for(int i = 0; i < (data.length-40)/8; i++) {
                    ((Branches) target).particles.get(i).x =
                            Main.fetchInt(data, i*8);
                    ((Branches) target).particles.get(i).y =
                            Main.fetchInt(data, i*8+4);
                }
            }

            @Override
            public byte[] deconstruct(WorldObject target, WorldObjectData info) {
                if(target instanceof Branches) {
                    byte[] result = new byte[40+((Branches) target).
                            particles.size()*8];
                    info.writeData(result,
                            "x", (int) target.x,
                            "y", (int) target.y,
                            "type", target.type,
                            "hashcode", target.hashCode(),
                            "depth", target.depth,
                            "dead", target.dead ? 1 : 0,
                            "numBranches", ((Branches) target).
                                    particles.size()
                    );
                    int i = 40;
                    for(UniParticle particle : ((Branches) target).particles) {
                        System.arraycopy(Main.convertToBytes(particle.x),
                                0, result, i, 4);
                        System.arraycopy(Main.convertToBytes(particle.y),
                                0, result, i+4, 4);
                        i += 8;
                    }
                    return result;
                }
                return new byte[0];
            }
        },
                new Variable("numBranches", new Pointer(28, Pointer.SIZE_INT))));
    }

    public static WorldObjectData getObjectData(int type) {
        return typeData.get(type);
    }

    public WorldObject(int index, int depth, int type) {
        super(index, 0);
        this.random = new Random();
        this.depth = depth;
//        this.depth = depth*1024+random.nextInt(1024);
        this.type = type;
    }

    public static abstract class Factory {
        public abstract GameObject construct(
                WorldObjectData info, byte[] data);
        public abstract void modify(@NotNull WorldObject target,
                WorldObjectData info, byte[] data);
        public abstract byte[] deconstruct(@NotNull WorldObject target,
                WorldObjectData info);
    }

    public int getType() {
        return type;
    }
}
