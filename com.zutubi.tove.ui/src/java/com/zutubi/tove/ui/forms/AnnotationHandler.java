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

import java.lang.annotation.Annotation;

/**
 * Handlers are used to take information from annotations and apply them to fields. This allows
 * fields to be configured by annotating the corresponding property without the form building code
 * knowing the details.
 * <p/>
 * Handlers may be able to work with only static/type information, or may require more context
 * (e.g. the instance being configured).  See {@link #requiresContext(Annotation)} for details.
 */
public interface AnnotationHandler
{
    /**
     * Indicates if this handler can run with only static information about the field, or whether
     * it needs more context information.  If context is not required the handler will process
     * all forms with a null context.  It context is required the handler will only process forms
     * where we know more about what is being configured (e.g. an existing instance).
     *
     * @param annotation annotation instance that links to this handler
     * @return true iff this handler requires context to process a field
     */
    boolean requiresContext(Annotation annotation);

    void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception;
}
