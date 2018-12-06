package com.youngdev.shooter;

import com.engine.libs.game.GameObject;
import com.engine.libs.game.Mask;
import com.engine.libs.input.Input;
import com.engine.libs.math.AdvancedMath;
import com.engine.libs.math.Vector;
import com.engine.libs.rendering.Image;
import com.engine.libs.rendering.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import static com.youngdev.shooter.Bunny.rotatePoint;

public class Hut extends GameObject {
    boolean drawRoof;
    int w, h;
    private ArrayList<int[]> flooring;
    private ArrayList<int[]> roof;
    private ArrayList<Color> flooringColor;
    private ArrayList<Color> roofColor;
    Color floorBaseColor = new Color(154, 88, 27);
    Random random;

    public Hut(int x, int y) {
        super(13, 21);
        this.x = x;
        this.y = y;
        this.w = 192;
        this.h = 192;
        drawRoof = false;

        // HERE: Fix depth
        this.random = new Random();
        this.depth = random.nextInt(1023)+depth*1024;

        this.mask = new Mask.Rectangle(x-w/2, y-h/2, w, h);
        solid = false;

        flooring = new ArrayList<>();
        flooringColor = new ArrayList<>();
        int xx = 0;
        while (xx < w) {
            int[] cords = new int[4];
            int width = random.nextInt(3)+2;
            int yy = random.nextInt(6)-3;
            cords[0] = x+xx;
            cords[1] = y-yy;
            cords[2] = width;
            cords[3] = h-yy;

            flooring.add(cords);

            double amount = random.nextDouble();

            Color pm = preMadeColors[random.nextInt(preMadeColors.length)];
            double mid = (pm.getRed()+pm.getGreen()+pm.getBlue())/12d;
            Color c = new Color(
                    (int)(amount*64+128),
                    (int)(amount*64+128),
                    (int)(amount*64+128)
            );


            flooringColor.add(c);

            xx += width;
        }

        int size = 3;
        int border = 8;
        int[] topBorder = new int[] {x+border, y+border, w-border*2, size};
        int[] leftBorder = new int[] {x+border, y+border, size, h-border*2};
        int[] rightBorder = new int[] {x+w-border, y+border, size, h-border*2};
        int[] bottomBorder = new int[] {x+border, y+w-border, w-border*2+size, size};

        flooring.add(topBorder);
        flooring.add(leftBorder);
        flooring.add(rightBorder);
        flooring.add(bottomBorder);

        Color color = new Color(64, 64, 64);
        flooringColor.add(color);
        flooringColor.add(color);
        flooringColor.add(color);
        flooringColor.add(color);

        roof = new ArrayList<>();
        roofColor = new ArrayList<>();
        double roofW = w/2d;
        double roofH = h/2d;
        double roofX = x+(w-roofW)/2d;
        double roofY = y+(h-roofH)/2d;
        double roofMiddleX = roofX + roofW/2;
        double roofMiddleY = roofY + roofH/2;
        for(int side = 0; side < 4; side++) {
            int numPieces = random.nextInt(4)+7;

            for(int piece = 0; piece < numPieces; piece++) {
                int[] pieceCords = new int[6];

                int add = random.nextInt(6);

                double _locX1 = roofX+roofW/numPieces*piece-add;
                double _locY1 = roofY-add;
                double _locX2 = roofX+roofW/numPieces*(piece+1)+add;
                double _locY2 = roofY-add;

                double[] p1 = rotatePoint( _locX1, _locY1, roofMiddleX, roofMiddleY, side*90);
                int locX1 = (int)(p1[0]);
                int locY1 = (int)(p1[1]);
                double[] p2 = rotatePoint( _locX2, _locY2, roofMiddleX, roofMiddleY, side*90);
                int locX2 = (int)(p2[0]);
                int locY2 = (int)(p2[1]);

                pieceCords[0] = locX1;
                pieceCords[1] = locY1;
                pieceCords[2] = locX2;
                pieceCords[3] = locY2;
                pieceCords[4] = (int)roofMiddleX;
                pieceCords[5] = (int)roofMiddleY;

                roof.add(pieceCords);

                double amount = random.nextDouble();
                Color c = new Color(
                        (int)(amount*64+32),
                        (int)(amount*64+32),
                        (int)(amount*64+32)
                );

                roofColor.add(c);
            }
        }
    }

