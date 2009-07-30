package com.zutubi.pulse.core.patchformats.unified;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Note this class just provides sanity tests, as {@link com.zutubi.pulse.core.patchformats.unified.UnifiedPatchFormat}
 * is a thin wrapper arounf the com.zutubi.diff library.
 */
public class UnifiedPatchFormatTest extends PulseTestCase
{
    private UnifiedPatchFormat patchFormat = new UnifiedPatchFormat();

    public void testApplyPatch() throws IOException, ScmException
    {
        File tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        try
        {
            File file = copyInputToDirectory("original", "txt", tempDir);
            File patchFile = copyInputToDirectory("patch", "txt", tempDir);

            List<Feature> features = patchFormat.applyPatch(null, patchFile, tempDir, EOLStyle.BINARY, null);
            assertEquals(0, features.size());

            if (SystemUtils.IS_WINDOWS)
            {
                FileSystemUtils.translateEOLs(file, SystemUtils.LF_BYTES, true);
            }

            String patchedContent = IOUtils.fileToString(file);
            String expectedContent = IOUtils.inputStreamToString(getInput("new", "txt"));
            assertEquals(expectedContent, patchedContent);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testReadFileStatuses() throws ScmException
    {
        List<FileStatus> statuses = patchFormat.readFileStatuses(getInputFile("patch", "txt"));
        assertEquals(1, statuses.size());
        FileStatus status = statuses.get(0);
        assertEquals("original.txt", status.getPath());
        assertEquals(FileStatus.PayloadType.DIFF, status.getPayloadType());
        assertEquals(FileStatus.State.MODIFIED, status.getState());
    }
}
