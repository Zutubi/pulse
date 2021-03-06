/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.AbstractScmIntegrationTestCase;
import com.zutubi.pulse.core.scm.ExpectedTestResults;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackAdapter;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;
import com.zutubi.pulse.core.util.PulseZipUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GitClientIntegrationTest extends AbstractScmIntegrationTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();

        List<Revision> revisions = new LinkedList<Revision>();
        revisions.add(new Revision("1b90697603d8db16be01e76541a67fa02f57eef2"));
        revisions.add(new Revision("f4fa18b061b07140e87eb7bc1610591248cd166b"));
        revisions.add(new Revision("036fe4a6cbf87c913cdef64853877832b62b5a89"));
        revisions.add(new Revision("8f3cb174fbc5ceb1d02e2a2f2e7b287e62cb8b84"));
        revisions.add(new Revision("cd7a1b08850329faa7fd8b2102523187f9047955"));

        URL url = getClass().getResource("GitClientIntegrationTest.git.zip");
        PulseZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        this.client = new GitClient("file://" + new File(tmp, "repo").getCanonicalPath(), "master", 0, GitConfiguration.CloneType.NORMAL, 0, 0, false, Collections.<String>emptyList());
        this.testData = new ExpectedTestResults(revisions);
        this.prefix = ""; // hmm, this is duplicated in the expected test results instance as well.

        this.client.init(context, new ScmFeedbackAdapter());
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
