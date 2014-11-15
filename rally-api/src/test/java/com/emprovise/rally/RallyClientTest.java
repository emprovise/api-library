package com.emprovise.rally;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class RallyClientTest {
	@Test
	public void extractRallyIdPrefix() {
		Assert.assertEquals("US12345", RallyClient.extractRallyIdPrefix("US12345 test"));
        Assert.assertEquals("US12345", RallyClient.extractRallyIdPrefix("The Story is US12345 - test"));
		Assert.assertEquals(null, RallyClient.extractRallyIdPrefix("updated this unknown thing"));
		Assert.assertEquals("DE12345", RallyClient.extractRallyIdPrefix("DE12345 and US12345 - test"));
		Assert.assertEquals("TA12345", RallyClient.extractRallyIdPrefix("test TA12345"));
	}
	
	@Test
	public void urlOf() {
		String expected = "https://rally1.rallydev.com/slm/rally.sp#/search?keywords=US12345";
		assertEquals(expected, RallyClient.generateRallySearchUrl("US12345"));
	}

}
