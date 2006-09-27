package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.i18n.TextProvider;

/**
 * <class-comment/>
 */
public class InlineHelpDecorator
{
    private TextProvider textProvider;

    public InlineHelpDecorator(TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        for (FieldDescriptor field : descriptor.getFieldDescriptors())
        {
            String inlineHelpKey = field.getName() + ".inlinehelp";
            String message = textProvider.getText(inlineHelpKey);
            if (message != null)
            {
                field.getParameters().put("inlinehelp", message);
            }
        }
        return descriptor;
    }
}
