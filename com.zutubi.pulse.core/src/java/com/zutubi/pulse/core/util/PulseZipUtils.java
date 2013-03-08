package com.zutubi.pulse.core.util;

import com.google.common.io.ByteStreams;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.ZipUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility functions for working with zip archives.
 */
public class PulseZipUtils
{
    public static final char ZIP_SEPARATOR = '/';

    private static final Logger LOG = Logger.getLogger(PulseZipUtils.class);

    static final String PROPERTY_USE_EXTERNAL_ARCHIVING = "pulse.use.external.archiving";
    static final String PROPERTY_ARCHIVE_COMMAND = "pulse.archive.command";
    static final String PROPERTY_UNARCHIVE_COMMAND = "pulse.unarchive.command";

    private static final String VARIABLE_ZIPFILE = "${zipfile}";
    private static final String VARIABLE_FILES = "${files}";

    private static boolean useExternalArchiving;
    private static String archiveCommand;
    private static String unarchiveCommand;
    private static Boolean useExternalZip = null;
    private static Boolean useExternalUnzip = null;

    static
    {
        setDefaults();
    }
    
    static void setDefaults()
    {
        useExternalArchiving = SystemUtils.getBooleanProperty(PROPERTY_USE_EXTERNAL_ARCHIVING, FileSystemUtils.ZIP_AVAILABLE);
        archiveCommand = System.getProperty(PROPERTY_ARCHIVE_COMMAND, "zip -qry " + VARIABLE_ZIPFILE + " " + VARIABLE_FILES);
        unarchiveCommand = System.getProperty(PROPERTY_UNARCHIVE_COMMAND, "unzip -qq " + VARIABLE_ZIPFILE);
        useExternalZip = null;
        useExternalUnzip = null;
    }

    static void setUseExternalArchiving(boolean useExternalArchiving)
    {
        PulseZipUtils.useExternalArchiving = useExternalArchiving;
    }

    static void setArchiveCommand(String archiveCommand)
    {
        PulseZipUtils.archiveCommand = archiveCommand;
    }

    static void setUnarchiveCommand(String unarchiveCommand)
    {
        PulseZipUtils.unarchiveCommand = unarchiveCommand;
    }

    public static void createZip(File zipFile, File source, String file) throws IOException
    {
        if (!source.exists())
        {
            throw new FileNotFoundException("Source directory '" + source.getAbsolutePath() + "' does not exist");
        }

        if (!source.isDirectory())
        {
            throw new FileNotFoundException("Source '" + source.getAbsolutePath() + "' is not a directory");
        }

        if (zipFile.exists())
        {
            FileSystemUtils.delete(zipFile);
        }
        else
        {
            // ensure that the parent directory exists.
            File parent = zipFile.getAbsoluteFile().getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs())
            {
                throw new IOException(String.format("Failed to create the directory '%s'", zipFile.getParentFile().getAbsolutePath()));
            }
        }

