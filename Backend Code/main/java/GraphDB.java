import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /**
     * Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc.
     */
    HashMap<Long, Node2> graph = new HashMap<>();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    static class Node2 implements Comparable<Node2> {
        Long id;
        String longitude;
        String latitude;
        //        Hashset below might contain nodes but for now we'll leave it as String file name'
        HashSet<Long> edges;
        double priorityNumber;
        double fromstarter;


        Node2(Long id, String lon, String lat) {
            this.id = id;
            this.longitude = lon;
            this.latitude = lat;
            this.edges = new HashSet<>();
            this.priorityNumber = 0;
            this.fromstarter = 0;
        }

        @Override
        public int compareTo(Node2 n2) {
            if (this.priorityNumber > n2.priorityNumber) {
                return 1;
            } else if (this.priorityNumber < n2.priorityNumber) {
                return -1;
            } else {
                return 0;
            }
        }
    }


    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        Iterable<Node2> vertices = graph.values();
        ArrayList<Node2> cut = new ArrayList<>();
        for (Node2 v : vertices) {
            if (graph.get(v.id).edges.size() == 0) {
                cut.add(v);
            }

        }
        for (Node2 m : cut) {
            graph.remove(m.id);
        }
        System.out.println(graph.size());
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        Iterable<Long> vertices = graph.keySet();
        return vertices;
    }

    /**
     * Returns ids of all vertices adjacent to v.
     */
    Iterable<Long> adjacent(long v) {
        Iterable<Long> adj = graph.get(v).edges;
        return adj;
    }

    /**
     * Returns the Euclidean distance between vertices v and w, where Euclidean distance
     * is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ).
     */
    double distance(long v, long w) {
        double lonDifference = Double.parseDouble(graph.get(v).longitude)
                - Double.parseDouble(graph.get(w).longitude);
        double latDifference = Double.parseDouble(graph.get(v).latitude)
                - Double.parseDouble(graph.get(w).latitude);
        double distance = Math.sqrt(Math.pow(lonDifference, 2) + Math.pow(latDifference, 2));
        return distance;
    }

    double distanceforclosest(long v, double lon, double lat) {
        double lonDifference = Double.parseDouble(graph.get(v).longitude) - lon;
        double latDifference = Double.parseDouble(graph.get(v).latitude) - lat;
        double distance = Math.sqrt(Math.pow(lonDifference, 2) + Math.pow(latDifference, 2));
        return distance;
    }

    /**
     * Returns the vertex id closest to the given longitude and latitude.
     */
    long closest(double lon, double lat) {
        Iterable<Long> vertices = graph.keySet();
        Object[] result = new Object[2];
        for (Long v : vertices) {
            double distance = distanceforclosest(v, lon, lat);
            if (result[1] == null || distance < (Double) result[1]) {
                result[0] = v;
                result[1] = distance;
            }
        }
        return (Long) result[0];
    }

    /**
     * Longitude of vertex v.
     */
    double lon(long v) {
        return Double.parseDouble(graph.get(v).longitude);
    }

    /**
     * Latitude of vertex v.
     */
    double lat(long v) {
        return Double.parseDouble(graph.get(v).latitude);
    }

    void addEdge(String n1, String n2) {
        graph.get(Long.parseLong(n1)).edges.add(Long.parseLong(n2));
        graph.get(Long.parseLong(n2)).edges.add(Long.parseLong(n1));
    }
}
