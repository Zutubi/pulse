package com.zutubi.prototype.wizard;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;

import java.util.Map;

/**
 *
 *
 */
public interface WizardState
{
    /**
     * @return a name for this state, unique in the wizard (used for I18N
     *         lookups)
     */
    String getName();
    /**
     * @return The record used to render the form for this state, which may
     * be different to the actual record that will be updated/inserted (e.g.
     * in the case of a templated scope).
     */
    Record getRenderRecord();
    /**
     * @return the record containing the data within this wizard state.  This
     * is the record that is eventually inserted if the wizard completes.
     */
    MutableRecord getDataRecord();
    /**
     * Updates the data record for this state with the given posted
     * parameters.
     *
     * @param parameters parameters posted from the web client
     */
    void updateRecord(Map parameters);

    Messages getMessages();

    FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name);

    boolean validate(String path, ValidationAware validationCallback) throws TypeException;
}
