package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class P4WebChangeViewerTest extends PulseTestCase
{
    private static final String BASE = "http://localhost:8080";

    private static final Revision CHANGE_REVISION          = new Revision("2508");
    private static final Revision PREVIOUS_CHANGE_REVISION = new Revision("2507");
    private static final Revision FILE_REVISION            = new Revision("8");
    private static final Revision PREVIOUS_FILE_REVISION   = new Revision("7");
    private static final String   FILE_PATH                = "//depot/foo";
    private static final String   SPECIAL_FILE_PATH        = "//depot/foo+bar baz";

    private ScmConfiguration scmConfiguration;
    private ScmClient mockScmClient;
    private P4WebChangeViewer viewer;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockScmClient = mock(ScmClient.class);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(CHANGE_REVISION), eq(false))).toReturn(PREVIOUS_CHANGE_REVISION);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(FILE_REVISION), eq(true))).toReturn(PREVIOUS_FILE_REVISION);

        scmConfiguration = new ScmConfiguration()
        {
            public String getType()
            {
                return "mock";
            }
        };
        viewer = new P4WebChangeViewer(BASE, "");
    }

    public void testGetChangesetURL()
    {
        assertEquals("http://localhost:8080/@md=d@/2508?ac=10", viewer.getRevisionURL(CHANGE_REVISION));
    }

    public void testGetFileViewURL()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo?ac=64&rev1=8", viewer.getFileViewURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDownloadURL()
    {
        assertEquals("http://localhost:8080/@md=d&rev1=8@//depot/foo", viewer.getFileDownloadURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDiffURL() throws ScmException
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo?ac=19&rev1=7&rev2=8", viewer.getFileDiffURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileViewSpecial()
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo%2bbar%20baz?ac=64&rev1=8", viewer.getFileViewURL(getContext(), getFileChange(SPECIAL_FILE_PATH)));
    }

    public void testGetFileDownloadSpecial()
    {
        assertEquals("http://localhost:8080/@md=d&rev1=8@//depot/foo%2bbar%20baz", viewer.getFileDownloadURL(getContext(), getFileChange(SPECIAL_FILE_PATH)));
    }

    public void testGetFileDiffSpecial() throws ScmException
    {
        assertEquals("http://localhost:8080/@md=d@//depot/foo%2bbar%20baz?ac=19&rev1=7&rev2=8", viewer.getFileDiffURL(getContext(), getFileChange(SPECIAL_FILE_PATH)));
    }

    private ChangeContext getContext()
    {
        return new ChangeContextImpl(CHANGE_REVISION, scmConfiguration, mockScmClient, null);
    }

    private FileChange getFileChange(String filePath)
    {
        return new FileChange(filePath, FILE_REVISION, FileChange.Action.EDIT);
    }
}
