/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.ui.forms;

import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.model.forms.FieldModel;
import com.zutubi.tove.ui.model.forms.OptionFieldModel;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Handles annotations for field types that present a list of options to the
 * user.  Uses an {@link OptionProvider}
 * to get the list of options.
 */
public class OptionAnnotationHandler extends FieldAnnotationHandler
{
    private ObjectFactory objectFactory;
    protected ConfigurationProvider configurationProvider;

    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return true;
    }

    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        super.process(annotatedType, property, annotation, field, context);

        OptionFieldModel optionField = (OptionFieldModel) field;
        if (!optionField.isLazy())
        {
            OptionProvider optionProvider = OptionProviderFactory.build(annotatedType, property.getType(), annotation, objectFactory);
            process(property, optionField, optionProvider, context);
        }
    }

    protected void process(TypeProperty property, OptionFieldModel field, OptionProvider optionProvider, FormContext context)
    {
        List optionList = optionProvider.getOptions(property, context);
        field.setList(optionList);

        Object emptyOption = optionProvider.getEmptyOption(property, context);
        if (emptyOption != null)
        {
            field.setEmptyOption(emptyOption);
        }

        String key = optionProvider.getOptionValue();
        if (key != null)
        {
            field.setListValue(key);
        }

        String value = optionProvider.getOptionText();
        if (value != null)
        {
            field.setListText(value);
        }
    }


    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
