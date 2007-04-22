package com.zutubi.prototype.wizard;

import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.i18n.Messages;
import com.zutubi.validation.XWorkValidationAdapter;

import java.util.Map;

/**
 *
 *
 */
public interface WizardState
{
    /**
     * The template record associated with the data for this wizard state.
     *
     * @return
     */
    TemplateRecord getTemplateRecord();

    /**
     * The record containing the data within this wizard state.
     *
     * @return
     */
    MutableRecord getRecord();

    void updateRecord(Map parameters);

    Messages getMessages();

    FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path);

    boolean validate(String path, XWorkValidationAdapter validationCallback) throws TypeException;
}
