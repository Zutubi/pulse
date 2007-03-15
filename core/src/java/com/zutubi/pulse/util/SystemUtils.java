package com.zutubi.pulse.util;

import com.zutubi.pulse.util.logging.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class SystemUtils
{
    private static final Logger LOG = Logger.getLogger(SystemUtils.class);
    
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("win");
    public static final boolean IS_LINUX = System.getProperty("os.name").toLowerCase().startsWith("linux");

    public static final String LINE_SEPARATOR;

    public static final byte[] LINE_SEPARATOR_BYTES;
    public static final byte[] CR_BYTES   = new byte [] { '\r' };
    public static final byte[] CRLF_BYTES = new byte [] { '\r', '\n' };
    public static final byte[] LF_BYTES   = new byte [] { '\n' };

    static
    {
        String sep = System.getProperty("line.separator");
        if(sep == null)
        {
            if(IS_WINDOWS)
            {
                sep = "\r\n";
            }
            else
            {
                sep = "\n";
            }
        }

        LINE_SEPARATOR = sep;
        LINE_SEPARATOR_BYTES = LINE_SEPARATOR.getBytes();
    }

    public static String osName()
    {
        return System.getProperty("os.name");
    }

    public static File findInPath(String name)
    {
        return findInPath(name, null);
    }

    public static String runCommandWithInput(String input, String... command) throws IOException
    {
        Process process;
        process = Runtime.getRuntime().exec(command);

        if(input != null)
        {
            OutputStream stdinStream = null;

            try
            {
                stdinStream = process.getOutputStream();
                stdinStream.write(input.getBytes("US-ASCII"));
            }
            finally
            {
                IOUtils.close(stdinStream);
            }
        }
        
        InputStreamReader stdoutReader = new InputStreamReader(process.getInputStream());
        StringWriter stdoutWriter = new StringWriter();
        IOUtils.joinReaderToWriter(stdoutReader, stdoutWriter);

        int exitCode = 0;
        try
        {
            exitCode = process.waitFor();
        }
        catch (InterruptedException e)
        {
            LOG.warning(e);
        }

        if (exitCode == 0)
        {
            return stdoutWriter.getBuffer().toString();
        }
        else
        {
            System.out.println(stdoutWriter.getBuffer().toString());
            throw new IOException(String.format("Command '%s' exited with code %d", command[0], exitCode));
        }
    }

    public static String runCommand(String... command) throws IOException
    {
        return runCommandWithInput(null, command);
    }

    /**
     * Attempts to find an executable with the given name in the given
     * extra paths or directories in the system PATH.  For most systems,
     * this equates to finding a file of the given name in one of the
     * extra paths or a directory in the PATH.  On windows, files are
     * expected to have one of the extensions in PATHEXT.
     *
     * @param name the name of the executable to look for
     * @param extraPaths a set of extra paths to check, in order, BEFORE
     *                   checking the system PATH
     * @return the file in the path, or null if not found
     */
    public static File findInPath(String name, Collection<String> extraPaths)
    {
        List<String> allPaths = new LinkedList<String>();
        if(extraPaths != null)
        {
            allPaths.addAll(extraPaths);
        }

        String path = System.getenv("PATH");
        if (path != null)
        {
            String[] paths = path.split(File.pathSeparator);
            allPaths.addAll(Arrays.asList(paths));
        }

        if (IS_WINDOWS)
        {
            return findInWindowsPaths(allPaths, name);
        }
        else
        {
            return findInPaths(allPaths, name);
        }
    }

    private static File findInPaths(List<String> paths, String name)
    {
        for (String dir : paths)
        {
            File test = new File(dir, name);
            if (test.isFile())
            {
                return test;
            }
        }

        return null;
    }

    private static File findInWindowsPaths(List<String> paths, String name)
    {
        // Force uppercase for name and extensions to do case insensitive
        // comparisons
        name = name.toUpperCase();

        // Use PATHEXT for executable extensions where is is defined,
        // otherwise use a sensible default list.
        String[] extensions;
        String pathext = System.getenv("PATHEXT");
        if(pathext == null)
        {
            extensions = new String[] { ".COM", ".EXE", ".BAT", ".CMD", ".VBS", ".VBE", ".JS", ".JSE", ".WSF", ".WSH" };
        }
        else
        {
            extensions = pathext.split(";");
            for(int i = 0; i < extensions.length; i++)
            {
                extensions[i] = extensions[i].toUpperCase();
            }
        }

        for (String p: paths)
        {
            File dir = new File(p);
            if(dir.isDirectory())
            {
                String[] list = dir.list();
                for(String filename: list)
                {
                    File candidate = new File(dir, filename);
                    if(candidate.isFile() && filenameMatches(name, filename, extensions))
                    {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private static boolean filenameMatches(String name, String filename, String[] extensions)
    {
        filename = filename.toUpperCase();

        for(String extension: extensions)
        {
            if(filename.equals(name + extension))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if this is a Unix-like system with the given binary
     * available in the path.
     *
     * @param binary the name of the binary to search for
     * @return true iff the binary is available
     */
    public static boolean unixBinaryAvailable(String binary)
    {
        return !IS_WINDOWS && findInPath(binary) != null;
    }
}
