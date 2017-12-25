import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by efetoros on 4/15/17.
 */
public class Quadtree {
    Node root;

    static class Node implements Comparable<Node> {
        String filename;
        Node topLeft;
        Node topRight;
        Node bottomLeft;
        Node bottomRight;
        double Ullon;
        double Ullat;
        double Lrlon;
        double Lrlat;
        int depth;

        Node(String filename, Node TL, Node TR, Node BL, Node BR, double Ullon,
             double Ullat, double Lrlon, double Lrlat, int depth) {
            this.filename = filename;
            this.topLeft = TL;
            this.topRight = TR;
            this.bottomLeft = BL;
            this.bottomRight = BR;
            this.Ullon = Ullon;
            this.Ullat = Ullat;
            this.Lrlon = Lrlon;
            this.Lrlat = Lrlat;
            this.depth = depth;
        }

        public boolean intersects(double Query_Ullon, double Query_Ullat,
                                  double Query_Lrlon, double Query_Lrlat) {
            if ((this.Ullon >= Query_Ullon || this.Lrlon >= Query_Ullon) &&
                    (this.Ullon <= Query_Lrlon || this.Lrlon <= Query_Lrlon) &&
                    (this.Ullat <= Query_Ullat || this.Lrlat <= Query_Ullat) &&
                    (this.Ullat >= Query_Lrlat || this.Lrlat >= Query_Lrlat)) {
                return true;
            } else {
                return false;
            }
        }

        public ArrayList<Node> FindMatchingNodes(double Query_Ullon, double Query_Ullat, double Query_Lrlon,
                                                 double Query_Lrlat, double Query_width) {
            ArrayList<Node> result = new ArrayList<>();
            if (this.intersects(Query_Ullon, Query_Ullat, Query_Lrlon, Query_Lrlat)) {
                if (this.depth == 7 || Rasterer.getLonDpp(this.Lrlon, this.Ullon, 256) <=
                        Rasterer.getLonDpp(Query_Lrlon, Query_Ullon, Query_width)) {
                    result.add(this);
                } else {
                    result.addAll(this.topLeft.FindMatchingNodes(Query_Ullon, Query_Ullat, Query_Lrlon, Query_Lrlat,
                            Query_width));
                    result.addAll(this.topRight.FindMatchingNodes(Query_Ullon, Query_Ullat, Query_Lrlon, Query_Lrlat,
                            Query_width));
                    result.addAll(this.bottomLeft.FindMatchingNodes(Query_Ullon, Query_Ullat, Query_Lrlon, Query_Lrlat,
                            Query_width));
                    result.addAll(this.bottomRight.FindMatchingNodes(Query_Ullon, Query_Ullat, Query_Lrlon, Query_Lrlat,
                            Query_width));
                }
            }
            return result;

        }

