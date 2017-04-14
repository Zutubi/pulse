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
import com.zutubi.tove.type.EnumType;
import com.zutubi.tove.type.Type;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Factory for building {@link com.zutubi.pulse.master.tove.handler.OptionProvider}
 * instances.
 */
public class OptionProviderFactory
{
    private static final String PROVIDER_ANNOTATION_PROPERTY = "optionProvider";

    /**
     * Builds an option provider for the given property of the given type.
     *
     * @param annotatedType the type that holds the property
     * @param propertyType  the property to build a provider for
     * @param annotation    option-related annotation on the property, must
     *                      have an optionProvider property
     * @param objectFactory factory used to build wred instances
     * @return the new provider
     * @throws Exception
     */
    public static OptionProvider build(CompositeType annotatedType, Type propertyType, Annotation annotation, ObjectFactory objectFactory) throws Exception
    {
        OptionProvider optionProvider;

        String className = getOptionProviderClass(annotation);
        if (!StringUtils.stringSet(className))
        {
            if (propertyType instanceof EnumType)
            {
                optionProvider = new EnumOptionProvider();
            }
            else
            {
                optionProvider = new EmptyOptionProvider();
            }
        }
        else
        {
            optionProvider = (OptionProvider) objectFactory.buildBean(ClassLoaderUtils.loadAssociatedClass(annotatedType.getClazz(), className));
        }

        return optionProvider;
    }

    private static String getOptionProviderClass(Annotation annotation) throws Exception
    {
        Method method = annotation.getClass().getMethod(PROVIDER_ANNOTATION_PROPERTY);
        return (String) method.invoke(annotation);
    }
}
