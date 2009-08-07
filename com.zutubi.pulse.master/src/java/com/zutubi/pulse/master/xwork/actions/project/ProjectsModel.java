package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.WebUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * JSON-encodable representation of a group of projects for display on the
 * dashboard or browse view.
 */
public class ProjectsModel
{
    private String groupName;
    private boolean labelled;

    private TemplateProjectModel root;

    public ProjectsModel(String name, boolean labelled, boolean collapsed)
    {
        this.groupName = name;
        this.labelled = labelled;
        root = new TemplateProjectModel(this, null, collapsed);
    }

    public String getGroupName()
    {
        return groupName;
    }

    public boolean isLabelled()
    {
        return labelled;
    }

    public String getId()
    {
        return labelled ? WebUtils.toValidHtmlName("group." + groupName) : "ungroup";
    }
    
    public TemplateProjectModel getRoot()
    {
        return root;
    }

    public List<ProjectModel> getFlattened()
    {
        final List<ProjectModel> result = new LinkedList<ProjectModel>();
        root.forEach(new UnaryProcedure<ProjectModel>()
        {
            public void process(ProjectModel projectModel)
            {
                if(projectModel != root)
                {
                    result.add(projectModel);
                }
            }
        });

        return result;
    }
}
