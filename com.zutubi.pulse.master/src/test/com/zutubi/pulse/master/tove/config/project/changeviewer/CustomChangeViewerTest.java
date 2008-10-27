package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.MockConfigurationProvider;

import java.text.ParseException;
import java.util.Date;

public class CustomChangeViewerTest extends PulseTestCase
{
    private CustomChangeViewerConfiguration viewer;

    protected void setUp() throws Exception
    {
        super.setUp();

        final ProjectConfiguration project = new ProjectConfiguration();
        project.setScm(new ScmConfiguration()
        {
            public String getType()
            {
                return "mock";
            }

            public String getPreviousRevision(String revision)
            {
                long number = Long.valueOf(revision);
                if (number > 0)
                {
                    return String.valueOf(number - 1);
                }
                return null;
            }
        });
        viewer = new CustomChangeViewerConfiguration();
        viewer.setConfigurationProvider(new MockConfigurationProvider()
        {
            public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
            {
                return (T) project;
            }
        });
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
        Date date = CustomChangeViewerConfiguration.PULSE_DATE_FORMAT.parse(DATE_STRING);
        assertEquals("author:branch:19700101-10:00:01 author branch " + DATE_STRING + " " + CustomChangeViewerConfiguration.FISHEYE_DATE_FORMAT.format(date) + " ${unknown}", viewer.getChangesetURL(rev));
    }

    public void testGetFileViewURL()
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}");
        assertEquals("http://hello/my/path?r=10", viewer.getFileViewURL("/my/path", "10"));
    }

    public void testGetFileDownloadURL()
    {
        viewer.setFileDownloadURL("http://hello${path}?r=${revision}&format=raw");
        assertEquals("http://hello/my/path?r=10&format=raw", viewer.getFileDownloadURL("/my/path", "10"));
    }

    public void testGetFileDiffURL()
    {
        viewer.setFileDiffURL("http://hello${path}?r=${revision}&p=${previous.revision}");
        assertEquals("http://hello/my/path?r=10&p=9", viewer.getFileDiffURL("/my/path", "10"));
    }

    public void testGetFileViewURLSpecial()
    {
        viewer.setFileViewURL("http://hello${path}?r=${revision}");
        assertEquals("http://hello/my/path+special%20chars?r=10", viewer.getFileViewURL("/my/path+special chars", "10"));
    }

    public void testGetFileDownloadURLSpecial()
    {
        viewer.setFileDownloadURL("http://hello${path}?r=${revision}&format=raw");
        assertEquals("http://hello/my/path+special%20chars?r=10&format=raw", viewer.getFileDownloadURL("/my/path+special chars", "10"));
    }

    public void testGetFileDiffURLSpecial()
    {
        viewer.setFileDiffURL("http://hello${path}?r=${revision}&p=${previous.revision}");
        assertEquals("http://hello/my/path+special%20chars?r=10&p=9", viewer.getFileDiffURL("/my/path+special chars", "10"));
    }

    public void testFilePropertiesSpecial()
    {
        viewer.setFileDiffURL("${path} ${path.raw} ${path.form}");
        assertEquals("/my/path+special%20chars /my/path+special chars %2Fmy%2Fpath%2Bspecial+chars", viewer.getFileDiffURL("/my/path+special chars", "10"));
    }
}
