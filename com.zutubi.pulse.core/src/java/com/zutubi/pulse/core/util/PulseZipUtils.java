package com.zutubi.pulse.core.util;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.ZipUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.NullOutputStream;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
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

        // ensure that the parent directory exists.
        if (zipFile.exists())
        {
            zipFile.delete();
        }
        else
        {
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
            zipFile.createNewFile();
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
            IOUtils.joinStreams(child.getInputStream(), new NullOutputStream());
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
                    testDir.mkdir();
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
                        FileSystemUtils.rmdir(tmpDir);
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
                IOUtils.joinStreams(is, os);
            }
            finally
            {
                IOUtils.close(is);
            }
        }
    }

    public static byte[] getZipModeBlock(int mode)
    {
        // extra field: (Variable)
        //
        //     ...
        //     files, the following structure should be used for all
        //     programs storing data in this field:
        //
        //     header1+data1 + header2+data2 . . .
        //
        //     Each header should consist of:
        //
        //       Header ID - 2 bytes
        //       Data Size - 2 bytes
        //
        //     Note: all fields stored in Intel low-byte/high-byte order.
        //
        //     The Header ID field indicates the type of data that is in
        //     the following data block.
        // ...
        //
        // The Data Size field indicates the size of the following
        // data block. Programs can use this value to skip to the
        // next header block, passing over any data blocks that are
        // not of interest.

        // Value         Size            Description
        // -----         ----            -----------
        // (Unix3) 0x756e        Short           tag for this extra block type
        //         TSize         Short           total data size for this block
        //         CRC           Long            CRC-32 of the remaining data
        //         Mode          Short           file permissions
        //         SizDev        Long            symlink'd size OR major/minor dev num
        //         UID           Short           user ID
        //         GID           Short           group ID
        //         (var.)        variable        symbolic link filename
        byte[] data = new byte[18];

        encodeZipShort(0x756E, data, 0);
        encodeZipShort(14, data, 2);

        // CRC fills this gap later

        encodeZipShort(mode, data, 8);
        Arrays.fill(data, 10, 18, (byte) 0);

        CRC32 crc = new CRC32();
        crc.update(data, 8, 10);
        long checksum = crc.getValue();

        encodeZipLong(checksum, data, 4);
        return data;
    }

    public static void encodeZipShort(int value, byte[] block, int offset)
    {
        block[offset] = (byte) (value & 0xFF);
        block[offset + 1] = (byte) ((value & 0xFF00) >> 8);
    }

    public static int decodeZipShort(byte[] block, int offset)
    {
        int value = (block[offset + 1] << 8) & 0xFF00;
        value += (block[offset] & 0xFF);
        return value;
    }

    public static void encodeZipLong(long value, byte[] block, int offset)
    {
        block[offset] = (byte) ((value & 0xFF));
        block[offset + 1] = (byte) ((value & 0xFF00) >> 8);
        block[offset + 2] = (byte) ((value & 0xFF0000) >> 16);
        block[offset + 3] = (byte) ((value & 0xFF000000L) >> 24);
    }

    public static long decodeZipLong(byte[] block, int offset)
    {
        long value = (block[offset + 3] << 24) & 0xFF000000L;
        value += (block[offset + 2] << 16) & 0xFF0000;
        value += (block[offset + 1] << 8) & 0xFF00;
        value += (block[offset] & 0xFF);
        return value;
    }

    public static int getModeFromZipBlock(byte[] block)
    {
        // Currently assumes the block was made by us.
        if (block.length == 18)
        {
            return decodeZipShort(block, 8);
        }

        return 0;
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
                    testDir.mkdir();
                    File testFile = new File(testDir, "afile");
                    FileSystemUtils.createFile(testFile, "content");
                    File zipFile = new File(tmpDir, "test.zip");

                    createZipInternal(zipFile, testDir, testDir);

                    File extractDir = new File(tmpDir, "extract");
                    extractDir.mkdir();
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
                        FileSystemUtils.rmdir(tmpDir);
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
