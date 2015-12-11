package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Action to prompt the user for the template that they would like to move an
 * instance under.
 */
public class MoveAction extends ToveFormActionSupport
{
    private static final Messages I18N = Messages.getInstance(MoveAction.class);
    
    private static final String FIELD_NEW_TEMPLATE_PARENT_KEY = "newTemplateParentKey";

    /**
     * Template to move the instance under.
     */
    private String newTemplateParentKey;
    /**
     * Set by preview to the paths that would be deleted as the result of a move.
     */
    private List<String> deletedPaths;

    private ConfigurationRefactoringManager configurationRefactoringManager;
    private ClassificationManager classificationManager;

    public MoveAction()
    {
        super(ConfigurationRefactoringManager.ACTION_MOVE, "move", "confirm");
    }

    public List<String> getDeletedPaths()
    {
        return deletedPaths;
    }

    public String getNewTemplateParentKey()
    {
        return newTemplateParentKey;
    }

    public void setNewTemplateParentKey(String newTemplateParentKey)
    {
        this.newTemplateParentKey = newTemplateParentKey;
    }

    @Override
    public void doCancel()
    {
        response = new ConfigurationResponse(path, configurationTemplateManager.getTemplatePath(path));
    }

    @Override
    protected void validatePath()
    {
        type = configurationTemplateManager.getType(path);
    }

    @Override
    protected void addFormFields(Form form)
    {
        Field field = new Field(FieldType.DROPDOWN, FIELD_NEW_TEMPLATE_PARENT_KEY);
        field.setLabel(I18N.format(FIELD_NEW_TEMPLATE_PARENT_KEY + ".label"));
        List<String> templates = configurationRefactoringManager.getMoveTemplates(path);
        Collections.sort(templates, new Sort.StringComparator());
        field.addParameter("list", templates);
        field.setValue(newTemplateParentKey);
        form.add(field);
    }

    @Override
    protected void validateForm()
    {
        if (!TextUtils.stringSet(newTemplateParentKey))
        {
            addFieldError(FIELD_NEW_TEMPLATE_PARENT_KEY, I18N.format(FIELD_NEW_TEMPLATE_PARENT_KEY + ".required"));
        }
    }

    @Override
    protected boolean doPreview()
    {
        ConfigurationRefactoringManager.MoveResult moveResult = configurationRefactoringManager.previewMove(path, newTemplateParentKey);
        if (moveResult.getDeletedPaths().size() > 0)
        {
            deletedPaths = new LinkedList<String>(moveResult.getDeletedPaths());
            Collections.sort(deletedPaths, new Sort.StringComparator());
            newPanel = new ConfigurationPanel("ajax/config/confirmMove.vm");
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    protected void doAction()
    {
        configurationRefactoringManager.move(path, newTemplateParentKey);
        String templatePath = configurationTemplateManager.getTemplatePath(path);
        response = new ConfigurationResponse(path, templatePath);
        response.addRemovedPath(path);
        response.addAddedFile(new ConfigurationResponse.Addition(path, PathUtils.getBaseName(path), templatePath, null, ToveUtils.getIconCls(path, classificationManager), false, configurationTemplateManager.isConcrete(path)));
        response.setStatus(new ConfigurationResponse.Status(ConfigurationResponse.Status.Type.SUCCESS, I18N.format("moved.feedback")));
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }

    public void setClassificationManager(ClassificationManager classificationManager)
    {
        this.classificationManager = classificationManager;
    }
}