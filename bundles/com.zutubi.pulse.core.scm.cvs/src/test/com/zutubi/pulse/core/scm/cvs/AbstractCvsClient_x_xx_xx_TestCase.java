package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.PersistentContextImpl;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AbstractCvsClient_x_xx_xx_TestCase extends PulseTestCase
{
    protected DateFormat dateFormat = new SimpleDateFormat(CvsRevision.DATE_AND_TIME_FORMAT_STRING);
    protected DateFormat serverDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    
    protected File tmp;
    protected ScmContextImpl scmContext;
    protected PulseExecutionContext exeContext;
    protected File exeBaseDir;
    protected File scmBaseDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();

        scmBaseDir = new File(tmp, "scmContext");
        scmContext = new ScmContextImpl(new PersistentContextImpl(scmBaseDir), new PulseExecutionContext());

        exeBaseDir = new File(tmp, "work");
        exeContext = new PulseExecutionContext();
        exeContext.setWorkingDir(exeBaseDir);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    protected String getPassword(String name) throws IOException
    {
        return CvsTestUtils.getPassword(name);
    }

    protected void assertFileExists(String path)
    {
        assertTrue(isFileExists(path));
    }

    protected void assertFileNotExists(String path)
    {
        assertFalse(isFileExists(path));
    }

    protected boolean isFileExists(String path)
    {
        return new File(exeBaseDir, path).exists();
    }

    /**
     * When creating time based revisions, we need the time to be server time, which is in GMT.
     * However, when we create our revision, we need local time.  This method handles the conversion
     * from the server time format to the cvs revisions expected time format.
     *
     * @param time  server time.
     * @return  cvs revision time.
     *
     * @throws java.text.ParseException if the format of the time argument is incorrect.
     */
    protected String localTime(String time) throws ParseException
    {
        return dateFormat.format(serverDate.parse(time));
    }

    protected void cleanWorkDir() throws IOException
    {
        FileSystemUtils.rmdir(exeContext.getWorkingDir());
        assertTrue(exeContext.getWorkingDir().mkdirs());
    }
}
