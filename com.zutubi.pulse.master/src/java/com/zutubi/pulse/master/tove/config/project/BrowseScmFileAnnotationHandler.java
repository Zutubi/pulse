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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.AnnotationHandler;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.model.forms.FieldModel;
import com.zutubi.util.StringUtils;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction} annotation.
 */
public class BrowseScmFileAnnotationHandler implements AnnotationHandler
{
    private static final String PARAMETER_BASE_DIR = "baseDirField";

    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return false;
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        BrowseScmFileAction action = (BrowseScmFileAction) annotation;
        if (StringUtils.stringSet(action.baseDirField()))
        {
            field.addParameter(PARAMETER_BASE_DIR, action.baseDirField());
        }
    }
}
