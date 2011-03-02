package com.zutubi.pulse.core.util.process;

import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A wrapper around a Java process that handles running the process
 * asynchronously, including reading the output and error streams.
 * <p/>
 * Output can be handled as bytes (most efficient), characters or lines of
 * characters.
 */
public class AsyncProcess
{
    private Process process;
    private Lock lock = new ReentrantLock();
    private AtomicBoolean destroyed = new AtomicBoolean(false);
    private Semaphore completedFlag = new Semaphore(0);
    private Integer exitCode = null;
    private AsyncProcess.Reader stdoutReader;
    private AsyncProcess.Reader stderrReader;
    private Exception readerException;

    // Package local for testing

    Thread stdoutThread = null;
    Thread stderrThread = null;
    Thread waiterThread = null;

    public AsyncProcess(Process process, StreamerBuilder streamerBuilder, boolean readErrors)
    {
        this.process = process;
        stdoutReader = new Reader(streamerBuilder.create(process.getInputStream(), false));
        stdoutThread = new Thread(stdoutReader, "AsyncProcess.Reader(stdout) for " + process.toString());
        stdoutThread.start();
        if (readErrors)
        {
            stderrReader = new Reader(streamerBuilder.create(process.getErrorStream(), true));
            stderrThread = new Thread(stderrReader, "AsyncProcess.Reader(stderr) for " + process.toString());
            stderrThread.start();
        }

        waiterThread = new Thread(new Waiter(), "AsyncProcess.Waiter for " + process.toString());
        waiterThread.start();
    }

    public AsyncProcess(Process process, ByteHandler handler, boolean readErrors)
    {
        this(process, new ByteStreamerBuilder(handler), readErrors);
    }

    public AsyncProcess(Process process, CharHandler handler, boolean readErrors)
    {
        this(process, new CharStreamerBuilder(handler), readErrors);
    }

    public AsyncProcess(Process process, LineHandler handler, boolean readErrors)
    {
        this(process, new LineStreamerBuilder(handler), readErrors);
    }

    public int waitFor() throws InterruptedException, IOException
    {
        completedFlag.acquire();
        checkForReaderException();
        return exitCode;
    }

    public void waitForSuccess() throws IOException, InterruptedException
    {
        checkExitCode(waitFor());
    }

    public Integer waitFor(long timeout, TimeUnit timeUnit) throws InterruptedException, IOException
    {
        Integer result = null;
        if (completedFlag.tryAcquire(timeout, timeUnit))
        {
            result = exitCode;
        }

        checkForReaderException();
        return result;
    }

    public Integer waitForSuccess(long timeout, TimeUnit timeUnit) throws InterruptedException, IOException
    {
        Integer result = null;
        if (completedFlag.tryAcquire(timeout, timeUnit))
        {
            result = exitCode;
            checkExitCode(result);
        }

        checkForReaderException();
        return result;
    }

    public int waitForOrThrow(long timeout, TimeUnit timeUnit) throws InterruptedException, IOException, TimeoutException
    {
        Integer result = waitFor(timeout, timeUnit);
        if(result == null)
        {
            throw new TimeoutException("Timed out waiting for command to complete");
        }

        return result;
    }

    public void waitForSuccessOrThrow(long timeout, TimeUnit timeUnit) throws InterruptedException, IOException, TimeoutException
    {
        int exitCode = waitForOrThrow(timeout, timeUnit);
        checkExitCode(exitCode);
    }

    private void checkExitCode(int exitCode) throws IOException
    {
        if(exitCode != 0)
        {
            throw new IOException("Command exited with non-zero code '" + exitCode + "'");
        }
    }

