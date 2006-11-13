package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.Date;

/**
 */
public class CustomChangeViewerTest extends PulseTestCase
{
    private CustomChangeViewer viewer = new CustomChangeViewer();

    public void testGetChangesetURL()
    {
        viewer.setChangesetURL("${revision} ${author} ${branch} ${time.pulse} ${time.fisheye} ${unknown}");
        Revision rev = new CvsRevision("author", "branch", "comment", new Date(1000));
        assertEquals("author:branch:19700101-10:00:01 author branch 19700101-10:00:01 19700101000001 ${unknown}", viewer.getChangesetURL(rev));
    }

    public void testGetFileViewURL()
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path?r=10", viewer.getFileViewURL("/my/path", rev));
    }

    public void testGetFileDownloadURL()
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}&format=raw");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path?r=10&format=raw", viewer.getFileViewURL("/my/path", rev));
    }

    public void testGetFileDiffURL()
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}&p=${previous.revision}");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path?r=10&p=9", viewer.getFileViewURL("/my/path", rev));
    }
}
