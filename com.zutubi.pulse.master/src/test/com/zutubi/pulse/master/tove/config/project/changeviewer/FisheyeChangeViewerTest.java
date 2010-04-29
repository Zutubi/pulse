package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.FakeConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import static org.mockito.Mockito.*;

public class FisheyeChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://fisheye.cinnamonbob.com";
    private static final String PATH = "Zutubi";

    private static final Revision CHANGE_REVISION          = new Revision("2508");
    private static final Revision PREVIOUS_CHANGE_REVISION = new Revision("2507");
    private static final Revision FILE_REVISION            = new Revision("25");
    private static final Revision PREVIOUS_FILE_REVISION   = new Revision("24");

    private FisheyeConfiguration viewer;
    private ScmClient mockScmClient;
    private String scmType = "mock";
    private ScmConfiguration scmConfiguration;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockScmClient = mock(ScmClient.class);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(CHANGE_REVISION), eq(false))).toReturn(PREVIOUS_CHANGE_REVISION);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(FILE_REVISION), eq(true))).toReturn(PREVIOUS_FILE_REVISION);

        // it is a bit of work to inject a working configuration provider this way.  Need to find a better way.
        final ProjectConfiguration project = new ProjectConfiguration();
        scmConfiguration = new ScmConfiguration()
        {
            public String getType()
            {
                return scmType;
            }
        };
        project.setScm(scmConfiguration);
        viewer = new FisheyeConfiguration(BASE, PATH);
        viewer.setConfigurationProvider(new FakeConfigurationProvider()
        {
            public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
            {
                return (T)project;
            }
        });
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/changelog/Zutubi/?cs=2508", viewer.getRevisionURL(CHANGE_REVISION));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r=2508", viewer.getFileViewURL(getContext(), getFileChange()));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/~raw,r=2508/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", viewer.getFileDownloadURL(getContext(), getFileChange()));
    }

    public void testGetFileDiffURL() throws ScmException
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r1=2507&r2=2508", viewer.getFileDiffURL(getContext(), getFileChange()));
    }

    public void testGetFileViewURLCvs()
    {
        scmType = FisheyeConfiguration.TYPE_CVS;
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r=25", viewer.getFileViewURL(getContext(), getFileChange()));
    }

    public void testGetFileDownloadCvs()
    {
        scmType = FisheyeConfiguration.TYPE_CVS;
        assertEquals("http://fisheye.cinnamonbob.com/browse/~raw,r=25/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", viewer.getFileDownloadURL(getContext(), getFileChange()));
    }

    public void testGetFileDiffCvs() throws ScmException
    {
        scmType = FisheyeConfiguration.TYPE_CVS;
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r1=24&r2=25", viewer.getFileDiffURL(getContext(), getFileChange()));
    }

    private ChangeContext getContext()
    {
        return new ChangeContextImpl(CHANGE_REVISION, scmConfiguration, mockScmClient, null);
    }

    private FileChange getFileChange()
    {
        return new FileChange("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", FILE_REVISION, FileChange.Action.EDIT);
    }
}
