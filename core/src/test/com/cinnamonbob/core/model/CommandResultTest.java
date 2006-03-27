package com.cinnamonbob.core.model;

import com.cinnamonbob.test.BobTestCase;

/**
 */
public class CommandResultTest extends BobTestCase
{
    public void testCIB249()
    {
        CommandResult result = new CommandResult();
        result.addFeature(Feature.Level.ERROR, "Error features detected");
        result.addFeature(Feature.Level.ERROR, "Error features detected");
        assertEquals(1, result.getFeatures().size());
    }
}
