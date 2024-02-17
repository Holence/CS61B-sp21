package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 70;
    private static final int HEIGHT = 70;
    private TERenderer ter;
    TETile[][] world;
    Random r = new Random();
    int hexSize;

    private class Position {
        private int x, y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position offset(int xOffset, int yOffset) {
            return new Position(x + xOffset, y + yOffset);
        }

        @Override
        public String toString() {
            return String.format("%s, %s", x, y);
        }
    }

    HexWorld(int hexSize) {
        this.hexSize = hexSize;
        ter = new TERenderer();
        world = new TETile[WIDTH][HEIGHT];
        initialize();

        Position start = new Position(30, 10);
        Position cursor;
        // 起点向西北走hexSize次
        for (int i = 0; i < hexSize; i++) {
            // 向东北画
            cursor = start;
            for (int j = 0; j < hexSize + i; j++) {
                addHexagon(cursor, randomTile());
                cursor = northeastHexStart(cursor);
            }
            start = northwestHexStart(start);
        }
        start = northeastHexStart(start);

        // 起点向北走hexSize-1次
        for (int i = hexSize - 2; i >= 0; i--) {
            // 向东北画
            cursor = start;
            for (int j = 0; j < hexSize + i; j++) {
                addHexagon(cursor, randomTile());
                cursor = northeastHexStart(cursor);
            }
            start = northHexStart(start);
        }
    }

    private void initialize() {
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        ter.initialize(WIDTH, HEIGHT);
        // initialize tiles
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    private void addTile(Position p, TETile t) {
        world[p.x][p.y] = TETile.colorVariant(t, 100, 100, 100, r);
    }

    private TETile randomTile() {
        int tileNum = r.nextInt(6);
        switch (tileNum) {
            case 0:
                return Tileset.WALL;
            case 1:
                return Tileset.FLOWER;
            case 2:
                return Tileset.GRASS;
            case 3:
                return Tileset.TREE;
            case 4:
                return Tileset.SAND;
            default:
                return Tileset.WATER;
        }
    }

    /**
     * 北方邻居六边形的起点
     * @param p
     * @return
     */
    private Position northHexStart(Position p) {
        return p.offset(0, hexSize * 2);
    }

    /**
     * 西北方邻居六边形的起点
     * @param p
     * @return
     */
    private Position northwestHexStart(Position p) {
        int maxRowLength = hexSize + (hexSize - 1) * 2;
        return p.offset(hexSize - 1 - maxRowLength, hexSize);
    }

    /**
     * 东北方邻居六边形的起点
     * @param p
     * @return
     */
    private Position northeastHexStart(Position p) {
        int maxRowLength = hexSize + (hexSize - 1) * 2;
        return p.offset(maxRowLength - (hexSize - 1), hexSize);
    }

    private void addHexagon(Position p, TETile t) {
        // aaaaaaa
        //  aaaaa 
        // . aaa  
        // p在.处，cursor逐层向上
        int rowLength = hexSize;
        Position cursor;
        for (int i = 0; i < hexSize; i++) {
            cursor = p.offset(hexSize - 1 - i, 0);
            for (int j = 0; j < rowLength; j++) {
                addTile(cursor, t);
                cursor = cursor.offset(1, 0);
            }
            rowLength += 2;
            p = p.offset(0, 1);
        }
        //   aaa  
        //  aaaaa 
        // aaaaaaa
        // p在左下角的a，cursor逐层向上
        rowLength -= 2;
        for (int i = hexSize - 1; i >= 0; i--) {
            cursor = p.offset(hexSize - 1 - i, 0);
            for (int j = 0; j < rowLength; j++) {
                addTile(cursor, t);
                cursor = cursor.offset(1, 0);
            }
            rowLength -= 2;
            p = p.offset(0, 1);
        }
    }

    private void render() {
        ter.renderFrame(world);
    }

    public static void main(String[] args) {
        HexWorld h = new HexWorld(4);
        h.render();
    }

}
