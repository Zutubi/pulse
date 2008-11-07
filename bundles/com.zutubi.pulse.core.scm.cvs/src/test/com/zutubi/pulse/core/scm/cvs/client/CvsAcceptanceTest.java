package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.cvs.CvsRevision;
import com.zutubi.util.junit.ZutubiTestCase;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.util.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CvsAcceptanceTest extends ZutubiTestCase
{
    private static final SimpleDateFormat SERVER_DATE;

    static
    {
        SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private LogInformationAnalyser analyser;
    private CvsCore cvs;

    protected void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");

        String cvsRoot = ":ext:daniel:xxxx@zutubi.com:/cvsroots/default";

        cvs = new CvsCore();
        cvs.setRoot(CVSRoot.parse(cvsRoot));

        analyser = new LogInformationAnalyser(CVSRoot.parse(cvsRoot));
    }

    protected void tearDown() throws Exception
    {
        cvs = null;
        analyser = null;
    }

    /**
     * Cvs Data: 2007.01.14
     *
     * add /dir
     * add, edit, edit, delete /dir/file.txt (06:10:00 -> 06:14:00)
     * add /dir/file2.txt  (06:21:00)
     * branch /dir BRANCH
     * add, edit, delete /dir/file3.txt (06:30:00 -> 06:33:00)
     * add, edit, delete /dir/file4.txt (06:40:00 -> 06:43:00) on BRANCH.
     *
     */

    /**
     * CIB-868: cvs scm triggers with empty changelists.  CVS is triggering builds with empty changelists. Problem
     * is either the triggering or the changelist checking.  In this test case we check the triggering, checking that
     * changes made to head to not trigger a change on the branch, and visa versa.
     *
     * @throws Exception if something unexpectedly goes wrong with this test.
     */
    public void testChangeInsulationBetweenBranchAndHead() throws Exception
    {
        String module = "acceptance-test";

        // between 6:30 and 6:34, changes are made to head.
        CvsRevision from = new CvsRevision("", "", "", SERVER_DATE.parse("2007-01-14 06:30:30 GMT"));
        CvsRevision to = new CvsRevision("", "", "", SERVER_DATE.parse("2007-01-14 06:33:30 GMT"));
        Date latestUpdate = analyser.latestUpdate(cvs.rlog(module, from, to)).getDate();
        assertEquals("2007-01-14 06:33:00 GMT", SERVER_DATE.format(latestUpdate)); // add.

        // ensure that during the same timeframe, no changes are detected on the branch.
        from = new CvsRevision("", "BRANCH", "", SERVER_DATE.parse("2007-01-14 06:30:30 GMT"));
        to = new CvsRevision("", "BRANCH", "", SERVER_DATE.parse("2007-01-14 06:33:30 GMT"));
        assertNull(analyser.latestUpdate(cvs.rlog(module, from, to)));

        // between 6:40 and 6:44, changes are made to BRANCH.
        from = new CvsRevision("", "BRANCH", "", SERVER_DATE.parse("2007-01-14 06:40:30 GMT"));
        to = new CvsRevision("", "BRANCH", "", SERVER_DATE.parse("2007-01-14 06:43:30 GMT"));
        latestUpdate = analyser.latestUpdate(cvs.rlog(module, from, to)).getDate();
        assertEquals("2007-01-14 06:43:00 GMT", SERVER_DATE.format(latestUpdate)); // add.

        // ensure that during the same timeframe, no changes are detected on the head.
        from = new CvsRevision("", "", "", SERVER_DATE.parse("2007-01-14 06:40:30 GMT"));
        to = new CvsRevision("", "", "", SERVER_DATE.parse("2007-01-14 06:43:30 GMT"));
        assertNull(analyser.latestUpdate(cvs.rlog(module, from, to)));
    }

}
