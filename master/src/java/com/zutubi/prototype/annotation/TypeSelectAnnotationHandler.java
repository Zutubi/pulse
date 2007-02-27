package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.i18n.Messages;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class TypeSelectAnnotationHandler extends FieldAnnotationHandler
{
    private TypeRegistry typeRegistry;

    public void process(Annotation annotation, Descriptor descriptor)
    {
        super.process(annotation, descriptor);

        try
        {
            Type type = typeRegistry.getType(((TypeSelect)annotation).configurationType());
            if (type == null)
            {
                return;
            }
            
            java.util.List<String> optionList = ((CompositeType)type).getExtensions();
            descriptor.addParameter("list", optionList);

            // It would be nice ot have some information about each of these options. This is
            // possible when the options represent types and we are in a wizard.
            for (String option: optionList)
            {
                String extraInformation = "No extra information avaiable";
                Type optionType = typeRegistry.getType(option);
                if (optionType != null)
                {
                    Messages messages = Messages.getInstance(optionType.getClazz());
                    String message = messages.format("introduction");
                    if (message != null)
                    {
                        extraInformation = message;
                    }
                }
                descriptor.addParameter(option + ".introduction", extraInformation);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

