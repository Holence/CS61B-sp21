package byow.Core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import byow.Core.Exit.Orientation;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class World {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 80;

    public static final int RETRY_TIMES = 6;
    public static final int RANDOM_MAX_LENGTH = Math.min(WIDTH, HEIGHT) / 4;
    private TETile[][] world;
    private Random randomizer = new Random();

    World(long seed) {
        randomizer.setSeed(seed);

        world = new TETile[WIDTH][HEIGHT];
        initialize();

        // Room/Hallway的队列，从队头取出当前的Room/Hallway
        // 根据是Room还是Hallway去探索周边，将周边有效的Room/Hallway加入队列
        // 并用Room.addMap将四周的墙壁绘制到图上（Hallway也用Room.addMap的方法绘制）
        // Exit是在最后绘制的
        Deque<Room> roomDeque = new ArrayDeque<>();

        // 有效的Exit（Room/Hallway或Hallway/Hallway的连接口）
        List<Exit> validExitList = new ArrayList<>();

        Room currentRoom;
        Room newRoom;
        // initial room
        currentRoom = Room.randomRoom(new Exit(new Position(WIDTH / 2, HEIGHT / 2), Orientation.UP, 1), RANDOM_MAX_LENGTH,
                RANDOM_MAX_LENGTH, randomizer);
        currentRoom.addToMap(this);
        roomDeque.add(currentRoom);
        while (!roomDeque.isEmpty()) {
            // 对于每个currentRoom
            currentRoom = roomDeque.removeFirst();

            // 如果是Hallway，尝试在EndExit上生成Room
            for (int i = 0; i < RETRY_TIMES; i++) {
                if (currentRoom.getClass().equals(Hallway.class)) {
                    newRoom = ((Hallway) currentRoom).exploreRandomRoom(world, randomizer);
                    if (newRoom != null) {
                        newRoom.addToMap(this);
                        roomDeque.addLast(newRoom);
                        break;
                    }
                }
            }

            // 不论是Room还是Hallway，都尝试在wall上生成Hallway
            for (int i = 0; i < RETRY_TIMES; i++) {
                newRoom = currentRoom.exploreRandomHallway(world, randomizer);
                if (newRoom != null) {
                    newRoom.addToMap(this);
                    roomDeque.addLast(newRoom);
                }
            }

            // 记录有效的Exit
            validExitList.addAll(currentRoom.getExitList());
        }

        // 最后绘制Exit
        for (Exit validExit : validExitList) {
            validExit.addToMap(this);
        }
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
        World w = new World(114514);
        TERenderer ter = new TERenderer();
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(w.getWorldTile());
    }
}
