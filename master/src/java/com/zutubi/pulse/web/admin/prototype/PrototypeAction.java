package com.zutubi.pulse.web.admin.prototype;

import com.zutubi.pulse.form.descriptor.ColumnDescriptor;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.descriptor.TableDescriptor;
import com.zutubi.pulse.form.ui.FormSupport;
import com.zutubi.pulse.form.ui.components.Column;
import com.zutubi.pulse.form.ui.components.Table;
import com.zutubi.pulse.prototype.*;
import com.zutubi.pulse.web.ActionSupport;
import freemarker.template.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class PrototypeAction extends ActionSupport
{
    private Configuration configuration;

    private DescriptorFactory descriptorFactory;

    // input: data to be loaded - path / id  from the template manager -> templateRecord.
    private TemplateManager templateManager = new TemplateManagerImpl();
    private RecordTypeRegistry typeRegistry = new RecordTypeRegistry();

    private long projectId;
    private String recordName = "scm";
    private String formHtml;
    private String tableHtml;

    public String execute() throws Exception
    {
        TemplateRecord scm = templateManager.load(Scopes.PROJECTS, Long.toString(projectId), recordName);

        typeRegistry.register("svnConfiguration", SvnConfiguration.class);
        
        Class scmConfigType = typeRegistry.getType(scm.getSymbolicName());

        FormSupport support = new FormSupport();
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(scmConfigType));
        support.setTheme(null);

        formHtml = support.renderForm(scmConfigType, scm);

        TableDescriptor tableDescriptor = descriptorFactory.createTableDescriptor(scmConfigType);

        Table t = new Table();
        t.setName("svnConfigs");

        for (ColumnDescriptor colDescriptor : tableDescriptor.getColumnDescriptors())
        {
            t.addColumn(new Column(colDescriptor.getName()));
        }

        tableHtml = support.render(t, null);

        return SUCCESS;
    }

    public List<SvnConfiguration> getSvnConfigs()
    {
/*
        List<TemplateRecord> cfgs = new ArrayList<TemplateRecord>();
        List<OwnedRecord> ors = new ArrayList<OwnedRecord>();
        ors.add(new OwnedRecord());

        TemplateRecord r = new TemplateRecord();
        cfgs.add()
*/

        List<SvnConfiguration> configs = new ArrayList<SvnConfiguration>();

        configs.add(new SvnConfiguration("url1", "name1", "password1"));
        configs.add(new SvnConfiguration("url2", "name2", "password2"));
        configs.add(new SvnConfiguration("url3", "name3", "password3"));

        return configs;
    }

    public String getFormHtml()
    {
        return formHtml;
    }

    public String getTableHtml()
    {
        return tableHtml;
    }

    public void setTemplateManager(TemplateManager templateManager)
    {
        this.templateManager = templateManager;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public void setTypeRegistry(RecordTypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }
}
