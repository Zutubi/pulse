package com.zutubi.pulse.restore;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 *
 */
public class ArchiveFactory
{
    private static final String MANIFEST_FILENAME = "manifest.properties";

    private File archiveDirectory;

    private File tmpDirectory;

    public ArchiveFactory()
    {
    }

    public Archive createArchive() throws ArchiveException
    {
        File archiveBase = new File(tmpDirectory, RandomUtils.randomString(10));
        while (archiveBase.exists())
        {
            archiveBase = new File(tmpDirectory, RandomUtils.randomString(10));
        }

        if (!archiveBase.mkdirs())
        {
            // failed to create the archive file.
            throw new ArchiveException();
        }

        // mostly for information at this stage, so that archives can be distinguished.  Will be used
        // for processing at some stage no doubt.
        ArchiveManifest manifest = new ArchiveManifest("created",
                "version",
                "author");

        // write the manifest - properties.
        try
        {
            File manifestFile = new File(archiveBase, MANIFEST_FILENAME);
            manifest.writeTo(manifestFile);
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }

        return new Archive(archiveBase, manifest);
    }

    public File exportArchive(Archive archive) throws ArchiveException
    {
        try
        {
            String archiveName = generateArchiveName() + ".zip";

            File exportFile = new File(archiveDirectory, archiveName);

            ZipUtils.createZip(exportFile, archive.getFile(), null);

            return exportFile;
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    public Archive importArchive(File archive) throws ArchiveException
    {
        File archiveBase = new File(tmpDirectory, RandomUtils.randomString(10));
        while (archiveBase.exists())
        {
            archiveBase = new File(tmpDirectory, RandomUtils.randomString(10));
        }

        if (!archiveBase.mkdirs())
        {
            throw new ArchiveException();
        }

        try
        {
            if (archive.isFile() && archive.getName().endsWith(".zip"))
            {
                ZipUtils.extractZip(archive, archiveBase);
            }
            else if (archive.isDirectory())
            {
                FileSystemUtils.copy(archiveBase, archive);
            }
            else
            {
                throw new ArchiveException("Unexpected archive file: " + archive.getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }

        ArchiveManifest manifest;
        try
        {
            File manifestFile = new File(archiveBase, MANIFEST_FILENAME);
            if (!manifestFile.isFile())
            {
                throw new ArchiveException("Invalid archive.  No manifest located.");
            }
            manifest = ArchiveManifest.readFrom(manifestFile);
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }

        return new Archive(archiveBase, manifest);
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");

    // name generator, based on a predefined file name format, rolling(of sorts) if the name is already in use.
    private String generateArchiveName()
    {
        String baseArchiveName = "archive-" + DATE_FORMAT.format(Calendar.getInstance().getTime());

        String archiveName = baseArchiveName;
        File candidateArchiveFile = new File(archiveDirectory, archiveName);
        int i = 0;
        while (candidateArchiveFile.exists())
        {
            i++;
            archiveName = baseArchiveName + "_" + i;
            candidateArchiveFile = new File(archiveDirectory, archiveName);
        }

        return archiveName;
    }

    public void setTmpDirectory(File tmpDirectory)
    {
        this.tmpDirectory = tmpDirectory;
    }

    public void setArchiveDirectory(File archiveDirectory)
    {
        this.archiveDirectory = archiveDirectory;
    }
}
