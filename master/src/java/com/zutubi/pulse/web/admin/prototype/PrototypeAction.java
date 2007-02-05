package com.zutubi.pulse.web.admin.prototype;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.form.*;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.prototype.CvsConfiguration;
import com.zutubi.pulse.prototype.SvnConfiguration;
import com.zutubi.pulse.prototype.record.InvalidRecordTypeException;
import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.web.admin.record.RecordActionSupport;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.*;

/**
 */
public class PrototypeAction extends RecordActionSupport
{
    private Configuration configuration;

    // input: data to be loaded - path / id  from the template manager -> templateRecord.
    private static RecordTypeRegistry typeRegistry = new RecordTypeRegistry();
    static
    {
        try
        {
            typeRegistry.register("svnConfiguration", SvnConfiguration.class);
            typeRegistry.register("cvsConfiguration", CvsConfiguration.class);
        }
        catch (InvalidRecordTypeException e)
        {
            e.printStackTrace();
        }
    }

    private String formHtml;

    private List<String> tableHtml = new LinkedList<String>();

    public String getFormHtml()
    {
        return formHtml;
    }

    public List<String> getTableHtml()
    {
        return tableHtml;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public String doSave() throws Exception
    {
        // save the changes.
        Map<String, String[]> parameters = ActionContext.getContext().getParameters();

        Set<String> hiddenFields = new HashSet<String>();
        hiddenFields.add("projectId");
        hiddenFields.add("path");

        Map<String, String> data = new HashMap<String, String>();
        for (String key : parameters.keySet())
        {
            if (!hiddenFields.contains(key))
            {
                data.put(key, parameters.get(key)[0]);
            }
        }
        
        projectConfigurationManager.setRecord(getProjectId(), getPath(), data);

        // regenerate the form html.
        generateHtml();
        
        return SUCCESS;
    }

    public String execute() throws Exception
    {
        // we have a project id and path.
        generateHtml();
        return SUCCESS;
    }

    private void generateHtml() throws Exception
    {
        Record r = getRecord();

        FormDescriptorFactory formFactory = new FormDescriptorFactory();
        formFactory.setTypeRegistry(typeRegistry);
        FormDescriptor formDescriptor = formFactory.createDescriptor(r.getSymbolicName());
        addHiddenFields(formDescriptor);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("form", formDescriptor.instantiate(r));
        context.put("i18nText", new GetTextMethod());

        // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
        DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        Template template = configuration.getTemplate("form.ftl");
        template.process(context, writer);

        formHtml = writer.toString();

        TableDescriptorFactory tableFactory = new TableDescriptorFactory();
        tableFactory.setTypeRegistry(typeRegistry);
        for (TableDescriptor tableDescriptor : tableFactory.createDescriptors(r.getSymbolicName()))
        {
            context = new HashMap<String, Object>();
            context.put("table", tableDescriptor.instantiate(r.get(tableDescriptor.getName())));
            context.put("i18nText", new GetTextMethod());

            // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
            DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

            // handle rendering of the freemarker template.
            writer = new StringWriter();

            template = configuration.getTemplate("table.ftl");
            template.process(context, writer);

            tableHtml.add(writer.toString());
        }
    }

    private void addHiddenFields(FormDescriptor formDescriptor)
    {
        FieldDescriptor hiddenProjectId = new FieldDescriptor();
        hiddenProjectId.setName("projectId");
        hiddenProjectId.getParameters().put("type", "hidden");
        hiddenProjectId.getParameters().put("value", getProjectId());
        formDescriptor.add(hiddenProjectId);

        FieldDescriptor hiddenRecordPath = new FieldDescriptor();
        hiddenRecordPath.setName("path");
        hiddenRecordPath.getParameters().put("type", "hidden");
        hiddenRecordPath.getParameters().put("value", getPath());
        formDescriptor.add(hiddenRecordPath);
    }
}
