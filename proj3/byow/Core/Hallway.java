package byow.Core;

import static byow.Core.RandomUtils.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byow.Core.World.Orientation;
import byow.TileEngine.Tileset;

public class Hallway extends Room {

    private Orientation orientation;

    /**
     * 
     * @param p
     * @param o
     * @param width 走廊的宽度，1或2
     * @param length
     */
    public Hallway(Position p, Orientation o, int width, int length) {
        // super();
        orientation = o;
        switch (orientation) {
            case Orientation.UP:
                // wffw
                // wffw
                // wffw
                // .ffw
                west = p.x();
                south = p.y();
                east = p.x() + width + 1;
                north = p.y() + (length - 1);
                break;
            case Orientation.DOWN:
                // .ffw
                // wffw
                // wffw
                // wffw
                west = p.x();
                north = p.y();
                east = p.x() + width + 1;
                south = p.y() - (length - 1);
                break;
            case Orientation.LEFT:
                // wwww
                // ffff
                // ffff
                // www.
                east = p.x();
                south = p.y();
                north = p.y() + width + 1;
                west = p.x() - (length - 1);
                break;
            case Orientation.RIGHT:
                // wwww
                // ffff
                // ffff
                // .www
                west = p.x();
                south = p.y();
                north = p.y() + width + 1;
                east = p.x() + (length - 1);
        }
        useablePostion = allWalls();
    }

    private List<Position> allWalls() {
        int x, y;
        List<Position> wall = new ArrayList<>();
        switch (orientation) {
            case Orientation.UP:
            case Orientation.DOWN:
                // Wall at LEFT
                x = west;
                for (y = south; y <= north; y++) {
                    wall.add(new Position(x, y));
                }
                // Wall at RIGHT
                x = east;
                for (y = south; y <= north; y++) {
                    wall.add(new Position(x, y));
                }
                break;
            case Orientation.LEFT:
            case Orientation.RIGHT:
                // Wall at UP
                y = north;
                for (x = west; x <= east; x++) {
                    wall.add(new Position(x, y));
                }
                // Wall at DOWN
                y = south;
                for (x = west; x <= east; x++) {
                    wall.add(new Position(x, y));
                }
                break;
        }
        return wall;
    }

    @Override
    public void addToMap(World w) {
        int x, y;
        super.addToMap(w);
        switch (orientation) {
            case Orientation.UP:
            case Orientation.DOWN:
                y = south;
                for (x = west + 1; x < east; x++) {
                    w.addTile(x, y, Tileset.FLOOR);
                }
                y = north;
                for (x = west + 1; x < east; x++) {
                    w.addTile(x, y, Tileset.FLOOR);
                }
                break;
            case Orientation.LEFT:
            case Orientation.RIGHT:
                x = east;
                for (y = south + 1; y < north; y++) {
                    w.addTile(x, y, Tileset.FLOOR);
                }
                x = west;
                for (y = south + 1; y < north; y++) {
                    w.addTile(x, y, Tileset.FLOOR);
                }
                break;
        }
    }

    public static Hallway randomHallway(Exit e, int maxLength, Random r) {
        return new Hallway(e.getPos(), e.getOrientation(), uniform(r, 1, 3), uniform(r, MIN_LENGTH, maxLength));
    }

    /**
     * 随机在Hallway的墙壁上生成通往Hallway的出口
     */
    public Exit getExit(Random r) {
        Exit e = null;
        switch (orientation) {
            case Orientation.UP:
            case Orientation.DOWN:

                switch (uniform(r, 2)) {
                    case 0:
                        // LEFT
                        e = Exit.randomExit(this, Orientation.LEFT, r);
                        break;
                    case 1:
                        // RIGHT
                        e = Exit.randomExit(this, Orientation.RIGHT, r);
                        break;
                }
                break;
            case Orientation.LEFT:
            case Orientation.RIGHT:
                switch (uniform(r, 2)) {
                    case 0:
                        // UP
                        e = Exit.randomExit(this, Orientation.UP, r);
                        break;
                    case 1:
                        // DOWN
                        e = Exit.randomExit(this, Orientation.DOWN, r);
                        break;
                }
                break;
        }
        System.out.println(e);
        if (checkExitValid(e)) {
            for (Position position : e.getPosList()) {
                useablePostion.remove(position);
            }
            return e;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("x: [%s %s]\ny: [%s %s] - %s", west, east, south, north, orientation);
    }
}
