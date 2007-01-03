package com.zutubi.pulse.web.admin.plugins;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;

import java.io.File;
import java.util.Collection;

/**
 * <class comment/>
 */
public class InstallPluginAction extends PluginActionSupport
{
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
            addActionError("Error uploading: " + multiWrapper.getFileSystemNames(inputField));
            return ERROR;
        }

        String[] fileNames = multiWrapper.getFileNames(inputField);

        pluginManager.installPlugin(fileNames[0], files[0].toURL());

        return SUCCESS;
    }
}
