package com.zutubi.pulse.restore;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 *
 */
public class ArchiveFactory
{
    /**
     * The name generator is used to generate the exported archive filenames.
     */
    private ArchiveNameGenerator nameGenerator = new UniqueDatestampedNameGenerator();

    private static final String MANIFEST_FILENAME = "manifest.properties";

    /**
     * Exported archive directory.
     */
    private File archiveDirectory;

    /**
     * Imported/new archive directory.
     */
    private File tmpDirectory;

    private static final SimpleDateFormat CREATED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public ArchiveFactory()
    {
    }

    /**
     * Generate a new archive.  This archive is initially empty.
     *
     * @return a reference to the new archive.
     * 
     * @throws ArchiveException if there is a problem creating the archive.
     */
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
            throw new ArchiveException("Failed to create directory '" + archiveBase.getAbsolutePath() + "'");
        }

        // mostly for information at this stage, so that archives can be distinguished.  Will be used
        // for processing at some stage no doubt.
        Date now = Calendar.getInstance().getTime();
        String creationDate = CREATED_FORMAT.format(now);

        // the archive format version is not currently used for anything.
        ArchiveManifest manifest = new ArchiveManifest(creationDate, "1.0");

        // write the manifest - properties.
        try
        {
            File manifestFile = new File(archiveBase, MANIFEST_FILENAME);
            manifest.writeTo(manifestFile);
        }
        catch (IOException e)
        {
            throw new ArchiveException("Failed to write the new archive manifest file.", e);
        }

        return new Archive(archiveBase, manifest);
    }

    /**
     * Exporting an archive zips up that archives directories and copies the zip file to the
     * exported archives directory
     *
     * @param archive to be zipped.
     * @return the file reference to the zip file.
     *
     * @throws ArchiveException if there is a problem zipping or copying the archive to the
     * export directory. Typically, problems would include disk space shortages or a failure to zip
     * the archive directory.
     */
    public File exportArchive(Archive archive) throws ArchiveException
    {
        File exportZipFile = null;
        try
        {
            String baseArchiveName = nameGenerator.newName(archiveDirectory);

            String archiveName = baseArchiveName + ".zip";
            File candidateArchiveFile = new File(archiveDirectory, archiveName);
            int i = 0;
            while (candidateArchiveFile.exists())
            {
                i++;
                archiveName = baseArchiveName + "_" + i + ".zip";
                candidateArchiveFile = new File(archiveDirectory, archiveName);
            }

            exportZipFile = new File(archiveDirectory, archiveName);

            ZipUtils.createZip(exportZipFile, archive.getBase(), null);

            return exportZipFile;
        }
        catch (IOException e)
        {
            throw new ArchiveException("Failed to zip directory '" + archive.getBase().getAbsolutePath()
                    + "' to file '" + exportZipFile.getAbsolutePath() + "'", e);
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

        return new Archive(archive, archiveBase, manifest);
    }

    public void setTmpDirectory(File tmpDirectory)
    {
        this.tmpDirectory = tmpDirectory;
    }

    public void setArchiveDirectory(File archiveDirectory)
    {
        this.archiveDirectory = archiveDirectory;
    }

    public void setArchiveNameGenerator(ArchiveNameGenerator nameGenerator)
    {
        this.nameGenerator = nameGenerator;
    }
}
