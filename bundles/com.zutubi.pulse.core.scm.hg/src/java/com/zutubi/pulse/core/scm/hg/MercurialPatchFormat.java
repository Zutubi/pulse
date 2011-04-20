package com.zutubi.pulse.core.scm.hg;

import com.zutubi.diff.Patch;
import com.zutubi.diff.PatchFile;
import com.zutubi.diff.PatchFileParser;
import com.zutubi.diff.PatchParseException;
import com.zutubi.diff.git.GitPatchParser;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.pulse.core.scm.process.api.ScmByteHandler;
import com.zutubi.pulse.core.scm.process.api.ScmOutputHandlerSupport;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A patch format that uses hg diff/import commands to create and apply
 * patches.  We instruct mercurial to use a git-compatible format as it handles
 * renames and binary files.
 */
public class MercurialPatchFormat implements PatchFormat
{
    public boolean writePatchFile(WorkingCopy workingCopy, WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        // Scope has the form:
        //
        // [:<rev>[:<rev>]] <file> ...
        //
        // If one revision is given we just output that change (hg diff -c), if
        // two they are both specified as a range (hg diff -r <rev> -r <rev>).

        String revision1 = null;
        String revision2 = null;
        String[] files;
        
        if (scope.length > 0 && scope[0].startsWith(":"))
        {
            String range = scope[0].substring(1);
            int separatorIndex = range.indexOf(":");
            if (separatorIndex > 0 && separatorIndex < range.length() - 1)
            {
                revision1 = range.substring(0, separatorIndex);
                revision2 = range.substring(separatorIndex + 1);
            }
            else if (separatorIndex >= 0)
            {
                context.getUI().error("Invalid revision specification '" + scope[0] + "': format is :<rev>[:<rev>]");
                return false;
            }
            else
            {
                revision1 = range;
            }

            files = new String[scope.length - 1];
            System.arraycopy(scope, 1, files, 0, files.length);
        }
        else
        {
            files = scope;
        }

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(patchFile);
            runDiff(context, os, revision1, revision2, files);
        }
        catch (Exception e)
        {
            context.getUI().error("Error writing patch file: " + e.getMessage(), e);
            return false;
        }
        finally
        {
            IOUtils.close(os);
        }
        
        if (patchFile.length() == 0)
        {
            context.getUI().status("No changes found.");
            if (!patchFile.delete())
            {
                throw new ScmException("Can't remove empty patch '" + patchFile.getAbsolutePath() + "'");
            }

            return false;
        }
        
        return true;
    }

    private void runDiff(WorkingCopyContext context, final FileOutputStream os, String revision1, String revision2, String[] files) throws ScmException
    {
        MercurialCore core = new MercurialCore();
        core.setWorkingDirectory(context.getBase());
        
        core.diff(new ScmByteHandler()
        {
            public void handleStdout(byte[] buffer, int n)
            {
                try
                {
                    os.write(buffer, 0, n);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            public void handleStderr(byte[] buffer, int n)
            {
            }

            public void handleCommandLine(String line)
            {
            }

            public void handleExitCode(int code) throws ScmException
            {
            }

            public void checkCancelled() throws ScmCancelledException
            {
            }
        }, revision1, revision2, files);
    }

    public List<Feature> applyPatch(ExecutionContext context, File patchFile, File baseDir, ScmClient scmClient, ScmFeedbackHandler scmFeedbackHandler) throws ScmException
    {
        MercurialCore core = new MercurialCore();
        core.setWorkingDirectory(baseDir);
        core.patch(new ScmOutputHandlerSupport(scmFeedbackHandler), patchFile);
        return Collections.emptyList();
    }

    public List<FileStatus> readFileStatuses(File patchFile) throws ScmException
    {
        try
        {
            PatchFileParser parser = new PatchFileParser(new GitPatchParser());
            PatchFile gitPatch = parser.parse(new FileReader(patchFile));
            return CollectionUtils.map(gitPatch.getPatches(), new Mapping<Patch, FileStatus>()
            {
                public FileStatus map(Patch patch)
                {
                    return new FileStatus(patch.getNewFile(), FileStatus.State.valueOf(patch.getType()), false);
                }
            });
        }
        catch (IOException e)
        {
            throw new ScmException("I/O error reading mercurial patch: " + e.getMessage(), e);
        }
        catch (PatchParseException e)
        {
            throw new ScmException("Unable to parse mercurial patch: " + e.getMessage(), e);
        }
    }

    public boolean isPatchFile(File patchFile)
    {
        return new GitPatchParser().isPatchFile(patchFile);
    }
}
