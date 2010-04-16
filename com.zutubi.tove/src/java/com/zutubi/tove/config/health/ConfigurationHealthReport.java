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

    ConfigurationHealthReport(HealthProblem... problems)
    {
        this.problems.addAll(Arrays.asList(problems));
    }

    /**
     * Indicates if any problems were found.
     * 
     * @return true if no problems were found
     */
    public boolean isHealthy()
    {
        return problems.isEmpty();
    }

    /**
     * Indicates how many problems were found.
     * 
     * @return the number of problems found
     */
    public int getProblemCount()
    {
        return problems.size();
    }

    /**
     * Indicates if all found problems can be solved.
     * 
     * @return true if all problems in this report can be solved, false if
     *         there is at least one unsolvable problem
     */
    public boolean isSolvable()
    {
        return !CollectionUtils.contains(problems, new Predicate<HealthProblem>()
        {
            public boolean satisfied(HealthProblem healthProblem)
            {
                return !healthProblem.isSolvable();
            }
        });
    }
    
    /**
     * Returns all problems that were found.
     * 
     * @return an iterator over the foudn problems
     */
    public Iterable<HealthProblem> getProblems()
    {
        return Collections.unmodifiableSet(problems);
    }

    /**
     * Returns all problems found at (and optionally under) the given path.
     * 
     * @param path          the path to find problems under
     * @param includeNested if true, all problems at or nested under the path
     *                      are returned; otherwise only problems directly at
     *                      the path are returned
     * @return the problems at or under the given path
     */
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

    /**
     * Adds the given problem to this report.
     * 
     * @param problem the problem to add
     */
    void addProblem(HealthProblem problem)
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
