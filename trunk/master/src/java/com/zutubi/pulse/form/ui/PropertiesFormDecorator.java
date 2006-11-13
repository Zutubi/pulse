package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.TextProvider;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;

/**
 * <class-comment/>
 */
public class PropertiesFormDecorator
{
    private TextProvider textProvider;

    public PropertiesFormDecorator(TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        // check the header property.
        handleHeader(descriptor);

        for (FieldDescriptor field : descriptor.getFieldDescriptors())
        {
            handleInlineHelp(field);
        }
        return descriptor;
    }

    private void handleInlineHelp(FieldDescriptor field)
    {
        if (!field.getParameters().containsKey("inlinehelp"))
        {
            String inlineHelpKey = field.getName() + ".inlinehelp";
            String message = textProvider.getText(inlineHelpKey);
            if (message != null)
            {
                field.getParameters().put("inlinehelp", message);
            }
        }
    }

    private void handleHeader(FormDescriptor descriptor)
    {
        if (!descriptor.getParameters().containsKey("heading"))
        {
            String key = descriptor.getName() + ".heading";
            String msg = textProvider.getText(key);
            if (msg != null)
            {
                descriptor.getParameters().put("heading", msg);
            }
        }
    }

}
