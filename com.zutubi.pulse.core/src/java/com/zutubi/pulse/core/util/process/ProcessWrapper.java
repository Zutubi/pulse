/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.util.process;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A wrapper around a Java process that handles running the process asynchronously, including
 * reading the output and error streams.
 * <p/>
 * The wrapper uses internal threads to read data from streams and wait on the process completion.
 * This avoids deadlocks around full output buffers, and also allows the underlying process to be
 * "cut loose" if it never exits (even despite attempts to terminate it).
 * <p/>
 * Output can be handled as bytes (most efficient), characters or lines of characters.  Standard
 * output is always processed, and the client may also choose to handle standard error.  Note that
 * separate threads are used to handle each stream, so handlers must be thread safe.  Errors
 * detected on reading threads are captured and raised via the various wait* calls.
 */
public class ProcessWrapper
{
    public static final int TIMEOUT_NONE = 0;

    private static final int READER_JOIN_TIMEOUT = 30000;
    private static final int BUFFER_SIZE = 4096;

    private Process process;
    private AtomicBoolean destroyed = new AtomicBoolean(false);
    private Semaphore completedFlag = new Semaphore(0);
    private Integer exitCode = null;
    private Lock readerExceptionLock = new ReentrantLock();
    private Exception readerException;

    // Package local for testing

    Thread stdoutThread = null;
    Thread stderrThread = null;
    Thread waiterThread = null;

    /**
     * Convenience method to run a command under a wrapper sending all output to a stream.
     *
     * @param command the command to run
     * @param inDir if not null, the working directory to run in
     * @param outputStream if not null, the stream to send all output to
     * @param timeout time limit to apply to wait for the command to finish
     * @param timeUnit units of the timeout
     * @throws InterruptedException if we're interrupted waiting for the command to run
     * @throws TimeoutException never thrown (this variant uses an infinite timeout)
     * @throws IOException on I/O error creating or reading output from the process
     */
    public static void runCommand(List<String> command, String inDir, PulseExecutionContext context, long timeout, TimeUnit timeUnit) throws IOException, InterruptedException, TimeoutException
    {
        ProcessWrapper processWrapper = null;
        try
        {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            if (StringUtils.stringSet(inDir))
            {
                builder.directory(new File(inDir));
            }

            RecipeUtils.addPulseEnvironment(context, builder);

            ByteHandler byteHandler;
            if (context.getOutputStream() == null)
            {
                byteHandler = new NullByteHandler();
            }
            else
            {
                byteHandler = new ForwardingByteHandler(context.getOutputStream());
            }

            processWrapper = new ProcessWrapper(builder.start(), byteHandler, false);
            if (timeout != TIMEOUT_NONE)
            {
                processWrapper.waitForSuccessOrThrow(timeout, timeUnit);
            }
            else
            {
                processWrapper.waitForSuccess();
            }
        }
        finally
        {
            if (processWrapper != null)
            {
                processWrapper.destroy();
            }
        }

    }

    private ProcessWrapper(Process process, StreamerBuilder streamerBuilder, boolean readErrors)
    {
        this.process = process;
        Reader stdoutReader = new Reader(streamerBuilder.create(process.getInputStream(), false));
        stdoutThread = new Thread(stdoutReader, "ProcessWrapper.Reader(stdout) for " + process.toString());
        stdoutThread.start();
        if (readErrors)
        {
            Reader stderrReader = new Reader(streamerBuilder.create(process.getErrorStream(), true));
            stderrThread = new Thread(stderrReader, "ProcessWrapper.Reader(stderr) for " + process.toString());
            stderrThread.start();
        }

        waiterThread = new Thread(new Waiter(), "ProcessWrapper.Waiter for " + process.toString());
        waiterThread.start();
    }

    /**
     * Wraps the given process, handling output as raw bytes.
     * 
     * @param process the process to wrap, should already by running
     * @param handler handler that will be passed raw output from the process
     * @param readErrors if true, standard error will also be read and passed to the handler
     */
    public ProcessWrapper(Process process, ByteHandler handler, boolean readErrors)
    {
        this(process, new ByteStreamerBuilder(handler), readErrors);
    }

    /**
     * Wraps the given process, handling output as characters.  The handler specifies the character
     * set used for conversion.
     *
     * @param process the process to wrap, should already by running
     * @param handler handler that will be passed raw output from the process
     * @param readErrors if true, standard error will also be read and passed to the handler
     */
    public ProcessWrapper(Process process, CharHandler handler, boolean readErrors)
    {
        this(process, new CharStreamerBuilder(handler), readErrors);
    }

    /**
     * Wraps the given process, handling output as lines of characters.  The handler specifies the
     * character set used for conversion.  Lines are read via
     * {@link java.io.BufferedReader#readLine()}.
     *
     * @param process the process to wrap, should already by running
     * @param handler handler that will be passed raw output from the process
     * @param readErrors if true, standard error will also be read and passed to the handler
     */
    public ProcessWrapper(Process process, LineHandler handler, boolean readErrors)
    {
        this(process, new LineStreamerBuilder(handler), readErrors);
    }

    /**
     * Waits until the underlying process exits, returning the exit code.  Note that an
     * unconditional wait is not recommended, as there is no way to cut a rogue process loose.
     * Consider {@link #waitFor(long, java.util.concurrent.TimeUnit)} instead.
     * 
     * @return the process exit code
     * @throws InterruptedException if this thread is interrupted while waiting
     * @throws IOException if an error is detected on one of the output reading threads
     */
    public int waitFor() throws InterruptedException, IOException
    {
        completedFlag.acquire();
        checkForReaderException();
        return exitCode;
    }

