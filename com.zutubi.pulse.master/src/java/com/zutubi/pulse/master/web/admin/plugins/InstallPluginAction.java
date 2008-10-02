package com.zutubi.pulse.master.web.admin.plugins;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import static com.zutubi.config.annotations.FieldParameter.ACTIONS;
import static com.zutubi.config.annotations.FieldParameter.SCRIPTS;
import com.zutubi.config.annotations.FieldType;
import com.zutubi.tove.model.Field;
import com.zutubi.tove.model.Form;
import com.zutubi.tove.webwork.ToveUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

/**
 * An action to install a plugin from an uploaded file.
 */
public class InstallPluginAction extends PluginActionSupport
{
    private static final Logger LOG = Logger.getLogger(InstallPluginAction.class);

    private String pluginPath;
    private String uploadFormSource;
    private String localFormSource;

    private Configuration configuration;

    public String getPluginPath()
    {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath)
    {
        this.pluginPath = pluginPath;
    }

    public String getUploadFormSource() throws IOException, TemplateException
    {
        if(uploadFormSource == null)
        {
            renderForms();
        }
        return uploadFormSource;
    }

    public String getLocalFormSource() throws IOException, TemplateException
    {
        if(localFormSource == null)
        {
            renderForms();
        }
        return localFormSource;
    }

    public void validate()
    {
        HttpServletRequest servletRequest = ServletActionContext.getRequest();
        if (servletRequest instanceof MultiPartRequestWrapper)
        {
            MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) servletRequest;

            if (multiWrapper.hasErrors())
            {
                Collection errors = multiWrapper.getErrors();
                for (Object error : errors)
                {
                    addActionError((String) error);
                }
            }
        }
    }

    private void renderForms() throws TemplateException, IOException
    {
        Form uploadForm = new Form("uploadForm", "uploadForm", "admin/plugins?install=upload");
        uploadForm.setAjax(false);
        uploadForm.setFileUpload(true);

        Field pluginField = new Field(FieldType.FILE, "plugin");
        pluginField.setLabel("path");
        pluginField.addParameter("size", "80");
        uploadForm.add(pluginField);

        Field uploadButton = new Field(FieldType.SUBMIT, "upload");
        uploadButton.setValue("upload");
        uploadForm.add(uploadButton);

        Field cancelButton = new Field(FieldType.SUBMIT, "cancel");
        cancelButton.setValue("cancel");
        uploadForm.add(cancelButton);

        StringWriter writer = new StringWriter();
        ToveUtils.renderForm(uploadForm, getClass(), writer, configuration);
        uploadFormSource = writer.toString();

        Form localForm = new Form("localForm", "localForm", "admin/plugins?install=local");
        localForm.setAjax(false);

        Field pluginPathField = new Field(FieldType.TEXT, "pluginPath");
        pluginPathField.setLabel("path");
        if (TextUtils.stringSet(pluginPath))
        {
            pluginPathField.setValue(pluginPath);
        }
        pluginPathField.addParameter(ACTIONS, Arrays.asList("browse"));
        pluginPathField.addParameter(SCRIPTS, Arrays.asList("InstallPluginAction.browse"));
        localForm.add(pluginPathField);

        Field continueButton = new Field(FieldType.SUBMIT, "continue");
        continueButton.setValue("continue");
        localForm.add(continueButton);
        localForm.add(cancelButton);

        writer = new StringWriter();
        ToveUtils.renderForm(localForm, getClass(), writer, configuration);
        localFormSource = writer.toString();
    }

    public String doInput() throws Exception
    {
        renderForms();
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
            if (names == null)
            {
                addFieldError("plugin", "File not found");
            }
            else
            {
                addFieldError("plugin", "Error uploading: " + names[0]);
            }
            renderForms();
            return ERROR;
        }

        String[] fileNames = multiWrapper.getFileNames(inputField);

        try
        {
            pluginManager.install(files[0].toURI(), fileNames[0], true);
        }
        catch (Exception e)
        {
            LOG.warning(e);
            addActionError(e.getMessage());
            renderForms();
            return ERROR;
        }

        return SUCCESS;
    }

    public String doLocal() throws Exception
    {
        if (!TextUtils.stringSet(pluginPath))
        {
            addFieldError("pluginPath", "path is required");
            return INPUT;
        }

        File pluginFile = new File(pluginPath);
        if (!pluginFile.exists())
        {
            addFieldError("pluginPath", "file does not exist");
            return INPUT;
        }

        if (!pluginFile.isFile())
        {
            addFieldError("pluginPath", "not a regular file");
            return INPUT;
        }

        try
        {
            pluginManager.install(pluginFile.toURI());
        }
        catch (Exception e)
        {
            LOG.warning(e);
            addActionError(e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
