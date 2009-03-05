package com.zutubi.pulse.master.tove.webwork.help;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.pulse.master.tove.wizard.TwoStepStateBuilder;
import com.zutubi.pulse.master.tove.wizard.webwork.ConfigurationWizardAction;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.type.CompositeType;

import java.util.LinkedList;
import java.util.List;

/**
 * Pulls together documentation for a type selection wizard state.
 */
public class WizardSelectHelpAction extends HelpActionSupport
{
    private Messages messages;
    private List<SelectionDocs> selections = new LinkedList<SelectionDocs>();

    private ConfigurationDocsManager configurationDocsManager;

    public Messages getMessages()
    {
        return messages;
    }

    public List<SelectionDocs> getSelections()
    {
        return selections;
    }

    public boolean isExpandable(SelectionDocs docs)
    {
        return  docs.verbose != null && !docs.verbose.equals(docs.brief);
    }

    public String execute() throws Exception
    {
        AbstractTypeWizard wizardInstance = (AbstractTypeWizard) ConfigurationWizardAction.getWizardInstance(getPath());
        TwoStepStateBuilder.SelectWizardState state = (TwoStepStateBuilder.SelectWizardState) wizardInstance.getCurrentState();

        CompositeType type = state.getType();
        messages = Messages.getInstance(type.getClazz());

        for (CompositeType extension: type.getExtensions())
        {
            Messages extensionMessages = Messages.getInstance(extension.getClazz());
            String label;
            if(extensionMessages.isKeyDefined("wizard.label"))
            {
                label = extensionMessages.format("wizard.label");
            }
            else
            {
                label = extensionMessages.format("label");
            }

            TypeDocs extensionDocs = configurationDocsManager.getDocs(extension);
            selections.add(new SelectionDocs(extension.getSymbolicName(), label, extensionDocs.getBrief(), extensionDocs.getVerbose()));
        }

        return SUCCESS;
    }

    public void setConfigurationDocsManager(ConfigurationDocsManager configurationDocsManager)
    {
        this.configurationDocsManager = configurationDocsManager;
    }

    public static class SelectionDocs
    {
        public String name;
        public String label;
        public String brief;
        public String verbose;

        public SelectionDocs(String name, String label, String brief, String verbose)
        {
            this.name = name;
            this.label = label;
            this.brief = brief;
            this.verbose = verbose;
        }

        public String getName()
        {
            return name;
        }

        public String getLabel()
        {
            return label;
        }

        public String getBrief()
        {
            return brief;
        }

        public String getVerbose()
        {
            return verbose;
        }
    }
}
