package byow.Core;

import static byow.Core.RandomUtils.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byow.Core.World.Orientation;

public class Exit {
    private Position pos;
    private Orientation orientation;

    public Exit(Position pos, Orientation orientation) {
        this.pos = pos;
        this.orientation = orientation;
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

    @Override
    public String toString() {
        return String.format("%s %s - %s", pos.x(), pos.y(), orientation.toString());
    }

    public List<Position> getPosList() {
        int x = pos.x(), y = pos.y();
        List<Position> wall = new ArrayList<>();
        switch (orientation) {
            case Orientation.UP:
            case Orientation.DOWN:
                for (int i = 0; i < 4; i++) {
                    wall.add(new Position(x + i, y));
                }
                break;
            case Orientation.LEFT:
            case Orientation.RIGHT:
                for (int i = 0; i < 4; i++) {
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
                return new Exit(new Position(uniform(r, room.west, room.east - 2), room.north), Orientation.UP);
            case Orientation.DOWN:
                return new Exit(new Position(uniform(r, room.west, room.east - 2), room.south), Orientation.DOWN);
            case Orientation.LEFT:
                return new Exit(new Position(room.west, uniform(r, room.south, room.north - 2)), Orientation.LEFT);
            case Orientation.RIGHT:
                return new Exit(new Position(room.east, uniform(r, room.south, room.north - 2)), Orientation.RIGHT);
            default:
                return null;
        }
    }
}
