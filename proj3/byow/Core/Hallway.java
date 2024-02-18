package byow.Core;

import static byow.Core.RandomUtils.uniform;
import static byow.Core.World.RANDOM_MAX_LENGTH;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byow.Core.Exit.Orientation;
import byow.TileEngine.TETile;

public class Hallway extends Room {
    private int width; // 指floor的宽度（不包含wall），为1或2
    private Orientation orientation;

    /**
     * 
     * @param p
     * @param o
     * @param width 走廊的宽度，1或2
     * @param length
     */
    public Hallway(Position p, Orientation o, int width, int length) {
        super();
        orientation = o;
        this.width = width;
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

    public static Hallway randomHallway(Exit e, int maxLength, Random r) {
        return new Hallway(e.getPos(), e.getOrientation(), e.getWidth(), uniform(r, MIN_LENGTH, maxLength));
    }

    /**
     * 随机在Hallway的墙壁上生成通往Hallway的Exit
     */
    @Override
    Exit getRandomExit(Random r) {
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
        if (checkExitValid(e)) {
            for (Position position : e.getPosList()) {
                useablePostion.remove(position);
            }
            return e;
        } else {
            return null;
        }
    }

    /**
     * Hallway终端的Exit（不是墙上的Exit）
     * @return
     */
    public Exit getEndExit() {
        switch (orientation) {
            case Orientation.UP:
                return new Exit(new Position(west, north), Orientation.UP, width);
            case Orientation.DOWN:
                return new Exit(new Position(west, south), Orientation.DOWN, width);
            case Orientation.LEFT:
                return new Exit(new Position(west, south), Orientation.LEFT, width);
            case Orientation.RIGHT:
                return new Exit(new Position(east, south), Orientation.RIGHT, width);
            default:
                return null;
        }
    }

    /**
     * 尝试在Hallway的EndExit上生成Room
     * 可能因为生成的新Room在地图上超出边缘或覆盖到已有的floor而失败
     * @param world
     * @param r
     * @return
     */
    public Room exploreRandomRoom(TETile[][] world, Random r) {
        Room newRoom;
        Exit newExit = getEndExit();
        newRoom = Room.randomRoom(newExit, RANDOM_MAX_LENGTH, RANDOM_MAX_LENGTH, r);

        if (newRoom.isValid(world)) {
            exitList.add(newExit);
            return newRoom;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Hallway - x: [%s %s] y: [%s %s] - %s", west, east, south, north, orientation);
    }
}
