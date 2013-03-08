package com.zutubi.pulse.core.patchformats.unified;

import com.google.common.io.Files;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
        File tempDir = createTempDirectory();
        try
        {
            File file = copyInputToDirectory("original", "txt", tempDir);
            File patchFile = copyInputToDirectory("patch", "txt", tempDir);

            List<Feature> features = patchFormat.applyPatch(null, patchFile, tempDir, null, null);
            assertEquals(0, features.size());

            if (SystemUtils.IS_WINDOWS)
            {
                FileSystemUtils.translateEOLs(file, SystemUtils.LF_BYTES, true);
            }

            String patchedContent = Files.toString(file, Charset.defaultCharset());
            String expectedContent = readInputFully("new", "txt");
            assertEquals(expectedContent, patchedContent);
        }
        finally
        {
            removeDirectory(tempDir);
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
