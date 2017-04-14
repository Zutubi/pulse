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

import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.model.forms.FieldModel;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.tove.annotations.FieldAction} annotation.
 */
public class FieldActionAnnotationHandler implements AnnotationHandler
{
    private static final Logger LOG = Logger.getLogger(FieldActionAnnotationHandler.class);

    private Configuration freemarkerConfiguration;
    private ObjectFactory objectFactory;

    @Override
    public boolean requiresContext(Annotation annotation)
    {
        FieldAction fieldAction = (FieldAction) annotation;
        return StringUtils.stringSet(fieldAction.filterClass());
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        FieldAction fieldAction = (FieldAction) annotation;
        if (StringUtils.stringSet(fieldAction.filterClass()))
        {
            Class<Object> filterClass = ClassLoaderUtils.loadAssociatedClass(annotatedType.getClazz(), fieldAction.filterClass());
            if (!satisfied(filterClass, field, fieldAction, context))
            {
                return;
            }
        }

        field.addAction(fieldAction.actionKey());
        FieldScriptAnnotationHandler.loadTemplate(annotatedType.getClazz(), field, fieldAction.template(), freemarkerConfiguration);
    }

    private boolean satisfied(Class<Object> filterClass, FieldModel field, FieldAction fieldAction, FormContext context)
    {
        if (!FieldActionPredicate.class.isAssignableFrom(filterClass))
        {
            LOG.warning("Field action filter class '" + filterClass.getName() + "' does not implement FieldActionPredicate: ignoring");
            return true;
        }

        FieldActionPredicate predicate = (FieldActionPredicate) objectFactory.buildBean(filterClass);
        return predicate.satisfied(field, fieldAction, context);
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
