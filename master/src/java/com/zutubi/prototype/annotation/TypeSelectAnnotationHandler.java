package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.util.logging.Logger;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 */
public class TypeSelectAnnotationHandler extends FieldAnnotationHandler
{
    private static final Logger LOG = Logger.getLogger(TypeSelectAnnotationHandler.class);

    private TypeRegistry typeRegistry;

    public void process(Annotation annotation, Descriptor descriptor) throws Exception
    {
        super.process(annotation, descriptor);

        List<String> optionList = Collections.EMPTY_LIST;

        Type type = typeRegistry.getType(((TypeSelect)annotation).configurationType());
        if (type != null)
        {
            optionList = ((CompositeType)type).getExtensions();
        }

        descriptor.addParameter("list", optionList);

/*
        // It would be nice ot have some information about each of these options. This is
        // possible when the options represent types.
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
*/
    }
    
    /**
     * Required resource.
     *
     * @param typeRegistry instance
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

