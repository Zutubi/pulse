package com.zutubi.pulse.core.scm.patch;

import com.thoughtworks.xstream.XStream;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatus;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatusBuilder;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implements the standard Pulse patch file format, building on top of the
 * {@link com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatusBuilder} interface and
 * {@link PatchArchive}.
 */
public class StandardPatchFormat implements PatchFormat
{
    public boolean writePatchFile(WorkingCopy workingCopy, WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        return writePatchFile((WorkingCopyStatusBuilder) workingCopy, context, patchFile, scope);
    }

    public boolean writePatchFile(WorkingCopyStatusBuilder statusBuilder, WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        WorkingCopyStatus status = statusBuilder.getLocalStatus(context, scope);
        if (!status.inConsistentState())
        {
            context.getUI().error("Working copy is not in a consistent state.");
            return false;
        }

        if (!status.hasStatuses())
        {
            context.getUI().status("No changes found.");
            return false;
        }

        try
        {
            createPatchArchive(statusBuilder, context, patchFile, status);
        }
        catch (IOException e)
        {
            throw new ScmException("I/O error writing patch: " + e.getMessage(), e);
        }

        return true;
    }

    private static void createPatchArchive(WorkingCopyStatusBuilder statusBuilder, WorkingCopyContext context, File patchFile, WorkingCopyStatus status) throws IOException, ScmException
    {
        // The zip archive is laid out as follows:
        // <root>/
        //     meta.xml: information about the patch: revision and change info for files
        //     files/
        //         <changed files>: all files that have been added/edited, laid out in
        //                          the same directory structure as the working copy
        ZipOutputStream os = null;
        try
        {
            os = new ZipOutputStream(new FileOutputStream(patchFile));
            PatchMetadata meta = new PatchMetadata(status.getFileStatuses());
            updatePayloadTypes(meta, statusBuilder, context);
            addMeta(meta, os, context.getUI());
            addFiles(status.getBase(), meta, statusBuilder, context, os);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    private static void updatePayloadTypes(PatchMetadata meta, WorkingCopyStatusBuilder statusBuilder, WorkingCopyContext context) throws ScmException
    {
        for (FileStatus status: meta.getFileStatuses())
        {
            if (status.getPayloadType() == FileStatus.PayloadType.DIFF && !statusBuilder.canDiff(context, status.getPath()))
            {
                status.setPayloadType(FileStatus.PayloadType.FULL);
            }
        }
    }

    private static void addMeta(PatchMetadata metadata, ZipOutputStream os, UserInterface ui) throws IOException
    {
        reportStatus(ui, PatchArchive.META_ENTRY);

        ZipEntry entry = new ZipEntry(PatchArchive.META_ENTRY);
        os.putNextEntry(entry);
        XStream xstream = PatchArchive.createXStream();
        xstream.toXML(metadata, os);
    }

    private static void addFiles(File base, PatchMetadata metadata, WorkingCopyStatusBuilder statusBuilder, WorkingCopyContext context, ZipOutputStream os) throws IOException, ScmException
    {
        os.putNextEntry(new ZipEntry(PatchArchive.FILES_PATH));
        for(FileStatus fs: metadata.getFileStatuses())
        {
            if (fs.getPayloadType() != FileStatus.PayloadType.NONE)
            {
                String path = PatchArchive.FILES_PATH + fs.getPath();
                reportStatus(context.getUI(), path);

                if (fs.getPayloadType() == FileStatus.PayloadType.DIFF)
                {
                    addDiff(os, statusBuilder, context, fs.getPath(), path);
                }
                else
                {
                    File f = new File(base, FileSystemUtils.localiseSeparators(fs.getPath()));
                    addEntry(os, f, path, f.isDirectory());
                }
            }
        }
    }

    private static void addDiff(ZipOutputStream os, WorkingCopyStatusBuilder statusBuilder, WorkingCopyContext context, String filePath, String entryPath) throws IOException, ScmException
    {
        ZipEntry entry = new ZipEntry(entryPath);
        os.putNextEntry(entry);
        statusBuilder.diff(context, filePath, os);
    }

    private static void addEntry(ZipOutputStream os, File f, String path, boolean directory) throws IOException
    {
        if (directory)
        {
            // Directory entries are identified by a trailing slash.
            path += '/';
        }

        ZipEntry entry = new ZipEntry(path);
        entry.setTime(f.lastModified());
        os.putNextEntry(entry);

        if (!directory)
        {
            FileInputStream is = null;

            try
            {
                is = new FileInputStream(f);
                IOUtils.joinStreams(is, os);
            }
            finally
            {
                IOUtils.close(is);
            }
        }
    }

    private static void reportStatus(UserInterface ui, String message)
    {
        if(ui != null)
        {
            ui.status(message);
        }
    }

    public List<Feature> applyPatch(ExecutionContext context, File patchFile, File baseDir, ScmClient scmClient, ScmFeedbackHandler scmFeedbackHandler) throws ScmException
    {
        try
        {
            PatchArchive archive = new PatchArchive(patchFile);
            return archive.apply(baseDir, context, scmClient, scmFeedbackHandler);
        }
        catch (PulseException e)
        {
            throw new ScmException(e);
        }
    }

    public List<FileStatus> readFileStatuses(File patchFile) throws ScmException
    {
        try
        {
            PatchArchive archive = new PatchArchive(patchFile);
            return archive.getMetadata().getFileStatuses();
        }
        catch (PulseException e)
        {
            throw new ScmException(e);
        }
    }

    public boolean isPatchFile(File patchFile)
    {
        try
        {
            // See if it is a zip with a meta file.
            new PatchArchive(patchFile);
            return true;
        }
        catch (PulseException e)
        {
            return false;
        }
    }
}
