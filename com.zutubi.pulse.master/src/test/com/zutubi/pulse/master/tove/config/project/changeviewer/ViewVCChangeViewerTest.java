package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class ViewVCChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://viewvc.tigris.org/source/browse";
    private static final String PATH = "viewvc";

    private static final Revision CHANGE_REVISION          = new Revision("12345");
    private static final Revision PREVIOUS_CHANGE_REVISION = new Revision("12344");
    private static final Revision FILE_REVISION            = new Revision("1412");
    private static final Revision PREVIOUS_FILE_REVISION   = new Revision("1411");
    private static final String   FILE_PATH                = "/trunk/viewvc.org/contact.html";
    private static final String   SPECIAL_FILE_PATH        = "/trunk/viewvc.org/contact+this number.html";

    private ScmClient mockScmClient;
    private ViewVCChangeViewer viewer;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockScmClient = mock(ScmClient.class);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(CHANGE_REVISION), eq(false))).toReturn(PREVIOUS_CHANGE_REVISION);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(FILE_REVISION), eq(true))).toReturn(PREVIOUS_FILE_REVISION);

        viewer = new ViewVCChangeViewer(BASE, PATH);
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc?rev=1412&view=rev", viewer.getRevisionURL(null, new Revision("1412")));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact.html?rev=1412&view=markup", viewer.getFileViewURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/*checkout*/viewvc/trunk/viewvc.org/contact.html?rev=1412", viewer.getFileDownloadURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDiffURL() throws ScmException
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact.html?r1=1411&r2=1412", viewer.getFileDiffURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileViewURLSpecial()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact%2bthis%20number.html?rev=1412&view=markup", viewer.getFileViewURL(getContext(), getFileChange(SPECIAL_FILE_PATH)));
    }

    public void testGetFileDownloadURLSpecial()
    {
        assertEquals("http://viewvc.tigris.org/source/browse/*checkout*/viewvc/trunk/viewvc.org/contact%2bthis%20number.html?rev=1412", viewer.getFileDownloadURL(getContext(), getFileChange(SPECIAL_FILE_PATH)));
    }

    public void testGetFileDiffURLSpecial() throws ScmException
    {
        assertEquals("http://viewvc.tigris.org/source/browse/viewvc/trunk/viewvc.org/contact%2bthis%20number.html?r1=1411&r2=1412", viewer.getFileDiffURL(getContext(), getFileChange(SPECIAL_FILE_PATH)));
    }

    private ChangeContext getContext()
    {
        return new ChangeContextImpl(CHANGE_REVISION, null, mockScmClient, null);
    }

    private FileChange getFileChange(String filePath)
    {
        return new FileChange(filePath, FILE_REVISION, FileChange.Action.EDIT);
    }
}
