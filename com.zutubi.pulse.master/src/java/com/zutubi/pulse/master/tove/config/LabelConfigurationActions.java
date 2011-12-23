package com.zutubi.pulse.master.tove.config;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.NullaryProcedure;
import com.zutubi.util.Predicate;

import java.util.Collection;

/**
 * Actions for labels.
 */
public class LabelConfigurationActions
{
    private ConfigurationTemplateManager configurationTemplateManager;
    private TransactionManager pulseTransactionManager;
    
    public NewLabelConfiguration prepareRename(LabelConfiguration instance)
    {
        return new NewLabelConfiguration(instance.getLabel());
    }
    
    public void doRename(final LabelConfiguration instance, final NewLabelConfiguration newName)
    {
        final String labelsPath = PathUtils.getPath(PROJECTS_SCOPE, WILDCARD_ANY_ELEMENT, "labels", WILDCARD_ANY_ELEMENT);
        final Collection<LabelConfiguration> labels = configurationTemplateManager.getAllInstances(labelsPath, LabelConfiguration.class, false);
        CollectionUtils.filterInPlace(labels, new Predicate<LabelConfiguration>()
        {
            public boolean satisfied(LabelConfiguration l)
            {
                return l.getLabel().equals(instance.getLabel());
            }
        });

        pulseTransactionManager.runInTransaction(new NullaryProcedure()
        {
            public void run()
            {
                for (LabelConfiguration l: labels)
                {
                    LabelConfiguration copy = configurationTemplateManager.deepClone(l);
                    copy.setLabel(newName.getLabel());
                    configurationTemplateManager.save(copy);
                }
            }
        });
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setPulseTransactionManager(TransactionManager pulseTransactionManager)
    {
        this.pulseTransactionManager = pulseTransactionManager;
    }
}
