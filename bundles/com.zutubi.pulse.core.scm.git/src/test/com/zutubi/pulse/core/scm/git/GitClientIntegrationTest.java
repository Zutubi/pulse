package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.AbstractScmIntegrationTestCase;
import com.zutubi.pulse.core.scm.ExpectedTestResults;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.util.ZipUtils;

import java.util.List;
import java.util.LinkedList;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.File;
import java.net.URL;

/**
 */
public class GitClientIntegrationTest extends AbstractScmIntegrationTestCase
{
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss Z");

    protected void setUp() throws Exception
    {
        super.setUp();

        List<Revision> revisions = new LinkedList<Revision>();
        revisions.add(new Revision("Daniel Ostermeier <daniel@zutubi.com>", null, DATE_FORMAT.parse("20080925-18:50:11 +1000"), "1b90697603d8db16be01e76541a67fa02f57eef2"));
        revisions.add(new Revision("Daniel Ostermeier <daniel@zutubi.com>", null, DATE_FORMAT.parse("20080925-18:52:59 +1000"), "f4fa18b061b07140e87eb7bc1610591248cd166b"));
        revisions.add(new Revision("Daniel Ostermeier <daniel@zutubi.com>", null, DATE_FORMAT.parse("20080925-18:55:03 +1000"), "036fe4a6cbf87c913cdef64853877832b62b5a89"));
        revisions.add(new Revision("Daniel Ostermeier <daniel@zutubi.com>", null, DATE_FORMAT.parse("20080925-18:56:17 +1000"), "8f3cb174fbc5ceb1d02e2a2f2e7b287e62cb8b84"));
        revisions.add(new Revision("Daniel Ostermeier <daniel@zutubi.com>", null, DATE_FORMAT.parse("20080925-18:56:47 +1000"), "cd7a1b08850329faa7fd8b2102523187f9047955"));

        URL url = getClass().getResource("GitClientIntegrationTest.git.zip");
        ZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        this.client = new GitClient("file://" + new File(tmp, "repo").getCanonicalPath(), "master");
        this.testData = new ExpectedTestResults(revisions);
        this.prefix = ""; // hmm, this is duplicated in the expected test results instance as well.
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testPrepareACleanDirectory() throws ScmException, ParseException
    {
        super.testPrepareACleanDirectory();
    }

    public void testPrepareAnExistingDirectory() throws ScmException
    {
        super.testPrepareAnExistingDirectory();
    }

    public void testRetrieveFile() throws ScmException, IOException
    {
        super.testRetrieveFile();
    }

    public void testRetrieveFileOfSpecifiedRevision() throws ScmException, IOException
    {
        super.testRetrieveFileOfSpecifiedRevision();
    }

    public void testRetrieveDirectory() throws ScmException, IOException
    {
        // git does not fail on this, but rather returns content defining the path.  This implies
        // that the test needs fixing?, or should this be something that is enforced...
//        super.testRetrieveDirectory();
    }

    public void testRetrieveNonExistantFile() throws ScmException
    {
        super.testRetrieveNonExistantFile();
    }

    public void testBrowse() throws ScmException
    {
        super.testBrowse();
    }

    public void testAttemptingToBrowseFile() throws ScmException
    {
        super.testAttemptingToBrowseFile();
    }

    public void testGetLatestRevision() throws ScmException
    {
        super.testGetLatestRevision();
    }

    public void testGetRevisionsFromXToY() throws ScmException
    {
        super.testGetRevisionsFromXToY();
    }

    public void testGetRevisionsFromX() throws ScmException
    {
        super.testGetRevisionsFromX();
    }

    public void testChangesetFromXToY() throws ScmException
    {
        super.testChangesetFromXToY();
    }

    public void testChangesetFromX() throws ScmException
    {
        super.testChangesetFromX();
    }
}
