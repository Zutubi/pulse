package com.zutubi.pulse.master.web.project;

import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryProcedure;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class ProjectsModel
{
    private String groupName;
    private boolean labelled;

    private TemplateProjectModel root = new TemplateProjectModel(this, null);

    public ProjectsModel(String name, boolean labelled)
    {
        this.groupName = name;
        this.labelled = labelled;
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
        return StringUtils.toValidHtmlName("group." + groupName);
    }
    
    public TemplateProjectModel getRoot()
    {
        return root;
    }

    public int getMaxDepth()
    {
        final int[] maxDepth = new int[]{0};
        root.forEach(new UnaryProcedure<ProjectModel>()
        {
            public void process(ProjectModel projectModel)
            {
                if (projectModel.isLeaf())
                {
                    int depth = projectModel.getDepth();
                    if (depth > maxDepth[0])
                    {
                        maxDepth[0] = depth;
                    }
                }
            }
        });

        return maxDepth[0];
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
