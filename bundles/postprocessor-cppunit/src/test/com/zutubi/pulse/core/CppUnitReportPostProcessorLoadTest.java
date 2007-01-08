package com.zutubi.pulse.core;

/**
 */
public class CppUnitReportPostProcessorLoadTest extends FileLoaderTestBase
{
    public void testBasic() throws PulseException
    {
        referenceHelper("cppunit");
    }

    public void testNoFail() throws PulseException
    {
        CppUnitReportPostProcessor pp = referenceHelper("nofail");
        assertFalse(pp.getFailOnFailure());
    }
}
