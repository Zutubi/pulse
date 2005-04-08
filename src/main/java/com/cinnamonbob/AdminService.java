package com.cinnamonbob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * The admin service provides remote clients the ability to interact with the
 * server by responding to commands sent to a dedicated admin port.
 * <p>Supported commands are:
 * <ul>
 * <li>shutdown</li> send a shutdown request to the server.
 * </ul>
 *
 * @author Daniel Ostermeier
 */
public class AdminService
{

    private BobServer bobServer;

    private ServerSocket socketServer;

    /**
     * The port on which the admin service will be listening for requests.
     */
    private int adminPort;

    private boolean stopping;

    private static final Logger LOG = Logger.getLogger(AdminService.class.getName());

    /**
     *
     */
    static interface Command
    {
        final String SHUTDOWN = "shutdown";
        final String BUILD = "build";
    }

    /**
     * @param adminPort
     */
    public AdminService(int adminPort, BobServer server)
    {
        this.adminPort = adminPort;
        this.bobServer = server;
    }

    /**
     * Start the admin service. Once started, the admin service will respond
     * to requests sent to the admin port.
     */
    public void start() throws IOException
    {
        stopping = false;
        socketServer = new ServerSocket(adminPort, 1);

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
     * Stop the admin service. Once stopped, this instance of the admin service
     * should not be started again.
     */
    public void stop()
    {
        stopping = true;
        try
        {
            socketServer.close();
        } catch (IOException e)
        {
            // nop
        }
    }

    /**
     * Main execution loop for the admin service.
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
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                try
                {
                    s.close();
                } catch (IOException e)
                {
                    // nop
                    e.printStackTrace();
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                socketServer.close();
            } catch (IOException e1)
            {
                // nop
                e1.printStackTrace();
            }
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
                processShutdown();
            } else if (Command.BUILD.equals(cmd))
            {
                processBuild();
            }
        } finally
        {
            reader.close();
        }
    }

    private void processShutdown()
    {
        LOG.info("Shutting down server...");

        bobServer.stop();

    }

    private void processBuild()
    {
        bobServer.build();
    }

}