    private void checkForReaderException() throws IOException
    {
        lock.lock();
        try
        {
            if(readerException != null)
            {
                IOException e = new IOException("Error encountered reading process output: " + readerException.getMessage());
                e.initCause(readerException);
                throw e;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void destroy()
    {
        // First guarantee that we never use a handler again.
        destroyed.set(true);

        // Ask our readers to terminate, if they have not already exited normally.
        if (stderrReader != null)
        {
            stderrReader.terminate();
        }
        stdoutReader.terminate();

        try
        {
            // If the process is finished, we can ask for the exit code.  We
            // need to destroy anyway to cleanup resources.
            process.exitValue();
            process.destroy();
        }
        catch (IllegalThreadStateException e)
        {
            // Not yet terminated, nail it if possible.
            ProcessControl.destroyProcess(process);
        }
    }

    private static interface Streamer
    {
        boolean stream(AtomicBoolean destroyed) throws Exception;
        void close();
    }

    private static class ByteStreamer implements Streamer
    {
        private InputStream in;
        private boolean error;
        private ByteHandler handler;
        private byte[] buffer;

        public ByteStreamer(InputStream in, boolean error, ByteHandler handler)
        {
            this.in = in;
            this.error = error;
            this.handler = handler;
            buffer = new byte[1024];
        }

        public boolean stream(AtomicBoolean destroyed) throws Exception
        {
            int n = in.read(buffer);
            if(n < 0)
            {
                return false;
            }

            if (!destroyed.get())
            {
                handler.handle(buffer, n, error);
            }
            return true;
        }

        public void close()
        {
            IOUtils.close(in);
        }
    }

    private static class CharStreamer implements Streamer
    {
        private BufferedReader reader;
        private boolean error;
        private CharHandler handler;
        private char[] buffer;

        public CharStreamer(InputStream in, boolean error, CharHandler handler)
        {
            reader = new BufferedReader(new InputStreamReader(in, handler.getCharset()));
            this.error = error;
            this.handler = handler;
            buffer = new char[1024];
        }

        public boolean stream(AtomicBoolean destroyed) throws IOException
        {
            int n = reader.read(buffer);
            if(n < 0)
            {
                return false;
            }

            if (!destroyed.get())
            {
                handler.handle(buffer, n, error);
            }
            return true;
        }

        public void close()
        {
            IOUtils.close(reader);
        }
    }

    private static class LineStreamer implements Streamer
    {
        private BufferedReader reader;
        private boolean error;
        private LineHandler handler;

        public LineStreamer(InputStream in, boolean error, LineHandler handler)
        {
            reader = new BufferedReader(new InputStreamReader(in, handler.getCharset()));
            this.error = error;
            this.handler = handler;
        }

        public boolean stream(AtomicBoolean destroyed) throws IOException
        {
            String line = reader.readLine();
            if(line == null)
            {
                return false;
            }

            if (!destroyed.get())
            {
                handler.handle(line, error);
            }
            return true;
        }

        public void close()
        {
            IOUtils.close(reader);
        }
    }

    private static interface StreamerBuilder
    {
        Streamer create(InputStream stream, boolean error);
    }

    private static class ByteStreamerBuilder implements StreamerBuilder
    {
        private ByteHandler handler;

        public ByteStreamerBuilder(ByteHandler handler)
        {
            this.handler = handler;
        }

        public Streamer create(InputStream stream, boolean error)
        {
            return new ByteStreamer(stream, error, handler);
        }
    }

    private static class CharStreamerBuilder implements StreamerBuilder
    {
        private CharHandler handler;

        public CharStreamerBuilder(CharHandler handler)
        {
            this.handler = handler;
        }

        public Streamer create(InputStream stream, boolean error)
        {
            return new CharStreamer(stream, error, handler);
        }
    }

    private static class LineStreamerBuilder implements StreamerBuilder
    {
        private LineHandler handler;

        public LineStreamerBuilder(LineHandler handler)
        {
            this.handler = handler;
        }

        public Streamer create(InputStream stream, boolean error)
        {
            return new LineStreamer(stream, error, handler);
        }
    }

    private class Waiter implements Runnable
    {
        public void run()
        {
            try
            {
                exitCode = process.waitFor();
                if (stderrThread != null)
                {
                    stderrThread.join(30000);
                }

                stdoutThread.join(30000);
            }
            catch (InterruptedException e)
            {
                // Empty
            }

            completedFlag.release();
        }
    }

    private class Reader implements Runnable
    {
        private boolean terminated = false;
        private Streamer streamer;

        public Reader(Streamer streamer)
        {
            this.streamer = streamer;
        }

        public void run()
        {
            try
            {
                while (!terminated && !Thread.interrupted() && streamer.stream(destroyed))
                {
                    // Empty
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // Programmer error
                e.printStackTrace();
            }
            catch (Exception e)
            {
                lock.lock();
                try
                {
                    if (readerException == null)
                    {
                        readerException = e;
                    }
                }
                finally
                {
                    lock.unlock();
                }
            }
            finally
            {
                streamer.close();
            }
        }

        public void terminate()
        {
            terminated = true;
        }
    }
}
