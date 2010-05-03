package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class CustomChangeViewerTest extends PulseTestCase
{
    private static final Revision CHANGE_REVISION          = new Revision("2345");
    private static final Revision PREVIOUS_CHANGE_REVISION = new Revision("2344");
    private static final String   FILE_PATH                = "/my/path";
    private static final Revision FILE_REVISION            = new Revision("123");
    private static final Revision PREVIOUS_FILE_REVISION   = new Revision("122");

    private CustomChangeViewerConfiguration viewer;
    private ScmClient mockScmClient;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockScmClient = mock(ScmClient.class);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(CHANGE_REVISION), eq(false))).toReturn(PREVIOUS_CHANGE_REVISION);
        stub(mockScmClient.getPreviousRevision((ScmContext) anyObject(), same(FILE_REVISION), eq(true))).toReturn(PREVIOUS_FILE_REVISION);

        viewer = new CustomChangeViewerConfiguration();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetChangesetURL() throws ParseException
    {
        final String DATE_STRING = "19700101-10:00:01";

        viewer.setChangesetURL("${revision} ${author} ${branch} ${time.pulse} ${time.fisheye} ${unknown}");
        Revision rev = new Revision("author:branch:" + DATE_STRING);
        assertEquals("author:branch:19700101-10:00:01 author branch " + DATE_STRING + " " + convertPulseToFisheyeDate(DATE_STRING) + " ${unknown}", viewer.getRevisionURL(rev));
    }

    private String convertPulseToFisheyeDate(String dateString) throws ParseException
    {
        SimpleDateFormat pulseDateFormat = new SimpleDateFormat(CustomChangeViewerConfiguration.PULSE_DATE_FORMAT_STRING);
        SimpleDateFormat fisheyeDateFormat = new SimpleDateFormat(CustomChangeViewerConfiguration.FISHEYE_DATE_FORMAT_STRING);
        fisheyeDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return fisheyeDateFormat.format(pulseDateFormat.parse(dateString));
    }

    public void testGetFileViewURL() throws ScmException
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}/${change.revision}");
        assertEquals("http://hello/my/path?r=123/2345", viewer.getFileViewURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDownloadURL() throws ScmException
    {
        viewer.setFileDownloadURL("http://hello${path}?r=${revision}/${change.revision}&format=raw");
        assertEquals("http://hello/my/path?r=123/2345&format=raw", viewer.getFileDownloadURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDiffURL() throws ScmException
    {
        viewer.setFileDiffURL("http://hello${path}?r=${revision}/${change.revision}&p=${previous.revision}/${previous.change.revision}");
        assertEquals("http://hello/my/path?r=123/2345&p=122/2344", viewer.getFileDiffURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileViewURLSpecial() throws ScmException
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}");
        assertEquals("http://hello/my/path%2bspecial%20chars?r=123", viewer.getFileViewURL(getContext(), getFileChange("/my/path+special chars")));
    }

    public void testGetFileDownloadURLSpecial() throws ScmException
    {
        viewer.setFileDownloadURL("http://hello${path}?r=${revision}&format=raw");
        assertEquals("http://hello/my/path%2bspecial%20chars?r=123&format=raw", viewer.getFileDownloadURL(getContext(), getFileChange("/my/path+special chars")));
    }

    public void testGetFileDiffURLSpecial() throws ScmException
    {
        viewer.setFileDiffURL("http://hello${path}?r=${revision}&p=${previous.revision}");
        assertEquals("http://hello/my/path%2bspecial%20chars?r=123&p=122", viewer.getFileDiffURL(getContext(), getFileChange("/my/path+special chars")));
    }

    public void testFilePropertiesSpecial() throws ScmException
    {
        viewer.setFileDiffURL("${path} ${path.raw} ${path.form}");
        assertEquals("/my/path%2bspecial%20chars /my/path+special chars %2Fmy%2Fpath%2Bspecial+chars", viewer.getFileDiffURL(getContext(), getFileChange("/my/path+special chars")));
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
