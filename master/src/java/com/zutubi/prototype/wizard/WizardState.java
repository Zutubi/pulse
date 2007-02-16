package com.zutubi.prototype.wizard;

import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.CompositeType;

/**
 *
 *
 */
public interface WizardState
{
    String name();

    Object data();

    CompositeType type();

    Form getForm(Object data);
}
