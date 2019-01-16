package com.youngdev.shooter;

import com.youngdev.shooter.gamemodes.Field;

import java.util.Hashtable;

import static com.youngdev.shooter.Main.chunkSize;

public class UserData {
    public String playerName = "";
    public boolean clientIdSent = false;
    public Player player = null;
    public int cX, cY, chunkXTopLeft,
            chunkYTopLeft, chunkXBottomRight,
            chunkYBottomRight, clientId;
    public Hashtable<String, Field.Value> values;

    public UserData() {
        values = new Hashtable<>();
    }

    public void putValue(String name, Field.Value value) {
        values.put(name, value);
    }

    public void calcVisibleArea() {
        if(player == null) return;
        cX = (int)player.x - Main.width/2;
        cY = (int)player.y - Main.height/2;

        chunkXTopLeft = (int)Math.floor(cX/(double)chunkSize)-2;
        chunkYTopLeft = (int)Math.floor(cY/(double)chunkSize)-2;
        chunkXBottomRight = (int)Math.floor((Main.width+cX)/(double)chunkSize)+2;
        chunkYBottomRight = (int)Math.floor((Main.height+cY)/(double)chunkSize)+2;
    }
}
