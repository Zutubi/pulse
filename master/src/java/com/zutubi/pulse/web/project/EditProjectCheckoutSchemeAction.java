/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project.CheckoutScheme;
import com.zutubi.pulse.model.Project;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class EditProjectCheckoutSchemeAction extends ProjectActionSupport
{
    private String checkoutScheme;

    /**
     * Get the available options.
     *
     * @return a map of available checkout schemes.
     */
    public Map<String, String> getCheckoutSchemes()
    {
        Map<String, String> result = new TreeMap<String, String>();
        result.put(CheckoutScheme.CHECKOUT_ONLY.toString(), "checkout only");
        if (getProject().getScm().supportsUpdate())
        {
            result.put(CheckoutScheme.CHECKOUT_AND_UPDATE.toString(), "checkout and update");
        }
        return result;
    }

    public String getCheckoutScheme()
    {
        return checkoutScheme;
    }

    public void setCheckoutScheme(String checkoutScheme)
    {
        this.checkoutScheme = checkoutScheme;
    }

    public void validate()
    {
        if (getProject() == null)
        {
            addUnknownProjectFieldError();
        }
    }

    public String doInput() throws Exception
    {
        Project project = getProject();
        checkoutScheme = project.getCheckoutScheme().toString();
        return super.doInput();
    }

    public String execute()
    {
        Project project = getProject();
        project.setCheckoutScheme(CheckoutScheme.valueOf(checkoutScheme));
        getProjectManager().save(project);
        return SUCCESS;
    }
}
