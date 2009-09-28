package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.AbstractScmIntegrationTestCase;
import com.zutubi.pulse.core.scm.ExpectedTestResults;
import com.zutubi.pulse.core.scm.api.Revision;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCvsClientIntegrationTestCase extends AbstractScmIntegrationTestCase
{
    protected DateFormat dateFormat = new SimpleDateFormat(CvsRevision.DATE_AND_TIME_FORMAT_STRING);
    protected DateFormat serverDate = new SimpleDateFormat("yyyyMMdd-HH:mm:ss z");

    protected void setUp() throws Exception
    {
        super.setUp();

        client = getClient();

        List<Revision> revisions = Arrays.asList(
                new Revision("cvstester::" + localTime("20090129-12:29:10 GMT")),
                new Revision("cvstester::" + localTime("20090129-12:35:08 GMT")),
                new Revision("cvstester::" + localTime("20090129-12:36:25 GMT")),
                new Revision("cvstester::" + localTime("20090129-12:38:18 GMT")),
                new Revision("cvstester::" + localTime("20090129-12:39:02 GMT"))
        );
        
        // installation directory is integration-test, this is independent of the module being worked with.
        prefix = "integration-test/";
        testData = new ExpectedTestResults(revisions, prefix);
    }

    protected abstract CvsClient getClient() throws IOException;

    protected String getPassword(String name) throws IOException
    {
        return CvsTestUtils.getPassword(name);
    }

    protected String localTime(String time) throws ParseException
    {
        return dateFormat.format(serverDate.parse(time));
    }
}
