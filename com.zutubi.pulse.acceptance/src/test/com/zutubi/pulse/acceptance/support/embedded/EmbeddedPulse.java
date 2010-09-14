package com.zutubi.pulse.acceptance.support.embedded;

import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.servercore.cli.ShutdownCommand;
import com.zutubi.pulse.servercore.cli.StartCommand;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of the Pulse interface that assumes that Pulse is
 * being run within IDEA.
 *
 */
public class EmbeddedPulse implements Pulse
{
    private int port = -1;
    
    private String dataDir;
    private String context;
    private String configFile;

    public int start() throws Exception
    {
        System.setProperty("bootstrap", "com/zutubi/pulse/master/bootstrap/ideaBootstrapContext.xml");

        Thread serverThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    StartCommand start = new StartCommand();
                    List<String> args = new LinkedList<String>();
                    if (dataDir != null)
                    {
                        args.add("-d");
                        args.add(dataDir);
                    }
                    if (port != -1)
                    {
                        args.add("-p");
                        args.add(Integer.toString(port));
                    }
                    if (context != null)
                    {
                        args.add("-c");
                        args.add(context);
                    }
                    if (configFile != null)
                    {
                        args.add("-f");
                        args.add(configFile);
                    }

                    start.execute(args.toArray(new String[args.size()]));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        return 0;
    }

    public int start(boolean wait)
    {
        try
        {
            int exitCode = start();
            if (exitCode != 0)
            {
                return exitCode;
            }

            waitForServer(true, 30000);
            return 0;
        }
        catch (Exception e)
        {
            return 1;
        }
    }

    public int start(boolean wait, boolean service)
    {
        throw new RuntimeException("Not yet supported.");
    }

    public int stop()
    {
        try
        {
            ShutdownCommand shutdown = new ShutdownCommand();
            shutdown.setExitJvm(false);
            List<String> args = new LinkedList<String>();
            args.add("-F");
            args.add("true");
            if (port != -1)
            {
                args.add("-p");
                args.add(Integer.toString(port));
            }
            if (context != null)
            {
                args.add("-c");
                args.add(context);
            }
            if (configFile != null)
            {
                args.add("-f");
                args.add(configFile);
            }

            return shutdown.execute(args.toArray(new String[args.size()]));
        }
        catch (Exception e)
        {
            return 1;
        }
    }

    public int stop(long timeout)
    {
        try
        {
            int exitCode = stop();
            if (exitCode != 0)
            {
                return exitCode;
            }
            
            waitForServer(false, timeout);
            return 0;
        }
        catch (Exception e)
        {
            return 1;
        }
    }

    public int stop(long timeout, boolean service)
    {
        throw new RuntimeException("Not yet supported.");
    }

    public int waitForProcessToExit(long timeout)
    {
        try
        {
            waitForServer(false, timeout);
            return 0;
        }
        catch (Exception e)
        {
            return 1;
        }
    }

    public void setPort(long port)
    {
        this.port = (int)port;
    }

    public void setDataDir(String path)
    {
        this.dataDir = path;
    }

    public void setConfigFile(String path)
    {
        this.configFile = path;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public void setUserHome(String path)
    {
        throw new RuntimeException("Not yet supported.");
    }

    public void setVerbose(boolean verbose)
    {
        throw new RuntimeException("Not yet supported.");
    }

    public boolean ping()
    {
        try
        {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(port));
            sock.close();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public String getScript()
    {
        throw new RuntimeException("Not yet supported.");
    }

    public String getPulseHome()
    {
        throw new RuntimeException("Not yet supported.");
    }

    public String getActiveVersionDirectory()
    {
        throw new RuntimeException("Not yet supported.");
    }

    public String getPluginRoot()
    {
        throw new RuntimeException("Not yet supported.");
    }

    public String getServerUrl()
    {
        throw new RuntimeException("Not yet supported.");
    }

    public String getAdminToken()
    {
        throw new RuntimeException("Not yet supported.");
    }

    protected void waitForServer(boolean expectedPing, long timeout) throws Exception
    {
        long end = System.currentTimeMillis() + timeout;
        while(end > System.currentTimeMillis())
        {
            if (ping() == expectedPing)
            {
                return;
            }
            Thread.sleep(1000);
        }
        throw new RuntimeException("timeout");
    }
}
