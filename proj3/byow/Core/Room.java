package byow.Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static byow.Core.RandomUtils.uniform;
import byow.Core.World.Orientation;
import byow.TileEngine.Tileset;

public class Room {
    static int MIN_LENGTH = 6;
    int east, south, west, north;

    List<Position> useablePostion;

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

        switch (e.getOrientation()) {
            case Orientation.UP:
                pp = pp.offset(uniform(r, (int) -width / 2, (int) width / 2), 0);
                break;
            case Orientation.DOWN:
                pp = pp.offset(uniform(r, (int) -width / 2, (int) width / 2), -height);
                break;
            case Orientation.LEFT:
                pp = pp.offset(-width, uniform(r, (int) -height / 2, (int) height / 2));
                break;
            case Orientation.RIGHT:
                pp = pp.offset(0, uniform(r, (int) -height / 2, (int) height / 2));
                break;
        }
        return new Room(pp, width, height);
    }

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
    public Exit getExit(Random r) {
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

    @Override
    public String toString() {
        return String.format("x: [%s %s]\ny: [%s %s]", west, east, south, north);
    }
}