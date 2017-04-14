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

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.model.forms.FieldModel;
import com.zutubi.util.bean.BeanException;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.reflection.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Generic field handling which copies properties from the annotation to the field.
 */
public class FieldAnnotationHandler implements AnnotationHandler
{
    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return false;
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        Map<String, Object> map = AnnotationUtils.collectPropertiesFromAnnotation(annotation);
        for (Map.Entry<String, Object> entry: map.entrySet())
        {
            String name = entry.getKey();
            if (!name.equals("type"))
            {
                try
                {
                    BeanUtils.setProperty(name, entry.getValue(), field);
                }
                catch (BeanException e)
                {
                    field.addParameter(name, entry.getValue());
                }
            }
        }
    }
}
