package com.zutubi.pulse;

import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The stop service provides a way for clients the to stop the
 * server.
 *
 * @author Daniel Ostermeier
 */
public class ShutdownService
{
    private ShutdownManager shutdownManager;
    private ServerSocket socketServer;
    /**
     * The port on which the admin service will be listening for requests.
     */
    private int port;
    private boolean stopping;

    private static final Logger LOG = Logger.getLogger(ShutdownService.class);

    /**
     *
     */
    public static interface Command
    {
        public static final String SHUTDOWN = "stop";
        public static final String FORCED_SHUTDOWN = "stop-now";
    }

    /**
     * @param port
     */
    public ShutdownService(int port)
    {
        this.port = port;
    }

    /**
     * Start the service. Once started, the service will listen for
     * stop requests.
     */
    public void start() throws IOException
    {
        stopping = false;
        socketServer = new ServerSocket(port, 1);

        Runnable r = new Runnable()
        {
            public void run()
            {
                runService();
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    /**
     * Stop the service. Once stopped, this instance of the service
     * should not be started again.
     */
    public void stop()
    {
        stopping = true;
        try
        {
            socketServer.close();
        }
        catch (IOException e)
        {
            // nop
        }
    }

    /**
     * Main execution loop for the stop service.
     */
    private void runService()
    {

        try
        {
            while (!stopping)
            {
                Socket s = socketServer.accept();
                try
                {
                    handleConnection(s);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                IOUtils.close(s);
            }
        }
        catch (IOException e)
        {
            LOG.severe("Error in socketServer, shutting down service.", e);
            IOUtils.close(socketServer);
        }

    }

    /**
     * Handle the new remote connection.
     *
     * @param s
     * @throws IOException
     */
    private void handleConnection(Socket s) throws IOException
    {
        // read the resquest.

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String cmd = reader.readLine();
            if (Command.SHUTDOWN.equals(cmd))
            {
                processShutdown(false);
            }
            else if (Command.FORCED_SHUTDOWN.equals(cmd))
            {
                processShutdown(true);
            }
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    private void processShutdown(boolean force)
    {
        LOG.info("Shutting down server...");
        shutdownManager.shutdown(force);
        stop();
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

}
