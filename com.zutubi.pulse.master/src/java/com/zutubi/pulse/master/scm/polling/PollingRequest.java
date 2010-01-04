package com.zutubi.pulse.master.scm.polling;

import com.zutubi.util.Predicate;
import com.zutubi.pulse.master.model.Project;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * A request to poll a particular project.  This request has a list of
 * associated predicates which must all be satisfied before this requests
 * project can be polled.
 */
public class PollingRequest
{
    private Project project;
    
    protected List<Predicate<PollingRequest>> predicates;

    public PollingRequest(Project project, Predicate<PollingRequest>... predicates)
    {
        this(project, Arrays.asList(predicates));
    }

    public PollingRequest(Project project, List<Predicate<PollingRequest>> predicates)
    {
        this.project = project;
        this.predicates = new LinkedList<Predicate<PollingRequest>>(predicates);
    }

    /**
     * Add the provided predicate to the list of predicates for this request.
     *
     * @param predicate a predicate
     */
    public void add(Predicate<PollingRequest> predicate)
    {
        this.predicates.add(predicate);
    }

    public Project getProject()
    {
        return project;
    }

    /**
     * Returns true if and only if all of this requests predicates are
     * satisfied.
     *
     * @return true if the predicates are satisfied, false otherwise.
     */
    public boolean satisfied()
    {
        for (Predicate<PollingRequest> predicate : predicates)
        {
            if (!predicate.satisfied(this))
            {
                return false;
            }
        }
        return true;
    }
}
