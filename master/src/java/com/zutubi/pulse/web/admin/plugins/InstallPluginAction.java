package com.zutubi.pulse.web.admin.plugins;

import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Collection;

/**
 * An action to install a plugin from an uploaded file.
 */
public class InstallPluginAction extends PluginActionSupport
{
    private static final Logger LOG = Logger.getLogger(InstallPluginAction.class);

    private String pluginPath;

    public String getPluginPath()
    {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath)
    {
        this.pluginPath = pluginPath;
    }

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

    public String doInput() throws Exception
    {
        return INPUT;
    }

    public String doUpload() throws Exception
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

    public String doLocal() throws Exception
    {
        if(!TextUtils.stringSet(pluginPath))
        {
            addFieldError("pluginPath", "path is required");
            return INPUT;
        }

        File pluginFile = new File(pluginPath);
        if(!pluginFile.exists())
        {
            addFieldError("pluginPath", "file does not exist");
            return INPUT;
        }

        if(!pluginFile.isFile())
        {
            addFieldError("pluginPath", "not a regular file");
            return INPUT;
        }

        try
        {
            pluginManager.installPlugin(pluginFile.toURL());
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
