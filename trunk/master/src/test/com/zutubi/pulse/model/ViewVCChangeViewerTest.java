package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class ViewVCChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://viewvc.tigris.org/source/browse";
    private static final String PATH = "viewvc";

    private ViewVCChangeViewer viewer;

    protected void setUp() throws Exception
    {
        viewer = new ViewVCChangeViewer(BASE, PATH);
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc?rev=1412&view=rev", viewer.getChangesetURL(new NumericalRevision(1412)));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact.html?rev=1412&view=markup", viewer.getFileViewURL("/trunk/viewvc.org/contact.html", new NumericalFileRevision(1412)));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/*checkout*/viewvc/trunk/viewvc.org/contact.html?rev=1412", viewer.getFileDownloadURL("/trunk/viewvc.org/contact.html", new NumericalFileRevision(1412)));
    }

    public void testGetFileDiffURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact.html?r1=1411&r2=1412", viewer.getFileDiffURL("/trunk/viewvc.org/contact.html", new NumericalFileRevision(1412)));
    }
}
