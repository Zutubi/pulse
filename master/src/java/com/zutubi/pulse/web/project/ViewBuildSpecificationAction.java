/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;

/**
 */
public class ViewBuildSpecificationAction extends ProjectActionSupport
{
    private BuildSpecification specification;
    private long id;
    private BuildSpecificationDao buildSpecificationDao;

    public BuildSpecification getSpecification()
    {
        return specification;
    }

    public void setSpecification(BuildSpecification specification)
    {
        this.specification = specification;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setBuildSpecificationDao(BuildSpecificationDao buildSpecificationDao)
    {
        this.buildSpecificationDao = buildSpecificationDao;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        specification = buildSpecificationDao.findById(id);
        if (specification == null)
        {
            addActionError("Unknown build specification '" + Long.toString(id) + "'");
        }
    }

    public String execute()
    {
        return SUCCESS;
    }
}
