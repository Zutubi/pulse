package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.util.logging.Logger;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class TypeSelectAnnotationHandler extends FieldAnnotationHandler
{
    private static final Logger LOG = Logger.getLogger(TypeSelectAnnotationHandler.class);

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
            LOG.severe(e);
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

