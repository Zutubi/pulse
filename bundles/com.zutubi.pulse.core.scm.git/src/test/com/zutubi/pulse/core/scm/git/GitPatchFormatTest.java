package com.zutubi.pulse.core.scm.git;

import com.zutubi.diff.*;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.git.diff.GitPatchParser;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class GitPatchFormatTest extends GitWorkingCopyTestBase
{
    private static final String EXTENSION_TXT = "txt";

    private GitPatchFormat patchFormat = new GitPatchFormat();

    public void testWritePatchFile() throws ScmException, IOException, PatchParseException
    {
        editFile(baseDir, "file2");
        assertPatchFile("file2", "file2", PatchType.EDIT);
    }

    public void testWritePatchFileStaged() throws ScmException, IOException, PatchParseException
    {
        stageFile(baseDir, "file2");
        assertPatchFile("file2", "file2", PatchType.EDIT, ":");
    }

    public void testWritePatchFileScoped() throws ScmException, IOException, PatchParseException
    {
        stageFile(baseDir, "file1");
        stageFile(baseDir, "file2");
        assertPatchFile("file2", "file2", PatchType.EDIT, ":", "file2");
    }

    public void testWritePatchSingleCommit() throws ScmException, IOException, PatchParseException
    {
        assertPatchFile("file1", "file1", PatchType.EDIT, ":74d50a8");
    }

    public void testWritePatchCommitRange() throws ScmException, IOException, PatchParseException
    {
        assertPatchFile("file1", "file1", PatchType.EDIT, ":70017..0cd9762");
    }

    private void assertPatchFile(String oldFile, String newFile, PatchType type, String... scope) throws PatchParseException, FileNotFoundException, ScmException
    {
        File file = new File(tempDir, "patch");
        assertTrue(patchFormat.writePatchFile(workingCopy, context, file, scope));

        PatchFileParser parser = new PatchFileParser(new GitPatchParser());
        PatchFile patchFile = parser.parse(new FileReader(file));
        List<Patch> patches = patchFile.getPatches();
        assertEquals(1, patches.size());
        Patch patch = patches.get(0);
        assertEquals(oldFile, patch.getOldFile());
        assertEquals(newFile, patch.getNewFile());
        assertEquals(type, patch.getType());
    }

    public void testWritePatchFileNoChanges() throws ScmException, IOException, PatchParseException
    {
        File file = new File(tempDir, "patch");
        assertFalse(patchFormat.writePatchFile(workingCopy, context, file));
        assertFalse(file.exists());
    }

    public void testWritePatchScopeHasNoChanges() throws ScmException, IOException, PatchParseException
    {
        editFile(baseDir, "file1");
        File file = new File(tempDir, "patch");
        assertFalse(patchFormat.writePatchFile(workingCopy, context, file, "file2"));
        assertFalse(file.exists());
    }

    public void testReadFileStatuses() throws ScmException, IOException
    {
        File f = copyInputToDirectory(EXTENSION_TXT, tempDir);
        List<FileStatus> statusList = patchFormat.readFileStatuses(f);
        assertEquals(9, statusList.size());
        assertStatus(statusList.get(0), ".gitignore", FileStatus.State.DELETED);
        assertStatus(statusList.get(1), "binfile", FileStatus.State.MODIFIED);
        assertStatus(statusList.get(2), "src/clojure/contrib/bin.clj", FileStatus.State.ADDED);
        assertStatus(statusList.get(3), "src/clojure/contrib/classy.clj", FileStatus.State.RENAMED);
        assertStatus(statusList.get(4), "src/clojure/contrib/miglayout.clj", FileStatus.State.MODIFIED);
        assertStatus(statusList.get(5), "src/clojure/contrib/monads.clj", FileStatus.State.DELETED);
        assertStatus(statusList.get(6), "src/clojure/contrib/new.clj", FileStatus.State.ADDED);
        assertStatus(statusList.get(7), "src/clojure/contrib/xul.clj", FileStatus.State.ADDED);
        assertStatus(statusList.get(8), "src/clojure/contrib/zip_filter.clj", FileStatus.State.MODIFIED);
    }

    private void assertStatus(FileStatus fileStatus, String path, FileStatus.State state)
    {
        assertEquals(path, fileStatus.getPath());
        assertEquals(state, fileStatus.getState());
    }
}
