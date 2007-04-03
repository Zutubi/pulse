package com.zutubi.prototype.wizard;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.type.record.MutableRecord;

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
     * The type of the data represented by this wizard state.
     *
     * @return
     */
    CompositeType getType();

    /**
     * The record containing the data within this wizard state.
     *
     * @return
     */
    MutableRecord getRecord();

}
