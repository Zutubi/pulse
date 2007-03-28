package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class TracChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://trac.edgewall.org";
    private static final String PATH = "";

    private TracChangeViewer viewer;

    protected void setUp() throws Exception
    {
        viewer = new TracChangeViewer(BASE, PATH);
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://trac.edgewall.org/changeset/3673", viewer.getChangesetURL(new NumericalRevision(3673)));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL?rev=3673", viewer.getFileViewURL("/trunk/INSTALL", new NumericalFileRevision(3673)));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL?rev=3673&format=raw", viewer.getFileDownloadURL("/trunk/INSTALL", new NumericalFileRevision(3673)));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://trac.edgewall.org/changeset?new=trunk%2FINSTALL%403673&old=trunk%2FINSTALL%403672", viewer.getFileDiffURL("/trunk/INSTALL", new NumericalFileRevision(3673)));
    }

    public void testGetFileViewURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL+this%20please?rev=3673", viewer.getFileViewURL("/trunk/INSTALL+this please", new NumericalFileRevision(3673)));
    }

    public void testGetFileDownloadURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL+this%20please?rev=3673&format=raw", viewer.getFileDownloadURL("/trunk/INSTALL+this please", new NumericalFileRevision(3673)));
    }

    public void testGetFileDiffURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/changeset?new=trunk%2FINSTALL%2Bthis+please%403673&old=trunk%2FINSTALL%2Bthis+please%403672", viewer.getFileDiffURL("/trunk/INSTALL+this please", new NumericalFileRevision(3673)));
    }
}
