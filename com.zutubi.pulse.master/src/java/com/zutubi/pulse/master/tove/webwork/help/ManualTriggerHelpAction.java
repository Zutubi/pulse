package com.zutubi.pulse.master.tove.webwork.help;

import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.docs.PropertyDocs;
import com.zutubi.tove.config.docs.TypeDocs;

import java.util.LinkedList;
import java.util.List;

/**
 * This action generates the help panel content for the manual trigger page.
 */
public class ManualTriggerHelpAction extends HelpActionSupport
{
    private ConfigurationProvider provider;

    private TypeDocs typeDocs;
    private List<String> formProperties;

    public TypeDocs getTypeDocs()
    {
        return typeDocs;
    }

    public List<String> getFormProperties()
    {
        return formProperties;
    }

    public String execute() throws Exception
    {
        ProjectConfiguration project = provider.get(getPath(), ProjectConfiguration.class);

        formProperties = new LinkedList<String>();

        // extract the details for the project form help.
        typeDocs = new TypeDocs(null);
        typeDocs.setTitle(getText("title"));
        typeDocs.setBrief(getText("brief"));

        PropertyDocs revisionDoc = new PropertyDocs("revision", null);
        revisionDoc.setLabel(getText("revision.label"));
        revisionDoc.setBrief(getText("revision.brief"));
        revisionDoc.setVerbose(getText("revision.verbose"));

        typeDocs.addProperty(revisionDoc);
        formProperties.add("revision");
        
        for (ResourcePropertyConfiguration property : project.getProperties().values())
        {
            PropertyDocs propertyDoc = new PropertyDocs(property.getName(), null);
            propertyDoc.setLabel(property.getName());
            propertyDoc.setBrief(property.getDescription());
            propertyDoc.setVerbose(property.getDescription());
            typeDocs.addProperty(propertyDoc);
            formProperties.add(property.getName());
        }

        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider provider)
    {
        this.provider = provider;
    }
}
