package com.zutubi.pulse.master.tove.wizard;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.model.FormDescriptor;
import com.zutubi.pulse.master.tove.model.FormDescriptorFactory;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;

import java.util.Map;

/**
 * State used as a placeholder then the next state is unknown.  This happens
 * when user choices change the wizard behaviour (classically with type
 * selection).
 */
public class UnknownTypeState extends AbstractChainableState
{
    private String id;
    private CompositeType baseType;

    public UnknownTypeState(int ord, CompositeType baseType)
    {
        id = Integer.toString(ord) + ".unknown";
        this.baseType = baseType;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return "configure";
    }

    public Record getRenderRecord()
    {
        throw new UnsupportedOperationException("This state cannot be rendered");
    }

    public MutableRecord getDataRecord()
    {
        return null;
    }

    public void updateRecord(Map parameters)
    {
        throw new UnsupportedOperationException("This state cannot be rendered");
    }

    public Messages getMessages()
    {
        return Messages.getInstance(baseType.getClazz());
    }

    public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
    {
        throw new UnsupportedOperationException("This state cannot be rendered");
    }

    public boolean validate(ValidationAware validationCallback) throws TypeException
    {
        return true;
    }

    public CompositeType getConfiguredBaseType()
    {
        return null;
    }

    public CompositeType getType()
    {
        return baseType;
    }

    public boolean hasFields()
    {
        return false;
    }

    public boolean hasConfigurationCheck()
    {
        return false;
    }
}
