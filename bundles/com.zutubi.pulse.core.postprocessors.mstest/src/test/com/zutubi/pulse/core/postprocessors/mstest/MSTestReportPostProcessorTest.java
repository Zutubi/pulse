package com.zutubi.pulse.core.postprocessors.mstest;

import com.zutubi.pulse.core.postprocessors.api.NameConflictResolution;
import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase;

import java.io.IOException;

public class MSTestReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private static final String EXTENSION = "trx";
    
    private MSTestReportPostProcessor pp;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        pp = new MSTestReportPostProcessor(new MSTestReportPostProcessorConfiguration());
    }

    public void testSimpleFailure() throws IOException
    {
        TestSuiteResult expected =
                buildSuite(null,
                           buildSuite("TestProject1.UnitTest1", 719,
                                      new TestCaseResult("TestFailure", 719, FAILURE, "Test method TestProject1.UnitTest1.TestFailure threw exception: \n" +
                                              "System.InvalidOperationException: There was an error generating the XML document. ---> System.InvalidOperationException: Unable to generate a temporary class (result=1).\n" +
                                              "\n" +
                                              "    at System.Xml.Serialization.Compiler.Compile(Assembly parent, String ns, XmlSerializerCompilerParameters xmlParameters, Evidence evidence)\n" +
                                              "   at System.Xml.Serialization.TempAssembly.GenerateAssembly(XmlMapping[] xmlMappings, Type[] types, String defaultNamespace, Evidence evidence, XmlSerializerCompilerParameters parameters, Assembly assembly, Hashtable assemblies)\n" +
                                              "   at System.Xml.Serialization.XmlSerializer.GenerateTempAssembly(XmlMapping xmlMapping, Type type, String defaultNamespace)\n" +
                                              "   at System.Xml.Serialization.XmlSerializer..ctor(Type type, String defaultNamespace)\n" +
                                              "   at System.Xml.Serialization.XmlSerializer..ctor(Type type)")
                           )
                );

        assertEquals(expected, runProcessorAndGetTests(pp, EXTENSION));
    }

    public void testOutcomes() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("TestProject1.UnitTest1", 5033,
                    new TestCaseResult("TestInconclusive", 719, PASS, "Test result is inconclusive"),
                    new TestCaseResult("TestTimeout", 719, ERROR, "Test timed out\n" +
                            "\n" +
                            "defined message"),
                    new TestCaseResult("TestAborted", 719, ERROR, "Test aborted"),
                    new TestCaseResult("TestBlocked", 719, SKIPPED, "Test blocked"),
                    new TestCaseResult("TestNotExecuted", 719, SKIPPED, "Test not executed"),
                    new TestCaseResult("TestWarning", 719, PASS, "Warnings reported"),
                    new TestCaseResult("TestError", 719, ERROR)
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp, EXTENSION));
    }

    public void testBSP() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("UnitTests.DBConnectionTest",
                    new TestCaseResult("GetConnectionTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GetDataTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GetListOfAddressAndZipTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GetListOfLoanNumbersTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GetPropertyDataByBNKStatusTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GetStatesListTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GetStatusListTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GetWebResultsTest", -1, FAILURE, "test message"),
                    new TestCaseResult("InsertDataIntoGoogleSearchResultsTest", -1, FAILURE, "test message")
                ),
                buildSuite("UnitTests.GoogleSearchResultsTest",
                    new TestCaseResult("GetGoogleSearchResultsTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GoogleSearchResultsConstructorTest", -1, FAILURE, "test message")
                ),
                buildSuite("UnitTests.GoogleSearchTest",
                    new TestCaseResult("CXTest", -1, FAILURE, "test message"),
                    new TestCaseResult("GoogleSearchConstructorTest", -1, FAILURE, "test message"),
                    new TestCaseResult("KeyTest", -1, FAILURE, "test message"),
                    new TestCaseResult("NumTest", -1, FAILURE, "test message"),
                    new TestCaseResult("OnSearchCompletedTest", -1, FAILURE, "test message"),
                    new TestCaseResult("SafeLevelTest", -1, FAILURE, "test message"),
                    new TestCaseResult("SearchTest", -1, FAILURE, "test message"),
                    new TestCaseResult("StartTest", -1, FAILURE, "test message")
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp, EXTENSION));
    }

    public void testRandomJunkIgnored() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("TestProject1.UnitTest1", 719,
                    new TestCaseResult("TestFailure", 719, FAILURE, "Test method TestProject1.UnitTest1.TestFailure threw exception:\n" +
                            "System.InvalidOperationException: There was an error generating the XML document. ---> System.InvalidOperationException: Unable to generate a temporary class (result=1).\n" +
                            "\n" +
                            "    at System.Xml.Serialization.Compiler.Compile(Assembly parent, String ns, XmlSerializerCompilerParameters xmlParameters, Evidence evidence)\n" +
                            "   at System.Xml.Serialization.TempAssembly.GenerateAssembly(XmlMapping[] xmlMappings, Type[] types, String defaultNamespace, Evidence evidence, XmlSerializerCompilerParameters parameters, Assembly assembly, Hashtable assemblies)\n" +
                            "   at System.Xml.Serialization.XmlSerializer.GenerateTempAssembly(XmlMapping xmlMapping, Type type, String defaultNamespace)\n" +
                            "   at System.Xml.Serialization.XmlSerializer..ctor(Type type, String defaultNamespace)\n" +
                            "   at System.Xml.Serialization.XmlSerializer..ctor(Type type)\n" +
                            "              <Junk>\n" +
                            "                  tag not recognised\n" +
                            "              </Junk>"
                    )
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp, EXTENSION));
    }
    
    public void testNoDefinedOutcome() throws IOException
    {
        final TestSuiteResult result = runProcessorAndGetTests(pp, EXTENSION);
        final TestSuiteResult suiteResult = result.findSuite("Pulse.UnitTestsClass");
        assertNotNull(suiteResult);
        final TestCaseResult caseResult = suiteResult.findCase("no_outcome");
        assertNotNull(caseResult);
        assertEquals(ERROR, caseResult.getStatus());
        assertEquals("The agent process was stopped while the test was running.", caseResult.getMessage());
    }

    public void testVS2012Sample() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("SmokeTests.PathFindingTests", 292494,
                    new TestCaseResult("TestMovement_Basic", 73219, PASS),
                    new TestCaseResult("TestMovement_Choke", 75610, PASS),
                    new TestCaseResult("TestMovement_Creature", 143665, PASS)),
                buildSuite("SmokeTests.SinglePlayerlevelTests", 918748,
                    new TestCaseResult("SinglePlayerlevelPlayers_guy", 288538, PASS),
                    new TestCaseResult("SinglePlayerlevelPlayers_girl", 357728, FAILURE, "Assert.Fail failed. Failed to trigger the following abilities: fire, \n" +
                       "   at SmokeTests.SinglePlayerlevelTests.PlayerTestWork(String bio, String[] abilities)\n" +
                       "   at SmokeTests.SinglePlayerlevelTests.SinglePlayerlevelPlayers_girl()"),
                    new TestCaseResult("levelTravel_guy", 272482, PASS)
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp, EXTENSION));
    }
}
