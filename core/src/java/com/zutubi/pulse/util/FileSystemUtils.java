package com.zutubi.pulse.util;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.*;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Miscellaneous utilities for manipulating the file system.
 *
 * @author jsankey
 */
public class FileSystemUtils
{
    private static final Logger LOG = Logger.getLogger(FileSystemUtils.class);

    public static final char ZIP_SEPARATOR = '/';

    // Unix-style file mode values
    
    public static final int PERMISSION_OWNER_READ    = 0x100;
    public static final int PERMISSION_OWNER_WRITE   = 0x080;
    public static final int PERMISSION_OWNER_EXECUTE = 0x040;
    public static final int PERMISSION_GROUP_READ    = 0x020;
    public static final int PERMISSION_GROUP_WRITE   = 0x010;
    public static final int PERMISSION_GROUP_EXECUTE = 0x008;
    public static final int PERMISSION_OTHER_READ    = 0x004;
    public static final int PERMISSION_OTHER_WRITE   = 0x002;
    public static final int PERMISSION_OTHER_EXECUTE = 0x001;

    public static final int PERMISSION_OWNER_FULL    = PERMISSION_OWNER_READ | PERMISSION_OWNER_WRITE | PERMISSION_OWNER_EXECUTE;
    public static final int PERMISSION_GROUP_FULL    = PERMISSION_GROUP_READ | PERMISSION_GROUP_WRITE | PERMISSION_GROUP_EXECUTE;
    public static final int PERMISSION_OTHER_FULL    = PERMISSION_OTHER_READ | PERMISSION_OTHER_WRITE | PERMISSION_OTHER_EXECUTE;

    public static final int PERMISSION_ALL_READ      = PERMISSION_OWNER_READ | PERMISSION_GROUP_READ | PERMISSION_OTHER_READ;
    public static final int PERMISSION_ALL_WRITE     = PERMISSION_OWNER_WRITE | PERMISSION_GROUP_WRITE | PERMISSION_OTHER_WRITE;
    public static final int PERMISSION_ALL_EXECUTE   = PERMISSION_OWNER_EXECUTE | PERMISSION_GROUP_EXECUTE | PERMISSION_OTHER_EXECUTE;
    public static final int PERMISSION_ALL_FULL      = PERMISSION_OWNER_FULL | PERMISSION_GROUP_FULL | PERMISSION_OTHER_FULL;

    /**
     * Recursively delete a directory and its contents.
     *
     * @param dir the directory to delete
     * @return true iff the whole directory was successfully delete
     */
    public static boolean removeDirectory(File dir)
    {
        if (dir == null)
        {
            return false;
        }

        if (!dir.exists())
        {
            return true;
        }

        if (!dir.isDirectory())
        {
            return false;
        }

        String canonicalDir;

        try
        {
            canonicalDir = dir.getCanonicalPath();
        }
        catch (IOException e)
        {
            return false;
        }

        String[] contents = dir.list();

        if (contents != null)
        {
            for (String child : contents)
            {
                File file = new File(dir, child);
                String canonicalFile;

                // The canonical path lets us distinguish symlinks from actual
                // directories.
                try
                {
                    canonicalFile = file.getCanonicalPath();
                }
                catch (IOException e)
                {
                    return false;
                }

                // We don't want to traverse symbolic links to directories.
                // The canonical path tells us where the file really is, and we
                // double check it is under the directory (using the canonical
                // path for the directory too).
                if (file.isDirectory() && canonicalFile.startsWith(canonicalDir))
                {
                    if (!removeDirectory(file))
                    {
                        return false;
                    }
                }
                else
                {
                    if (!file.delete())
                    {
                        return false;
                    }
                }
            }
        }

        return dir.delete();
    }

    public static void cleanOutputDir(File output) throws IOException
    {
        if (output.isDirectory())
        {
            if (!FileSystemUtils.removeDirectory(output))
            {
                throw new IOException("Unable to remove existing output directory '" + output.getPath() + "'");
            }
        }

        if (!output.mkdirs())
        {
            throw new IOException("Unable to create output directory '" + output.getPath() + "'");
        }
    }

