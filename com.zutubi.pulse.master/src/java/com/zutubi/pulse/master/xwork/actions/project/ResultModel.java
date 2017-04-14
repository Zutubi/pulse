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
