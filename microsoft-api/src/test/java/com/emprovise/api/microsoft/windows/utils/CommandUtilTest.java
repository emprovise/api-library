package com.emprovise.api.microsoft.windows.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class CommandUtilTest {

    @Test
    public void executeCommand() throws Exception {
        String results = CommandUtil.executeCommand("tasklist");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}