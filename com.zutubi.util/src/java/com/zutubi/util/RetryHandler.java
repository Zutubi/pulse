package com.zutubi.util;

import com.zutubi.util.logging.Logger;
import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;
import com.zutubi.util.time.TimeStamps;

import java.util.concurrent.TimeUnit;

/**
 * Handles retrying on exceptions, waiting between retries and with a limit on the total amount of time that can elapse
 * before giving up and reporting an error.  By default retries occur at regular intervals.  This can be tuned using
 * {@link #setBackoff(boolean)} and {@link #setExponentialBackoff(boolean)} if desired.
 *
 * <p/>
 * Basic usage in a work loop looks like:
 * <pre>{@code
 *     RetryHandler retryHandler = new RetryHandler(1, TimeUnit.SECONDS, 5, TimeUnit.MINUTES);
 *     while (!finished)
 *     {
 *         try
 *         {
 *             finished = doSomeWork();
 *
 *             // We got to the end safely, reset errors.
 *             retryHandler.reset();
 *         }
 *         catch (Throwable t)
 *         {
 *             // This will pause before returning in accordance with the handler's configuration.
 *             retryHandler.handle(t);
 *         }
 *     }
 * }</pre>
 */
public class RetryHandler
{
    private static final Logger LOG = Logger.getLogger(RetryHandler.class);
    
    private long waitIntervalMillis;
    private long exhaustedIntervalMillis;
    private boolean backoff = false;
    private boolean exponentialBackoff = false;

    private long firstError;
    private long nextWaitIntervalMillis;

    private Clock clock = new SystemClock();

    /**
     * Creates a new handler with the given wait and exhaustion intervals.
     *
     * @param waitInterval      time to wait between retries, by blocking in {@link #handle(Throwable)}
     * @param waitUnit          units of waitInterval
     * @param exhaustedInterval time from the first error, without an intervening reset, after which to give up
     * @param exhaustedUnit     units of exhaustedInterval
     */
    public RetryHandler(long waitInterval, TimeUnit waitUnit, long exhaustedInterval, TimeUnit exhaustedUnit)
    {
        waitIntervalMillis = waitUnit.toMillis(waitInterval);
        exhaustedIntervalMillis = exhaustedUnit.toMillis(exhaustedInterval);
        reset();
    }

    /**
     * Resets any error handling state.  This should be called on any successful execution of a unit of work.
     */
    public void reset()
    {
        firstError = 0;
        nextWaitIntervalMillis = waitIntervalMillis;
    }

    /**
     * Handles a throwable raised during a unit of work.  Details of the problem are logged, and if retries are not
     * exhausted this method blocks for an appropriate wait interval before returning control to the caller to try
     * again.  If retries are exhausted the throwable is wrapped and rethrown (the handler automatically resets).
     *
     * @param t the throwable to handle
     * @throws RetriesExhaustedException if the exhausted interval has elapsed since the first error (with no
     *         intervening reset)
     */
    public void handle(Throwable t)
    {
        LOG.severe("Caught unexpected throwable: " + t.getMessage(), t);
        long now = clock.getCurrentTimeMillis();
        if (firstError == 0)
        {
            // Always retry once, setting the first error time.
            firstError = now;
            LOG.severe("Retrying now.");
        }
        else
        {
            if (now - firstError > exhaustedIntervalMillis)
            {
                LOG.severe("Giving up by rethrowing.");
                reset();
                throw new RetriesExhaustedException(t);
            }

            try
            {
                LOG.severe("Retrying after " + TimeStamps.getPrettyElapsed(nextWaitIntervalMillis) + ".");
                Thread.sleep(nextWaitIntervalMillis);
            }
            catch (InterruptedException e)
            {
                LOG.warning("Interrupted while waiting to retry: " + e.getMessage(), e);
            }

            if (backoff)
            {
                if (exponentialBackoff)
                {
                    nextWaitIntervalMillis *= 2;
                }
                else
                {
                    nextWaitIntervalMillis += waitIntervalMillis;
                }
            }
        }
    }

    /**
     * If set to true, the wait interval increases each retry until reset or exhaustion.  By default the interval
     * increases linearly (by the addition of the original interval each time), see {@link #setExponentialBackoff(boolean)}.
     *
     * @param backoff true to use a backoff strategy, i.e. waiting longer between each successive retry, false to use
     *                the same wait interval each time
     */
    public void setBackoff(boolean backoff)
    {
        this.backoff = backoff;
    }

    /**
     * Controls if backoff is geometric: i.e. the wait interval is doubled each time.  This is only used when backoff is
     * enabled, see {@link #setBackoff(boolean)}.
     *
     * @param exponentialBackoff true to use an exponential backoff strategy, i.e. doubling the wait time between each
     *                           successive retry, false to use a linear backoff strategy
     */
    public void setExponentialBackoff(boolean exponentialBackoff)
    {
        this.exponentialBackoff = exponentialBackoff;
    }

    /**
     * Sets the clock to use to get the current time, useful for testing.  If not called the system clock is used
     *
     * @param clock the clock to use
     */
    public void setClock(Clock clock)
    {
        this.clock = clock;
    }
}
