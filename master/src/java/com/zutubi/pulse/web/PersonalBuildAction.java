package com.zutubi.pulse.web;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 */
public class PersonalBuildAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(PersonalBuildAction.class);

    private String project;
    private String specification;
    private String version;
    private EventManager eventManager;
    private ProjectManager projectManager;
    private UserManager userManager;
    private MasterConfigurationManager configurationManager;

    public void setProject(String project)
    {
        this.project = project;
    }

    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String execute()
    {
        ActionContext ac = ActionContext.getContext();
        HttpServletRequest request = (HttpServletRequest) ac.get(ServletActionContext.HTTP_REQUEST);

        if (!(request instanceof MultiPartRequestWrapper))
        {
            return ERROR;
        }

        MultiPartRequestWrapper mpr = (MultiPartRequestWrapper) request;
        File[] files = mpr.getFiles("patch.zip");
        if(files == null || files.length == 0 || files[0] == null)
        {
            // No patch??
            return ERROR;
        }

        Project p = projectManager.getProject(project);
        if(p == null)
        {
            return ERROR;
        }

        BuildSpecification spec = p.getBuildSpecification(specification);
        if(spec == null)
        {
            return ERROR;
        }

        User user = null;
        Object principle = getPrinciple();
        if(principle != null)
        {
            user = userManager.getUser((String) principle);
        }

        if(user == null)
        {
            return ERROR;
        }

        long number = userManager.getNextBuildNumber(user);
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File patchDir = paths.getUserPatchDir(user);
        if(!patchDir.isDirectory())
        {
            patchDir.mkdirs();
        }
        File patchFile = paths.getUserPatchFile(user, number);
        files[0].renameTo(patchFile);

        PatchArchive archive = null;
        try
        {
            archive = new PatchArchive(patchFile);
        }
        catch (PulseException e)
        {
            LOG.severe(e);
            return ERROR;
        }

        projectManager.triggerBuild(number, p, spec, user, archive);

        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
