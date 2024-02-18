package byow.Core;

import static byow.Core.RandomUtils.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byow.Core.World.Orientation;
import byow.TileEngine.Tileset;

/**
 * Room/Hallway或Hallway/Hallway的连接口
 */
public class Exit {
    private Position pos;
    private Orientation orientation;
    private int width; // 指floor的宽度（不包含wall），为1或2

    public Exit(Position pos, Orientation orientation, int width) {
        this.pos = pos;
        this.orientation = orientation;
        this.width = width;
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * 包含wall的width，为3或4
     * @return
     */
    private int getTrueWidth() {
        return width + 2;
    }

    public List<Position> getPosList() {
        int x = pos.x(), y = pos.y();
        List<Position> wall = new ArrayList<>();
        switch (orientation) {
            case Orientation.UP:
            case Orientation.DOWN:
                // .ffw
                for (int i = 0; i < getTrueWidth(); i++) {
                    wall.add(new Position(x + i, y));
                }
                break;
            case Orientation.LEFT:
            case Orientation.RIGHT:
                // w
                // f
                // f
                // .
                for (int i = 0; i < getTrueWidth(); i++) {
                    wall.add(new Position(x, y + i));
                }
                break;
        }
        return wall;
    }

    public static Exit randomExit(Room room, Orientation o, Random r) {
        // -2是防止Hallway跟最边上的墙岔开
        switch (o) {
            case Orientation.UP:
                return new Exit(new Position(uniform(r, room.west, room.east - 2), room.north), Orientation.UP,
                        uniform(r, 1, 3));
            case Orientation.DOWN:
                return new Exit(new Position(uniform(r, room.west, room.east - 2), room.south), Orientation.DOWN,
                        uniform(r, 1, 3));
            case Orientation.LEFT:
                return new Exit(new Position(room.west, uniform(r, room.south, room.north - 2)), Orientation.LEFT,
                        uniform(r, 1, 3));
            case Orientation.RIGHT:
                return new Exit(new Position(room.east, uniform(r, room.south, room.north - 2)), Orientation.RIGHT,
                        uniform(r, 1, 3));
            default:
                return null;
        }
    }

    public void addToMap(World w) {
        int x = pos.x(), y = pos.y();
        switch (orientation) {
            case Orientation.UP:
            case Orientation.DOWN:
                // .ffw
                for (int i = 1; i < getTrueWidth() - 1; i++) {
                    w.addTile(new Position(x + i, y), Tileset.FLOOR);
                }
                break;
            case Orientation.LEFT:
            case Orientation.RIGHT:
                // w
                // f
                // f
                // .
                for (int i = 1; i < getTrueWidth() - 1; i++) {
                    w.addTile(new Position(x, y + i), Tileset.FLOOR);
                }
                break;
        }
    }

    @Override
    public String toString() {
        return String.format("Exit - %s %s - %s", pos.x(), pos.y(), orientation.toString());
    }
}
