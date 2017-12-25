import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double slo, double sla,
                                                double dlo, double dla) {
        HashMap<Long, Double> bestKnownDistance = new HashMap<>();
        HashMap<Long, Long> edges = new HashMap<>();
        GraphDB.Node2 start = g.graph.get(g.closest(slo, sla));
        GraphDB.Node2 end = g.graph.get(g.closest(dlo, dla));
        GraphDB.Node2 current = start;
        PriorityQueue<GraphDB.Node2> collector = new PriorityQueue<>();
        collector.add(current);
        bestKnownDistance.put(start.id, 0.0);

        while (!collector.isEmpty()) {
            current = collector.remove();
            if (g.distance(current.id, end.id) == 0) {
                break;
            }
            for (Long neighbor : current.edges) {
                GraphDB.Node2 childnode = g.graph.get(neighbor);
                if (!edges.containsKey(childnode.id)) {
                    edges.put(neighbor, current.id);

                    childnode.fromstarter = current.fromstarter
                            + g.distance(current.id, childnode.id);

                    bestKnownDistance.put(neighbor, childnode.fromstarter);


                    double prioritynumber = priorityNumberGetter(bestKnownDistance,
                            current, childnode, end, g);
                    childnode.priorityNumber = prioritynumber;

                    collector.add(childnode);

                } else {
                    if (bestKnownDistance.get(childnode.id) >  current.fromstarter
                            + g.distance(current.id, childnode.id)) {


                        childnode.fromstarter = current.fromstarter
                                + g.distance(current.id, childnode.id);

                        edges.put(neighbor, current.id);
                        bestKnownDistance.put(childnode.id, childnode.fromstarter);


                        double prioritynumber = priorityNumberGetter(bestKnownDistance,
                                current, childnode, end, g);
                        childnode.priorityNumber = prioritynumber;

                        collector.add(childnode);


                    }

                }
            }
        }
        LinkedList<Long> result = new LinkedList<>();
        Long key = end.id;
        while (key != start.id) {
            result.add(key);
            key = edges.get(key);
        }
        result.add(start.id);
        Collections.reverse(result);
        return result;
    }

    public static double priorityNumberGetter(HashMap<Long, Double> best, GraphDB.Node2
            currentnode, GraphDB.Node2 childNode, GraphDB.Node2 endNode, GraphDB g) {
        return best.get(currentnode.id) + g.distance(currentnode.id, childNode.id)
                + g.distance(childNode.id, endNode.id);

    }
}
