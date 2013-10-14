package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class Trac10ChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://trac.edgewall.org";
    private static final String PATH = "";

    private static final Revision FILE_REVISION            = new Revision("3673");
    private static final Revision PREVIOUS_FILE_REVISION   = new Revision("3672");
    private static final Revision CHANGE_REVISION          = new Revision("12345");
    private static final Revision PREVIOUS_CHANGE_REVISION = new Revision("12344");

    private ScmClient mockScmClient;
    private Trac10ChangeViewer viewer;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockScmClient = mock(ScmClient.class);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(CHANGE_REVISION), eq(false))).toReturn(PREVIOUS_CHANGE_REVISION);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(FILE_REVISION), eq(true))).toReturn(PREVIOUS_FILE_REVISION);

        viewer = new Trac10ChangeViewer(BASE, PATH);
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://trac.edgewall.org/changeset/3673", viewer.getRevisionURL(null, FILE_REVISION));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL?rev=3673", viewer.getFileViewURL(getContext(), getFileChange("/trunk/INSTALL")));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL?rev=3673&format=raw", viewer.getFileDownloadURL(getContext(), getFileChange("/trunk/INSTALL")));
    }

    public void testGetFileDiffURL() throws ScmException
    {
        assertEquals("http://trac.edgewall.org/changeset?new=trunk%2FINSTALL%403673&old=trunk%2FINSTALL%403672", viewer.getFileDiffURL(getContext(), getFileChange("/trunk/INSTALL")));
    }

    public void testGetFileViewURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL%2bthis%20please?rev=3673", viewer.getFileViewURL(getContext(), getFileChange("/trunk/INSTALL+this please")));
    }

    public void testGetFileDownloadURLSpecial()
    {
        assertEquals("http://trac.edgewall.org/browser/trunk/INSTALL%2bthis%20please?rev=3673&format=raw", viewer.getFileDownloadURL(getContext(), getFileChange("/trunk/INSTALL+this please")));
    }

    public void testGetFileDiffURLSpecial() throws ScmException
    {
        assertEquals("http://trac.edgewall.org/changeset?new=trunk%2FINSTALL%2Bthis+please%403673&old=trunk%2FINSTALL%2Bthis+please%403672", viewer.getFileDiffURL(getContext(), getFileChange("/trunk/INSTALL+this please")));
    }

    public FileChange getFileChange(String path)
    {
        return new FileChange(path, FILE_REVISION, FileChange.Action.EDIT);
    }

    public ChangeContext getContext()
    {
        return new ChangeContextImpl(CHANGE_REVISION, null, mockScmClient, null);
    }
}
