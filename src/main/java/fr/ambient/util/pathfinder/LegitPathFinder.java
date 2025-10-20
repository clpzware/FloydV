package fr.ambient.util.pathfinder;

import fr.ambient.util.pathfinder.api.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.*;

public class LegitPathFinder {
    private static final int[][] DIRECTIONS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
            {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1},
            {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1},
            {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1}
    };

    public static List<Vec3> findPath(BlockPos start, BlockPos goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<String, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start.getX(), start.getY(), start.getZ(), 0, heuristic(start, goal), null);
        openSet.add(startNode);
        allNodes.put(key(start), startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.getX() == goal.getX() && current.getY() == goal.getY() && current.getZ() == goal.getZ()) {
                return reconstructPath(current);
            }

            for (int[] dir : DIRECTIONS) {
                int nx = current.getX() + dir[0];
                int ny = current.getY() + dir[1];
                int nz = current.getZ() + dir[2];
                BlockPos newPos = new BlockPos(nx, ny, nz);

                if (isValid(newPos)) {
                    int newG = current.getG() + ((dir[0] != 0 && dir[1] != 0 && dir[2] != 0) ? 3 : (dir[0] != 0 && dir[1] != 0 || dir[0] != 0 && dir[2] != 0 || dir[1] != 0 && dir[2] != 0) ? 2 : 1);
                    String key = key(newPos);
                    Node neighbor = allNodes.getOrDefault(key, new Node(nx, ny, nz, Integer.MAX_VALUE, heuristic(newPos, goal), null));

                    if (newG < neighbor.getG()) {
                        neighbor.setG(newG);
                        neighbor.setParent(current);
                        allNodes.put(key, neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private static int heuristic(BlockPos a, BlockPos b) {
        return Math.max(Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY())), Math.abs(a.getZ() - b.getZ()));
    }

    private static boolean isValid(BlockPos pos) {
        return Minecraft.getMinecraft().theWorld.isAirBlock(pos);
    }

    private static List<Vec3> reconstructPath(Node node) {
        List<Vec3> path = new ArrayList<>();
        while (node != null) {
            path.add(new Vec3(node.getX(), node.getY(), node.getZ())); // No smoothing
            node = node.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private static String key(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
