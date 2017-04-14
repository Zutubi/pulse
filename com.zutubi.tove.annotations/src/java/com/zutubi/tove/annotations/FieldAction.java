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
 * Used to add a custom action to a form field.  Actions are rendered as buttons after the field.
 * When the button is clicked an "action" event is raised on the form with the field and action
 * name.  A template can be associated with the link to handle this event.  See {@link FieldScript}
 * for information about the template.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Handler(className = DefaultAnnotationHandlers.FIELD_ACTION)
public @interface FieldAction
{
    /**
     * @return the name of the template used to render the link's associated
     * code.
     */
    String template() default "";

    /**
     * @return the i18n key for the link.
     */
    String actionKey() default "browse";

    /**
     * @return An optional class used to filter the presence of this action
     *         based on context (by default the action is always shown).  The
     *         class must implement
     *         com.zutubi.pulse.master.tove.handler.FieldActionPredicate.
     */
    String filterClass() default "";
}
