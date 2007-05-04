package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.AnyCapableBuildHostRequirements;
import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.BuildSpecificationNode;
import com.zutubi.pulse.model.MasterBuildHostRequirements;
import com.zutubi.pulse.model.SlaveBuildHostRequirements;
import com.zutubi.pulse.xwork.interceptor.Cancelable;

/**
 */
public class BuildStageActionSupport extends BuildSpecificationActionSupport implements Cancelable
{
    private long specId;
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

    }
}