    /**
     * Create a temporary directory using pre-defined prefix and suffix values.  Use this when you really don't
     * care what the directory is called.
     *
     * @return a File object for the created directory
     * @throws IOException if the directory cannot be created
     */
    public static File createTempDirectory() throws IOException
    {
        return createTempDirectory("dir", null);
    }

    public static File createTempDirectory(String prefix, String suffix) throws IOException
    {
        return createTempDirectory(prefix, suffix, null);
    }

    public static File createTempDirectory(String prefix, String suffix, File base) throws IOException
    {
        if (base != null && !base.exists() && !base.mkdirs())
        {
            throw new IOException();
        }
        File file = File.createTempFile(prefix, suffix, base);
        if (!file.exists())
        {
            throw new IOException();
        }
        if (!file.delete())
        {
            throw new IOException();
        }
        if (!file.mkdirs())
        {
            throw new IOException();
        }
        return file;
    }

    public static void createDirectory(File file) throws IOException
    {
        if (file.exists())
        {
            if (!file.isDirectory())
            {
                throw new IOException("Can not create directory. File '" + file + "' already exists.");
            }
            return;
        }
        if (!file.mkdirs())
        {
            throw new IOException("Failed to create directory '" + file + "'");
        }
    }

    public static boolean isParentOf(File parent, File child) throws IOException
    {
        String parentPath = parent.getCanonicalPath();
        String childPath = child.getCanonicalPath();

        return childPath.startsWith(parentPath);
    }

    public static boolean isSymlink(File file) throws IOException
    {
        return !SystemUtils.IS_WINDOWS && !file.getCanonicalPath().equals(file.getAbsolutePath());
    }

