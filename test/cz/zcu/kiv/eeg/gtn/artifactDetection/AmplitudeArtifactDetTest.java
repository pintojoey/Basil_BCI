package cz.zcu.kiv.eeg.gtn.artifactDetection;

import org.junit.Before;
import org.junit.Test;

import cz.zcu.kiv.eeg.gtn.algorithm.math.AmplitudeArtifactDet;

import static org.junit.Assert.*;

public class AmplitudeArtifactDetTest {

	public AmplitudeArtifactDet a;

	@Before
	public void setUp() throws Exception {

		a = new AmplitudeArtifactDet();
	}

	@Test
	public void testAmplitudeArtifactDetDouble() {
		
		a = new AmplitudeArtifactDet(20);
		assertEquals(20, a.getThreshold(), 0.0003);
	}
	
	@Test
	public void testAmplitudeArtifactDet() {
		
		assertEquals(AmplitudeArtifactDet.DEFAULT_THRESHOLD, a.getThreshold(), 0.0003);
	}


	@Test
	public void testDetectArtifact1() {
		
		a = new AmplitudeArtifactDet(60);
		assertEquals(null, a.detectArtifact(TestovaciData.eSpatna));
	}
	
	@Test
	public void testDetectArtifact2() { 
		
		a = new AmplitudeArtifactDet(60);
		assertEquals(TestovaciData.eSpravna, a.detectArtifact(TestovaciData.eSpravna));
	}
	
	@Test
	public void testGetThreshold() { 
		
		assertEquals(AmplitudeArtifactDet.DEFAULT_THRESHOLD, a.getThreshold(),0.0003);
	}
	
	@Test
	public void testSetThreshold1() { 
		
		a.setThreshold(50);
		assertEquals(50, a.getThreshold(),0.0003);
	}
	
	@Test
	public void testSetThreshold2() { 
		
		a.setThreshold(-50);
		assertEquals(100, a.getThreshold(),0.0003);
	}

}
