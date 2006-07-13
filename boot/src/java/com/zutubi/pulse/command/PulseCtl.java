package com.zutubi.pulse.command;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Pulse bootstrap handler. Controls the initialisation of the pulse class loader
 * and then triggers the requested command.
 *
 * Use the -Ddebug=true command line option to print the pulse application classpath to standard error.
 */
public class PulseCtl
{
    private static final String PULSE_HOME = "pulse.home";

    private static final String DEBUG = "debug";

    private static final Map<String, String> COMMANDS = new HashMap<String, String>();

    static
    {
        COMMANDS.put("local", "com.zutubi.pulse.local.LocalBuildCommand");
        COMMANDS.put("start", "com.zutubi.pulse.command.StartCommand");
        COMMANDS.put("shutdown", "com.zutubi.pulse.command.ShutdownCommand");
        COMMANDS.put("setpassword", "com.zutubi.pulse.command.SetPasswordCommand");
    }

    public int process(String[] argv)
    {
        try
        {
            if (argv.length == 0)
            {
                printHelp();
                return 1;
            }

            // validate the requested command.
            String commandName = argv[0];

            if (!COMMANDS.containsKey(commandName))
            {
                // print warning, unknown command requested.
                printError("Unknown command " + commandName);
                printHelp();
                return 1;
            }

            String[] commandArgs = new String[argv.length - 1];
            System.arraycopy(argv, 1, commandArgs, 0, commandArgs.length);

            // setup the class loader context.
            String pulseHomeStr = System.getProperty(PULSE_HOME);
            File pulseHome = new File(pulseHomeStr);

            // validate pulseHome.

            ClassLoader classpath;
            try
            {
                classpath = makeClassLoader(pulseHome);
            }
            catch (MalformedURLException e)
            {
                printError(e);
                return 2;
            }

            Thread.currentThread().setContextClassLoader(classpath);

            String commandClassName = COMMANDS.get(commandName);
            Command command = (Command) classpath.loadClass(commandClassName).newInstance();
            command.parse(commandArgs);
            return command.execute();
        }
        catch (Exception e)
        {
            printError(e);
            return 3;
        }
    }

    private void printError(String msg)
    {
        System.err.println("Error: " + msg);
        System.err.println(msg);
    }

    private void printError(Exception e)
    {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace(System.err);
    }

    private void printHelp()
    {
        System.err.println("The following admin commands are available:");
        System.err.println("    start:\t\t\tstart the server.");
        System.err.println("    shutdown:\t\t\tshutdown the server.");
        System.err.println("    setpassword:\t\t\tset a users password.");
        System.err.println("To see specific help information about any of these commands, type admin 'command' --help");
    }

    private static boolean isDebugEnabled()
    {
        return Boolean.getBoolean(DEBUG);
    }

    public static void main(String argv[])
    {
        int exitStatus = new PulseCtl().process(argv);
        if (exitStatus != 0)
        {
            System.exit(exitStatus);
        }
        // DO NOT CALL System.exit(0). This would abort the app. if start was the command.
    }

    private static ClassLoader makeClassLoader(File pulseHome) throws Exception
    {
        ClassLoader parent = PulseCtl.class.getClassLoader();
        if (parent == null)
        {
            parent = ClassLoader.getSystemClassLoader();
        }

        File libdir = new File(pulseHome, "lib");
        if (!libdir.isDirectory())
        {
            throw new Exception("ERROR: lib is not a directory (or not found): " + libdir.getAbsolutePath());
        }

        List<URL> classpath = new LinkedList<URL>();

        // construct the classpath:
        //for i in "$PULSE_HOME"/system/www/WEB-INF/classes \
        //         "$PULSE_HOME"/lib                        \
        //         "$PULSE_HOME"/lib/*.jar                  \
        //         "$PULSE_HOME"/lib/*.xml; do
        //  LOCALCLASSPATH="$LOCALCLASSPATH":"$i"
        //done

        classpath.add(new File(pulseHome, asPath("system", "www", "WEB-INF", "classes")).toURL());
        classpath.add(new File(pulseHome, asPath("lib")).toURL());

        // add the jdks tools.jar to the classpath.
        addToolsJar(classpath);

        final File[] files = libdir.listFiles();
        appendToClasspath(files, ".jar", classpath);
        appendToClasspath(files, ".xml", classpath);

        URL[] urls = classpath.toArray(new URL[0]);
        if (isDebugEnabled())
        {
            // print the classpath to the Std error for debugging.
            System.err.println("Pulse Classpath:");
            for (URL url : urls)
            {
                System.err.println(" - " + url);
            }
        }

        return new URLClassLoader(urls, parent);
    }

    private static void appendToClasspath(File[] files, String extension, List<URL> classpath)
            throws MalformedURLException
    {
        for (File file : files)
        {
            if (file.getName().endsWith(extension))
            {
                classpath.add(file.toURL());
            }
        }
    }

    private static String asPath(String... elements)
    {
        StringBuffer buffer = new StringBuffer();
        String sep = "";
        for (String element : elements)
        {
            buffer.append(sep);
            buffer.append(element);
            sep = File.separator;
        }
        return buffer.toString();
    }

    private static void addToolsJar(List<URL> jarUrls) throws MalformedURLException
    {
        File javaHome = new File(System.getProperty("java.home"));

        File tools;

        tools = new File(javaHome, asPath("lib", "tools.jar"));
        if (tools.isFile())
        {
            jarUrls.add(tools.toURL());
            return;
        }

        tools = new File(javaHome, asPath("..", "lib", "tools.jar"));
        if (tools.isFile())
        {
            jarUrls.add(tools.toURL());
        }
    }
}
