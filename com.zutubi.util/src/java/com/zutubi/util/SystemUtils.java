package com.zutubi.util;

import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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
    public static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().startsWith("mac");

    public static final String LINE_SEPARATOR;

    public static final byte[] LINE_SEPARATOR_BYTES;
    public static final byte[] CR_BYTES = new byte[]{'\r'};
    public static final byte[] CRLF_BYTES = new byte[]{'\r', '\n'};
    public static final byte[] LF_BYTES = new byte[]{'\n'};

    static
    {
        String sep = System.getProperty("line.separator");
        if (sep == null)
        {
            if (IS_WINDOWS)
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

    /**
     * Runs a command started by the given process builder, reading all
     * standard output into a string.
     *
     * @param expectedExitCode if non-negative, specifies the expected exit
     *                         code from the child process.  If the actual exit
     *                         code differs an exception is thrown.
     * @param input            if not null, input to feed to the standard in
     *                         of the child process
     * @param processBuilder   builder used to start the child process
     * @return the standard output of the process
     * @throws IOException on an error running the process
     */
    public static String runCommandWithInput(int expectedExitCode, String input, ProcessBuilder processBuilder) throws IOException
    {
        Process process = processBuilder.start();

        try
        {
            if (input != null)
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

            if (expectedExitCode < 0 || exitCode == expectedExitCode)
            {
                return stdoutWriter.getBuffer().toString();
            }
            else
            {
                throw new IOException(String.format("Command '%s' exited with code %d", processBuilder.command().get(0), exitCode));
            }
        }
        finally
        {
            process.destroy();
        }
    }

    /**
     * Runs a command started by the given process builder, reading all
     * standard output into a string.  The command must return exit status zero
     * or an exception is thrown.
     *
     * @param input          if not null, input to feed to the standard in of
     *                       the child process
     * @param processBuilder builder used to start the child process
     * @return the standard output of the process
     * @throws IOException on an error running the process
     */
    public static String runCommandWithInput(String input, ProcessBuilder processBuilder) throws IOException
    {
        return runCommandWithInput(0, input, processBuilder);
    }

    /**
     * Runs a command specified by the given strings, reading all standard
     * output into a string.
     *
     * @param expectedExitCode if non-negative, specifies the expected exit
     *                         code from the child process.  If the actual exit
     *                         code differs an exception is thrown.
     * @param input            if not null, input to feed to the standard in
     *                         of the child process
     * @param command          the command to run, followed by the arguments to
     *                         pass to the command
     * @return the standard output of the process
     * @throws IOException on an error running the process
     */
    public static String runCommandWithInput(int expectedExitCode, String input, String... command) throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        return runCommandWithInput(expectedExitCode, input, processBuilder);
    }

   /**
    * Runs a command specified by the given strings, reading all standard
    * output into a string.  The command must return exit status zero or an
     * exception is thrown.
    *
    * @param input   if not null, input to feed to the standard in of the child
    *                process
    * @param command the command to run, followed by the arguments to pass to
    *                the command
    * @return the standard output of the process
    * @throws IOException on an error running the process
    */
   public static String runCommandWithInput(String input, String... command) throws IOException
   {
       return runCommandWithInput(0, input, command);
   }

    /**
     * Runs a command specified by the given strings, reading all standard
     * output into a string.
     *
     * @param expectedExitCode if non-negative, specifies the expected exit
     *                         code from the child process.  If the actual exit
     *                         code differs an exception is thrown.
     * @param command          the command to run, followed by the arguments to
     *                         pass to the command
     * @return the standard output of the process
     * @throws IOException on an error running the process
     */
    public static String runCommand(int expectedExitCode, String... command) throws IOException
    {
        return runCommandWithInput(expectedExitCode,  null, command);
    }

    /**
     * Runs a command specified by the given strings, reading all standard
     * output into a string.  The command must return exit status zero or an
     * exception is thrown.
     *
     * @param command the command to run, followed by the arguments to pass to
     *        the command
     * @return the standard output of the process
     * @throws IOException on an error running the process
     */
    public static String runCommand(String... command) throws IOException
    {
        return runCommand(0, command);
    }

    /**
     * Attempts to find an executable with the given name in the given
     * extra paths or directories in the system PATH.  For most systems,
     * this equates to finding a file of the given name in one of the
     * extra paths or a directory in the PATH.  On windows, both the given
     * name and all variants of it by adding the extensions in PATHEXT are
     * tried.
     *
     * @param name       the name of the executable to look for
     * @param extraPaths a set of extra paths to check, in order, BEFORE
     *                   checking the system PATH
     * @return the file in the path, or null if not found
     */
    public static File findInPath(String name, Collection<String> extraPaths)
    {
        List<String> allPaths = new LinkedList<String>();
        if (extraPaths != null)
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
        if (pathext == null)
        {
            extensions = new String[]{".COM", ".EXE", ".BAT", ".CMD", ".VBS", ".VBE", ".JS", ".JSE", ".WSF", ".WSH"};
        }
        else
        {
            extensions = pathext.split(";");
            for (int i = 0; i < extensions.length; i++)
            {
                extensions[i] = extensions[i].toUpperCase();
            }
        }

        for (String p : paths)
        {
            File dir = new File(p);
            if (dir.isDirectory())
            {
                for (String filename : FileSystemUtils.list(dir))
                {
                    File candidate = new File(dir, filename);
                    if (candidate.isFile() && filenameMatches(name, filename, extensions))
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
        if (filename.equals(name))
        {
            return true;
        }

        filename = filename.toUpperCase();

        for (String extension : extensions)
        {
            if (filename.equals(name + extension))
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

    public static boolean getBooleanProperty(String key, boolean defaultValue)
    {
        String value = System.getProperty(key);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return Boolean.valueOf(value);
        }
    }

    /**
     * Trigger a dump of all the threads currently active in the system.
     *
     * @param out   the print stream to which the thread dump is written.
     */
    public static void threadDump(PrintStream out)
    {
        for (Thread t : getAllThreads())
        {
            out.println(t);
            StackTraceElement[] traceElements = t.getStackTrace();
            for (StackTraceElement element : traceElements) 
            {
                out.println("\tat " + element);
            }
            out.println();
        }
    }

    /**
     * Get a list of all the threads currently active within the system.
     *
     * @return a list of threads.
     */
    public static List<Thread> getAllThreads()
    {
        final ThreadGroup root = getRootThreadGroup();
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        int nAlloc = thbean.getThreadCount();
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);

        List<Thread> result = new LinkedList<Thread>();
        CollectionUtils.filter(threads, new Predicate<Thread>()
        {
            public boolean satisfied(Thread thread)
            {
                return thread != null;
            }
        }, result);
        return result;
    }

    private static ThreadGroup getRootThreadGroup() {

        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentThreadGroup;
        while ((parentThreadGroup = threadGroup.getParent()) != null)
        {
            threadGroup = parentThreadGroup;
        }
        return threadGroup;
    }
}
