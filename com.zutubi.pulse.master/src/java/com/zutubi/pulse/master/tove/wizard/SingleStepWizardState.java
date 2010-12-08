package com.zutubi.pulse.master.tove.wizard;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;

import java.util.Map;

/**
 * A wizard state that is used for configuring a single, non-extendable
 * type.
 */
public class SingleStepWizardState extends AbstractTypeWizardState
{
    /**
     * Every wizard state / form is represented by a type.
     */
    private CompositeType type;
    private MutableRecord dataRecord = null;
    private TemplateRecord templateRecord;

    public SingleStepWizardState(AbstractTypeWizard wizard, int ord, String parentPath, CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
    {
        super(wizard, Integer.toString(ord), parentPath, baseType);
        SpringComponentContext.autowire(this);
        this.type = type;
        this.templateRecord = templateRecord;

        if (templateRecord == null)
        {
            this.dataRecord = type.createNewRecord(true);
        }
        else
        {
            this.dataRecord = (MutableRecord) templateRecord.getMoi();
        }
    }

    /**
     * The type that defines this wizards state.  It is from this type that the form is generated.
     *
     * @return the state type.
     */
    public CompositeType getType()
    {
        return type;
    }

    public CompositeType getConfiguredBaseType()
    {
        return getBaseType();
    }

    public Record getRenderRecord()
    {
        if (templateRecord == null)
        {
            return dataRecord;
        }
        else
        {
            return templateRecord;
        }
    }

    public MutableRecord getDataRecord()
    {
        return dataRecord;
    }

    @SuppressWarnings({"unchecked"})
    public void updateRecord(Map parameters)
    {
        super.updateRecord(parameters);
        if(templateRecord != null)
        {
            // unsuppress password values before scrubbing.
            ToveUtils.unsuppressPasswords(templateRecord.getParent(), dataRecord, type, true);
            configurationTemplateManager.scrubInheritedValues(templateRecord, dataRecord);
        }
    }
}
