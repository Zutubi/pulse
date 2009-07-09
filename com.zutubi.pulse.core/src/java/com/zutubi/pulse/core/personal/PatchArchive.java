package com.zutubi.pulse.core.personal;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.zutubi.diff.DiffException;
import com.zutubi.diff.Patch;
import com.zutubi.diff.PatchFile;
import com.zutubi.diff.PatchFileParser;
import com.zutubi.diff.unified.UnifiedPatchParser;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.api.FileStatus;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.NullOutputStream;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 */
public class PatchArchive
{
    public static final String FILES_PATH = "files/";
    public static final String META_ENTRY = "meta.xml";

    private File patchFile;
    private PatchMetadata metadata;

    /**
     * Creates a patch from an existing archive file.
     *
     * @param patchFile the file to create the patch from
     * @throws com.zutubi.pulse.core.api.PulseException
     *          on error
     */
    public PatchArchive(File patchFile) throws PulseException
    {
        this.patchFile = patchFile;

        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));
            ZipEntry entry = zin.getNextEntry();
            if (entry == null || !entry.getName().equals(META_ENTRY))
            {
                throw new PulseException("Missing meta entry in patch file '" + patchFile.getAbsolutePath() + "'");
            }

            XStream xstream = createXStream();
            metadata = (PatchMetadata) xstream.fromXML(zin);
        }
        catch (IOException e)
        {
            throw new PulseException("I/O error reading status from patch file '" + patchFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(zin);
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

    public List<Feature> apply(File base, EOLStyle localEOL, ScmFeedbackHandler scmFeedbackHandler) throws PulseException
    {
        scmFeedbackHandler.status("Applying patch...");
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

            List<Feature> features = new LinkedList<Feature>();
            for (FileStatus fs : statuses)
            {
                preApply(fs, base, features);
                scmFeedbackHandler.checkCancelled();
            }

            unzip(base, scmFeedbackHandler);

            for (FileStatus fs : statuses)
            {
                postApply(fs, base, localEOL);
                scmFeedbackHandler.checkCancelled();
            }

            scmFeedbackHandler.status("Patch applied.");
            return features;
        }
        catch (IOException e)
        {
            throw new PulseException("I/O error applying patch file '" + patchFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
    }

    private void preApply(FileStatus fileStatus, File base, List<Feature> features) throws IOException
    {
        switch (fileStatus.getState())
        {
            case MODIFIED:
            case MERGED:
            case METADATA_MODIFIED:
                checkTargetFile(fileStatus, base, features);
                break;

            case DELETED:
            case REPLACED:
                File f = checkTargetFile(fileStatus, base, features);
                if (fileStatus.isDirectory())
                {
                    if (!FileSystemUtils.rmdir(f))
                    {
                        features.add(new Feature(Feature.Level.WARNING, "Problem applying patch: Unable to delete target directory '" + fileStatus.getTargetPath() + "'"));
                    }
                }
                else
                {
                    if (!f.delete())
                    {
                        features.add(new Feature(Feature.Level.WARNING, "Problem applying patch: Unable to delete target file '" + fileStatus.getTargetPath() + "'"));
                    }
                }
                break;
        }
    }

    private File checkTargetFile(FileStatus fileStatus, File base, List<Feature> features)
    {
        File f = new File(base, fileStatus.getTargetPath());
        if (!f.exists())
        {
            features.add(new Feature(Feature.Level.WARNING, "Problem applying patch: Target file '" + fileStatus.getTargetPath() + "' with status " + fileStatus.getState() + " in patch does not exist"));
        }

        return f;
    }

    private void postApply(FileStatus fileStatus, File base, EOLStyle localEOL) throws IOException
    {
        File file = new File(base, fileStatus.getTargetPath());

        // Apply line ending settings
        String eolName = fileStatus.getProperty(FileStatus.PROPERTY_EOL_STYLE);
        if (eolName != null)
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
        if (executableValue != null)
        {
            boolean executable = Boolean.parseBoolean(executableValue);
            if (file.exists())
            {
                FileSystemUtils.setExecutable(file, executable);
            }
        }
    }

    private void unzip(File base, ScmFeedbackHandler scmFeedbackHandler) throws IOException, PulseException
    {
        ZipInputStream zin = null;

        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));

            // Skip over meta.xml and files/ entries
            if (zin.getNextEntry() == null)
            {
                return;
            }
            IOUtils.joinStreams(zin, new NullOutputStream());

            if (zin.getNextEntry() == null)
            {
                return;
            }
            IOUtils.joinStreams(zin, new NullOutputStream());

            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null)
            {
                extractEntry(base, entry, zin, scmFeedbackHandler);
                scmFeedbackHandler.checkCancelled();
            }
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void extractEntry(File base, ZipEntry entry, ZipInputStream zin, ScmFeedbackHandler scmFeedbackHandler) throws IOException, PulseException
    {
        String path = getPath(entry);
        FileStatus status = metadata.getFileStatus(path);
        if (status == null)
        {
            throw new PulseException("Entry for path '" + path + "' has no metadata");
        }

        scmFeedbackHandler.status(status.toString());

        File f = new File(base, path);
        if (entry.isDirectory())
        {
            if (!f.isDirectory() && !f.mkdirs())
            {
                throw new IOException("Cannot create directory '" + f.getAbsolutePath() + "'");
            }
        }
        else
        {
            File parent = f.getParentFile();
            if (parent != null && !parent.isDirectory())
            {
                if (!parent.mkdirs())
                {
                    throw new IOException("Cannot create parent directory '" + parent.getAbsolutePath() + "'");
                }
            }

            if (!f.canWrite())
            {
                FileSystemUtils.setWritable(f);
            }

            if (status.getPayloadType() == FileStatus.PayloadType.DIFF)
            {
                applyDiff(path, IOUtils.inputStreamToString(zin), f);
            }
            else
            {
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
    }

    private void applyDiff(String path, String diff, File targetFile) throws PulseException
    {
        try
        {
            PatchFileParser parser = new PatchFileParser(new UnifiedPatchParser());
            PatchFile patchFile = parser.parse(new StringReader(diff));
            List<Patch> patches = patchFile.getPatches();
            if (patches.size() != 1)
            {
                throw new PulseException("Patch for path '" + path + "' is invalid");
            }

            Patch patch = patches.get(0);
            patch.apply(targetFile);
        }
        catch (DiffException e)
        {
            throw new PulseException(e);
        }
    }

    private String getPath(ZipEntry entry) throws IOException
    {
        String name = entry.getName();
        if (!name.startsWith(FILES_PATH))
        {
            throw new IOException("Unexpected entry path '" + name + "': should start with '" + FILES_PATH + "'");
        }

        name = name.substring(FILES_PATH.length());
        if (name.endsWith("/"))
        {
            name = name.substring(0, name.length() - 1);
        }
        
        return name;
    }

    public boolean containsPath(String path)
    {
        for (FileStatus fs : metadata.getFileStatuses())
        {
            if (fs.getPath().equals(path) && fs.getState().preferredPayloadType() != FileStatus.PayloadType.NONE)
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
            while ((entry = zin.getNextEntry()) != null)
            {
                if (entry.getName().equals(path))
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

    public static XStream createXStream()
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
}
