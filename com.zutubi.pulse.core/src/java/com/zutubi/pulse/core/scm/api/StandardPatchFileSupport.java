package com.zutubi.pulse.core.scm.api;

import com.thoughtworks.xstream.XStream;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.personal.PatchMetadata;
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
 * Supporting static methods for creating and applying patch files using the
 * standard Pulse patch file format.  Helps implement the personal build-related
 * methods on the (@link ScmClient} and {@link WorkingCopy} interfaces in the
 * case where the standard patch file is used.
 *
 * @see ScmClient
 * @see WorkingCopy
 * @see WorkingCopyStatusBuilder
 */
public class StandardPatchFileSupport
{
    /**
     * Writes a standard patch file based on the working copy status retrieved
     * from the given status builder.  This method may be used to implement
     * {@link WorkingCopy#writePatchFile(WorkingCopyContext, java.io.File, String[])}
     * around a status builder.  The status builder may also be called on to
     * provide diffs for changed files.
     *
     * @param statusBuilder used to retrieve the current status of the working
     *                      copy and diffs for changed text files (where
     *                      possible)
     * @param context       context for the working copy operations
     * @param patchFile     output file that the patch will be written to
     * @param scope         specification of what to include in the patch
     *                      (should be passed through directly from
     *                      {@link WorkingCopy#writePatchFile(WorkingCopyContext, java.io.File, String[])},
     *                      - this method is passes it on to
     *                      {@link WorkingCopyStatusBuilder#getLocalStatus(WorkingCopyContext, String[])})
     * @return true if a non-trivial patch file could be created, false if the
     *         working copy state is inconsistent or there are no pending
     *         changes
     * @throws ScmException on any error
     *
     * @see WorkingCopy#writePatchFile(WorkingCopyContext, java.io.File, String[])
     */
    public static boolean writePatchFile(WorkingCopyStatusBuilder statusBuilder, WorkingCopyContext context, File patchFile, String... scope) throws ScmException
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

    private static void addMeta(PatchMetadata metadata, ZipOutputStream os, PersonalBuildUI ui) throws IOException
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
            if (!fs.isDirectory() && fs.getPayloadType() != FileStatus.PayloadType.NONE)
            {
                String path = PatchArchive.FILES_PATH + fs.getTargetPath();
                reportStatus(context.getUI(), path);

                if (fs.getPayloadType() == FileStatus.PayloadType.DIFF)
                {
                    addDiff(os, statusBuilder, context, fs.getPath(), path);
                }
                else
                {
                    File f = new File(base, FileSystemUtils.localiseSeparators(fs.getPath()));
                    addFile(os, f, path);
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

    private static void addFile(ZipOutputStream os, File f, String path) throws IOException
    {
        ZipEntry entry = new ZipEntry(path);
        entry.setTime(f.lastModified());
        os.putNextEntry(entry);
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

    private static void reportStatus(PersonalBuildUI ui, String message)
    {
        if(ui != null)
        {
            ui.status(message);
        }
    }

    /**
     * Applies a patch file in standard Pulse format (i.e. created by
     * {@link #writePatchFile(WorkingCopyStatusBuilder, WorkingCopyContext, java.io.File, String[])})
     * to the given directory.  This method may be used to implement
     * {@link ScmClient#applyPatch(com.zutubi.pulse.core.engine.api.ExecutionContext, java.io.File, java.io.File, EOLStyle, ScmFeedbackHandler)}
     * for standard patch files.
     * <p/>
     * Feedback is provided via the given handler, which is also regularly
     * checked to determine if the operation has been cancelled.
     *
     * @param patchFile          file containing the standard Pulse patch to be
     *                           applied
     * @param baseDir            base of the working copy to which the patch
     *                           should be applied
     * @param localEOL           the local SCM configuration's end-of-line
     *                           policy for text files - should be passed
     *                           through directly from {@link ScmClient#applyPatch(com.zutubi.pulse.core.engine.api.ExecutionContext, java.io.File, java.io.File, EOLStyle, ScmFeedbackHandler)}
     * @param scmFeedbackHandler handler used to report status and check for
     *                           asynchronous cancellation
     * @return a list of messages from the patch application, e.g. warnings
     *         about recoverable issues found when applying changes
     * @throws ScmException on any error, including non-recoverable problems
     *         applying the changes (e.g. an unclean patch of a text file)
     *
     * @see ScmClient#applyPatch(com.zutubi.pulse.core.engine.api.ExecutionContext, java.io.File, java.io.File, EOLStyle, ScmFeedbackHandler)
     */
    public static List<Feature> applyPatch(File patchFile, File baseDir, EOLStyle localEOL, ScmFeedbackHandler scmFeedbackHandler) throws ScmException
    {
        try
        {
            PatchArchive archive = new PatchArchive(patchFile);
            return archive.apply(baseDir, localEOL, scmFeedbackHandler);
        }
        catch (PulseException e)
        {
            throw new ScmException(e);
        }
    }

    /**
     * Reads and returns file status information from a patch file in standard
     * Pulse format (i.e. created by {@link #writePatchFile(WorkingCopyStatusBuilder, WorkingCopyContext, java.io.File, String[])}).
     * This method may be used to implement {@link ScmClient#readFileStatuses(ScmContext, java.io.File)}
     * for standard patch files.
     *
     * @param patchFile file containing the standard pulse patch to examine
     * @return the statuses of all files stored in the patch
     * @throws ScmException on any error
     *
     * @see ScmClient#readFileStatuses(ScmContext, java.io.File)
     */
    public static List<FileStatus> readFileStatuses(File patchFile) throws ScmException
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
}
