package com.zutubi.pulse.master.tove.nimda;

import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.model.*;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.StringUtils;

/**
 */
public class ConfigDetailAction extends ActionSupport
{
    private String path;
    private ConfigDetailModel model;
    private ConfigurationTemplateManager configurationTemplateManager;
    private FormDescriptorFactory formDescriptorFactory;

    public void setPath(String path)
    {
        this.path = path;
    }

    public ConfigDetailModel getModel()
    {
        return model;
    }

    @Override
    public String execute() throws Exception
    {
        // We expose four broad kinds of detail view:
        //
        // - Composite types:
        //   - With known type (existing, or no extensions) -> form, check, state, links, actions
        //     This may also embed a collapsed collection (summary table).
        //   - With possible choices of types (extensions) -> lead in to wizard
        //   - Not configurable as descendants already configured -> pointers to descendants
        // - Collections of composite types -> summary table, state
        //
        // These need to support a read-only mode, where forms are not editable, tables don't have
        // certain actions like reordering and no wizard links are shown. (Other things link actions
        // are also filtered by permission, generally the client UI just won't know about them in
        // that case.)
        //
        // Any other path is invalid, including:
        //
        // - Unrecognised type (possibly a missing plugin).
        // - Path to something without details (simple field, collection of strings)
        //
        // We break this down into two types of response:
        //
        // - The most common detail response, which has one or more of the form, table, etc.
        //   This response always has a heading and blurb, and the blurb may contain links that
        //   launch a wizard or point to configured descendants.
        // - An error response, with a message.

        model = new ConfigDetailModel(path);

        try
        {
            ComplexType type = configurationTemplateManager.getType(path);
            if (type instanceof CompositeType)
            {
                CompositeType compositeType = (CompositeType) type;
                model.setForm(createForm(compositeType));
            }
        }
        catch (IllegalArgumentException e)
        {
            // Undefined type at this path...
        }

        return SUCCESS;
    }

    private Form createForm(CompositeType compositeType)
    {
        // FIXME
        boolean displayMode = true;

        FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(PathUtils.getParentPath(path), PathUtils.getBaseName(path), compositeType, configurationTemplateManager.isConcrete(path), "mainForm");
        formDescriptor.setDisplayMode(displayMode);
        formDescriptor.setReadOnly(!configurationSecurityManager.hasPermission(path, AccessManager.ACTION_WRITE));
        formDescriptor.setAjax(true);
        //formDescriptor.setNamespace(namespace);
        if (compositeType.getClazz().equals(LabelConfiguration.class))
        {
            formDescriptor.setAction("saveLabel");
        }

        // These decorations should be genericised
        if (displayMode)
        {
            formDescriptor.setActions("apply", "reset");
        }

        Record data = configurationTemplateManager.getRecord(path);
        if (data != null && data instanceof TemplateRecord)
        {
            TemplateFormDecorator templateDecorator = new TemplateFormDecorator((TemplateRecord) data);
            templateDecorator.decorate(formDescriptor);
        }

        // Decorate the form to include the symbolic name as a hidden field. This is necessary for configuration.
        // This is probably not the best place for this, but until i think of a better location, it stays.
        HiddenFieldDescriptor hiddenFieldDescriptor = new HiddenFieldDescriptor();
        hiddenFieldDescriptor.setName("symbolicName");
        hiddenFieldDescriptor.setValue(compositeType.getSymbolicName());
        formDescriptor.add(hiddenFieldDescriptor);

        // Create the context object used to define the freemarker rendering context
        Class clazz = compositeType.getClazz();
        Form form = formDescriptor.instantiate(path, data);
//        if (StringUtils.stringSet(action))
//        {
//            form.setAction(action);
//        }

        return form;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }
}
