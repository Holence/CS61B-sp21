package byow.Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static byow.Core.RandomUtils.uniform;
import static byow.Core.World.RANDOM_MAX_LENGTH;
import byow.Core.Exit.Orientation;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Room {
    static int MIN_LENGTH = 4;
    int east, south, west, north;
    List<Exit> exitList = new ArrayList<>();

    List<Position> useablePostion;

    public int getEast() {
        return east;
    }

    public int getSouth() {
        return south;
    }

    public int getWest() {
        return west;
    }

    public int getNorth() {
        return north;
    }

    public List<Exit> getExitList() {
        return exitList;
    }

    Room() {

    }

    /**
     * wwwwwww
     * wfffffw
     * wfffffw
     * .wwwwww
     * @param p 左下角
     * @param width 房间中地板的东西向长度
     * @param height 房间中地板的南北向长度
     */
    Room(Position p, int width, int height) {
        west = p.x();
        east = west + width + 1;
        south = p.y();
        north = south + height + 1;

        useablePostion = allWalls();
    }

    private List<Position> allWalls() {
        int x, y;
        List<Position> wall = new ArrayList<>();
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
        return wall;
    }

    public void addToMap(World w) {
        int x, y;
        // wall
        y = south;
        for (x = west; x <= east; x++) {
            w.addTile(x, y, Tileset.WALL);
        }
        y = north;
        for (x = west; x <= east; x++) {
            w.addTile(x, y, Tileset.WALL);
        }
        x = west;
        for (y = south; y <= north; y++) {
            w.addTile(x, y, Tileset.WALL);
        }
        x = east;
        for (y = south; y <= north; y++) {
            w.addTile(x, y, Tileset.WALL);
        }
        // floor
        for (x = west + 1; x < east; x++) {
            for (y = south + 1; y < north; y++) {
                w.addTile(x, y, Tileset.FLOOR);
            }
        }
    }

    public static Room randomRoom(Exit e, int maxWidth, int maxHeight, Random r) {
        int width = uniform(r, MIN_LENGTH, maxWidth);
        int height = uniform(r, MIN_LENGTH, maxHeight);
        Position pp = e.getPos();

        //  wwwwwww
        //  wfffffw
        //  wfffffw
        //  .wwwwww
        switch (e.getOrientation()) {
            case Orientation.UP:
                // 允许往左随机偏移
                pp = pp.offset(uniform(r, (int) -width / 2, 1), 0);
                break;
            case Orientation.DOWN:
                // 允许往左随机偏移
                pp = pp.offset(uniform(r, (int) -width / 2, 1), -height);
                break;
            case Orientation.LEFT:
                // 允许往下随机偏移
                pp = pp.offset(-width, uniform(r, (int) -height / 2, 1));
                break;
            case Orientation.RIGHT:
                // 允许往下随机偏移
                pp = pp.offset(0, uniform(r, (int) -height / 2, 1));
                break;
        }
        return new Room(pp, width, height);
    }

    /**
     * 如果useablePostion无法提供exit所需的占用空间，则返回false
     * @param e
     * @return
     */
    public boolean checkExitValid(Exit e) {
        boolean valid = true;
        for (Position position : e.getPosList()) {
            if (!useablePostion.contains(position)) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    /**
     * 随机在Room墙壁生成通往Hallway的出口
     */
    Exit getRandomExit(Random r) {
        Exit e = null;
        switch (uniform(r, 4)) {
            case 0:
                // UP
                e = Exit.randomExit(this, Orientation.UP, r);
                break;
            case 1:
                // DOWN
                e = Exit.randomExit(this, Orientation.DOWN, r);
                break;
            case 2:
                // LEFT
                e = Exit.randomExit(this, Orientation.LEFT, r);
                break;
            case 3:
                // RIGHT
                e = Exit.randomExit(this, Orientation.RIGHT, r);
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
     * 尝试在wall生成Hallway
     * 可能因为没有剩余的地方作为exit而失败
     * 也可能因为生成的新Hallway在地图上超出边缘或覆盖到已有的floor而失败
     * @param world
     * @param r
     * @return
     */
    public Hallway exploreRandomHallway(TETile[][] world, Random r) {
        Hallway newHallway;
        Exit newExit;

        newExit = getRandomExit(r);
        if (newExit != null) {
            newHallway = Hallway.randomHallway(newExit, RANDOM_MAX_LENGTH, r);
            if (newHallway.isValid(world)) {
                exitList.add(newExit);
                return newHallway;
            }
        }
        return null;
    }

    /**
     * 如果在地图上超出边缘或覆盖到已有的floor，则返回false
     * @param world
     * @return
     */
    public boolean isValid(TETile[][] world) {
        int WIDTH = world.length;
        int HEIGHT = world[0].length;
        if (west < 0 || east >= WIDTH || south < 0 || north >= HEIGHT) {
            return false;
        } else {
            for (int x = west; x <= east; x++) {
                for (int y = south; y <= north; y++) {
                    if (world[x][y].character() == Tileset.FLOOR.character()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    @Override
    public String toString() {
        return String.format("Room - x: [%s %s] y: [%s %s]", west, east, south, north);
    }
}