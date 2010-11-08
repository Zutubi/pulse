package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.tove.config.cleanup.HideRecordCleanupTask;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.i18n.TextProvider;

/**
 * Action for deleting a record.  Also handles displaying confirmation when
 * necessary.
 */
public class DeleteAction extends ToveActionSupport
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

    public void doCancel()
    {
        parentPath = PathUtils.getParentPath(path);
        response = new ConfigurationResponse(parentPath, configurationTemplateManager.getTemplatePath(path));
        path = response.getNewPath();
    }

    public String execute() throws Exception
    {
        parentPath = PathUtils.getParentPath(path);
        type = configurationTemplateManager.getType(path);
        textProvider = new MessagesTextProvider(type.getClazz());

        if (isConfirmSelected())
        {
            task = configurationTemplateManager.getCleanupTasks(getPath());
            newPanel = new ConfigurationPanel("ajax/config/confirm.vm");
            return "confirm";
        }
        else
        {
            String templatePath = configurationTemplateManager.getTemplatePath(path);
            if (isDeleteSelected())
            {
                String newTemplatePath = templatePath == null ? null : PathUtils.getParentPath(templatePath);
                String originalDisplayName = ToveUtils.getDisplayName(path, configurationTemplateManager);
                configurationTemplateManager.delete(path);

                response = new ConfigurationResponse(parentPath, newTemplatePath);
                boolean collectionElement = (configurationTemplateManager.getType(parentPath) instanceof CollectionType);
                if (collectionElement)
                {
                    response.addRemovedPath(path);
                }
                else
                {
                    String newDisplayName = ToveUtils.getDisplayName(path, configurationTemplateManager);
                    if(!newDisplayName.equals(originalDisplayName))
                    {
                        response.addRenamedPath(new ConfigurationResponse.Rename(path, path, newDisplayName, null));
                    }
                }

                path = response.getNewPath();
                return SUCCESS;
            }
        }
        return ERROR;
    }
}
