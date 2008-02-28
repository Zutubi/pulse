package com.zutubi.prototype.webwork;

import com.zutubi.config.annotations.FieldType;
import com.zutubi.prototype.config.ConfigurationRefactoringManager;
import com.zutubi.prototype.config.TemplateNode;
import com.zutubi.prototype.model.Field;
import com.zutubi.prototype.model.Form;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Action to gather new keys and request a clone for map items.
 */
public class CloneAction extends PrototypeSupport
{
    private ConfigurationPanel newPanel;
    private String formSource;
    private String cloneKey;
    private MasterConfigurationManager configurationManager;
    private ConfigurationRefactoringManager configurationRefactoringManager;

    public ConfigurationPanel getNewPanel()
    {
        return newPanel;
    }

    public String getFormSource()
    {
        return formSource;
    }

    public void setCloneKey(String cloneKey)
    {
        this.cloneKey = cloneKey;
    }

    public String execute() throws Exception
    {
        if(isCancelSelected())
        {
            return "cancel";
        }

        if(isInputSelected())
        {
            renderForm();
            return INPUT;
        }

        path = configurationRefactoringManager.clone(path, cloneKey);
        String templatePath = configurationTemplateManager.getTemplatePath(path);
        response = new ConfigurationResponse(path, templatePath);
        String iconCls;
        TemplateNode templateNode = configurationTemplateManager.getTemplateNode(path);
        if(templateNode == null)
        {
            iconCls = PrototypeUtils.getIconCls(configurationTemplateManager.getType(path));
        }
        else
        {
            iconCls = "config-" + (templateNode.isConcrete() ? "concrete" : "template") + "-icon";
        }

        boolean leaf = PrototypeUtils.isLeaf(path, configurationTemplateManager, configurationSecurityManager);
        response.addAddedFile(new ConfigurationResponse.Addition(path, cloneKey, templatePath, iconCls, leaf));
        return SUCCESS;
    }

    private void renderForm() throws IOException, TemplateException
    {
        Form form = new Form("form", "clone", PrototypeUtils.getConfigURL(path, "clone", null, "aconfig"));
        Field field = new Field(FieldType.HIDDEN, "path");
        field.setValue(getPath());
        form.add(field);

        field = new Field(FieldType.TEXT, "cloneKey");
        field.setLabel("clone name");
        field.setValue("clone of ");
        form.add(field);

        addSubmit(form, "clone");
        addSubmit(form, "cancel");

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("actionErrors", getActionErrors());
        context.put("fieldErrors", getFieldErrors());

        StringWriter writer = new StringWriter();
        PrototypeUtils.renderForm(context, form, getClass(), writer, configurationManager);
        formSource = writer.toString();

        newPanel = new ConfigurationPanel("aconfig/clone.vm");
    }

    private void addSubmit(Form form, String name)
    {
        Field field = new Field(FieldType.SUBMIT, name);
        field.setValue(name);
        form.add(field);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }
}
