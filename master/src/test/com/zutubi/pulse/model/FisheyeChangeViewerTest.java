package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.prototype.config.changeviewer.FisheyeConfiguration;

/**
 */
public class FisheyeChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://fisheye.cinnamonbob.com";
    private static final String PATH = "Zutubi";

    private FisheyeConfiguration viewer;

    protected void setUp() throws Exception
    {
        viewer = new FisheyeConfiguration(BASE, PATH);
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/changelog/Zutubi/?cs=2508", viewer.getChangesetURL(new NumericalRevision(2508)));   
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r=2508", viewer.getFileViewURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new NumericalFileRevision(2508)));   
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/~raw,r=2508/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", viewer.getFileDownloadURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new NumericalFileRevision(2508)));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://fisheye.cinnamonbob.com/browse/Zutubi/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java?r1=2507&r2=2508", viewer.getFileDiffURL("/pulse/trunk/master/src/java/com/zutubi/pulse/api/RemoteApi.java", new NumericalFileRevision(2508)));
    }
}
