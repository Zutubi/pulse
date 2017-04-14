/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

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

        viewer.setChangesetURL("$(revision) $(author) $(branch) $(time.pulse) $(time.fisheye) $(unknown)");
        Revision rev = new Revision("author:branch:" + DATE_STRING);
        assertEquals("author:branch:19700101-10:00:01 author branch " + DATE_STRING + " " + convertPulseToFisheyeDate(DATE_STRING) + " $(unknown)", viewer.getRevisionURL(null, rev));
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
        viewer.setFileViewURL("http://hello$(path)?r=$(revision)/$(change.revision)");
        assertEquals("http://hello/my/path?r=123/2345", viewer.getFileViewURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDownloadURL() throws ScmException
    {
        viewer.setFileDownloadURL("http://hello$(path)?r=$(revision)/$(change.revision)&format=raw");
        assertEquals("http://hello/my/path?r=123/2345&format=raw", viewer.getFileDownloadURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileDiffURL() throws ScmException
    {
        viewer.setFileDiffURL("http://hello$(path)?r=$(revision)/$(change.revision)&p=$(previous.revision)/$(previous.change.revision)");
        assertEquals("http://hello/my/path?r=123/2345&p=122/2344", viewer.getFileDiffURL(getContext(), getFileChange(FILE_PATH)));
    }

    public void testGetFileViewURLSpecial() throws ScmException
    {
        viewer.setFileViewURL("http://hello$(path)?r=$(revision)");
        assertEquals("http://hello/my/path%2bspecial%20chars?r=123", viewer.getFileViewURL(getContext(), getFileChange("/my/path+special chars")));
    }

    public void testGetFileDownloadURLSpecial() throws ScmException
    {
        viewer.setFileDownloadURL("http://hello$(path)?r=$(revision)&format=raw");
        assertEquals("http://hello/my/path%2bspecial%20chars?r=123&format=raw", viewer.getFileDownloadURL(getContext(), getFileChange("/my/path+special chars")));
    }

    public void testGetFileDiffURLSpecial() throws ScmException
    {
        viewer.setFileDiffURL("http://hello$(path)?r=$(revision)&p=$(previous.revision)");
        assertEquals("http://hello/my/path%2bspecial%20chars?r=123&p=122", viewer.getFileDiffURL(getContext(), getFileChange("/my/path+special chars")));
    }

    public void testFilePropertiesSpecial() throws ScmException
    {
        viewer.setFileDiffURL("$(path) $(path.raw) $(path.form)");
        assertEquals("/my/path%2bspecial%20chars /my/path+special chars %2Fmy%2Fpath%2Bspecial+chars", viewer.getFileDiffURL(getContext(), getFileChange("/my/path+special chars")));
    }

    public void testProjectProperties() throws ScmException
    {
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.getProperties().put("p1", new ResourcePropertyConfiguration("p1", "v1"));
        ChangeContext context = new ChangeContextImpl(CHANGE_REVISION, projectConfig, mockScmClient, null);
        viewer.setChangesetURL("change $(p1)");
        viewer.setFileViewURL("file $(p1)");
        assertEquals("change v1", viewer.getRevisionURL(projectConfig, new Revision("1")));
        assertEquals("file v1", viewer.getFileViewURL(context, getFileChange("path")));
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
