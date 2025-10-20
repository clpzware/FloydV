package fr.ambient.util.pathfinder.api;

import lombok.Data;

@Data
public class Node implements Comparable<Node> {
    private int x, y, z, g, h;
    private Node parent;

    public Node(int x, int y, int z, int g, int h, Node parent) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.g = g;
        this.h = h;
        this.parent = parent;
    }

    public int f() {
        return g + h;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.f(), other.f());
    }
}
