package com.zutubi.util;

import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Miscellaneous utilities for manipulating the file system.
 *
 * @author jsankey
 */
public class FileSystemUtils
{
    private static final Logger LOG = Logger.getLogger(FileSystemUtils.class);

    private static final int DELETE_RETRIES = 3;

    public static final String NORMAL_SEPARATOR      = "/";
    public static final char   NORMAL_SEPARATOR_CHAR = NORMAL_SEPARATOR.charAt(0);

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

    public static final boolean CP_AVAILABLE;
    public static final boolean LN_AVAILABLE;
    public static final boolean STAT_AVAILABLE;
    public static final boolean ZIP_AVAILABLE;

    static
    {
        CP_AVAILABLE = SystemUtils.unixBinaryAvailable("cp");
        LN_AVAILABLE = SystemUtils.unixBinaryAvailable("ln");
        STAT_AVAILABLE = SystemUtils.unixBinaryAvailable("stat");
        ZIP_AVAILABLE  = SystemUtils.unixBinaryAvailable("zip");
    }

    /**
     * Recursively delete a directory and its contents.
     *
     * @param dir the directory to delete
     * 
     * @return true iff the whole directory was successfully deleted
     */
    public static boolean rmdir(File dir)
    {
        if (dir == null)
        {
            return false;
        }

        if (!dir.exists())
        {
            return true;
        }

        if (dir.isFile())
        {
            throw new IllegalArgumentException(String.format("removeDirectory can only be used on directories. %s is not a directory.", dir));
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
                    if (!rmdir(file))
                    {
                        return false;
                    }
                }
                else
                {
                    if (!robustDelete(file))
                    {
                        return false;
                    }
                }
            }
        }

        return robustDelete(dir);
    }

    public static boolean robustDelete(File f)
    {
        boolean deleted = false;
        for(int i = 0; i < DELETE_RETRIES; i++)
        {
            deleted = f.delete();
            if(deleted)
            {
                break;
            }
            else
            {
                System.gc();
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    // Ignore
                }
            }
        }

        return deleted;
    }

    public static void cleanOutputDir(File output) throws IOException
    {
        if (output.isDirectory())
        {
            if (!FileSystemUtils.rmdir(output))
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
    public static File createTempDir() throws IOException
    {
        return createTempDir("dir", null);
    }

    public static File createTempDir(String prefix, String suffix) throws IOException
    {
        return createTempDir(prefix, suffix, null);
    }

    public static File createTempDir(String prefix, String suffix, File base) throws IOException
    {
        if (base != null && !base.exists() && !base.mkdirs())
        {
            throw new IOException("Failed to create temporary directory. Base directory does not exist: " + base.getAbsolutePath());
        }
        
        File file = null;
        IOException exception = null;
        int retries = SystemUtils.IS_WINDOWS ? 3 : 0;
        do
        {
            try
            {
                file = File.createTempFile(prefix, suffix, base);
                break;
            }
            catch (IOException e)
            {
                exception = e;
            }

            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                // Ignore
            }
        }
        while(retries-- > 0);
        
        if(exception != null)
        {
            IOException e = new IOException(exception.getMessage());
            e.initCause(exception);
            throw e;
        }

        assert(file != null);
        if (!file.exists())
        {
            throw new IOException("Failed to create temporary directory. Reason: File.createTempFile failed.");
        }
        if (!file.delete())
        {
            throw new IOException("Failed to create temporary directory. Reason: tmpFile.delete failed.");
        }
        if (!file.mkdirs())
        {
            throw new IOException("Failed to create temporary directory. Reason: tmpDir.mkdirs failed.");
        }
        return file;
    }

    public static void createDirectory(File file) throws IOException
    {
        if (file.exists())
        {
            if (!file.isDirectory())
            {
                throw new IOException(String.format("Can not create directory. File '%s' already exists.", file));
            }
            return;
        }
        if (!file.mkdirs())
        {
            throw new IOException(String.format("Failed to create directory '%s'", file));
        }
    }

    public static boolean isParentOf(File parent, File child) throws IOException
    {
        String parentPath = parent.getCanonicalPath();
        String childPath = child.getCanonicalPath();

        return childPath.startsWith(parentPath);
    }

    /**
     * Returns true iff the given file is a relative symlink.
     *
     * @param file file to test
     * @return true if the file is a symlink with a relative path
     */
    public static boolean isSymlink(File file)
    {
        // WARNING: only detects relative symnlinks
        if(!SystemUtils.IS_WINDOWS)
        {
            // Try testing the canonical path then.
            File parent = file.getParentFile();
            if(parent != null)
            {
                try
                {
                    String parentCanonical = parent.getCanonicalPath() + "/";
                    String fileCanonical = file.getCanonicalPath();

                    if(fileCanonical.startsWith(parentCanonical))
                    {
                        String canonicalName = fileCanonical.substring(parentCanonical.length());
                        return !canonicalName.equals(file.getName());
                    }
                }
                catch (IOException e)
                {
                    LOG.warning(e);
                }
            }
        }

        return false;
    }

    public static boolean pathContainsSymlink(File file) throws IOException
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

        if (SystemUtils.IS_WINDOWS || !STAT_AVAILABLE)
        {
            return -1;
        }

        Process process = null;
        try
        {
            try
            {
                String[] command;
                if(SystemUtils.IS_LINUX)
                {
                    command = new String[] { "stat",  "-c",  "%a", file.getAbsolutePath()};
                }
                else
                {
                    command = new String[] { "stat",  "-f",  "%Lp", file.getAbsolutePath()};
                }
                
                process = Runtime.getRuntime().exec(command);
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
                LOG.warning("Unable to get permissions for '%s': stat exited with code %d", file.getAbsolutePath(), exitCode);
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

    public static boolean setWritable(File file, boolean writable)
    {
        if(writable)
        {
            if(SystemUtils.IS_WINDOWS)
            {
                try
                {
                    SystemUtils.runCommand("attrib", "-R", file.getAbsolutePath());
                    return true;
                }
                catch (IOException e)
                {
                    LOG.warning(e);
                    return false;
                }
            }
            else
            {
                return runChmod(file, "a+w");
            }
        }
        else
        {
            return file.setReadOnly();
        }
    }

    private static boolean runChmod(File file, String arg)
    {
        if(!SystemUtils.IS_WINDOWS)
        {
            Process p = null;
            try
            {
                p = Runtime.getRuntime().exec(new String[] { "chmod", arg, file.getAbsolutePath()});
                int exitCode = p.waitFor();
                return exitCode == 0;
            }
            catch (Exception e)
            {
                // Oh well, we tried
            }
            finally
            {
                if (p != null)
                {
                    p.destroy();
                }
            }
        }

        return false;
    }

    /**
     *
     * @param src  source file
     * @param dest detination file
     * @param force delete the destination directory if it already exists before renaming.
     *
     * @return true if the rename was successful, false otherwise.
     */
    public static boolean rename(File src, File dest, boolean force)
    {
        if (force && dest.exists())
        {
            if(dest.isDirectory())
            {
                rmdir(dest);
            }
            else
            {
                robustDelete(dest);
            }
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
        if (LN_AVAILABLE)
        {
            SystemUtils.runCommand("ln", "-s", destination.getAbsolutePath(), symlink.getAbsolutePath());
            return true;
        }

        return false;
    }

    public static File composeFile(String... parts)
    {
        String result = composeFilename(parts);
        return new File(result);
    }

    public static String join(String... parts)
    {
        return FileSystemUtils.composeFilename(parts);
    }

    public static String composeFilename(String... parts)
    {
        return StringUtils.join(File.separator, parts);
    }

    public static String composeFilename(Collection<String> parts)
    {
        return StringUtils.join(File.separator, parts);
    }

    public static String composeSearchPath(String ...parts)
    {
        return StringUtils.join(File.pathSeparator, parts);
    }

    /**
     * Ensures all separator characters are forward slashes, which works on all
     * supported file systems.
     *
     * @param path the path to normalise
     * @return an equivalent path, but with all separators as forward slashes
     */
    public static String normaliseSeparators(String path)
    {
        return path.replace('\\', NORMAL_SEPARATOR_CHAR);
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
        if (File.separatorChar == NORMAL_SEPARATOR_CHAR)
        {
            return path.replace('\\', NORMAL_SEPARATOR_CHAR);
        }
        else
        {
            return path.replace(NORMAL_SEPARATOR_CHAR, '\\');
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
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
                type = URLConnection.guessContentTypeFromStream(fis);
            }
            catch (IOException e)
            {
                // Oh well
            }
            finally
            {
                IOUtils.close(fis);
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

    /**
     * Copies source the source file(s) to the destination.  A few modes are
     * supported:
     *
     * Single source:
     *   File -> File: copies a file to a file, overwriting an existing dest
     *   File -> Dir : copy file into an existing dest directory, overwriting
     *                 any existing child file (but not existing child dir!)
     *   Dir  -> Dir : recursive copy of directory, overwrites existing dest,
     *                 (even if it is a file) creates dest if necessary
     *
     * Multiple sources (must all be files):
     *   File(s) -> Dir: copies files into existing dest dir, overwrites
     *                   existing dest, creates dest if necessary
     * 
     * @param dest destination to copy file(s) to
     * @param src  source files to be copied
     * @throws IOException on any error
     */
    public static void copy(File dest, File... src) throws IOException
    {
        if (src.length == 0)
        {
            return;
        }

        if (CP_AVAILABLE)
        {
            unixCopy(dest, src);
        }
        else
        {
            javaCopy(dest, src);
        }
    }

    public static void delete(File f) throws IOException
    {
        if (f.exists())
        {
            if (f.isDirectory())
            {
                if (!rmdir(f))
                {
                    throw new IOException("Cannot remove existing directory '" + f.getAbsolutePath() + "'");
                }
            }
            else
            {
                if (!f.delete())
                {
                    throw new IOException("Cannot remove existing file '" + f.getAbsolutePath() + "'");
                }
            }
        }
    }

    public static void ensureEmptyDirectory(File dir) throws IOException
    {
        delete(dir);
        if(!dir.mkdirs())
        {
            throw new IOException("Unable to create destination directory '" + dir.getAbsolutePath() + "'");
        }
    }

    private static void ensureNoDirectories(File[] files) throws IOException
    {
        for(File f: files)
        {
            if(f.isDirectory())
            {
                throw new IOException("Copy failed: multiple sources including an existing directory '" + f.getAbsolutePath() + "'");
            }
        }
    }

    static void unixCopy(File dest, File... src) throws IOException
    {
        // Use the Unix cp command because it:
        //   - preserves permissions; and
        //   - is likely to be faster when it matters (i.e. large copy)
        String flags = "-pR";
        if(SystemUtils.IS_LINUX)
        {
            flags += "d";
        }

        if (src.length == 1)
        {
            // cp handles file->file and file->dir as expected.  Help is
            // required for dir->dir, we need to eliminate an existing dest.
            if(src[0].isDirectory())
            {
                delete(dest);
            }
        }
        else
        {
            ensureNoDirectories(src);
            ensureEmptyDirectory(dest);
        }

        List<String> argsList = new LinkedList<String>();
        argsList.add("cp");
        argsList.add(flags);
        for (File f : src)
        {
            argsList.add(f.getAbsolutePath());
        }
        argsList.add(dest.getAbsolutePath());

        String[] args = argsList.toArray(new String[argsList.size()]);
        Process child = Runtime.getRuntime().exec(args);
        try
        {
            int exit = child.waitFor();
            if (exit != 0)
            {
                // Attempt to copy ourselves.
                LOG.warning("Copy using '"+ StringUtils.join(" ", args)+"' failed, trying internal copy");
                rmdir(dest);
                javaCopy(dest, src);
            }
        }
        catch (InterruptedException e)
        {
            IOException ioe = new IOException("Interrupted while executing '"+StringUtils.join(" ", args)+"'");
            ioe.initCause(e);
            throw ioe;
        }
        finally
        {
            if (child != null)
            {
                child.destroy();
            }
        }
    }

    static void javaCopy(File dest, File... src) throws IOException
    {
        if(src.length == 1)
        {
            File singleSource = src[0];
            if(singleSource.isFile())
            {
                if(dest.isDirectory())
                {
                    dest = new File(dest, singleSource.getName());
                    if(dest.isDirectory())
                    {
                        throw new IOException("Copy failed: destination directory contains existing directory '" + dest.getAbsolutePath() + "' with same name as source file");
                    }
                }

                delete(dest);
                IOUtils.copyFile(singleSource, dest);
            }
            else if(singleSource.isDirectory())
            {
                ensureEmptyDirectory(dest);
                internalCopy(singleSource, dest);
            }
            else
            {
                throw new IOException(("Copy failed: source '" + singleSource.getAbsolutePath() + "' does not exist"));
            }
        }
        else
        {
            ensureNoDirectories(src);
            ensureEmptyDirectory(dest);

            for (File f : src)
            {
                // copy into dest directory.
                internalCopy(f, new File(dest, f.getName()));
            }
        }
    }

    // WARNING: will not handle recursive symlinks
    protected static void internalCopy(File src, File dest) throws IOException
    {
        if (src.isDirectory())
        {
            if (!dest.isDirectory() && !dest.mkdirs())
            {
                throw new IOException(String.format("Copy failed. Failed to create dir %s", dest.getAbsolutePath()));
            }

            for (String file : src.list())
            {
                internalCopy(new File(src, file), new File(dest, file));
            }
        }
        else
        {
            if (dest.isFile())
            {
                // trouble..
                throw new IOException(String.format("Copy failed. Failed to copy to file %s, it already exists.", dest.getAbsolutePath()));
            }
            if (!dest.createNewFile())
            {
                throw new IOException(String.format("Copy failed. Failed to create file %s", dest.getAbsolutePath()));
            }
            IOUtils.copyFile(src, dest);
        }
    }
}
