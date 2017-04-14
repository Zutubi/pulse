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

package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a custom script for a form field.  When the field is added to a form, the field
 * script is evaluated.  If it evaluates to a function that function is invoked with the form
 * and field. Note the function must be wrapped in parentheses.
 * <p/>
 * A typical script will bind a handler to process an action:
 * <p/>
 * <code><pre>   (function(form, field)
 * {
 *   form.bind('action', function(e)
 *   {
 *     if (e.field !== field || e.action !== 'my-action') return;
 *
 *     // Handle action 'my-action' here.
 *   }
 * })
 * </pre></code>
 * <p/>
 * The action could itself be a custom button added via a {@link FieldAction} annotation.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Handler(className = DefaultAnnotationHandlers.FIELD_SCRIPT)
public @interface FieldScript
{
    /**
     * @return the name of the template used to render the link's associated
     * code.
     */
    String template() default "";
}