    @Override
    public void update(Input i) {
        Player player = Main.main.player;
        drawRoof = !AdvancedMath.inRange(player.x, player.y, x-w/2d, y-h/2d, w, h);
    }

    @Override
    public void render(Renderer r) {
        for(int i = 0 ; i < flooring.size(); i++) {
            r.fillRectangle(flooring.get(i)[0], flooring.get(i)[1],
                    flooring.get(i)[2], flooring.get(i)[3], flooringColor.get(i));
        }
        for(int i = 0 ; i < roof.size(); i++) {
            r.fillPolygon(new int[]{
                    roof.get(i)[0],
                    roof.get(i)[2],
                    roof.get(i)[4]
            },new int[]{
                    roof.get(i)[1],
                    roof.get(i)[3],
                    roof.get(i)[5]
            }, roofColor.get(i));
        }
    }

    @Override
    public String shareSend() {
        return null;
    }

    @Override
    public void shareReceive(String s) {

    }

    public static double[] rotatePoint(double x, double y, double anchorX, double anchorY, double degrees) {
        double xx = (x - anchorX) * Math.cos(degrees * Math.PI / 180) - (y - anchorY) * Math.sin(degrees * Math.PI / 180) + anchorX;
        double yy = (x - anchorX) * Math.sin(degrees * Math.PI / 180) + (y - anchorY) * Math.cos(degrees * Math.PI / 180) + anchorY;
        return new double[]{xx, yy};
    }

