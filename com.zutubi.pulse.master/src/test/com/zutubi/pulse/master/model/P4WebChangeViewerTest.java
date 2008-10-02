package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.tove.config.project.changeviewer.P4WebChangeViewer;
import com.zutubi.tove.config.MockConfigurationProvider;

/**
 */
public class P4WebChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://localhost:8080";

    private P4WebChangeViewer viewer;

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
                if(number > 0)
                {
                    return String.valueOf(number - 1);
                }
                return null;
            }
        });
        viewer = new P4WebChangeViewer(BASE, "");
        viewer.setConfigurationProvider(new MockConfigurationProvider()
        {
            public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
            {
                return (T)project;
            }
        });
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://localhost:8080/@md=d@/2508?ac=10", viewer.getChangesetURL(new Revision(null, null, null, "2508")));   
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo?ac=64&rev1=5", viewer.getFileViewURL("//depot/foo", "5"));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://localhost:8080/@md=d&rev1=8@//depot/foo", viewer.getFileDownloadURL("//depot/foo", "8"));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo?ac=19&rev1=4&rev2=5", viewer.getFileDiffURL("//depot/foo", "5"));
    }

    public void testGetFileVieSpecial()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo+bar%20baz?ac=64&rev1=5", viewer.getFileViewURL("//depot/foo+bar baz", "5"));
    }

    public void testGetFileDownloadSpecial()
    {
        assertEquals("http://localhost:8080/@md=d&rev1=8@//depot/foo+bar%20baz", viewer.getFileDownloadURL("//depot/foo+bar baz", "8"));
    }

    public void testGetFileDiffSpecial()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo+bar%20baz?ac=19&rev1=4&rev2=5", viewer.getFileDiffURL("//depot/foo+bar baz", "5"));
    }
}
