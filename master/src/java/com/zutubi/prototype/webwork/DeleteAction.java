package com.zutubi.prototype.webwork;

import com.zutubi.prototype.config.cleanup.HideRecordCleanupTask;
import com.zutubi.prototype.config.cleanup.RecordCleanupTask;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.i18n.TextProvider;

/**
 * Action for deleting a record.  Also handles displaying confirmation when
 * necessary.
 */
public class DeleteAction extends PrototypeSupport
{
    private RecordCleanupTask task;
    private String parentPath;
    private ConfigurationPanel newPanel;

    private TextProvider textProvider;

    public boolean isHide()
    {
        return task instanceof HideRecordCleanupTask;
    }

    public RecordCleanupTask getTask()
    {
        return task;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public ConfigurationPanel getNewPanel()
    {
        return newPanel;
    }

    public TextProvider getTextProvider()
    {
        return textProvider;
    }

    public String execute() throws Exception
    {
        parentPath = PathUtils.getParentPath(path);

        type = configurationTemplateManager.getType(path);
        textProvider = new MessagesTextProvider(type.getClazz());

        if (isConfirmSelected())
        {
            task = configurationTemplateManager.getCleanupTasks(getPath());
            newPanel = new ConfigurationPanel("aconfig/confirm.vm");
            return "confirm";
        }
        else
        {
            String templatePath = configurationTemplateManager.getTemplatePath(path);
            if (isDeleteSelected())
            {
                String newTemplatePath = templatePath == null ? null : PathUtils.getParentPath(templatePath);
                configurationTemplateManager.delete(path);

                response = new ConfigurationResponse(parentPath, newTemplatePath);
                if (!configurationTemplateManager.pathExists(path))
                {
                    response.addRemovedPath(path);
                }
                path = response.getNewPath();
                return SUCCESS;
            }
            else if (isCancelSelected())
            {
                response = new ConfigurationResponse(parentPath, templatePath);
                path = response.getNewPath();
                return "cancel";
            }
        }
        return ERROR;
    }
}
