package no.uib.inf252.katscan.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import no.uib.inf252.katscan.data.VoxelMatrix;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.OutputStream;

import static junit.framework.TestCase.assertNotNull;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import sun.misc.GC;

/**
 * @author Marcelo Lima
 */
public class LoadSaveHandlerTest {

    private DatLoadSaveHandler sut;

    public LoadSaveHandlerTest() {
    }

//    @BeforeClass
//    public static void setUpClass() {
//    }
//    @AfterClass
//    public static void tearDownClass() {
//    }
    @Before
    public void setUp() {
        sut = new DatLoadSaveHandler();
    }

//    @After
//    public void tearDown() {
//    }
    @Test
    public void constructionAndInitializationOfLoadSaveHandler() {
        assertNotNull(sut);
    }

    @Test
    public void testLoadData() {
        InputStream stream = sut.getClass().getResourceAsStream("/sinusveins-256x256x166.dat");

        VoxelMatrix loadedData = sut.loadData(stream);
        assertNotNull(loadedData);

        assertEquals(256, loadedData.getLength(VoxelMatrix.Axis.X));
        assertEquals(256, loadedData.getLength(VoxelMatrix.Axis.Y));
        assertEquals(166, loadedData.getLength(VoxelMatrix.Axis.Z));

        assertEquals(1, loadedData.getValue(0, 0, 0));
        assertEquals(1039, loadedData.getValue(100, 100, 100));
    }

    @Test
    public void testSaveData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(20 * 20 * 20 + 6);
        VoxelMatrix grid = new VoxelMatrix(10, 10, 10);
        grid.setValue(0, 0, 0, (short)2);
        
        sut.saveData(out, grid);
        assertTrue(out.size() > 0);
        byte[] byteArray = out.toByteArray();
        assertNotNull(byteArray);
        assertTrue((byteArray[0] | (byteArray[1] << 1)) == 10);
    }
    
    @Test
    public void testLoadSaveLoad() throws IOException {
        InputStream stream = sut.getClass().getResourceAsStream("/sinusveins-256x256x166.dat");

        System.gc();
        VoxelMatrix loadedData = sut.loadData(stream);
        
        int sizeX = loadedData.getLength(VoxelMatrix.Axis.X);
        int sizeY = loadedData.getLength(VoxelMatrix.Axis.Y);
        int sizeZ = loadedData.getLength(VoxelMatrix.Axis.Z);     
        
        ByteArrayOutputStream out = new ByteArrayOutputStream(sizeX * sizeY * sizeZ + 6); //Not doubling values on purpose
        
        sut.saveData(out, loadedData);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        VoxelMatrix otherLoad = sut.loadData(in);
        in.close();
        out.close();
        in = null;
        out = null;
        System.gc();
        
        assertEquals(loadedData, otherLoad);
    }
}
