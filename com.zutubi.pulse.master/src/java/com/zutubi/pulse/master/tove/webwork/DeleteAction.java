package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.tove.config.cleanup.HideRecordCleanupTask;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.ListType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.i18n.TextProvider;

/**
 * Action for deleting a record.  Also handles displaying confirmation when
 * necessary.
 */
public class DeleteAction extends ToveActionSupport
{
    private static final String ACTION_CANCEL_DIRECT = "canceldirect";
    private static final String ACTION_CONFIRM_DIRECT = "confirmdirect";
    
    private RecordCleanupTask task;
    private String parentPath;
    private ConfigurationPanel newPanel;

    private TextProvider textProvider;

    public boolean isDirect()
    {
        return isSelected(ACTION_CONFIRM_DIRECT);
    }

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

    @Override
    public boolean isCancelled()
    {
        return super.isCancelled() || isSelected(ACTION_CANCEL_DIRECT);
    }

    @Override
    public boolean isConfirmSelected()
    {
        return isSelected(ACTION_CONFIRM) || isSelected(ACTION_CONFIRM_DIRECT);
    }

    public void doCancel()
    {
        boolean isListItem = false;
        parentPath = PathUtils.getParentPath(path);
        if (parentPath != null)
        {
            ComplexType parentType = configurationTemplateManager.getType(parentPath);
            isListItem = parentType instanceof ListType;
        }
        
        if (!isListItem && isSelected(ACTION_CANCEL_DIRECT))
        {
            response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));            
        }
        else
        {
            response = new ConfigurationResponse(parentPath, configurationTemplateManager.getTemplatePath(path));
            path = response.getNewPath();
        }
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
