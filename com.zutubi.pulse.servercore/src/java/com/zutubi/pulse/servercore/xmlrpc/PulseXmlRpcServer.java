package com.zutubi.pulse.servercore.xmlrpc;

import com.zutubi.util.logging.Logger;
import org.apache.xmlrpc.XmlRpcContext;
import org.apache.xmlrpc.XmlRpcServer;
import org.apache.xmlrpc.XmlRpcWorker;

import java.io.InputStream;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Overrides the Apache "server" implementation to:
 *
 * <ol>
 *     <li>Create our own workers of a custom type.</li>
 *     <li>Make the pool management thread-safe (http://jira.zutubi.com/browse/CIB-3103).</li>
 *     <li>Remove output to stdout (uncool for a server library!).</li>
 * </ol>
 *
 * We copy the pool management code (despite it being a different style) to be conservative (this
 * fix is for a patch release).
 */
public class PulseXmlRpcServer extends XmlRpcServer
{
    private static final Logger LOG = Logger.getLogger(PulseXmlRpcServer.class);

    private final Stack<XmlRpcWorker> pool;
    private int nbrWorkers;
    private int maxThreads = -1;

    public PulseXmlRpcServer()
    {
        pool = new Stack<XmlRpcWorker>();
        nbrWorkers = 0;
    }

    public byte[] execute(InputStream is, XmlRpcContext context)
    {
        XmlRpcWorker worker = getWorker();
        try
        {
            return worker.execute(is, context);
        }
        finally
        {
            synchronized (pool)
            {
                pool.push(worker);
            }
        }
    }

    public int getMaxThreads()
    {
        if (maxThreads == -1)
        {
            maxThreads = Integer.getInteger("pulse.xmlrpc.max.threads", 100);
        }

        return maxThreads;
    }

    protected XmlRpcWorker getWorker()
    {
        synchronized (pool)
        {
            try
            {
                return pool.pop();
            }
            catch(EmptyStackException x)
            {
                int maxThreads = getMaxThreads();
                if (nbrWorkers < maxThreads)
                {
                    nbrWorkers += 1;
                    if (nbrWorkers >= maxThreads * .95)
                    {
                        LOG.warning("95% of XML-RPC server threads in use");
                    }
                    return createWorker();
                }
                throw new RuntimeException("System overload: Maximum number of " +
                                           "concurrent requests (" + maxThreads +
                                           ") exceeded");
            }
        }
    }

    protected XmlRpcWorker createWorker()
    {
        return new PulseXmlRpcWorker(getHandlerMapping());
    }
}