        @Override
        public int compareTo(Node o) {
            if (this.Ullon < o.Ullon) {
                return -1;
            } else if (this.Ullon > o.Ullon) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public Quadtree() {
        this.root = new Node("root.png", null, null, null, null,
                -122.2998046875, 37.892195547244356, -122.2119140625, 37.82280243352756, 0);
        double dividingfactorlat = (37.892195547244356 - 37.82280243352756) / 2;
        double dividingFactorlon = ((-122.2119140625) - (-122.2998046875)) / 2;
        this.root.topLeft = RecursiveBuild("1", -122.2998046875, 37.892195547244356, -122.2119140625 - dividingFactorlon, 37.82280243352756 + dividingfactorlat, dividingfactorlat / 2, dividingFactorlon / 2, 1);
        this.root.topRight = RecursiveBuild("2", -122.2998046875 + dividingFactorlon, 37.892195547244356, -122.2119140625, 37.82280243352756 + dividingfactorlat, dividingfactorlat / 2, dividingFactorlon / 2, 1);
        this.root.bottomLeft = RecursiveBuild("3", -122.2998046875, 37.892195547244356 - dividingfactorlat, -122.2119140625 - dividingFactorlon, 37.82280243352756, dividingfactorlat / 2, dividingFactorlon / 2, 1);
        this.root.bottomRight = RecursiveBuild("4", -122.2998046875 + dividingFactorlon, 37.892195547244356 - dividingfactorlat, -122.2119140625, 37.82280243352756, dividingfactorlat / 2, dividingFactorlon / 2, 1);

    }

    public Node RecursiveBuild(String filename, double Ullon, double Ullat, double Lrlon, double Lrlat,
                               double dividingfactorlat, double dividingfactorlon, int depth) {
        if (depth == 7) {
            filename = filename + ".png";
            return new Node(filename, null, null, null, null, Ullon,
                    Ullat, Lrlon, Lrlat, 7);
        } else {
            Node TL = RecursiveBuild(filename + "1", Ullon, Ullat, Lrlon - dividingfactorlon, Lrlat + dividingfactorlat, dividingfactorlat / 2, dividingfactorlon / 2, depth + 1);
            Node TR = RecursiveBuild(filename + "2", Ullon + dividingfactorlon, Ullat, Lrlon, Lrlat + dividingfactorlat, dividingfactorlat / 2, dividingfactorlon / 2, depth + 1);
            Node BL = RecursiveBuild(filename + "3", Ullon, Ullat - dividingfactorlat, Lrlon - dividingfactorlon, Lrlat, dividingfactorlat / 2, dividingfactorlon / 2, depth + 1);
            Node BR = RecursiveBuild(filename + "4", Ullon + dividingfactorlon, Ullat - dividingfactorlat, Lrlon, Lrlat, dividingfactorlat / 2, dividingfactorlon / 2, depth + 1);
            filename = filename + ".png";
            return new Node(filename, TL, TR, BL, BR, Ullon, Ullat,
                    Lrlon, Lrlat, depth);
        }

    }

    public static HashMap<Double, ArrayList<Node>> GroupByLat(ArrayList<Node> Alist) {
        HashMap<Double, ArrayList<Node>> hashMap = new HashMap<>();
        for (Node node : Alist) {
            if (!hashMap.containsKey(node.Ullat)) {
                ArrayList<Node> list = new ArrayList<>();
                list.add(node);

                hashMap.put(node.Ullat, list);
            } else {
                hashMap.get(node.Ullat).add(node);
            }
        }
        return hashMap;
    }

    public static String[][] StringSort(ArrayList<Node> map) {
        HashMap<Double, ArrayList<Node>> sorted = GroupByLat(map);
        Object[] keys = sorted.keySet().toArray();
        java.util.Arrays.sort(keys, Collections.reverseOrder());
        String[][] result = new String[sorted.size()][sorted.get(keys[0]).size()];
        int row = 0;
        int col = 0;
        for (Object key : keys) {
            ArrayList<Node> he = sorted.get(key);
            for (Node node : he) {
                result[row][col] = "img/" + node.filename;
                col = col + 1;
            }
            col = 0;
            row = row + 1;
        }
        return result;
    }


    public static void main(String[] args) {
        Quadtree me = new Quadtree();
//
//        ArrayList<Node> hi = me.root.FindMatchingNodes(-122.24163047377972, 37.87655856892288,
//                -122.24053369025242, 37.87548268822065, 892.0);
//        Collections.sort(hi);
//        for (Node node : hi) {
//            System.out.print(node.filename +"        ");
//            System.out.print(node.Ullat+"        ");
//            System.out.println(node.Lrlat+"        ");
//        }
//        System.out.println();
//        HashMap<Double, ArrayList<Node>> bye = GroupByLat(hi);
//        Object[] keys = bye.keySet().toArray();
//        java.util.Arrays.sort(keys, Collections.reverseOrder());
//
//        for(Object key : keys) {
//            ArrayList<Node> he = bye.get(key);
//            for (Node node : he)
//            System.out.println(node.filename);
//        }
//        System.out.println();
//        System.out.println();
//        String[][] result = StringSort(hi);
//        for (int i = 0; i < 3; i ++) {
//            for (int m = 0; m < 3; m ++) {
//                System.out.println(result[i][m]);
//            }
//        }
//        System.out.println();
//        System.out.println();
//        HashMap<Integer, String> map = new HashMap<Integer, String>();
//        map.put (1, "Mark");
//        map.put (2, "Mark");
//        map.put (3, "Tarryn");
//        ArrayList<String> list = new ArrayList<String>(map.values());
//        for (String s : list) {
//            System.out.println(s);
//        }


//        System.out.println();
//            System.out.println(bye);

    }
}


