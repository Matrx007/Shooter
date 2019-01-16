package com.youngdev.shooter.gamemodes.hideandseek;

import com.youngdev.shooter.Main;
import com.youngdev.shooter.UserData;
import com.youngdev.shooter.gamemodes.Field;
import com.youngdev.shooter.gamemodes.GameMode;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class HideAndSeek extends GameMode {
    private Random random;

    public HideAndSeek() {
        super(new Field(Field.TYPE_CHOICE,
                Arrays.asList("Hider", "Seeker")));
        this.random = new Random();
    }

    @Override
    public Point getPlayerJoinPosition(UserData userData) {
        if(!userData.values.containsKey("hiderOrSeeker")) {
            return new Point(getMiddleX(), getMiddleY());
        }
        if(userData.values.get("hiderOrSeeker").
                getChoiceIndex()==0) {
            // Hider
            if(random.nextBoolean()) {
                // Horizontal side
                return new Point(getMiddleX()-
                        random.nextInt(getWorldWidth()*
                                Main.chunkSize)-getWorldWidth()/2,
                        getMiddleY()-
                                (random.nextBoolean() ? 0 :
                                        getWorldHeight()* Main.chunkSize)
                );
            } else {
                // Vertical side
                return new Point(
                        getMiddleX()-
                                (random.nextBoolean() ? 0 : getWorldWidth()*
                                Main.chunkSize),
                        getMiddleY()-
                        random.nextInt(getWorldHeight()*
                                Main.chunkSize)
                );
            }
        } else {
            return new Point(getMiddleX(), getMiddleY());
        }
    }

    @Override
    public int getWorldWidth() {
        return 32;
    }

    @Override
    public int getWorldHeight() {
        return 32;
    }

    @Override
    public int getMiddleX() {
        return Integer.MAX_VALUE/2/Main.chunkSize;
    }

    @Override
    public int getMiddleY() {
        return Integer.MAX_VALUE/2/Main.chunkSize;
    }
}
