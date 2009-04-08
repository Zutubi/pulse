package com.zutubi.pulse.master.xwork.actions;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 */
public class PersonalBuildAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(PersonalBuildAction.class);

    private String project;
    private String revision;
    private long number;
    private String errorMessage;
    private MasterConfigurationManager configurationManager;

    public void setProject(String project)
    {
        this.project = project;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
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

        if(!accessManager.hasPermission(userManager.getPrinciple(user), ServerPermission.PERSONAL_BUILD.toString(), null))
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

        File uploadedPatch = files[0];
        if(!uploadedPatch.exists())
        {
            errorMessage = "Uploaded patch file '" + uploadedPatch.getAbsolutePath() + "' does not exist";
            return ERROR;
        }

        if(!uploadedPatch.isFile())
        {
            errorMessage = "Uploaded patch file '" + uploadedPatch.getAbsolutePath() + "' is not a regular file";
            return ERROR;
        }

        Project p = projectManager.getProject(project, false);
        if(p == null)
        {
            errorMessage = "Unknown project '" + project + "'";
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
        if(patchFile.exists())
        {
            errorMessage = "Patch file '" + patchFile.getAbsolutePath() + "' already exists.  Retry the build.";
        }

        PatchArchive archive;
        try
        {
            IOUtils.copyFile(uploadedPatch, patchFile);
            uploadedPatch.delete();
            
            archive = new PatchArchive(patchFile);
            projectManager.triggerBuild(number, p, user, new Revision(revision), archive);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            errorMessage = e.getClass().getName() + ": " + e.getMessage();
            return ERROR;
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
