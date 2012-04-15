package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.Result;
import com.zutubi.util.time.TimeStamps;

/**
 * Base for JSON models for various results.
 */
public abstract class ResultModel
{
    private long id;
    private String status;
    private int errors = -1;
    private int warnings = -1;
    private DateModel when;
    private DateModel completed;
    private ElapsedModel elapsed;

    protected ResultModel(long id, String status)
    {
        this.id = id;
        this.status = status;
    }

    protected ResultModel(Result result)
    {
        id = result.getId();
        status = result.getState().getPrettyString();
        elapsed = new ElapsedModel(result.getStamps());
        errors = result.getErrorFeatureCount();
        warnings = result.getWarningFeatureCount();

        if (result.commenced())
        {
            when = new DateModel(result.getStartTime());
        }
            
        if (result.completed())
        {
            completed = new DateModel(result.getStamps().getEndTime());
        }
    }

    public long getId()
    {
        return id;
    }

    public String getStatus()
    {
        return status;
    }

    public int getErrors()
    {
        return errors;
    }

    public int getWarnings()
    {
        return warnings;
    }

    public DateModel getWhen()
    {
        return when;
    }

    public DateModel getCompleted()
    {
        return completed;
    }

    public ElapsedModel getElapsed()
    {
        return elapsed;
    }

    /**
     * Defines JSON data for an elapsed time.
     */
    public static class ElapsedModel
    {
        private String prettyElapsed;
        private String prettyEstimatedTimeRemaining;
        private int estimatedPercentComplete;
    
        public ElapsedModel(TimeStamps stamps)
        {
            prettyElapsed = TimeStamps.getPrettyElapsed(stamps.getElapsed(), 2);
            if (stamps.hasEstimatedTimeRemaining())
            {
                prettyEstimatedTimeRemaining = stamps.getPrettyEstimatedTimeRemaining();
                estimatedPercentComplete = stamps.getEstimatedPercentComplete();
            }
        }
    
        public String getPrettyElapsed()
        {
            return prettyElapsed;
        }
    
        public String getPrettyEstimatedTimeRemaining()
        {
            return prettyEstimatedTimeRemaining;
        }
    
        public int getEstimatedPercentComplete()
        {
            return estimatedPercentComplete;
        }
    }
}
