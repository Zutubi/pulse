/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ArchiveFactory
{
    /**
     * The name generator is used to generate the exported archive filenames.
     */
    private ArchiveNameGenerator nameGenerator = new UniqueDatestampedNameGenerator();

    private static final String MANIFEST_FILENAME = "manifest.properties";

    /**
     * Imported/new archive directory.
     */
    private File tmp;

    /**
     * Generate a new archive.  This archive is initially empty.
     *
     * @return a reference to the new archive.
     * 
     * @throws ArchiveException if there is a problem creating the archive.
     */
    public Archive createArchive() throws ArchiveException
    {
        File archiveBase = createNewArchiveBaseDirectory();

        // mostly for information at this stage, so that archives can be distinguished.  Will be used
        // for processing at some stage no doubt.
        Date now = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        String creationDate = dateFormat.format(now);

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
     * specified export directory
     *
     * @param archive to be zipped.
     * @param exportDir directory into which the archive zip file will be written to.
     * @return the file reference to the zip file.
     *
     * @throws ArchiveException if there is a problem zipping or copying the archive to the
     * export directory. Typically, problems would include disk space shortages or a failure to zip
     * the archive directory.
     */
    public File exportArchive(Archive archive, File exportDir) throws ArchiveException
    {
        File exportZipFile = null;
        try
        {
            String archiveName = nameGenerator.newName(exportDir);

            exportZipFile = new File(exportDir, archiveName);

            PulseZipUtils.createZip(exportZipFile, archive.getBase(), null);

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
        File archiveBase = createNewArchiveBaseDirectory();

        try
        {
            // if the archive is a zip file, then unzip it into our base directory.
            if (archive.isFile() && archive.getName().endsWith(".zip"))
            {
                PulseZipUtils.extractZip(archive, archiveBase);
            }
            // if the arhive is a directory, then copy the contents into our base directory (expanded archive).
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

    private File createNewArchiveBaseDirectory() throws ArchiveException
    {
        File archiveBase = new File(tmp, RandomUtils.insecureRandomString(10));
        while (archiveBase.exists())
        {
            archiveBase = new File(tmp, RandomUtils.insecureRandomString(10));
        }

        if (!archiveBase.mkdirs())
        {
            // failed to create the archive file.
            throw new ArchiveException("Failed to create directory '" + archiveBase.getAbsolutePath() + "'");
        }
        return archiveBase;
    }

    public void setTmpDirectory(File tmpDirectory)
    {
        this.tmp = tmpDirectory;
    }

    public void setArchiveNameGenerator(ArchiveNameGenerator nameGenerator)
    {
        this.nameGenerator = nameGenerator;
    }
}