    private Color[] preMadeColors = new Color[] {
            new Color(155, 88, 45),
            new Color(203, 133, 45),
            new Color(201, 137, 40),
            new Color(186, 117, 40),
            new Color(187, 124, 44),
            new Color(188, 120, 39),
            new Color(200, 129, 49),
            new Color(185, 119, 41),
            new Color(205, 136, 43),
            new Color(121, 68, 16),
            new Color(178, 104, 39),
            new Color(144, 80, 44),
            new Color(163, 97, 37),
            new Color(187, 120, 42),
            new Color(203, 133, 48),
            new Color(197, 130, 41),
            new Color(204, 137, 48),
            new Color(213, 148, 46),
            new Color(159, 96, 29),
            new Color(185, 118, 40),
            new Color(207, 137, 49),
            new Color(197, 129, 46),
            new Color(192, 125, 44),
            new Color(189, 123, 39),
            new Color(194, 123, 43),
            new Color(209, 141, 42),
            new Color(196, 131, 47),
            new Color(152, 86, 34),
            new Color(200, 137, 42),
            new Color(194, 126, 43),
            new Color(208, 142, 46),
            new Color(194, 129, 39),
            new Color(205, 139, 43),
            new Color(204, 135, 44),
            new Color(197, 131, 47),
            new Color(207, 140, 49),
            new Color(213, 150, 45),
            new Color(205, 148, 43),
            new Color(199, 132, 41),
            new Color(202, 137, 45),
            new Color(192, 125, 46),
            new Color(173, 108, 28),
            new Color(222, 162, 52),
            new Color(212, 150, 49),
            new Color(201, 135, 48),
            new Color(200, 135, 41),
            new Color(201, 136, 44),
            new Color(196, 128, 43),
            new Color(209, 145, 45),
            new Color(199, 134, 42),
            new Color(207, 141, 44),
            new Color(185, 120, 54),
            new Color(187, 117, 32),
            new Color(163, 99, 37),
            new Color(200, 135, 43),
            new Color(208, 149, 47),
            new Color(219, 156, 51),
            new Color(197, 128, 37),
            new Color(201, 135, 39),
            new Color(188, 121, 40),
            new Color(215, 153, 44),
            new Color(211, 161, 38),
            new Color(221, 171, 48),
            new Color(176, 111, 47),
            new Color(88, 41, 0),
            new Color(185, 115, 43),
            new Color(170, 103, 32),
            new Color(192, 127, 43),
            new Color(191, 126, 46),
            new Color(215, 151, 53),
            new Color(213, 150, 47),
            new Color(207, 146, 53),
            new Color(215, 162, 46),
            new Color(226, 178, 52),
            new Color(194, 128, 42),
            new Color(203, 139, 42),
            new Color(209, 143, 46),
            new Color(207, 143, 46),
            new Color(209, 149, 51),
            new Color(219, 164, 47),
            new Color(149, 88, 33),
            new Color(209, 155, 47),
            new Color(189, 121, 38),
            new Color(193, 126, 47),
            new Color(214, 152, 51),
            new Color(168, 105, 38),
            new Color(189, 118, 40),
            new Color(156, 97, 29),
            new Color(161, 97, 23),
            new Color(195, 128, 49),
            new Color(208, 145, 42),
            new Color(208, 144, 46),
            new Color(219, 157, 56),
            new Color(195, 125, 40),
            new Color(159, 94, 30),
            new Color(198, 131, 42),
            new Color(212, 152, 56),
            new Color(205, 143, 42),
            new Color(202, 142, 44),
            new Color(204, 138, 41),
            new Color(198, 132, 45),
            new Color(196, 130, 46),
            new Color(195, 128, 39),
            new Color(198, 133, 41),
            new Color(180, 116, 45),
            new Color(70, 26, 0),
            new Color(200, 132, 47),
            new Color(189, 123, 45),
            new Color(188, 120, 37),
            new Color(187, 118, 40),
            new Color(201, 130, 48),
            new Color(189, 119, 34),
            new Color(206, 137, 46),
            new Color(207, 137, 51),
            new Color(191, 125, 38),
            new Color(199, 138, 45),
            new Color(151, 88, 37),
            new Color(184, 116, 45),
            new Color(150, 82, 35),
            new Color(212, 155, 48),
            new Color(213, 147, 51),
            new Color(205, 141, 43),
            new Color(210, 152, 42),
            new Color(212, 151, 45),
            new Color(205, 138, 49),
            new Color(209, 145, 47),
            new Color(204, 141, 46),
            new Color(206, 136, 50),
            new Color(213, 156, 41),
            new Color(136, 73, 40),
            new Color(171, 108, 29),
            new Color(205, 138, 47),
            new Color(208, 143, 43),
            new Color(195, 127, 46),
            new Color(188, 121, 42),
            new Color(175, 112, 35),
            new Color(204, 133, 53),
            new Color(195, 126, 48),
            new Color(176, 110, 34),
            new Color(173, 111, 28),
            new Color(152, 87, 47),
            new Color(207, 141, 47),
            new Color(201, 132, 37),
            new Color(211, 159, 47),
            new Color(228, 179, 58),
            new Color(159, 100, 32),
            new Color(116, 62, 26),
            new Color(198, 136, 37),
            new Color(214, 153, 46),
            new Color(212, 148, 51),
            new Color(147, 84, 43),
            new Color(197, 126, 38),
            new Color(209, 150, 46),
            new Color(204, 138, 44),
            new Color(211, 145, 51),
            new Color(219, 166, 50),
            new Color(208, 143, 51),
            new Color(211, 153, 46),
            new Color(211, 149, 48),
            new Color(187, 118, 43),
            new Color(206, 137, 44),
            new Color(170, 106, 34),
            new Color(149, 88, 34),
            new Color(97, 47, 0),
            new Color(167, 103, 32),
            new Color(109, 48, 4),
            new Color(174, 119, 37),
            new Color(155, 90, 32),
            new Color(151, 87, 39),
            new Color(155, 94, 29),
            new Color(161, 102, 32),
            new Color(200, 131, 38),
            new Color(200, 135, 45),
            new Color(213, 151, 52),
            new Color(205, 128, 46),
            new Color(193, 127, 41),
            new Color(186, 119, 40),
            new Color(186, 118, 35),
            new Color(203, 136, 47)
    };
}
