package cz.zcu.kiv.eeg.gtn.online.gui;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Marek on 17. 5. 2017.
 */
public class StimulTest {
    Stimul stimul;
    @Before
    public void setUp() throws Exception {
        stimul = new Stimul(1,"name","url1","url2","test");
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("name",stimul.getName());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(1,stimul.getId());
    }
    @Test
    public void testGetDescription() throws Exception {
        assertEquals("test",stimul.getDescription());
    }
    @Test
    public void testLoadImages() throws Exception {
        assertEquals(false,stimul.isImgFile1);
        assertEquals(false,stimul.isImgFile2);
    }

}
