package com.zutubi.pulse.master.util.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JobManager
{
    private Map<String, Job<Task>> jobs = new HashMap<String, Job<Task>>();
    private Map<String, JobRunner> jobRunners = new HashMap<String, JobRunner>();
    private List<String> activeJobs = new LinkedList<String>();

    public synchronized void register(String key, Job<Task> job)
    {
        if (jobs.containsKey(key))
        {
            throw new IllegalArgumentException("Job with key '" + key + "' already registered.");
        }
        jobs.put(key, job);
        jobRunners.put(key, new JobRunner());
    }

    public synchronized void register(String key, Task... tasks)
    {
        register(key, new ArrayJobWrapper<Task>(tasks));
    }

    public synchronized void register(String key, List<Task> tasks)
    {
        register(key, new ListJobWrapper<Task>(tasks));
    }

    public synchronized Job<Task> getJob(String key)
    {
        return jobs.get(key);
    }

    public synchronized List<String> getJobKeys()
    {
        return new LinkedList<String>(jobs.keySet());
    }

    public synchronized Monitor getMonitor(String key)
    {
        Job job = getJob(key);
        if (job == null)
        {
            return null;
        }

        return jobRunners.get(key).getMonitor();
    }

    public void start(String key)
    {
        Job job;
        JobRunner jobRunner;

        synchronized (this)
        {
            job = getJob(key);
            if (job == null)
            {
                throw new IllegalArgumentException("Unknown job '" + key + "'.");
            }

            if (activeJobs.contains(key))
            {
                throw new IllegalArgumentException("Job '" + key + "' has already been started.");
            }

            jobRunner = jobRunners.get(key);
            activeJobs.add(key);
        }

        // this can take a very long time - so DO NOT run it in the synchronised block.
        jobRunner.run(job);
    }

    public synchronized Job unregister(String key)
    {
        if (activeJobs.contains(key))
        {
            Monitor monitor = getMonitor(key);
            if (monitor != null)
            {
                if (!monitor.isFinished())
                {
                    throw new IllegalStateException("Can not unregister a running Job.");
                }
            }
        }

        activeJobs.remove(key);
        jobRunners.remove(key);
        return jobs.remove(key);
    }
}
