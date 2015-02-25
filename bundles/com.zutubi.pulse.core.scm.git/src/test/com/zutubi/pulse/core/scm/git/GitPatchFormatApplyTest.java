package com.zutubi.pulse.core.scm.git;

import com.google.common.io.Files;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.Matchers;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.zutubi.util.io.FileSystemUtils.normaliseNewlines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

public class GitPatchFormatApplyTest extends GitClientTestBase
{
    public void testApplyPatch() throws ScmException, IOException
    {
        // FIXME pal line endings seem to screw this up, but only under cygwin/windows.
        if (!SystemUtils.IS_WINDOWS)
        {
            client.checkout(context, null, handler);
            handler.reset();
            GitPatchFormat patchFormat = new GitPatchFormat();
            patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, null, handler);
            assertEquals("edited by " + getName() + "\n", normaliseNewlines(Files.toString(new File(workingDir, "a.txt"), Charset.defaultCharset())));
            assertCleanPatch();
        }
    }

    public void testApplyRenamePatch() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        handler.reset();
        GitPatchFormat patchFormat = new GitPatchFormat();
        patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, null, handler);
        assertEquals(CONTENT_A_TXT, normaliseNewlines(Files.toString(new File(workingDir, "ren.txt"), Charset.defaultCharset())));
        assertCleanPatch();
    }

    public void testApplyBinaryPatch() throws ScmException, IOException
    {
        // FIXME pal under cygwin/windows this complains that -p1 leaves no filename info, but I'm not sure why!
        if (!SystemUtils.IS_WINDOWS) {
            client.checkout(context, null, handler);
            handler.reset();
            GitPatchFormat patchFormat = new GitPatchFormat();
            patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, null, handler);
            assertTrue(new File(workingDir, "binfile").exists());
            assertCleanPatch();
        }
    }

    private void assertCleanPatch()
    {
        assertThat(handler.getStatusMessages(), hasItem(Matchers.matchesRegex(".*Applied patch .* cleanly.*")));
    }

    public void testApplyPatchUnclean() throws ScmException, IOException
    {
        client.checkout(context, new Revision(REVISION_MASTER_PREVIOUS), handler);
        handler.reset();
        try
        {
            GitPatchFormat patchFormat = new GitPatchFormat();
            patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, null, handler);
            fail("Patch should not apply");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("error: patch failed"));
        }
    }
}
