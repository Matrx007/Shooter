package com.youngdev.shooter.multiPlayerManagement;

import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WorldObjectData {
    private Map<String, Pointer> data;
    private Variable[] rawData;
    private WorldObject.Factory factory;

    public WorldObjectData(@NotNull WorldObject.Factory factory, Variable... variables) {
        this.factory = factory;
        this.data = new HashMap<>();
        boolean waitingForKey = true;
        String key = null;
        for(Variable var : variables) {
            if(var.getName() != null)
                this.data.put(var.getName(), var.getPointer());
        }
        rawData = variables;
    }
    public WorldObjectData(Variable... variables) {
        this.factory = null;
        this.data = new HashMap<>();
        boolean waitingForKey = true;
        String key = null;
        for(Variable var : variables) {
            if(var.getName() != null)
                this.data.put(var.getName(), var.getPointer());
        }
        rawData = variables;
    }

    public WorldObjectData createWithFactory(WorldObject.Factory factory) {
        return new WorldObjectData(factory, rawData);
    }

    public WorldObjectData addFactoryAndData(WorldObject.Factory factory, Variable... variables) {
        Variable[] args = new Variable[rawData.length+variables.length];
        for(int i = 0; i < variables.length; i++) {
            args[i] = variables[i];
        }
        int add = variables.length;
        for(int i = 0; i < rawData.length; i++) {
            args[i+add] = rawData[i];
        }
        return new WorldObjectData(factory, args);
    }

    public WorldObject.Factory getFactory() {
        return factory;
    }

    public int fetchInt(byte[] data, String parameter) {
        Pointer p = getPointer(parameter);
        if(p == null) return -1;
        byte[] result = new byte[p.getSize()];
        System.arraycopy(data, p.getLoc(), result,
                0, p.getSize());
        return ((0xFF & result[0]) << 24) | ((0xFF & result[1]) << 16) |
                ((0xFF & result[2]) << 8) | (0xFF & result[3]);
    }

    public byte[] fetchValue(byte[] data, String parameter) {
        Pointer p = getPointer(parameter);
        byte[] result = new byte[p.getSize()];
        System.arraycopy(data, p.getLoc(), result,
                0, p.getSize());
        return result;
    }

    public Pointer getPointer(String parameter) {
        return data.get(parameter);
    }

    public void writeInt(byte[] data, String parameter, int value) {
        Pointer pointer = this.data.get(parameter);
        System.arraycopy(convertToBytes(value), 0, data,
                pointer.getLoc(), pointer.getSize());
    }

    public void writeData(byte[] data, Object... info) {
        boolean waitingForKey = true;
        String key = null;
        for(Object object : info) {
            if(waitingForKey && object instanceof String) {
                key = (String) object;
                waitingForKey = false;
            } else if(!waitingForKey) {
                Pointer pointer = this.data.get(key);
                if(object instanceof Integer) {
                    System.arraycopy(convertToBytes((Integer) object),
                            0, data, pointer.getLoc(), pointer.getSize());
                }
                waitingForKey = true;
            }
        }
    }

    public byte[] convertToBytes(int a) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (a >> 24);
        bytes[1] = (byte) (a >> 16);
        bytes[2] = (byte) (a >> 8);
        bytes[3] = (byte) (a);
        return bytes;
    }
}
