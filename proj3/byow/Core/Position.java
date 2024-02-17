package byow.Core;

public class Position {
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
        return String.format("(%s, %s)", x, y);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Position)) {
            return false;
        }

        Position c = (Position) o;

        return x == c.x() && y == c.y();
    }
}