        if(file == null && FileSystemUtils.list(source).length == 0)
        {
            // Special case: we create an empty file.  This is understood by
            // our inverse (extractZip).
            FileSystemUtils.createNewFile(zipFile, false);
        }
        else
        {
            if(useExternalZip())
            {
                createZipExternal(zipFile, source, file);
            }
            else
            {
                createZipInternal(zipFile, source, file == null ? source : new File(source, file));
            }
        }
    }

    static void createZipExternal(File zipFile, File source, String file) throws IOException
    {
        runArchiveProcess(createZipBuilder(zipFile, source, file));
    }

    private static void runArchiveProcess(ProcessBuilder pb) throws IOException
    {
        pb.redirectErrorStream(true);
        Process child = pb.start();
        try
        {
            ByteStreams.copy(child.getInputStream(), ByteStreams.nullOutputStream());
            int exitCode = child.waitFor();
            if(exitCode != 0)
            {
                throw new IOException("External archiving process returned code " + exitCode);
            }
        }
        catch (InterruptedException e)
        {
            throw new IOException("Interrupted waiting for external archiving process");
        }
        finally
        {
            child.destroy();
        }
    }

    private static ProcessBuilder createZipBuilder(File zipFile, File source, String file)
    {
        List<String> pieces = StringUtils.split(archiveCommand);
        List<String> command = new LinkedList<String>();
        for(String piece: pieces)
        {
            if(VARIABLE_ZIPFILE.equals(piece))
            {
                command.add(zipFile.getAbsolutePath());
            }
            else if(VARIABLE_FILES.equals(piece))
            {
                if(file == null)
                {
                    command.addAll(Arrays.asList(FileSystemUtils.list(source)));
                }
                else
                {
                    command.add(file);
                }
            }
            else
            {
                command.add(piece);
            }
        }

        ProcessBuilder result = new ProcessBuilder(command);
        result.directory(source);
        return result;
    }

    private synchronized static boolean useExternalZip()
    {
        if(useExternalZip == null)
        {
            useExternalZip = false;
            if(useExternalArchiving)
            {
                File tmpDir = null;

                // Do a simple test run of zipping.
                try
                {
                    tmpDir = FileSystemUtils.createTempDir(PulseZipUtils.class.getName());
                    File testDir = new File(tmpDir, "test");
                    FileSystemUtils.createDirectory(testDir);
                    File testFile = new File(testDir, "afile");
                    FileSystemUtils.createFile(testFile, "content");
                    File zipFile = new File(tmpDir, "test.zip");

                    createZipExternal(zipFile, testDir, null);

                    File extractDir = new File(tmpDir, "extract");
                    extractZipInternal(zipFile, extractDir);
                    testFile = new File(extractDir, "afile");
                    if(!testFile.exists())
                    {
                        throw new IOException("Extraction of test zip failed: expected file not found.");
                    }

                    useExternalZip = true;
                }
                catch (IOException e)
                {
                    LOG.warning("I/O error testing external archiving: " + e.getMessage(), e);
                    LOG.warning("External archiving is disabled");
                }
                finally
                {
                    if (tmpDir != null)
                    {
                        try
                        {
                            FileSystemUtils.rmdir(tmpDir);
                        }
                        catch (IOException e)
                        {
                            // Ignore.
                        }
                    }
                }
            }
        }

        return useExternalZip;
    }

    static void createZipInternal(File zipFile, File base, File source) throws IOException
    {
        if (!source.exists())
        {
            throw new FileNotFoundException("Source file '" + source.getAbsolutePath() + "' does not exist");
        }

        if (!FileSystemUtils.isParentOf(base, source))
        {
            throw new IOException("Base '" + base.getAbsolutePath() + "' is not a parent of source '" + source.getAbsolutePath() + "'");
        }

        ZipOutputStream os = null;

        try
        {
            os = new ZipOutputStream(new FileOutputStream(zipFile));
            String sourcePath = source.getAbsolutePath().substring(base.getAbsolutePath().length());
            // The additional check for slashes is for systems that may accept something other than
            // their canonical file separator (e.g. '/' is acceptable on Windows despite '\' being
            // the canonical separator).
            sourcePath = sourcePath.replace('\\', ZIP_SEPARATOR);
            sourcePath = sourcePath.replace(File.separatorChar, ZIP_SEPARATOR);
            if (sourcePath.startsWith("/"))
            {
                sourcePath = sourcePath.substring(1);
            }
            addToZip(os, base, sourcePath);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    public static void addToZip(ZipOutputStream os, File base, String sourcePath) throws IOException
    {
        File source = new File(base, sourcePath);

        if (!FileSystemUtils.isParentOf(base, source) || FileSystemUtils.isRelativeSymlink(source))
        {
            return;
        }

        long modifiedTime = source.lastModified();

        if (source.isDirectory())
        {
            String dirPath = "";
            if (!("".equals(sourcePath)))
            {
                dirPath = sourcePath + ZIP_SEPARATOR;
                ZipEntry entry = new ZipEntry(dirPath);
                entry.setTime(modifiedTime);
                os.putNextEntry(entry);
            }

            for (String filename : FileSystemUtils.list(source))
            {
                String path = dirPath + filename;
                addToZip(os, base, path);
            }
        }
        else if(source.exists())
        {
            ZipEntry entry = new ZipEntry(sourcePath);
            entry.setTime(modifiedTime);
            os.putNextEntry(entry);

            FileInputStream is = null;

            try
            {
                is = new FileInputStream(source);
                ByteStreams.copy(is, os);
            }
            finally
            {
                IOUtils.close(is);
            }
        }
    }

    public static void extractZip(File zipFile, File dir) throws IOException
    {
        if(!zipFile.isFile())
        {
            throw new FileNotFoundException("Zip file '" + zipFile.getAbsolutePath() + "' does not exist");
        }

        if(zipFile.length() == 0)
        {
            // Special case: nothing to do.
            return;
        }

        if(!dir.isDirectory())
        {
            if (!dir.mkdirs())
            {
                throw new IOException("Unable to create destination directory '" + dir.getAbsolutePath() + "'");
            }
        }

        if(useExternalUnzip())
        {
            extractZipExternal(zipFile, dir);
        }
        else
        {
            extractZipInternal(zipFile, dir);
        }
    }

    private synchronized static boolean useExternalUnzip()
    {
        if(useExternalUnzip == null)
        {
            useExternalUnzip = false;
            if(useExternalArchiving)
            {
                File tmpDir = null;

                // Do a simple test run of unzipping.
                try
                {
                    tmpDir = FileSystemUtils.createTempDir(PulseZipUtils.class.getName());
                    File testDir = new File(tmpDir, "test");
                    FileSystemUtils.createDirectory(testDir);
                    File testFile = new File(testDir, "afile");
                    FileSystemUtils.createFile(testFile, "content");
                    File zipFile = new File(tmpDir, "test.zip");

                    createZipInternal(zipFile, testDir, testDir);

                    File extractDir = new File(tmpDir, "extract");
                    FileSystemUtils.createDirectory(extractDir);
                    extractZipExternal(zipFile, extractDir);
                    testFile = new File(extractDir, "afile");
                    if(!testFile.exists())
                    {
                        throw new IOException("Extraction of test zip failed: expected file not found.");
                    }

                    useExternalUnzip = true;
                }
                catch (IOException e)
                {
                    LOG.warning("I/O error testing external unarchiving: " + e.getMessage(), e);
                    LOG.warning("External unarchiving is disabled");
                }
                finally
                {
                    if (tmpDir != null)
                    {
                        try
                        {
                            FileSystemUtils.rmdir(tmpDir);
                        }
                        catch (IOException e)
                        {
                            // Ignore.
                        }
                    }
                }
            }
        }

        return useExternalUnzip;
    }

    static void extractZipExternal(File zipFile, File extractDir) throws IOException
    {
        runArchiveProcess(createUnzipBuilder(zipFile, extractDir));
    }

    private static ProcessBuilder createUnzipBuilder(File zipFile, File dest)
    {
        List<String> pieces = StringUtils.split(unarchiveCommand);
        List<String> command = new LinkedList<String>();
        for(String piece: pieces)
        {
            if(VARIABLE_ZIPFILE.equals(piece))
            {
                command.add(zipFile.getAbsolutePath());
            }
            else
            {
                command.add(piece);
            }
        }

        ProcessBuilder result = new ProcessBuilder(command);
        result.directory(dest);
        return result;
    }

    static void extractZipInternal(File zipFile, File dir) throws IOException
    {
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(zipFile));
            ZipUtils.extractZip(zin, dir);
        }
        finally
        {
            IOUtils.close(zin);
        }
    }
}
