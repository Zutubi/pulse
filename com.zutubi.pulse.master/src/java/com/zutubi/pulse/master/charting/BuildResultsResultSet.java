package com.zutubi.pulse.master.charting;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;

import java.util.Iterator;
import java.util.List;

/**
 * <class comment/>
 */
public class BuildResultsResultSet implements ResultSet
{
    private Iterator<BuildResult> results;
    private List<BuildResult> source;
    private Object current;

    public BuildResultsResultSet(List<BuildResult> source)
    {
        this.source = source;
        this.results = this.source.iterator();
    }

    public boolean hasNext()
    {
        return results.hasNext();
    }

    public boolean next()
    {
        if (results.hasNext())
        {
            current = results.next();
            return true;
        }
        return false;
    }

    public long getEndTime()
    {
        return ((BuildResult) current).getStamps().getEndTime();
    }

    public ResultState getState()
    {
        return ((BuildResult) current).getState();
    }

    public long getId()
    {
        return ((BuildResult) current).getId();
    }

    public long getNumber()
    {
        return ((BuildResult) current).getNumber();
    }

    public long getElapsed()
    {
        return ((BuildResult) current).getStamps().getElapsed();
    }

    public double getAverageStageTime()
    {
        int count = 0;
        long total = 0;

        for(RecipeResultNode node: ((BuildResult)current))
        {
            RecipeResult recipeResult = node.getResult();
            if(recipeResult != null)
            {
                count++;
                total += recipeResult.getStamps().getElapsed();
            }
        }

        if(total > 0)
        {
            return ((double)total) / count;
        }
        else
        {
            return 0;
        }
    }

    public long getTotalTests()
    {
        return ((BuildResult)current).getTestSummary().getTotal();
    }
    
    public Object getFieldValue(String fieldName)
    {
        BuildResult currentResult = (BuildResult) current;
        if (fieldName.equals("date"))
        {
            return currentResult.getStamps().getEndTime();
        }
        else if (fieldName.equals("state"))
        {
            return currentResult.getState().toString();
        }
        else if (fieldName.equals("id"))
        {
            return currentResult.getId();
        }
        else if (fieldName.equals("elapsed"))
        {
            return currentResult.getStamps().getElapsed();
        }
        return null;
    }

    public List<BuildResult> getSource()
    {
        return source;
    }
}
