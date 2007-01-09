package com.zutubi.pulse.core;

/**
 */
public class CppUnitReportPostProcessorLoadTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("cppunit.pp", CppUnitReportPostProcessor.class);
    }

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
