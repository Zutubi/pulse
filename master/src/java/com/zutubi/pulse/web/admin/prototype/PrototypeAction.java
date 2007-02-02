package com.zutubi.pulse.web.admin.prototype;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.form.FieldDescriptor;
import com.zutubi.prototype.form.FormDescriptor;
import com.zutubi.prototype.form.FormDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.prototype.RecordTypeRegistry;
import com.zutubi.pulse.prototype.SvnConfiguration;
import com.zutubi.pulse.prototype.InvalidRecordTypeException;
import com.zutubi.pulse.prototype.CvsConfiguration;
import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.web.admin.record.RecordActionSupport;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 */
public class PrototypeAction extends RecordActionSupport
{
    private Configuration configuration;

    // input: data to be loaded - path / id  from the template manager -> templateRecord.
//    private TemplateManager templateManager = new TemplateManagerImpl();
    private RecordTypeRegistry typeRegistry = new RecordTypeRegistry();

    private String formHtml;

    private String[] tableHtml = new String[0];

    public PrototypeAction() throws InvalidRecordTypeException
    {
        typeRegistry.register("svnConfiguration", SvnConfiguration.class);
        typeRegistry.register("cvsConfiguration", CvsConfiguration.class);
    }

    public String getFormHtml()
    {
        return formHtml;
    }

    public void setTypeRegistry(RecordTypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
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

        Class type = typeRegistry.getType(r.getSymbolicName());

        FormDescriptorFactory factory = new FormDescriptorFactory();
        FormDescriptor formDescriptor = factory.createDescriptor(type);
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

    private static TemplateLoader getMultiLoader()
    {
        File root = new File("c:/projects/pulse/trunk");
        List<String> templateRoots = Arrays.asList("master/src/templates", "master/src/www");

        FileTemplateLoader loaders[] = new FileTemplateLoader[templateRoots.size()];
        for (int i = 0; i < loaders.length; i++)
        {
            try
            {
                loaders[i] = new FileTemplateLoader(new File(root, templateRoots.get(i)));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return new MultiTemplateLoader(loaders);
    }

}
