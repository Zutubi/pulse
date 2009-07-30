package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.Matchers;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

import java.io.File;
import java.io.IOException;

public class GitPatchFormatApplyTest extends GitClientTestBase
{
    public void testApplyPatch() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        handler.reset();
        GitPatchFormat patchFormat = new GitPatchFormat();
        patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, EOLStyle.BINARY, handler);
        assertEquals("edited by " + getName() + "\n", IOUtils.fileToString(new File(workingDir, "a.txt")));
        assertCleanPatch();
    }

    public void testApplyRenamePatch() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        handler.reset();
        GitPatchFormat patchFormat = new GitPatchFormat();
        patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, EOLStyle.BINARY, handler);
        assertEquals(CONTENT_A_TXT + "\n", IOUtils.fileToString(new File(workingDir, "ren.txt")));
        assertCleanPatch();
    }

    public void testApplyBinaryPatch() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        handler.reset();
        GitPatchFormat patchFormat = new GitPatchFormat();
        patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, EOLStyle.BINARY, handler);
        assertTrue(new File(workingDir, "binfile").exists());
        assertCleanPatch();
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
            patchFormat.applyPatch(context, getInputFile(EXTENSION_TXT), workingDir, EOLStyle.BINARY, handler);
            fail("Patch should not apply");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("error: patch failed"));
        }
    }
}