    /**
     * Waits until the underlying process exits, and verifies it did so with a zero exit code.  Note
     * that an unconditional wait is not recommended, as there is no way to cut a rogue process
     * loose.  Consider {@link #waitForSuccess(long, java.util.concurrent.TimeUnit)} instead.
     *
     * @throws InterruptedException if this thread is interrupted while waiting
     * @throws IOException if the process returned a non-zero exit code, or an error is detected on
     *         one of the output reading threads
     */
    public void waitForSuccess() throws IOException, InterruptedException
    {
        checkExitCode(waitFor());
    }

    /**
     * Waits until the underlying process exits or the given timeout is reached.
     *
     * @param timeout timeout magnitude
     * @param timeUnit timeout units
     * @return the process exit code, or null if the timeout expired before the process exited 
     * @throws InterruptedException if this thread is interrupted while waiting
     * @throws IOException if an error is detected on one of the output reading threads
     */
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

    /**
     * Waits until the underlying process exits or the given timeout is reached.  If the process
     * does exit this method verifies it did so with a zero exit code.
     *
     * @param timeout timeout magnitude
     * @param timeUnit timeout units
     * @return the process exit code, either zero or null if the timeout expired before the process
     *         exited 
     * @throws InterruptedException if this thread is interrupted while waiting
     * @throws IOException if the process returned a non-zero exit code, or an error is detected on
     *         one of the output reading threads
     */
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

    /**
     * Waits until the underlying process exits or the given timeout is reached, throwing an
     * exception on timeout.
     *
     * @param timeout timeout magnitude
     * @param timeUnit timeout units
     * @return the process exit code
     * @throws InterruptedException if this thread is interrupted while waiting
     * @throws IOException if an error is detected on one of the output reading threads
     * @throws TimeoutException if the process does not complete within the timeout
     */
    public int waitForOrThrow(long timeout, TimeUnit timeUnit) throws InterruptedException, IOException, TimeoutException
    {
        Integer result = waitFor(timeout, timeUnit);
        if (result == null)
        {
            throw new TimeoutException("Timed out waiting for command to complete");
        }

        return result;
    }

    /**
     * Waits until the underlying process exits or the given timeout is reached, throwing an
     * exception on timeout.  If the process does exit the exit code is verified and an exception is
     * thrown when it is not zero.
     *
     * @param timeout timeout magnitude
     * @param timeUnit timeout units
     * @throws InterruptedException if this thread is interrupted while waiting
     * @throws IOException if the process exits with a non-zero code or an error is detected on one
     *         of the output reading threads
     * @throws TimeoutException if the process does not complete within the timeout
     */
    public void waitForSuccessOrThrow(long timeout, TimeUnit timeUnit) throws InterruptedException, IOException, TimeoutException
    {
        int exitCode = waitForOrThrow(timeout, timeUnit);
        checkExitCode(exitCode);
    }

    private void checkExitCode(int exitCode) throws IOException
    {
        if (exitCode != 0)
        {
            throw new IOException("Command exited with non-zero code '" + exitCode + "'");
        }
    }

    private void checkForReaderException() throws IOException
    {
        readerExceptionLock.lock();
        try
        {
            if (readerException != null)
            {
                IOException e = new IOException("Error encountered reading process output: " + readerException.getMessage());
                e.initCause(readerException);
                throw e;
            }
        }
        finally
        {
            readerExceptionLock.unlock();
        }
    }

    /**
     * Kills the underlying process.  After this call returns, the handler will not be called again.
     * All effort is made to clean up resources associated with the process, however it is possible
     * that the process and/or internal management threads remain (orphaned) if the process does not
     * die.
     * <p/>
     * If the process is killed this will release threads waiting on a wait* call of this.  It is
     * likely a non-zero exit code will be seen, but not guaranteed.
     */
    public void destroy()
    {
        // Guarantee that we never use a handler again, and flag our readers to stop.
        destroyed.set(true);

        stdoutThread.interrupt();
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

    /**
     * Interface for adaptors between the {@link InputStream} instances and the various Handlers.
     */
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
            buffer = new byte[BUFFER_SIZE];
        }

        public boolean stream(AtomicBoolean destroyed) throws Exception
        {
            int n = in.read(buffer);
            if (n < 0)
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

        public boolean stream(AtomicBoolean destroyed) throws Exception
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

        public boolean stream(AtomicBoolean destroyed) throws Exception
        {
            String line = reader.readLine();
            if (line == null)
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

    /**
     * Adds a level of indirection around the type of streamer used so we can share more logic.
     */
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

    /**
     * Used to wait for the process to exit.  This is done in a separate thread in cse the wait
     * never returns.
     */
    private class Waiter implements Runnable
    {
        public void run()
        {
            try
            {
                exitCode = process.waitFor();
                if (stderrThread != null)
                {
                    stderrThread.join(READER_JOIN_TIMEOUT);
                }

                stdoutThread.join(READER_JOIN_TIMEOUT);
            }
            catch (InterruptedException e)
            {
                // Empty
            }

            completedFlag.release();
        }
    }

    /**
     * A helper class that reads from a Streamer until it is done or the process is destroyed.
     */
    private class Reader implements Runnable
    {
        private Streamer streamer;

        public Reader(Streamer streamer)
        {
            this.streamer = streamer;
        }

        public void run()
        {
            try
            {
                while (!destroyed.get() && streamer.stream(destroyed))
                {
                    // Empty
                }
            }
            catch (Exception e)
            {
                readerExceptionLock.lock();
                try
                {
                    if (readerException == null)
                    {
                        readerException = e;
                    }
                }
                finally
                {
                    readerExceptionLock.unlock();
                }
            }
            finally
            {
                streamer.close();
            }
        }
    }
}
