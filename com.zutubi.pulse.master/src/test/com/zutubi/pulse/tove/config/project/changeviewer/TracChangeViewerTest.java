package com.zutubi.pulse.tove.config.project.changeviewer;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.MockConfigurationProvider;

/**
 */
public class TracChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://trac.edgewall.org";
    private static final String PATH = "";

    private TracChangeViewer viewer;

    protected void setUp() throws Exception
    {
        final ProjectConfiguration project = new ProjectConfiguration();
        project.setScm(new ScmConfiguration()
        {
            public String getType()
            {
                return "mock";
            }

            public String getPreviousRevision(String revision)
            {
                long number = Long.valueOf(revision);
                if (number > 0)
                {
                    return String.valueOf(number - 1);
                }
                return null;
            }
        });
        viewer = new TracChangeViewer(BASE, PATH);
        viewer.setConfigurationProvider(new MockConfigurationProvider()
        {
            public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
            {
                return (T) project;
            }
        });
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://trac.edgewall.org/changeset/3673", viewer.getChangesetURL(new Revision(null, null, null, "3673")));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL?rev=3673", viewer.getFileViewURL("/trunk/INSTALL", "3673"));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL?rev=3673&format=raw", viewer.getFileDownloadURL("/trunk/INSTALL", "3673"));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://trac.edgewall.org/changeset?new=trunk%2FINSTALL%403673&old=trunk%2FINSTALL%403672", viewer.getFileDiffURL("/trunk/INSTALL", "3673"));
    }

    public void testGetFileViewURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL+this%20please?rev=3673", viewer.getFileViewURL("/trunk/INSTALL+this please", "3673"));
    }

    public void testGetFileDownloadURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL+this%20please?rev=3673&format=raw", viewer.getFileDownloadURL("/trunk/INSTALL+this please", "3673"));
    }

    public void testGetFileDiffURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/changeset?new=trunk%2FINSTALL%2Bthis+please%403673&old=trunk%2FINSTALL%2Bthis+please%403672", viewer.getFileDiffURL("/trunk/INSTALL+this please", "3673"));
    }
}
