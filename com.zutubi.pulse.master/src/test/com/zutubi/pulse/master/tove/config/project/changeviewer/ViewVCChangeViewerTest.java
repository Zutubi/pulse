package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.MockConfigurationProvider;

/**
 */
public class ViewVCChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://viewvc.tigris.org/source/browse";
    private static final String PATH = "viewvc";

    private ViewVCChangeViewer viewer;

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
        viewer = new ViewVCChangeViewer(BASE, PATH);
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
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc?rev=1412&view=rev", viewer.getChangesetURL(new Revision("1412")));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact.html?rev=1412&view=markup", viewer.getFileViewURL("/trunk/viewvc.org/contact.html", "1412"));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/*checkout*/viewvc/trunk/viewvc.org/contact.html?rev=1412", viewer.getFileDownloadURL("/trunk/viewvc.org/contact.html", "1412"));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact.html?r1=1411&r2=1412", viewer.getFileDiffURL("/trunk/viewvc.org/contact.html", "1412"));
    }

    public void testGetFileViewURLSpecial()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact+this%20number.html?rev=1412&view=markup", viewer.getFileViewURL("/trunk/viewvc.org/contact+this number.html", "1412"));
    }

    public void testGetFileDownloadURLSpecial()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/*checkout*/viewvc/trunk/viewvc.org/contact+this%20number.html?rev=1412", viewer.getFileDownloadURL("/trunk/viewvc.org/contact+this number.html", "1412"));
    }

    public void testGetFileDiffURLSpecial()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact+this%20number.html?r1=1411&r2=1412", viewer.getFileDiffURL("/trunk/viewvc.org/contact+this number.html", "1412"));
    }
}
