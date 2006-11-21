package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class EditBuildSpecificationAction extends BuildSpecificationActionSupport implements Preparable
{
    private long id;
    private Project project;
    private BuildSpecification spec;
    private BuildSpecificationDao buildSpecificationDao;
    private boolean isolateChangelists;
    private boolean retainWorkingCopy;
    private boolean timeoutEnabled;
    private boolean prompt;
    private String checkoutSchemeName;
    private int timeout = 60;
    private static final List<String> PREPARE_PARAMS = Arrays.asList("id", "projectId");
    private Map<String, String> checkoutSchemes;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public BuildSpecification getSpec()
    {
        return spec;
    }

    public boolean isIsolateChangelists()
    {
        return isolateChangelists;
    }

    public void setIsolateChangelists(boolean isolateChangelists)
    {
        this.isolateChangelists = isolateChangelists;
    }

    public boolean isRetainWorkingCopy()
    {
        return retainWorkingCopy;
    }

    public void setRetainWorkingCopy(boolean retainWorkingCopy)
    {
        this.retainWorkingCopy = retainWorkingCopy;
    }

    public String getCheckoutSchemeName()
    {
        return checkoutSchemeName;
    }

    public void setCheckoutSchemeName(String checkoutSchemeName)
    {
        this.checkoutSchemeName = checkoutSchemeName;
    }

    public boolean isTimeoutEnabled()
    {
        return timeoutEnabled;
    }

    public void setTimeoutEnabled(boolean timeoutEnabled)
    {
        this.timeoutEnabled = timeoutEnabled;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public boolean isPrompt()
    {
        return prompt;
    }

    public void setPrompt(boolean prompt)
    {
        this.prompt = prompt;
    }

    public boolean checkSpec()
    {
        if (spec == null)
        {
            addActionError("Unknown build specification [" + getId() + "]");
            return true;
        }

        return false;
    }

    public Map<String, String> getCheckoutSchemes()
    {
        if(checkoutSchemes == null)
        {
            checkoutSchemes = new TreeMap<String, String>();
            if(project != null && !project.getScm().supportsUpdate())
            {
                checkoutSchemes.put(BuildSpecification.CheckoutScheme.CLEAN_CHECKOUT.toString(), getName(BuildSpecification.CheckoutScheme.CLEAN_CHECKOUT));
            }
            else
            {
                for(BuildSpecification.CheckoutScheme scheme: BuildSpecification.CheckoutScheme.values())
                {
                    checkoutSchemes.put(scheme.toString(), getName(scheme));
                }
            }
        }

        return checkoutSchemes;
    }

    private String getName(BuildSpecification.CheckoutScheme scheme)
    {
        return getText("buildspec.checkout.scheme." + scheme.toString());
    }

    public List<String> getPrepareParameterNames()
    {
        return PREPARE_PARAMS;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        spec = buildSpecificationDao.findById(id);
    }

    public void validate()
    {
        if (checkSpec())
        {
            return;
        }

        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        if (TextUtils.stringSet(spec.getName()))
        {
            BuildSpecification specOfName = project.getBuildSpecification(spec.getName());
            if (specOfName != null && specOfName.getId() != id)
            {
                addFieldError("spec.name", "A build specification with name '" + spec.getName() + "' already exists in this project.");
            }
        }

        try
        {
            BuildSpecification.CheckoutScheme scheme = BuildSpecification.CheckoutScheme.valueOf(checkoutSchemeName);
            if(scheme != BuildSpecification.CheckoutScheme.CLEAN_CHECKOUT && !project.getScm().supportsUpdate())
            {
                addFieldError("checkoutSchemeName", "Invalid checkout scheme for this project (the SCM does not support updating)");
            }
        }
        catch(IllegalArgumentException e)
        {
            addFieldError("checkoutSchemeName", "Invalid checkout scheme");
        }

        if (timeoutEnabled)
        {
            if (timeout <= 0)
            {
                addFieldError("timeout", "Timeout must be a positive value");
            }
        }
    }

    public String doInput()
    {
        if (checkSpec())
        {
            return ERROR;
        }

        isolateChangelists = spec.getIsolateChangelists();
        retainWorkingCopy = spec.getRetainWorkingCopy();
        checkoutSchemeName = spec.getCheckoutScheme().toString();

        timeoutEnabled = spec.getTimeout() != BuildSpecification.TIMEOUT_NEVER;
        if (timeoutEnabled)
        {
            timeout = spec.getTimeout();
        }
        else
        {
            timeout = 60;
        }

        prompt = spec.getPrompt();

        return INPUT;
    }

    public String execute()
    {
        projectManager.checkWrite(project);
        
        spec.setIsolateChangelists(isolateChangelists);
        spec.setRetainWorkingCopy(retainWorkingCopy);
        BuildSpecification.CheckoutScheme newScheme = BuildSpecification.CheckoutScheme.valueOf(checkoutSchemeName);
        if(newScheme != spec.getCheckoutScheme())
        {
            spec.setForceClean(true);
            spec.setCheckoutScheme(newScheme);
        }

        if (timeoutEnabled)
        {
            spec.setTimeout(timeout);
        }
        else
        {
            spec.setTimeout(BuildSpecification.TIMEOUT_NEVER);
        }

        spec.setPrompt(prompt);
        buildSpecificationDao.save(spec);
        return SUCCESS;
    }

    public void setBuildSpecificationDao(BuildSpecificationDao buildSpecificationDao)
    {
        this.buildSpecificationDao = buildSpecificationDao;
    }
}
