package com.zutubi.tove.wizard;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.tove.FormDescriptor;
import com.zutubi.tove.FormDescriptorFactory;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;

import java.util.Map;

/**
 * Interface for all states of {@link Wizard}s.
 */
public interface WizardState
{
    /**
     * @return a unique id for the state in the wizard
     */
    String getId();
    /**
     * @return a name for this state, used for I18N lookups
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

    /**
     * Retrieves i18n messages for this state.
     *
     * @return a context for i18n for this state
     */
    Messages getMessages();

    FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name);

    boolean validate(ValidationAware validationCallback) throws TypeException;

    /**
     * Returns the following state, which may change depending on the
     * decisions the user has made in this state.
     *
     * @return the next state, or null if there are no more
     */
    WizardState getNextState();
}
