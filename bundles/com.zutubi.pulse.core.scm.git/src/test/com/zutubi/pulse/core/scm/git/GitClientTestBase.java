package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.PulseZipUtils;

import java.io.File;
import java.net.URL;

public abstract class GitClientTestBase extends PulseTestCase
{
    protected static final String REVISION_INITIAL = "96e8d45dd7627d9e3cab980e90948e3ae1c99c62";
    protected static final String REVISION_MASTER_LATEST = "a495e21cd263d9dca25379dfbff733461f0d9873";
    protected static final String REVISION_MASTER_PREVIOUS = "2d1ce48a43c5c675c618f915af43e76ed7dac253";
    protected static final String REVISION_MASTER_TWO_PREVIOUS = "e34da05e88de03a4aa5b10b338382f09bbe65d4b";
    protected static final String REVISION_DEV_MERGE_NO_CONFLICTS = "9751b1dbd8cdbbeedce404bad38d1df1053078f6";
    protected static final String REVISION_DEV_MERGE_CONFLICTS = "2b54f24e1facb7d97643d38e0f89cd5db88b186a";
    protected static final String REVISION_MULTILINE_COMMENT = "01aaf6555b7524871204c8df64273597c0bc1f1b";
    protected static final String REVISION_MASTER_INTERMEDIATE = "b69a48a6b0f567d0be110c1fbca2c48fc3e1b112";
    protected static final String REVISION_SIMPLE_INTERMEDIATE = "83d35b25a6b4711c4d9424c337bf82e5398756f3";
    protected static final String REVISION_SIMPLE_LATEST = "c34b545b6954b8946967c250dde7617c24a9bb4b";
    protected static final String BRANCH_SIMPLE = "branch";
    protected static final String BRANCH_MERGES = "devbranch";
    protected static final String TEST_AUTHOR = "Jason Sankey";
    protected static final String CONTENT_A_TXT = "another a edit";
    protected static final String EXTENSION_TXT = "txt";

    private File tmp;
    protected String repository;
    protected GitClient client;
    protected File workingDir;
    protected PulseExecutionContext context;
    protected RecordingScmFeedbackHandler handler;
    protected ScmContextImpl scmContext;
    protected File repositoryBase;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();

        URL url = getClass().getResource("GitClientTestBase.zip");
        PulseZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        repositoryBase = new File(tmp, "repo");
        repository = "file://" + repositoryBase.getCanonicalPath();

        client = new GitClient(repository, "master", 0, false);

        workingDir = new File(tmp, "wd");
        context = new PulseExecutionContext();
        context.setWorkingDir(workingDir);

        File persistentWorkingDir = new File(tmp, "scm");
        assertTrue(persistentWorkingDir.mkdir());
        scmContext = new ScmContextImpl();
        scmContext.setPersistentWorkingDir(persistentWorkingDir);

        handler = new RecordingScmFeedbackHandler();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }
}

//  $ git log --name-status
//    commit 6e16107f3d99519a0cf341d3f455283d8244536e
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 14:04:04 2009 +0000
//
//        A regular commit to finish up.
//
//    M	b.txt
//
//    commit 01aaf6555b7524871204c8df64273597c0bc1f1b
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:37:33 2009 +0000
//
//        This is a
//        multi-line
//        commit comment
//
//    M	a.txt
//
//    commit 2b54f24e1facb7d97643d38e0f89cd5db88b186a
//    Merge: 9751b1d... a495e21...
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:13:50 2009 +0000
//
//        Fixed merge conflict.
//
//    commit a495e21cd263d9dca25379dfbff733461f0d9873
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:12:32 2009 +0000
//
//        Edit, add and remove on master.
//
//    M	a.txt
//    D	c.txt
//    A	d.txt
//
//    commit 9751b1dbd8cdbbeedce404bad38d1df1053078f6
//    Merge: 21aa65b... 2d1ce48...
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:10:25 2009 +0000
//
//        Merge branch 'master' into devbranch
//
//    commit 2d1ce48a43c5c675c618f915af43e76ed7dac253
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:10:04 2009 +0000
//
//        Simple edit to b.txt on master.
//
//    M	b.txt
//
//    commit 21aa65b9a35bffaefead12680334288d6d69e596
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:09:23 2009 +0000
//
//        Simple edit to a.txt on devbranch.
//
//    M	a.txt
//
//    commit e34da05e88de03a4aa5b10b338382f09bbe65d4b
//    Author: Daniel Ostermeier <daniel@zutubi.com>
//    Date:   Sun Sep 28 15:06:49 2008 +1000
//
//        removed content from a.txt
//
//    M	a.txt
//
//    commit b69a48a6b0f567d0be110c1fbca2c48fc3e1b112
//    Author: Daniel Ostermeier <daniel@zutubi.com>
//    Date:   Sun Sep 28 15:06:32 2008 +1000
//
//        added content to a.txt
//
//    M	a.txt
//
//    commit 96e8d45dd7627d9e3cab980e90948e3ae1c99c62
//    Author: Daniel Ostermeier <daniel@zutubi.com>
//    Date:   Sun Sep 28 13:26:10 2008 +1000
//
//        initial commit
//
//    A	a.txt
//    A	b.txt
//    A	c.txt
