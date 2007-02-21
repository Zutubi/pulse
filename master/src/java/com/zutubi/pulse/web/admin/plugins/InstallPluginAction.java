package com.zutubi.pulse.web.admin.plugins;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.zutubi.pulse.plugins.PluginException;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.util.Collection;
import java.net.MalformedURLException;

/**
 * An action to install a plugin from an uploaded file.
 */
public class InstallPluginAction extends PluginActionSupport
{
    private static final Logger LOG = Logger.getLogger(InstallPluginAction.class);

    public void validate()
    {
        MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();

        if (multiWrapper.hasErrors())
        {
            Collection errors = multiWrapper.getErrors();
            for (Object error : errors)
            {
                addActionError((String) error);
            }
        }
    }

    public String execute() throws Exception
    {
        MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();

        String inputField = "plugin";

        // Get a File object for the uploaded File
        File[] files = multiWrapper.getFiles(inputField);

        // If it's null the upload failed
        if (files == null || files.length == 0)
        {
            String[] names = multiWrapper.getFileSystemNames(inputField);
            if(names == null)
            {
                addFieldError("plugin", "File not found");
            }
            else
            {
                addFieldError("plugin", "Error uploading: " + names[0]);
            }
            return ERROR;
        }

        String[] fileNames = multiWrapper.getFileNames(inputField);

        try
        {
            pluginManager.installPlugin(fileNames[0], files[0].toURL());
        }
        catch (Exception e)
        {
            LOG.warning(e);
            addActionError(e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }
}
