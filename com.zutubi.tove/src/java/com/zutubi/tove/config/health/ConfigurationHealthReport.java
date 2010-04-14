package com.zutubi.tove.config.health;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A report on the health (internal consistency) of a tove configuration store.
 *
 * @see ConfigurationHealthChecker
 */
public class ConfigurationHealthReport
{
    /**
     * List of all detected problems.  Maintains the order in which problems
     * are reported.
     */
    private List<ConfigurationHealthProblem> problems = new LinkedList<ConfigurationHealthProblem>();

    public ConfigurationHealthReport()
    {
    }

    public ConfigurationHealthReport(ConfigurationHealthProblem... problems)
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
    
    public Iterable<ConfigurationHealthProblem> getProblems()
    {
        return Collections.unmodifiableList(problems);
    }

    public Iterable<ConfigurationHealthProblem> getProblems(final String path, final boolean includeNested)
    {
        return CollectionUtils.filter(problems, new Predicate<ConfigurationHealthProblem>()
        {
            public boolean satisfied(ConfigurationHealthProblem problem)
            {
                return problem.getPath().startsWith(path) && (includeNested || problem.getPath().length() == path.length());
            }
        });
    }

    public void addProblem(String path, String message)
    {
        problems.add(new ConfigurationHealthProblem(path, message));
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
            for (ConfigurationHealthProblem problem: problems)
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
