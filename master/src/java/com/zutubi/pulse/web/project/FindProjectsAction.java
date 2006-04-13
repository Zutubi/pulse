/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import java.util.Collections;
import java.util.List;

/**
 *
 *
 */
public class FindProjectsAction extends ProjectActionSupport
{
    private String name;
    private List results = Collections.EMPTY_LIST;

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List getResults()
    {
        return results;
    }

    public String execute()
    {
        if (name == null)
            name = "";

        // for now, make use of the databases wildcard matching
        results = getProjectManager().getProjectsWithNameLike(name.replace('*', '%'));

        return SUCCESS;
    }
}
