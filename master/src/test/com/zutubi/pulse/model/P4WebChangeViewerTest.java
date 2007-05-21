package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.prototype.config.changeviewer.P4WebChangeViewer;

/**
 */
public class P4WebChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://localhost:8080";

    private P4WebChangeViewer viewer;

    protected void setUp() throws Exception
    {
        viewer = new P4WebChangeViewer(BASE, "");
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://localhost:8080/@md=d@/2508?ac=10", viewer.getChangesetURL(new NumericalRevision(2508)));   
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo?ac=64&rev1=5", viewer.getFileViewURL("//depot/foo", new NumericalFileRevision(5)));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://localhost:8080/@md=d&rev1=8@//depot/foo", viewer.getFileDownloadURL("//depot/foo", new NumericalFileRevision(8)));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo?ac=19&rev1=4&rev2=5", viewer.getFileDiffURL("//depot/foo", new NumericalFileRevision(5)));
    }

    public void testGetFileVieSpecial()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo+bar%20baz?ac=64&rev1=5", viewer.getFileViewURL("//depot/foo+bar baz", new NumericalFileRevision(5)));
    }

    public void testGetFileDownloadSpecial()
    {
        assertEquals("http://localhost:8080/@md=d&rev1=8@//depot/foo+bar%20baz", viewer.getFileDownloadURL("//depot/foo+bar baz", new NumericalFileRevision(8)));
    }

    public void testGetFileDiffSpecial()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo+bar%20baz?ac=19&rev1=4&rev2=5", viewer.getFileDiffURL("//depot/foo+bar baz", new NumericalFileRevision(5)));
    }
}
