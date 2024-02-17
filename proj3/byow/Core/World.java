package byow.Core;

import java.util.Random;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class World {
    private static final int WIDTH = 70;
    private static final int HEIGHT = 70;
    private TETile[][] world;
    private Random randomizer = new Random();

    public enum Orientation {
        UP, DOWN, RIGHT, LEFT
    }

    World() {

        world = new TETile[WIDTH][HEIGHT];
        initialize();

        Position start = new Position(30, 30);
        Exit e = new Exit(start, Orientation.UP);
        Room r = Room.randomRoom(e, 15, 15, randomizer);
        r.addToMap(this);

        e = r.getExit(randomizer);
        Hallway h = Hallway.randomHallway(e, 20, randomizer);
        h.addToMap(this);
        Hallway he;

        System.out.println(h);
        e = h.getExit(randomizer);
        he = Hallway.randomHallway(e, 9, randomizer);
        he.addToMap(this);

    }

    private void initialize() {
        // initialize tiles
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public TETile[][] getWorldTile() {
        return world;
    }

    public void addTile(Position p, TETile t) {
        world[p.x()][p.y()] = TETile.colorVariant(t, 100, 100, 100, randomizer);
    }

    public void addTile(int x, int y, TETile t) {
        world[x][y] = TETile.colorVariant(t, 100, 100, 100, randomizer);
    }

    public static void main(String[] args) {
        World w = new World();
        TERenderer ter = new TERenderer();
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(w.getWorldTile());
    }
}
