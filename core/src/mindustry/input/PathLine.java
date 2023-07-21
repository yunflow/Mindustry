package mindustry.input;

public class PathLine {
    boolean convetors;
    int startX;
    int startY;
    int endX;
    int endY;

    public PathLine(boolean convetors, int startX, int startY, int endX, int endY) {
        this.convetors = convetors;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public boolean conveyors() {
        return convetors;
    }

    public int startX() {
        return startX;
    }

    public int startY() {
        return startY;
    }

    public int endX() {
        return endX;
    }

    public int endY() {
        return endY;
    }
}