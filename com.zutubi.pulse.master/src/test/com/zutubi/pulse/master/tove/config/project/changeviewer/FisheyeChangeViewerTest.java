package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.tove.config.Configuration;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.MockConfigurationProvider;

/**
 */
public class FisheyeChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://fisheye.cinnamonbob.com";
    private static final String PATH = "Zutubi";

    private FisheyeConfiguration viewer;

    protected void setUp() throws Exception
    {
        // it is a bit of work to inject a working configuration provider this way.  Need to find a better way.
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
        viewer = new FisheyeConfiguration(BASE, PATH);
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
        assertEquals("http://fisheye.cinnamonbob.com/changelog/Zutubi/?cs=2508", viewer.getChangesetURL(new Revision("2508")));   
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r=2508", viewer.getFileViewURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", "2508"));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/~raw,r=2508/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", viewer.getFileDownloadURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", "2508"));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r1=2507&r2=2508", viewer.getFileDiffURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", "2508"));
    }
}
