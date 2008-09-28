package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.AbstractScmIntegrationTestCase;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ExpectedTestResults;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;

public abstract class AbstractCvsClientIntegrationTestCase extends AbstractScmIntegrationTestCase
{
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss z");

    public static Revision FIRST_REVISION = null;
    public static Revision SECOND_REVISION = null;
    public static Revision THIRD_REVISION = null;
    public static Revision FOURTH_REVISION = null;
    public static Revision FIFTH_REVISION = null;

    static
    {
        try
        {
            // revisions are defined when the cvs test data is set up.
            FIRST_REVISION = CvsClient.convertRevision(new CvsRevision("cvstester", null, DATE_FORMAT.parse("20070729-12:29:10 gmt")));
            SECOND_REVISION = CvsClient.convertRevision(new CvsRevision("cvstester", null, DATE_FORMAT.parse("20070729-12:35:08 gmt")));
            THIRD_REVISION = CvsClient.convertRevision(new CvsRevision("cvstester", null, DATE_FORMAT.parse("20070729-12:36:25 gmt")));
            FOURTH_REVISION = CvsClient.convertRevision(new CvsRevision("cvstester", null, DATE_FORMAT.parse("20070729-12:38:18 gmt")));
            FIFTH_REVISION = CvsClient.convertRevision(new CvsRevision("cvstester", null, DATE_FORMAT.parse("20070729-12:39:02 gmt")));
        }
        catch (ParseException e)
        {
            // noop.
        }
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        client = getClient();

        List<Revision> revisions = Arrays.asList(FIRST_REVISION, SECOND_REVISION, THIRD_REVISION, FOURTH_REVISION, FIFTH_REVISION);
        
        // installation directory is integration-test, this is independent of the module being worked with.
        prefix = "integration-test/";
        testData = new ExpectedTestResults(revisions, prefix);
    }

    protected void tearDown() throws Exception
    {
        testData = null;
        client = null;

        super.tearDown();
    }

    protected abstract CvsClient getClient();
}