    /**
     * On supported systems, returns the permissions of the given file
     * encoded as a single integer.  The encoding depends on the platform:
     * <p/>
     * - Un*x: the same mode format used by chmod/stat.
     * <p/>
     * On unsupported platforms, this call always returns -1.
     *
     * @param file the file to return the permissions for
     * @return the permissions of the given file, or -1 if they cannot be
     *         determined
     */
    public static int getPermissions(File file)
    {
        int result = -1;

        if (SystemUtils.IS_WINDOWS)
        {
            return -1;
        }

        Process process = null;
        try
        {
            try
            {
                process = Runtime.getRuntime().exec(new String[] { "stat",  "-c",  "%a", file.getAbsolutePath()});
            }
            catch (IOException e)
            {
                // This occurs when there is no stat: i.e. unsupported platform.
                return 0;
            }

            InputStreamReader stdoutReader = new InputStreamReader(process.getInputStream());
            StringWriter stdoutWriter = new StringWriter();
            IOUtils.joinReaderToWriter(stdoutReader, stdoutWriter);

            int exitCode = process.waitFor();

            if (exitCode == 0)
            {
                result = Integer.parseInt(stdoutWriter.getBuffer().toString().trim(), 8);
            }
            else
            {
                LOG.warning("Unable to get permissions for '" + file.getAbsolutePath() + "': stat exited with code " + exitCode);
            }
        }
        catch (Exception e)
        {
            LOG.warning("Unable to get permissions for '" + file.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            if (process != null)
            {
                process.destroy();
            }
        }

        return result;
    }

    /**
     * Attempts to set the permissions on the given file to the given
     * permissions.  Not supported on all systems.
     *
     * @param file        the file to set permissions on
     * @param permissions the permissions to set, as encoded by
 *                    {@link #getPermissions(java.io.File)}
     * @return true if the operation succeeded
     */
    public static boolean setPermissions(File file, int permissions)
    {
        if (SystemUtils.IS_WINDOWS || permissions < 0)
        {
            return false;
        }

        return runChmod(file, Integer.toString(permissions, 8));
    }

    public static boolean setExecutable(File file)
    {
        return setExecutable(file, true);
    }

    public static boolean setExecutable(File file, boolean executable)
    {
        if(executable)
        {
            return runChmod(file, "a+x");
        }
        else
        {
            return runChmod(file, "a-x");
        }
    }

    public static boolean setWritable(File file)
    {
        return setWritable(file, true);
    }

    public static boolean setWritable(File file, boolean executable)
    {
        if(executable)
        {
            return runChmod(file, "a+w");
        }
        else
        {
            return runChmod(file, "a-w");
        }
    }

    private static boolean runChmod(File file, String arg)
    {
        if(!SystemUtils.IS_WINDOWS)
        {
            try
            {
                Process p = Runtime.getRuntime().exec(new String[] { "chmod", arg, file.getAbsolutePath()});
                int exitCode = p.waitFor();
                return exitCode == 0;
            }
            catch (Exception e)
            {
                // Oh well, we tried
            }
        }

        return false;
    }

    public static void createZip(File zipFile, File base, File source) throws IOException
    {
        if (!source.exists())
        {
            throw new FileNotFoundException("Source file '" + source.getAbsolutePath() + "' does not exist");
        }

        if (!isParentOf(base, source))
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

    private static void addToZip(ZipOutputStream os, File base, String sourcePath) throws IOException
    {
        File source = new File(base, sourcePath);

        if (isSymlink(source))
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

            String[] files = source.list();

            for (String filename : files)
            {
                String path = dirPath + filename;
                addToZip(os, base, path);
            }
        }
        else
        {
            ZipEntry entry = new ZipEntry(sourcePath);
            entry.setTime(modifiedTime);
            //entry.setExtra(Integer.toOctalString(getPermissions(source)).getBytes());
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
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(zipFile));
            extractZip(zin, dir);
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    public static void extractZip(ZipInputStream zin, File dir) throws IOException
    {
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null)
        {
            File file = new File(dir, entry.getName());

            if (entry.isDirectory())
            {
                file.mkdirs();
            }
            else
            {
                // ensure that the files parents already exist.
                if (!file.getParentFile().isDirectory())
                {
                    file.getParentFile().mkdirs();
                }
                unzip(zin, file);
//                String octalPermissions = new String(entry.getExtra());
//                int permissions = Integer.parseInt(octalPermissions, 8);
//                if (permissions != 0)
//                {
//                    setPermissions(file, permissions);
//                }
            }

            file.setLastModified(entry.getTime());
        }
    }

    private static void unzip(InputStream zin, File file) throws IOException
    {
        FileOutputStream out = null;

        try
        {
            out = new FileOutputStream(file);
            byte[] b = new byte[512];
            int len;
            while ((len = zin.read(b)) != -1)
            {
                out.write(b, 0, len);
            }
        }
        finally
        {
            IOUtils.close(out);
        }
    }

    /**
     * @param src  source file
     * @param dest detination file
     * @param force delete the destination directory if it already exists before renaming.
     * @return true if the rename was successful, false otherwise.
     */
    public static boolean rename(File src, File dest, boolean force)
    {
        if (dest.exists() && force)
        {
            removeDirectory(dest);
        }

        return src.renameTo(dest);
    }

    public static boolean rename(File src, File dest)
    {
        return rename(src, dest, false);
    }

    public static void createFile(File file, String data) throws IOException
    {
        FileOutputStream os = null;
        OutputStreamWriter ow = null;

        try
        {
            os = new FileOutputStream(file);
            ow = new OutputStreamWriter(os);
            ow.write(data);
            ow.flush();
        }
        finally
        {
            IOUtils.close(ow);
            IOUtils.close(os);
        }
    }

    public static File createTempFile(String prefix, String suffix, String data) throws IOException
    {
        File file = File.createTempFile(prefix, suffix);
        createFile(file, data);
        return file;
    }

    public static void createFile(File file, byte[] data) throws IOException
    {
        FileOutputStream os = null;

        try
        {
            os = new FileOutputStream(file);
            os.write(data);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    public static File createTempFile(String prefix, String suffix, byte[] data) throws IOException
    {
        File file = File.createTempFile(prefix, suffix);
        createFile(file, data);
        return file;
    }

    public static void createFile(File file, InputStream is) throws IOException
    {
        FileOutputStream os = null;

        try
        {
            os = new FileOutputStream(file);
            IOUtils.joinStreams(is, os);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    public static boolean createSymlink(File symlink, File destination) throws IOException
    {
        if (SystemUtils.IS_LINUX)
        {
            Process p = Runtime.getRuntime().exec("ln -s " + destination.getAbsolutePath() + " " + symlink.getAbsolutePath());
            int result = 0;
            try
            {
                result = p.waitFor();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            return result == 0;
        }

        return false;
    }

    public static File composeFile(String... parts)
    {
        String result = composeFilename(parts);
        return new File(result);
    }

    public static String composeFilename(String... parts)
    {
        String result = "";
        boolean first = true;

        for (String part : parts)
        {
            if (first)
            {
                first = false;
                result = part;
            }
            else
            {
                result = result + File.separatorChar + part;
            }
        }
        return result;
    }

    public static String normaliseSeparators(String path)
    {
        if (File.separatorChar != '/')
        {
            path = path.replace(File.separatorChar, '/');
        }

        return path;
    }

    public static String denormaliseSeparators(String path)
    {
        if (File.separatorChar != '/')
        {
            path = path.replace('/', File.separatorChar);
        }

        return path;
    }

    /**
     * Converts any separator characters (/ or \) to the local separator
     * character.
     *
     * @param path path to convert
     * @return the path with all separators in local form
     */
    public static String localiseSeparators(String path)
    {
        if (File.separatorChar == '/')
        {
            return path.replace('\\', '/');
        }
        else
        {
            return path.replace('/', '\\');
        }
    }

    public static void copyRecursively(File from, File to) throws IOException
    {
        if (!SystemUtils.IS_WINDOWS && SystemUtils.findInPath("cp") != null)
        {
            // Use the Unix cp command because it:
            //   - preserves permissions; and
            //   - is likely to be faster when it matters (i.e. large copy)
            String flags = "-p";
            if (from.isDirectory())
            {
                if (to.exists())
                {
                    if (to.isDirectory())
                    {
                        if (!removeDirectory(to))
                        {
                            throw new IOException("Cannot remove existing directory '" + to.getAbsolutePath() + "'");
                        }
                    }
                    else
                    {
                        if (!to.delete())
                        {
                            throw new IOException("Cannot remove existing file '" + to.getAbsolutePath() + "'");
                        }
                    }
                }

                flags += "r";
            }

            Process child = Runtime.getRuntime().exec(new String[] { "cp", flags, from.getAbsolutePath(), to.getAbsolutePath() });
            try
            {
                int exit = child.waitFor();
                if (exit != 0)
                {
                    // Attempt to copy ourselves.
                    LOG.info("Copy using cp from '" + from.getAbsolutePath() + "' to '" + to.getAbsolutePath() + "' failed, trying internal copy");
                    removeDirectory(to);
                    internalCopy(from, to);
                }
            }
            catch (InterruptedException e)
            {
                IOException ioe = new IOException("Interrupted while copying from '" + from.getAbsolutePath() + "' to '" + to.getAbsolutePath() + "'");
                ioe.initCause(e);
                throw ioe;
            }
        }
        else
        {
            internalCopy(from, to);
        }
    }

    // WARNING: will not handle recursive symlinks
    private static void internalCopy(File from, File to) throws IOException
    {
        if (from.isDirectory())
        {
            ensureDirectory(to);

            for (String file : from.list())
            {
                internalCopy(new File(from, file), new File(to, file));
            }
        }
        else
        {
            IOUtils.copyFile(from, to);
        }
    }

    private static void ensureDirectory(File to) throws IOException
    {
        if (!to.isDirectory() && !to.mkdirs())
        {
            throw new IOException("Unable to create destination directory '" + to.getAbsolutePath() + "'");
        }
    }

    /**
     * This method returns true if the specified file is the root of a file system.
     *
     * @param f file to test
     * @return true iff the given file is a root
     */
    public static boolean isRoot(File f)
    {
        return f.getParentFile() == null;
    }

    /**
     * Returns true iff the given path is set and refers to a directory.
     *
     * @param path the path to test (may be null)
     * @return true iff path is a directory
     */
    public static boolean isDirectory(String path)
    {
        if (TextUtils.stringSet(path))
        {
            File f = new File(path);
            return f.isDirectory();
        }

        return false;
    }

    /**
     * Returns true iff the given path is set and refers to a regular file.
     *
     * @param path the path to test (may be null)
     * @return true iff path is a regular file
     */
    public static boolean isFile(String path)
    {
        if (TextUtils.stringSet(path))
        {
            File f = new File(path);
            return f.isFile();
        }

        return false;
    }

    public static String getMimeType(File file)
    {
        String type = URLConnection.guessContentTypeFromName(file.getName());
        if (type == null)
        {
            try
            {
                type = URLConnection.guessContentTypeFromStream(new FileInputStream(file));
            }
            catch (IOException e)
            {
                // Oh well
            }

            if (type == null)
            {
                type = "text/plain";
            }
        }
        return type;
    }

    /**
     * Tests if the two files given are identical.
     *
     * @param f1 the first file
     * @param f2 the second file
     * @return true iff the files are byte-by-byte identical
     * @throws IOException if an error occurs reading file data
     */
    public static boolean filesMatch(File f1, File f2) throws IOException
    {
        if (f1.length() != f2.length())
        {
            return false;
        }

        FileInputStream in1 = null;
        FileInputStream in2 = null;

        try
        {
            in1 = new FileInputStream(f1);
            in2 = new FileInputStream(f2);
            byte[] b1 = new byte[1024];
            byte[] b2 = new byte[1024];
            int n;

            while ((n = in2.read(b2)) > 0)
            {
                if (in1.read(b1) != n)
                {
                    return false;
                }

                if (!Arrays.equals(b2, b1))
                {
                    return false;
                }
            }

            return true;
        }
        finally
        {
            IOUtils.close(in1);
            IOUtils.close(in2);
        }
    }

    /**
     * Translates all line endings (CR, CRLF or LF) in the given file to the
     * given bytes.
     *
     * @param file                the file to translate
     * @param eol                 the new line ending as a byte array
     * @param preservePermissions if true, the permissions on the file will
     *                            be preserved
     * @throws IOException if an error occurs
     */
    public static void translateEOLs(File file, byte[] eol, boolean preservePermissions) throws IOException
    {
        File tempFile = null;
        int permissions = -1;

        if(preservePermissions)
        {
            permissions = getPermissions(file);
        }

        try
        {
            tempFile = File.createTempFile(file.getName(), ".tmp", file.getParentFile());

            InputStream in = null;
            OutputStream out = null;

            try
            {
                in = new FileInputStream(file);
                out = new BufferedOutputStream(new FileOutputStream(tempFile));

                byte[] buffer = new byte[1024];
                int n;
                boolean skipNewline = false;

                while((n = in.read(buffer)) > 0)
                {
                    for(int i = 0; i < n; i++)
                    {
                        byte b = buffer[i];
                        switch(b)
                        {
                            case '\r':
                                out.write(eol);
                                skipNewline = true;
                                break;

                            case '\n':
                                if(skipNewline)
                                {
                                    skipNewline = false;
                                }
                                else
                                {
                                    out.write(eol);
                                }
                                break;

                            default:
                                skipNewline = false;                                
                                out.write(b);
                                break;
                        }

                    }
                }
            }
            finally
            {
                IOUtils.close(in);
                IOUtils.close(out);
            }

            file.delete();
            if(!tempFile.renameTo(file))
            {
                throw new IOException("Unable to rename temporary file '" + tempFile.getAbsolutePath() + "' to '" + file.getAbsolutePath() + "'");
            }

            if(permissions >= 0)
            {
                setPermissions(file, permissions);
            }
            
            tempFile = null;
        }
        finally
        {
            if(tempFile != null)
            {
                tempFile.delete();
            }
        }
    }
}
