package com.zutubi.pulse.master.tove.webwork;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.NewLabelConfiguration;
import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

/**
 * A custom action for saving labels.  Checks for label renames, and if one is detected prompts the
 * user to possibly rename other labels.
 *
 * FIXME kendo reimplement this somehow
 */
public class SaveLabelAction extends SaveAction
{
    private static final String ACTION_RENAME_ONE = "one";
    private static final String ACTION_RENAME_ALL = "all";
    
    private ConfigurationPanel configurationPanel;
    private int otherLabelCount;
    private String originalName;
    private String newName;
    private String formSource;

    private Configuration freemarkerConfiguration;
    private ActionManager actionManager;

    public ConfigurationPanel getConfigurationPanel()
    {
        return configurationPanel;
    }

    public int getOtherLabelCount()
    {
        return otherLabelCount;
    }

    public String getOriginalName()
    {
        return originalName;
    }

    public String getNewName()
    {
        return newName;
    }

    public String getFormSource()
    {
        return formSource;
    }

    @Override
    public String execute() throws Exception
    {
        if (isSaveSelected())
        {
            return doSave();
        }
        else if (isSelected(ACTION_RENAME_ALL))
        {
            return doRenameAll();
        }
        else if (isSelected(ACTION_RENAME_ONE))
        {
            return super.doSave();
        }
        else
        {
            return doRender();
        }
    }

    private String doRenameAll() throws Exception
    {
        bindRecord();

        if (validateRecord())
        {
            actionManager.execute("rename", configurationProvider.get(path, LabelConfiguration.class), new NewLabelConfiguration((String) record.get("label")));
            setupResponse(path);
            return doRender();
        }
        else
        {
            return INPUT;
        }
    }

    @Override
    protected String doSave() throws Exception
    {
        CompositeType type = getType();
        if (type == null)
        {
            return doRender();
        }

        bindRecord();

        if (validateRecord())
        {
            if (isRename())
            {
                renderForm();
                configurationPanel = new ConfigurationPanel("ajax/config/renameLabel.vm");
                return "rename";
            }
            else
            {
                setupResponse(configurationTemplateManager.saveRecord(path, (MutableRecord) record));
                return doRender();
            }
        }
        else
        {
            return INPUT;
        }

    }

    private void renderForm() throws IOException, TemplateException
    {
        final String symbolicName = getType().getSymbolicName();
        Form form = new Form("form", symbolicName, ToveUtils.getConfigURL(path, "saveLabel", null, "ajax/config"), ACTION_RENAME_ALL);
        
        Field typeField = new Field(FieldType.HIDDEN, "symbolicName");
        typeField.setValue(symbolicName);
        form.add(typeField);

        Field nameField = new Field(FieldType.TEXT, "label");
        nameField.setLabel("new label");
        nameField.setValue(newName);
        form.add(nameField);
        
        addSubmit(form, "rename all matching labels", ACTION_RENAME_ALL, true);
        addSubmit(form, "only rename this label", ACTION_RENAME_ONE, false);
        addSubmit(form, "cancel", ACTION_CANCEL, false);

        StringWriter writer = new StringWriter();
        ToveUtils.renderForm(form, getClass(), writer, freemarkerConfiguration);
        formSource = writer.toString();
    }

    private void addSubmit(Form form, String label, String value, boolean isDefault)
    {
        Field field = new Field(FieldType.SUBMIT, value);
        field.setLabel(label);
        field.setValue(value);
        if (isDefault)
        {
            field.addParameter("default", true);
        }

        form.add(field);
    }

    private boolean isRename()
    {
        final LabelConfiguration originalLabel = configurationProvider.get(path, LabelConfiguration.class);
        originalName = originalLabel.getLabel();
        newName = (String) record.get("label");
        if (Objects.equal(originalName, newName))
        {
            return false;
        }
        
        final String labelsPath = PathUtils.getPath(PROJECTS_SCOPE, WILDCARD_ANY_ELEMENT, "labels", WILDCARD_ANY_ELEMENT);
        Collection<LabelConfiguration> labels = configurationTemplateManager.getAllInstances(labelsPath, LabelConfiguration.class, false);
        otherLabelCount = size(filter(labels, new Predicate<LabelConfiguration>()
        {
            public boolean apply(LabelConfiguration l)
            {
                return Objects.equal(l.getLabel(), originalLabel.getLabel());
            }
        })) - 1;
        
        return otherLabelCount > 0;
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
