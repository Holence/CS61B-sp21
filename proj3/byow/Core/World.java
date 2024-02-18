package byow.Core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class World {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 40;

    private static final int RETRY_TIMES = 3;
    private static final int RANDOM_MAX_LENGTH = Math.min(WIDTH, HEIGHT) / 4;
    private TETile[][] world;
    private Random randomizer;

    public enum Orientation {
        UP, DOWN, RIGHT, LEFT
    }

    World() {

        randomizer = new Random();
        long seed = randomizer.nextLong();
        randomizer.setSeed(seed);
        System.out.println("Seed " + seed);

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
        // initial room
        currentRoom = Room.randomRoom(new Exit(new Position(WIDTH / 2, HEIGHT / 2), Orientation.UP, 1), RANDOM_MAX_LENGTH,
                RANDOM_MAX_LENGTH, randomizer);
        currentRoom.addToMap(this);
        roomDeque.add(currentRoom);
        while (!roomDeque.isEmpty()) {
            currentRoom = roomDeque.removeFirst();

            if (currentRoom.getClass().equals(Room.class)) {
                exploreHallway(validExitList, roomDeque, currentRoom);
            } else if (currentRoom.getClass().equals(Hallway.class)) {
                exploreRoom(validExitList, roomDeque, (Hallway) currentRoom);
                exploreHallway(validExitList, roomDeque, currentRoom);
            }
        }
        // 最后绘制Exit
        for (Exit validExit : validExitList) {
            validExit.addToMap(this);
        }
    }

    private void exploreHallway(List<Exit> validExitList, Deque<Room> roomDeque, Room currentRoom) {
        Room newRoom;
        Exit newExit;
        for (int i = 0; i < RETRY_TIMES; i++) {
            newExit = currentRoom.getRandomExit(randomizer);
            if (newExit != null) {
                newRoom = Hallway.randomHallway(newExit, RANDOM_MAX_LENGTH, randomizer);
                if (isValid(newRoom)) {
                    newRoom.addToMap(this);
                    validExitList.add(newExit);
                    roomDeque.addLast(newRoom);
                }
            }
        }
    }

    private void exploreRoom(List<Exit> validExitList, Deque<Room> roomDeque, Hallway currentRoom) {
        Exit newExit = currentRoom.getExit();
        Room newRoom = Room.randomRoom(newExit, RANDOM_MAX_LENGTH, RANDOM_MAX_LENGTH, randomizer);
        if (isValid(newRoom)) {
            newRoom.addToMap(this);
            validExitList.add(newExit);
            roomDeque.addLast(newRoom);
        }
    }

    private boolean isValid(Room r) {
        if (r.getWest() < 0 || r.getEast() >= WIDTH || r.getSouth() < 0 || r.getNorth() >= HEIGHT) {
            return false;
        } else {
            for (int x = r.getWest(); x <= r.getEast(); x++) {
                for (int y = r.getSouth(); y <= r.getNorth(); y++) {
                    if (world[x][y].character() == Tileset.FLOOR.character()) {
                        return false;
                    }
                }
            }
            return true;
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
        World w = new World();
        TERenderer ter = new TERenderer();
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(w.getWorldTile());
    }
}
