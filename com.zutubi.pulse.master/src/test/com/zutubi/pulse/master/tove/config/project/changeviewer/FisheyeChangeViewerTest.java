package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.MockConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;

/**
 */
public class FisheyeChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://fisheye.cinnamonbob.com";
    private static final String PATH = "Zutubi";

    private FisheyeConfiguration viewer;
    private String scmType = "mock";

    protected void setUp() throws Exception
    {
        // it is a bit of work to inject a working configuration provider this way.  Need to find a better way.
        final ProjectConfiguration project = new ProjectConfiguration();
        project.setScm(new ScmConfiguration()
        {
            public String getType()
            {
                return scmType;
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
        assertEquals("http://fisheye.cinnamonbob.com/changelog/Zutubi/?cs=2508", viewer.getChangelistURL(new Revision("2508")));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r=1033", viewer.getFileViewURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new Revision("1033"), "25"));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/~raw,r=1033/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", viewer.getFileDownloadURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new Revision("1033"), "25"));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r1=1032&r2=1033", viewer.getFileDiffURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new Revision("1033"), "25"));
    }

    public void testGetFileViewURLCvs()
    {
        scmType = FisheyeConfiguration.TYPE_CVS;
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r=25", viewer.getFileViewURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new Revision("1033"), "25"));
    }

    public void testGetFileDownloadCvs()
    {
        scmType = FisheyeConfiguration.TYPE_CVS;
        assertEquals("http://fisheye.cinnamonbob.com/browse/~raw,r=25/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", viewer.getFileDownloadURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new Revision("1033"), "25"));
    }

    public void testGetFileDiffCvs()
    {
        scmType = FisheyeConfiguration.TYPE_CVS;
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r1=24&r2=25", viewer.getFileDiffURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new Revision("1033"), "25"));
    }
}
