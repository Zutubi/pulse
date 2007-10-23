package com.zutubi.pulse.search;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.Project;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import java.util.Collection;

/**
 * The Build result restrictions provides a set of pre-defined expressions that can be applied to Search Queries
 * that target build resutls.
 */
public class BuildResultExpressions
{
    public static Criterion projectEq(Project project)
    {
        return Expression.eq("project", project);
    }

    public static Criterion projectEq(long projectId)
    {
        return Expression.eq("project.id", projectId);
    }

    public static Criterion projectIn(Collection<Project> projects)
    {
        return Expression.in("project", projects);
    }

    public static Criterion buildSpecEq(String buildSpec)
    {
        return Expression.eq("buildSpecification", buildSpec);
    }

    public static Criterion statesIn(ResultState... states)
    {
        // convert to string.
        String[] stateNames = new String[states.length];
        for (int i = 0; i < states.length; i++)
        {
            stateNames[i] = states[i].toString();
        }
        return Expression.in("stateName", stateNames);
    }

    public static Criterion buildResultCompleted()
    {
        return statesIn(ResultState.SUCCESS, ResultState.FAILURE, ResultState.ERROR);
    }

    public static Criterion startsAfter(long timestamp)
    {
        return Expression.ge("stamps.startTime", timestamp);
    }

    public static Criterion startsBefore(long timestamp)
    {
        return Expression.le("stamps.startTime", timestamp);
    }

    public static Criterion hasWorkDirectory(boolean b)
    {
        return Expression.eq("hasWorkDir", b);
    }

    public static Order orderByDescEndDate()
    {
        return Order.desc("stamps.endTime");
    }

    public static Order orderByDescId()
    {
        return Order.desc("id");
    }
}
