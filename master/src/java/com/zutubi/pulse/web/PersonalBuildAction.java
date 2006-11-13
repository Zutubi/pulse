package com.zutubi.pulse.web;

import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;

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
    private long number;
    private String errorMessage;
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

    public long getNumber()
    {
        return number;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public String execute()
    {
        ActionContext ac = ActionContext.getContext();
        HttpServletRequest request = (HttpServletRequest) ac.get(ServletActionContext.HTTP_REQUEST);

        User user = null;
        Object principle = getPrinciple();
        if(principle != null)
        {
            user = userManager.getUser((String) principle);
        }

        if(user == null)
        {
            errorMessage = "Unable to determine user";
            return ERROR;
        }

        if(!userManager.hasAuthority(user, GrantedAuthority.PERSONAL))
        {
            throw new AccessDeniedException("User does not have authority to submit personal build requests.");
        }

        if (!(request instanceof MultiPartRequestWrapper))
        {
            errorMessage = "Invalid request: expecting multipart POST";
            return ERROR;
        }

        MultiPartRequestWrapper mpr = (MultiPartRequestWrapper) request;
        File[] files = mpr.getFiles("patch.zip");
        if(files == null || files.length == 0 || files[0] == null)
        {
            errorMessage = "POST does not contain required file parameter 'patch.zip'";
            return ERROR;
        }

        Project p = projectManager.getProject(project);
        if(p == null)
        {
            errorMessage = "Unknown project '" + project + "'";
            return ERROR;
        }

        BuildSpecification spec;
        if(TextUtils.stringSet(specification))
        {
            spec = p.getBuildSpecification(specification);
        }
        else
        {
            spec = p.getDefaultSpecification();
        }
        
        if(spec == null)
        {
            errorMessage = "Unknown build specification '" + specification + "'";
            return ERROR;
        }

        number = userManager.getNextBuildNumber(user);
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File patchDir = paths.getUserPatchDir(user.getId());
        if(!patchDir.isDirectory())
        {
            patchDir.mkdirs();
        }
        File patchFile = paths.getUserPatchFile(user.getId(), number);
        files[0].renameTo(patchFile);

        PatchArchive archive = null;
        try
        {
            archive = new PatchArchive(patchFile);
            projectManager.triggerBuild(number, p, spec, user, archive);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            errorMessage = e.getClass().getName() + ": " + e.getMessage();
            return ERROR;
        }

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
