package com.youngdev.shooter.gamemodes;

import com.sun.istack.internal.NotNull;
import com.youngdev.shooter.UserData;

import java.awt.*;

public abstract class GameMode {
    private Field[] fields;

    public GameMode(Field... fields) {
        this.fields = fields;
    }

    public Field[] getFields() {
        return fields;
    }

    public abstract Point getPlayerJoinPosition(UserData userData);
    public abstract int getWorldWidth();
    public abstract int getWorldHeight();
    public abstract int getMiddleX();
    public abstract int getMiddleY();
}
