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
        viewer.setFileDownloadURL("http://hello${path}?r=${revision}&format=raw");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path?r=10&format=raw", viewer.getFileDownloadURL("/my/path", rev));
    }

    public void testGetFileDiffURL()
    {
        viewer.setFileDiffURL("http://hello${path}?r=${revision}&p=${previous.revision}");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path?r=10&p=9", viewer.getFileDiffURL("/my/path", rev));
    }

    public void testGetFileViewURLSpecial()
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path+special%20chars?r=10", viewer.getFileViewURL("/my/path+special chars", rev));
    }

    public void testGetFileDownloadURLSpecial()
    {
        viewer.setFileDownloadURL("http://hello${path}?r=${revision}&format=raw");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path+special%20chars?r=10&format=raw", viewer.getFileDownloadURL("/my/path+special chars", rev));
    }

    public void testGetFileDiffURLSpecial()
    {
        viewer.setFileDiffURL("http://hello${path}?r=${revision}&p=${previous.revision}");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("http://hello/my/path+special%20chars?r=10&p=9", viewer.getFileDiffURL("/my/path+special chars", rev));
    }

    public void testFilePropertiesSpecial()
    {
        viewer.setFileDiffURL("${path} ${path.raw} ${path.form}");
        FileRevision rev = new NumericalFileRevision(10);
        assertEquals("/my/path+special%20chars /my/path+special chars %2Fmy%2Fpath%2Bspecial+chars", viewer.getFileDiffURL("/my/path+special chars", rev));
    }
}
