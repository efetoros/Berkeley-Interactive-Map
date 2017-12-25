/**
 * Created by efetoros on 4/16/17.
 */
import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class testclass {

    @Test
    public void testGetUllon() {
        double result = Rasterer.getUllon("1342122.png");
        assertEquals(result,-122.28126525878906,0);

    }

    @Test
    public void testGetUllat() {
        double result = Rasterer.getUllat("1342122.png");
        assertEquals(result,37.86617312960056,0);

    }

    @Test
    public void testGetLrlat() {
        double result = Rasterer.getLrlat("1342122.png");
        assertEquals(result,37.86563099589965,0);

    }

    @Test
    public void testGetLrlon() {
        double result = Rasterer.getLrlon("1342122.png");
        assertEquals(result,-122.28057861328125,0);

    }


}
