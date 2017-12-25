import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.
    static Quadtree myQuadtree;

    /**
     * imgRoot is the name of the directory containing the images.
     * You may not actually need this for your class.
     */

    public Rasterer(String imgRoot) {
        myQuadtree = new Quadtree();
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * </p>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     * Can also be interpreted as the length of the numbers in the image
     * string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     * forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public static double getUllon(String filename) {
        double ullon = -122.2998046875;
        double dividingFactor = ((-122.2119140625) - (-122.2998046875)) / 2;
        for (char ch : filename.substring(0, filename.length() - 4).toCharArray()) {
            if (ch == '1' || ch == '3') {
                dividingFactor = dividingFactor / 2;
            } else if (ch == '2' || ch == '4') {
                ullon = ullon + dividingFactor;
                dividingFactor = dividingFactor / 2;

            }
        }
        return ullon;
    }

    public static double getUllat(String filename) {
        double ullat = 37.892195547244356;
        double dividingfactor = (37.892195547244356 - 37.82280243352756) / 2;

        for (char ch : filename.substring(0, filename.length() - 3).toCharArray()) {
            if (ch == '3' || ch == '4') {
                ullat = ullat - dividingfactor;
                dividingfactor = dividingfactor / 2;
            } else if (ch == '1' || ch == '2') {
                dividingfactor = dividingfactor / 2;
            }
        }
        return ullat;
    }

    public static double getLrlat(String filename) {
        double lrlat = 37.82280243352756;
        double dividingfactor = (37.892195547244356 - 37.82280243352756) / 2;
        for (char ch : filename.substring(0, filename.length() - 3).toCharArray()) {
            if (ch == '1' || ch == '2') {
                lrlat = lrlat + dividingfactor;
                dividingfactor = dividingfactor / 2;
            } else if (ch == '3' || ch == '4') {
                dividingfactor = dividingfactor / 2;

            }
        }
        return lrlat;
    }

    public static double getLrlon(String filename) {
        double lrlon = -122.2119140625;
        double dividingfactor = (-122.2119140625 - (-122.2998046875)) / 2;
        for (char ch : filename.substring(0, filename.length() - 3).toCharArray()) {
            if (ch == '1' || ch == '3') {
                lrlon = lrlon - dividingfactor;
                dividingfactor = dividingfactor / 2;
            } else if (ch == '2' || ch == '4') {
                dividingfactor = dividingfactor / 2;

            }
        }
        return lrlon;
    }

    public static double getLonDpp(double lrLon, double ullon, double width) {
        return (lrLon - ullon) / width;
    }


    public static Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        double a = params.get("ullon");
        double b = params.get("lrlon");
        double c = params.get("ullat");
        double d = params.get("lrlat");
        double e = params.get("w");
        ArrayList<Quadtree.Node> hi = myQuadtree.root.FindMatchingNodes(a, c, b, d, e);
        results.put("depth", hi.get(0).depth);
//        String[][] rendergrid = Quadtree.StringSort(hi);

        HashMap<Double, ArrayList<Quadtree.Node>> sorted = Quadtree.GroupByLat(hi);
        Object[] keys = sorted.keySet().toArray();
        java.util.Arrays.sort(keys, Collections.reverseOrder());
        String[][] rendergrid = new String[sorted.size()][sorted.get(keys[0]).size()];
        int row = 0;
        int col = 0;
        for (Object key : keys) {
            ArrayList<Quadtree.Node> he = sorted.get(key);
            for (Quadtree.Node node : he) {
                rendergrid[row][col] = "img/" + node.filename;
                col = col + 1;
            }
            col = 0;
            row = row + 1;
        }

        results.put("render_grid", rendergrid);
        results.put("raster_ul_lon", sorted.get(keys[0]).get(0).Ullon);
        results.put("raster_ul_lat", getUllat(rendergrid[0][0]));
        int number = rendergrid[rendergrid.length - 1].length - 1;
        results.put("raster_lr_lon", getLrlon(rendergrid[rendergrid.length - 1][number]));
        results.put("raster_lr_lat", getLrlat(rendergrid[rendergrid.length - 1][number]));
        results.put("query_success", true);
        return results;
    }

    public static void main(String[] args) {
        HashMap<String, Double> params = new HashMap<>();
        Rasterer rasterer = new Rasterer("90");
        params.put("lrlon", -122.24053369025242);
        params.put("ullon", -122.24163047377972);
        params.put("w", 892.0);
        params.put("h", 875.0);
        params.put("ullat", 37.87655856892288);
        params.put("lrlat", 37.87548268822065);
        Map<String, Object> hi = getMapRaster(params);
    }
}




