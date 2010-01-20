package com.zutubi.pulse.master.util.monitor;

import com.zutubi.i18n.Messages;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The job manager is responsible for the execution and tracking of jobs.
 *
 * You can run a job via the following workflow.
 * <ul>
 * <li>{@link #register(String, Task[])}.  Firstly, register the job with a unique key.  This key
 * can be used in future to refer to this specific job.</li>
 * <li>{@link #run(String)}. Once registered, you can run the job.  You can monitor its progress
 * using a {@link #getMonitor(String)}.</li>
 * <li>Once the job is complete, you can clean up any remaining references to it by unregistering the
 * job via {@link #unregister(String)}</li>
 * </ul>
 */
public class JobManager
{
    private static final Messages I18N = Messages.getInstance(JobManager.class);

    private Map<String, Job<Task>> jobs = new HashMap<String, Job<Task>>();
    private Map<String, JobRunner<Task>> jobRunners = new HashMap<String, JobRunner<Task>>();
    private List<String> activeJobs = new LinkedList<String>();

    /**
     * Register a new job.
     *
     * @param key   the key used to uniquely identify the job.
     * @param job   the job being registered.
     *
     * @throws IllegalArgumentException if the specified key is already in use.
     */
    public synchronized void register(String key, Job<Task> job)
    {
        if (jobs.containsKey(key))
        {
            throw new IllegalArgumentException(I18N.format("key.invalid.alreadyRegistered", key));
        }
        jobs.put(key, job);
        jobRunners.put(key, new JobRunner<Task>());
    }

    /**
     * Register a list of tasks that make up a single job.
     *
     * @param key   the key used to uniquely identify the job.
     * @param tasks the tasks that define a job.
     *
     * @throws IllegalArgumentException if the specified key is already in use.
     */
    public synchronized void register(String key, Task... tasks)
    {
        register(key, new ArrayJobWrapper<Task>(tasks));
    }

    /**
     * Register a list of tasks that make up a single job.
     *
     * @param key   the key used to uniquely identify the job.
     * @param tasks the tasks that define a job.
     *
     * @throws IllegalArgumentException if the specified key is already in use.
     */
    public synchronized void register(String key, List<Task> tasks)
    {
        register(key, new ListJobWrapper<Task>(tasks));
    }

    /**
     * Retrieve the job identified by the unique key.
     *
     * @param key   the key that uniquely identifies the job.
     * @return  the requested job.
     */
    public synchronized Job<Task> getJob(String key)
    {
        return jobs.get(key);
    }

    /**
     * Get the list of currently registered job keys.
     *
     * @return the list of all job keys currently registered with this manager.
     */
    public synchronized List<String> getJobKeys()
    {
        return new LinkedList<String>(jobs.keySet());
    }

    /**
     * Get the monitor for the job identified by the key.
     *
     * @param key   the unique identifier for the job
     * @return the monitor for the specified job.
     */
    public synchronized Monitor<Task> getMonitor(String key)
    {
        Job job = getJob(key);
        if (job == null)
        {
            return null;
        }

        return jobRunners.get(key).getMonitor();
    }

    /**
     * Run the job identified by the specified key.  This method returns when the job is completed.
     *
     * @param key   the key identifying the job to be started.
     *
     * @throws IllegalArgumentException if either the unique key does not refer
     * to a registered job or if the job has already been started.
     */
    public void run(String key)
    {
        Job<Task> job;
        JobRunner<Task> jobRunner;

        synchronized (this)
        {
            job = getJob(key);
            if (job == null)
            {
                throw new IllegalArgumentException(I18N.format("job.unknown"));
            }

            if (activeJobs.contains(key))
            {
                throw new IllegalArgumentException(I18N.format("already.started"));
            }

            jobRunner = jobRunners.get(key);
            activeJobs.add(key);
        }

        // this can take a very long time - so DO NOT run it in the synchronised block.
        jobRunner.run(job);
    }

    /**
     * Unregister the key.  The job can not be running when this is done.
     * Unregistering cleans up any references to the job and its key, allowing
     * the key to be used again at some point in the future.
     *
     * @param key   the key of the job to unregister.
     * @return the unregistered job.
     *
     * @throws IllegalStateException if the job was running at the time.
     */
    public synchronized Job unregister(String key)
    {
        if (activeJobs.contains(key))
        {
            Monitor monitor = getMonitor(key);
            if (monitor != null)
            {
                if (!monitor.isFinished())
                {
                    throw new IllegalStateException(I18N.format("unregister.fail.jobStillRunning"));
                }
            }
        }

        activeJobs.remove(key);
        jobRunners.remove(key);
        return jobs.remove(key);
    }
}
