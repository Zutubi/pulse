package com.zutubi.tove.config.health;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A report on the health (internal consistency) of a tove configuration store.
 *
 * @see ConfigurationHealthChecker
 */
public class ConfigurationHealthReport
{
    /**
     * Set of all detected problems.  Maintains the order in which problems
     * are reported.
     */
    private Set<HealthProblem> problems = new LinkedHashSet<HealthProblem>();

    public ConfigurationHealthReport()
    {
    }

    public ConfigurationHealthReport(HealthProblem... problems)
    {
        this.problems.addAll(Arrays.asList(problems));
    }
    
    public boolean isHealthy()
    {
        return problems.isEmpty();
    }

    public int getProblemCount()
    {
        return problems.size();
    }
    
    public Iterable<HealthProblem> getProblems()
    {
        return Collections.unmodifiableSet(problems);
    }

    public Iterable<HealthProblem> getProblems(final String path, final boolean includeNested)
    {
        return CollectionUtils.filter(problems, new Predicate<HealthProblem>()
        {
            public boolean satisfied(HealthProblem problem)
            {
                return problem.getPath().startsWith(path) && (includeNested || problem.getPath().length() == path.length());
            }
        });
    }

    public void addProblem(HealthProblem problem)
    {
        problems.add(problem);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ConfigurationHealthReport report = (ConfigurationHealthReport) o;

        if (problems != null ? !problems.equals(report.problems) : report.problems != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return problems != null ? problems.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        int problemCount = getProblemCount();
        if (problemCount > 0)
        {
            StringBuilder result = new StringBuilder();
            result.append(String.format("%d configuration problem%s found:", problemCount, problemCount == 1 ? "" : "s"));
            for (HealthProblem problem: problems)
            {
                result.append("\n  - ");
                result.append(problem);
            }

            return result.toString();
        }
        else
        {
            return "No configuration problems found.";
        }
    }
}
