/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.xwork.interceptor.Cancelable;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 */
public class BuildStageActionSupport extends BuildSpecificationActionSupport implements Cancelable
{
    private long specId;
    private BuildSpecification specification;
    private long id;
    BuildSpecificationNode node;

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }

    public BuildSpecification getSpecification()
    {
        return specification;
    }

    protected void lookupSpec()
    {
        specification = getProject().getBuildSpecification(specId);
        if(specification == null)
        {
            addActionError("Unknown build specification [" + specId + "]");
        }
    }

    protected void getFieldsFromStage()
    {
        name = stage.getName();

        BuildHostRequirements requirements = stage.getHostRequirements();
        if(requirements == null || requirements instanceof  AnyCapableBuildHostRequirements)
        {
            buildHost = 0L;
        }
        else if(requirements instanceof MasterBuildHostRequirements)
        {
            buildHost = 1L;
        }
        else
        {
            SlaveBuildHostRequirements slaveReqs = (SlaveBuildHostRequirements) requirements;
            buildHost = slaveReqs.getSlave().getId();
        }
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    protected void lookupNode()
    {
        node = getSpecification().getNode(id);
        if(node == null)
        {
            addActionError("Unknown stage [" + id + "]");
        }
    }
}
