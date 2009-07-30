package com.zutubi.pulse.core.patchformats.unified;

import com.zutubi.diff.*;
import com.zutubi.diff.unified.UnifiedPatch;
import com.zutubi.diff.unified.UnifiedPatchParser;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * A {@link com.zutubi.pulse.core.scm.patch.api.PatchFormat} implementation that reads unified
 * diffs - i.e. the standard format used by the Unix patch command.  Built on top of the
 * com.zutubi.diff library.
 */
public class UnifiedPatchFormat implements PatchFormat
{
    public boolean writePatchFile(WorkingCopy workingCopy, WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        throw new UnsupportedOperationException("Creation of unified patches is not supported.");
    }

    public List<Feature> applyPatch(ExecutionContext context, File patchFile, File baseDir, EOLStyle localEOL, ScmFeedbackHandler scmFeedbackHandler) throws ScmException
    {
        PatchFile patch = parse(patchFile);
        try
        {
            patch.apply(baseDir, 0);
        }
        catch (PatchApplyException e)
        {
            throw new ScmException("While applying patch: " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    public List<FileStatus> readFileStatuses(File patchFile) throws ScmException
    {
        PatchFile patch = parse(patchFile);
        return CollectionUtils.map(patch.getPatches(), new Mapping<Patch, FileStatus>()
        {
            public FileStatus map(Patch patch)
            {
                return new FileStatus(patch.getNewFile(), FileStatus.State.valueOf(patch.getType()), false);
            }
        });
    }

    public boolean isPatchFile(File patchFile)
    {
        try
        {
            // Be really conservative: as lots of other formats will use
            // something semi-compatible with patch, only guess it is a plain
            // old patch in the case where there are no extra header lines.
            BufferedReader reader = new BufferedReader(new FileReader(patchFile));
            String line = reader.readLine();
            return line != null && line.startsWith(UnifiedPatch.HEADER_OLD_FILE);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private PatchFile parse(File patchFile) throws ScmException
    {
        PatchFile patch;
        PatchFileParser parser = new PatchFileParser(new UnifiedPatchParser());
        try
        {
            patch = parser.parse(new FileReader(patchFile));
        }
        catch (PatchParseException e)
        {
            throw new ScmException("Unable to parse patch file: " + e.getMessage(), e);
        }
        catch (FileNotFoundException e)
        {
            throw new ScmException("Patch file '" + patchFile.getAbsolutePath() + "' does not exist.", e);
        }
        return patch;
    }
}
