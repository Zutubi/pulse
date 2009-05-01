package com.zutubi.pulse.core.personal;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.NullOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 */
public class PatchArchive
{
    private static final String FILES_PATH = "files/";
    private static final String META_ENTRY = "meta.xml";

    private File patchFile;
    private PatchMetadata metadata;

    /**
     * Creates a new archive from a working copy at base.  The state of the
     * working copy is established and the patch created based on that state.
     *
     * @param revision  revision to which this patch should be applied
     * @param status    status of the working copy based at base
     * @param patchFile the destination of the patch file created
     * @param ui the ui reference to allow logging to the command output.
     *
     * @throws PersonalBuildException in the event of any error creating the patch
     */
    public PatchArchive(Revision revision, WorkingCopyStatus status, File patchFile, PersonalBuildUI ui) throws PersonalBuildException
    {
        this.patchFile = patchFile;
        this.metadata = new PatchMetadata(revision, status.getFileStatuses());

        try
        {
            createPatchArchive(status.getBase(), ui);
        }
        catch (IOException e)
        {
            throw new PersonalBuildException("I/O error creating patch file: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a patch from an existing archive file.
     *
     * @param patchFile the file to create the patch from
     * @throws com.zutubi.pulse.core.api.PulseException on error
     */
    public PatchArchive(File patchFile) throws PulseException
    {
        this.patchFile = patchFile;

        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));
            ZipEntry entry = zin.getNextEntry();
            if(entry == null || !entry.getName().equals(META_ENTRY))
            {
                throw new PulseException("Missing meta entry in patch file '" + patchFile.getAbsolutePath() + "'");
            }

            XStream xstream = createXStream();
            metadata = (PatchMetadata) xstream.fromXML(zin);
        }
        catch(IOException e)
        {
            throw new PulseException("I/O error reading status from patch file '" + patchFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void createPatchArchive(File base, PersonalBuildUI ui) throws IOException
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
            addMeta(os, ui);
            addFiles(base, os, ui);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    private void addMeta(ZipOutputStream os, PersonalBuildUI ui) throws IOException
    {
        status(ui, META_ENTRY);

        ZipEntry entry = new ZipEntry(META_ENTRY);
        os.putNextEntry(entry);
        XStream xstream = createXStream();
        xstream.toXML(metadata, os);
    }

    private XStream createXStream()
    {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("status", PatchMetadata.class);
        xstream.addImplicitCollection(PatchMetadata.class, "fileStatuses");
        xstream.alias("revision", Revision.class);
        xstream.alias("fileStatus", FileStatus.class);
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.registerConverter(new FileStatusConverter());
        xstream.registerConverter(new RevisionConverter());

        return xstream;
    }

    private void addFiles(File base, ZipOutputStream os, PersonalBuildUI ui) throws IOException
    {
        os.putNextEntry(new ZipEntry(FILES_PATH));
        for(FileStatus fs: metadata.getFileStatuses())
        {
            if (fs.getState().requiresFile())
            {
                String path = FILES_PATH + fs.getTargetPath();
                status(ui, path);

                File f = new File(base, FileSystemUtils.localiseSeparators(fs.getPath()));
                addEntry(os, f, path, fs.isDirectory());
            }
        }
    }

    private void addEntry(ZipOutputStream os, File f, String path, boolean directory) throws IOException
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

    public File getPatchFile()
    {
        return patchFile;
    }

    public PatchMetadata getMetadata()
    {
        return metadata;
    }

    public void apply(File base, EOLStyle localEOL, CommandResult result) throws PulseException
    {
        try
        {
            List<FileStatus> statuses = new LinkedList<FileStatus>(metadata.getFileStatuses());

            // Sort the statuses so that we handle nested files before their parents.  So, for
            // example, if all files in a tree are marked for delete we can verify they all exist
            // as we delete them from leaf to root.
            Collections.sort(statuses, new Comparator<FileStatus>()
            {
                public int compare(FileStatus o1, FileStatus o2)
                {
                    return o2.getTargetPath().length() - o1.getTargetPath().length();
                }
            });

            for(FileStatus fs: statuses)
            {
                preApply(fs, base, result);
            }

            unzip(base);

            for(FileStatus fs: statuses)
            {
                postApply(fs, base, localEOL);
            }
        }
        catch (IOException e)
        {
            throw new PulseException("I/O error applying patch file '" + patchFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
    }

    private void preApply(FileStatus fileStatus, File base, CommandResult result) throws IOException
    {
        switch (fileStatus.getState())
        {
            case MODIFIED:
            case MERGED:
            case METADATA_MODIFIED:
                checkTargetFile(fileStatus, base, result);
                break;

            case DELETED:
            case REPLACED:
                File f = checkTargetFile(fileStatus, base, result);
                if (fileStatus.isDirectory())
                {
                    if (!FileSystemUtils.rmdir(f))
                    {
                        result.addFeature(Feature.Level.WARNING, "Problem applying patch: Unable to delete target directory '" + fileStatus.getTargetPath() + "'");
                    }
                }
                else
                {
                    if (!f.delete())
                    {
                        result.addFeature(Feature.Level.WARNING, "Problem applying patch: Unable to delete target file '" + fileStatus.getTargetPath() + "'");
                    }
                }
                break;
        }
    }

    private File checkTargetFile(FileStatus fileStatus, File base, CommandResult result)
    {
        File f = new File(base, fileStatus.getTargetPath());
        if (!f.exists())
        {
            result.addFeature(Feature.Level.WARNING, "Problem applying patch: Target file '" + fileStatus.getTargetPath() + "' with status " + fileStatus.getState() + " in patch does not exist");
        }
        
        return f;
    }

    private void postApply(FileStatus fileStatus, File base, EOLStyle localEOL) throws IOException
    {
        File file = new File(base, fileStatus.getTargetPath());

        // Apply line ending settings
        String eolName = fileStatus.getProperty(FileStatus.PROPERTY_EOL_STYLE);
        if(eolName != null)
        {
            try
            {
                EOLStyle eol = EOLStyle.valueOf(eolName);
                eol.apply(file, localEOL);
            }
            catch (IllegalArgumentException e)
            {
                // Just ignore it values we don't support.
            }
        }

        // Handle the executable bit
        String executableValue = fileStatus.getProperty(FileStatus.PROPERTY_EXECUTABLE);
        if(executableValue != null)
        {
            boolean executable = Boolean.parseBoolean(executableValue);
            if(file.exists())
            {
                FileSystemUtils.setExecutable(file, executable);
            }
        }
    }

    private void unzip(File base) throws IOException
    {
        ZipInputStream zin = null;

        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));

            // Skip over meta entry
            zin.getNextEntry();
            IOUtils.joinStreams(zin, new NullOutputStream());

            ZipEntry entry;
            while((entry = zin.getNextEntry()) != null)
            {
                extractEntry(base, entry, zin);
            }
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void extractEntry(File base, ZipEntry entry, ZipInputStream zin) throws IOException
    {
        String path = getPath(entry);
        File f = new File(base, path);
        if(entry.isDirectory())
        {
            f.mkdirs();
        }
        else
        {
            File parent = f.getParentFile();
            if(parent != null && !parent.isDirectory())
            {
                parent.mkdirs();
            }

            if(!f.canWrite())
            {
                FileSystemUtils.setWritable(f);
            }

            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(f);
                IOUtils.joinStreams(zin, out);
            }
            finally
            {
                IOUtils.close(out);
            }
        }
    }

    private String getPath(ZipEntry entry) throws IOException
    {
        String name = entry.getName();
        if(!name.startsWith(FILES_PATH))
        {
            throw new IOException("Unexpected entry path '" + name + "': should start with '" + FILES_PATH + "'");
        }

        return name.substring(FILES_PATH.length());
    }

    public boolean containsPath(String path)
    {
        for(FileStatus fs: metadata.getFileStatuses())
        {
            if(fs.getPath().equals(path) && fs.getState().requiresFile())
            {
                return true;
            }
        }

        return false;
    }

    public String retrieveFile(String path) throws IOException
    {
        path = FILES_PATH + path;
        
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));
            ZipEntry entry;
            while((entry = zin.getNextEntry()) != null)
            {
                if(entry.getName().equals(path))
                {
                    // This is it
                    return IOUtils.inputStreamToString(zin);
                }
            }

            throw new IOException("Path '" + path + "' not found in archive");
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void status(PersonalBuildUI ui, String message)
    {
        if(ui != null)
        {
            ui.status(message);
        }
    }
}
