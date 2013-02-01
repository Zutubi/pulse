package com.zutubi.pulse.core.scm.git;

import com.google.common.base.Function;
import com.zutubi.diff.Patch;
import com.zutubi.diff.PatchFile;
import com.zutubi.diff.PatchFileParser;
import com.zutubi.diff.PatchParseException;
import com.zutubi.diff.git.GitPatchParser;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.pulse.core.util.process.ForwardingCharHandler;
import com.zutubi.pulse.core.util.process.ProcessWrapper;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import static java.util.Arrays.asList;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link com.zutubi.pulse.core.scm.patch.api.PatchFormat} implementation for
 * git.  Uses git's own support for creating and applying patches, including
 * binary patches.
 */
public class GitPatchFormat implements PatchFormat
{
    public boolean writePatchFile(WorkingCopy workingCopy, WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        // Scope is of the form:
        //   [:<range>] <file> ...
        // where the optional <range> itself is of the form:
        //   [<commit>[..[.]<commit>]]
        // and is either empty (in which case we pass --cached), a single
        // commit (passed through to diff) or a commit range (passed through).
        //
        // If no commit range is present, we use <remote>/<remote branch>.  We
        // always add "--" after the commit range to avoid ambiguities.
        NativeGit git = new NativeGit();
        git.setWorkingDirectory(context.getBase());

        List<String> args = new LinkedList<String>();
        args.add(git.getGitCommand());
        args.add(COMMAND_DIFF);
        args.add(FLAG_BINARY);
        args.add(FLAG_FIND_COPIES);

        String specDescription = "working copy";
        boolean filesSpecified = false;
        if (scope.length > 0 && scope[0].startsWith(":"))
        {
            String range = scope[0].substring(1);
            if (range.length() == 0)
            {
                specDescription = "staging index";
                args.add(FLAG_CACHED);
            }
            else
            {
                specDescription = "specified commit range";
                args.add(range);
            }

            if (scope.length > 1)
            {
                filesSpecified = true;
                args.add(FLAG_SEPARATOR);
                args.addAll(asList(scope).subList(1, scope.length));
            }
        }
        else
        {
            GitWorkingCopy gitWorkingCopy = (GitWorkingCopy) workingCopy;
            args.add(gitWorkingCopy.getRemoteRef(git));
            if (scope.length > 0)
            {
                filesSpecified = true;
                args.add(FLAG_SEPARATOR);
                args.addAll(asList(scope));
            }
        }

        // Run the process directly so we can capture raw output.  Going
        // through a line handler munges newlines.
        ProcessWrapper processWrapper = null;
        Writer output = null;
        StringWriter error = new StringWriter();
        try
        {
            output = new FileWriter(patchFile);

            ProcessBuilder builder = new ProcessBuilder(args);
            builder.directory(context.getBase());
            Process p = builder.start();

            processWrapper = new ProcessWrapper(p, new ForwardingCharHandler(output, error), true);
            int exitCode = processWrapper.waitFor();
            if (exitCode != 0)
            {
                context.getUI().error(error.toString());
                context.getUI().error("git diff exited with code " + exitCode + ".");
                return false;
            }
        }
        catch (Exception e)
        {
            context.getUI().error("Error writing patch file: " + e.getMessage(), e);
            return false;
        }
        finally
        {
            IOUtils.close(output);
            if (processWrapper != null)
            {
                processWrapper.destroy();
            }
        }

        if (patchFile.length() == 0)
        {
            context.getUI().status("Git command '" + StringUtils.join(" ", args) + "' created an empty patch file.");
            context.getUI().status("i.e. no changes were found in the " + (filesSpecified ? "given files in the " : "") + specDescription + ".");
            if (!patchFile.delete())
            {
                throw new GitException("Can't remove empty patch '" + patchFile.getAbsolutePath() + "'");
            }

            return false;
        }

        return true;
    }

    public List<Feature> applyPatch(ExecutionContext context, File patchFile, File baseDir, ScmClient scmClient, ScmFeedbackHandler scmFeedbackHandler) throws ScmException
    {
        NativeGit git = new NativeGit(0, context);
        git.setWorkingDirectory(baseDir);
        git.apply(scmFeedbackHandler, patchFile);
        return Collections.emptyList();
    }

    public List<FileStatus> readFileStatuses(File patchFile) throws ScmException
    {
        try
        {
            PatchFileParser parser = new PatchFileParser(new GitPatchParser());
            PatchFile gitPatch = parser.parse(new FileReader(patchFile));
            return CollectionUtils.map(gitPatch.getPatches(), new Function<Patch, FileStatus>()
            {
                public FileStatus apply(Patch patch)
                {
                    return new FileStatus(patch.getNewFile(), FileStatus.State.valueOf(patch.getType()), false);
                }
            });
        }
        catch (IOException e)
        {
            throw new GitException("I/O error reading git patch: " + e.getMessage(), e);
        }
        catch (PatchParseException e)
        {
            throw new GitException("Unable to parse git patch: " + e.getMessage(), e);
        }
    }

    public boolean isPatchFile(File patchFile)
    {
        return new GitPatchParser().isPatchFile(patchFile);
    }
}
