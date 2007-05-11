package com.zutubi.prototype.webwork;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.util.logging.Logger;

import java.io.StringWriter;

/**
 * A data object holding a new panel to show in the display pane of a
 * configuration page.
 */
public class ConfigurationPanel
{
    private static final Logger LOG = Logger.getLogger(ConfigurationPanel.class);

    private String template;
    private boolean success = true;
    
    public ConfigurationPanel(String template)
    {
        this.template = template;
    }

    public boolean getSuccess()
    {
        return success;
    }

    public String getNewPanel()
    {
        StringWriter writer = new StringWriter();
        ActionContext actionContext = ActionContext.getContext();

        VelocityManager velocityManager = VelocityManager.getInstance();
        try
        {
            velocityManager.getVelocityEngine().mergeTemplate(template, velocityManager.createContext(actionContext.getValueStack(), ServletActionContext.getRequest(), ServletActionContext.getResponse()), writer);
            return writer.toString();
        }
        catch (Exception e)
        {
            LOG.severe(e);
            success = false;
            return null;
        }
    }
}
