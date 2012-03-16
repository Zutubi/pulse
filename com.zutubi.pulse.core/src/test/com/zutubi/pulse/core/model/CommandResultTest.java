package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;

public class CommandResultTest extends PulseTestCase
{
    public void testCIB249()
    {
        CommandResult result = new CommandResult();
        result.addFeature(Feature.Level.ERROR, "Error features detected");
        result.addFeature(Feature.Level.ERROR, "Error features detected");
        assertEquals(1, result.getFeatures().size());
    }
}
