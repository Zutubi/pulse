package com.zutubi.pulse.command;

import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
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
    public static final String PULSE_HOME = "pulse.home";
    public static final String VERSION_HOME = "pulse.version.home";

    private static final String DEBUG = "debug";

    private static final int CONFIGURATION_ERROR = 1;
    private static final int UNEXPECTED_ERROR = 2;
    private static final int INVOCATION_ERROR = 3;

    public int process(String[] argv)
    {
        try
        {
            File versionHome = getVersionHome();
            System.setProperty(VERSION_HOME, versionHome.getAbsolutePath());

            ClassLoader classpath = makeClassLoader(versionHome);

            Map<String, Command> commands = loadCommands(versionHome, classpath);

            if (argv.length == 0)
            {
                printHelp(commands);
                return INVOCATION_ERROR;
            }

            // validate the requested command.
            String commandName = argv[0];

            if (!commands.containsKey(commandName))
            {
                // print warning, unknown command requested.
                printError("Unknown command " + commandName);
                printHelp(commands);
                return INVOCATION_ERROR;
            }

            String[] commandArgs = new String[argv.length - 1];
            System.arraycopy(argv, 1, commandArgs, 0, commandArgs.length);

            // setup the class loader context.
            Thread.currentThread().setContextClassLoader(classpath);

            Command command = commands.get(commandName);
            command.parse(commandArgs);
            return command.execute();
        }
        catch (Exception e)
        {
            printError(e);
            return UNEXPECTED_ERROR;
        }
    }

    private File getVersionHome()
    {
        String pulseHomeStr = System.getProperty(PULSE_HOME);
        if(pulseHomeStr == null)
        {
            printError("Require property '" + PULSE_HOME + "' not set");
            System.exit(CONFIGURATION_ERROR);
        }

        File pulseHome = new File(pulseHomeStr);
        if(!pulseHome.isDirectory())
        {
            printError("Invalid Pulse home '" + pulseHome.getAbsolutePath() + "'");
            System.exit(CONFIGURATION_ERROR);
        }

        // Read the active version and readjust
        File activeVersion = new File(pulseHome, "active-version.txt");
        if(!activeVersion.exists())
        {
            printError("Active version file '" + activeVersion + "' does not exist");
            System.exit(CONFIGURATION_ERROR);
        }

        try
        {
            String version = fileToString(activeVersion);
            File versionHome = new File(pulseHome,  version);

            if(!versionHome.exists())
            {
                printError("Active version directory '" + versionHome.getAbsolutePath() + "' does not exist.");
                System.exit(CONFIGURATION_ERROR);
            }

            return versionHome;
        }
        catch (IOException e)
        {
            printError(e);
            System.exit(UNEXPECTED_ERROR);

            // Appease the compiler
            return null;
        }
    }

    private HashMap<String, Command> loadCommands(File pulseHome, ClassLoader classpath)
    {
        HashMap<String, Command> commands = new HashMap<String, Command>();

        // Commands are stored in $PULSE_HOME/commands/*.properties
        Properties commandClasses = new Properties();

        File commandsDir = new File(pulseHome, "commands");
        if(commandsDir.isDirectory())
        {
            String[] propertiesFiles = commandsDir.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".properties");
                }
            });

            for(String propertiesFile: propertiesFiles)
            {
                FileInputStream in = null;

                try
                {
                    in = new FileInputStream(new File(commandsDir, propertiesFile));
                    commandClasses.load(in);
                }
                catch (IOException e)
                {
                    printError(e);
                    System.exit(UNEXPECTED_ERROR);
                }
                finally
                {
                    close(in);
                }
            }
        }

        for(Map.Entry<Object, Object> entry: commandClasses.entrySet())
        {
            addCommand(commands, classpath, (String)entry.getKey(), (String)entry.getValue());
        }

        return commands;
    }

    private void addCommand(HashMap<String, Command> commands, ClassLoader classpath, String name, String className)
    {
        try
        {
            Command command = (Command) classpath.loadClass(className).newInstance();
            commands.put(name, command);
        }
        catch (Exception e)
        {
            printError("Unable to load command '" + name + "'");
            printError(e);
            System.exit(CONFIGURATION_ERROR);
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

    private void printHelp(Map<String, Command> commands)
    {
        System.err.println("The following commands are available:");
        for(Map.Entry<String, Command> entry: commands.entrySet())
        {
            String help = entry.getValue().getHelp();
            if(help != null)
            {
                System.err.println("    " + entry.getKey() + ":\t\t" + help);
            }
        }
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

        File classpathFile = new File(pulseHome, "classpath.txt");
        if(!classpathFile.exists())
        {
            throw new Exception("Classpath file '" + classpathFile.getAbsolutePath() + "' does not exist");
        }

        List<URL> classpath = new LinkedList<URL>();
        BufferedReader reader = new BufferedReader(new FileReader(classpathFile));
        String line;

        while((line = reader.readLine()) != null)
        {
            addToClasspath(pulseHome, line, classpath);
        }


        // add the jdks tools.jar to the classpath.
        addToolsJar(classpath);

        URL[] urls = classpath.toArray(new URL[classpath.size()]);
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

    private static void addToClasspath(File pulseHome, String entry, List<URL> classpath) throws Exception
    {
        entry = entry.trim();
        if(entry.length() == 0)
        {
            return;
        }

        if(entry.endsWith("/"))
        {
            // The entry is just a directory
            classpath.add(new File(pulseHome, normaliseSeparators(entry)).toURL());
        }
        else
        {
            // A file, potentially with a regex name
            File dir;
            String pattern;

            int index = entry.lastIndexOf('/');
            if(index > 0)
            {
                dir = new File(pulseHome, normaliseSeparators(entry.substring(0, index)));
            }
            else
            {
                dir = pulseHome;
            }

            if(index < entry.length() - 1)
            {
                pattern = entry.substring(index + 1);
            }
            else
            {
                throw new Exception("Invalid classpath entry '" + entry + "'");
            }

            final Pattern regex = Pattern.compile(pattern);
            String[] files = dir.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return regex.matcher(name).matches();
                }
            });

            for(String file: files)
            {
                classpath.add(new File(dir, file).toURL());
            }
        }
    }

    private static String normaliseSeparators(String entry)
    {
        return entry.replace('/', File.separatorChar);
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

    public static void close(Closeable closeable)
    {
        try
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
        catch (IOException e)
        {
            // Do nothing
        }
    }

    public static void joinStreams(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[1024];
        int n;

        while ((n = input.read(buffer)) > 0)
        {
            output.write(buffer, 0, n);
        }
    }

    public static void joinStreams(InputStream input, OutputStream output, boolean close) throws IOException
    {
        joinStreams(input, output);
        if (close)
        {
            close(input);
            close(output);
        }
    }

    public static String inputStreamToString(InputStream is) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        joinStreams(is, os);
        return os.toString();
    }

    public static String fileToString(File file) throws IOException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(file);
            return inputStreamToString(is);
        }
        finally
        {
            close(is);
        }
    }

}
