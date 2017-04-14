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

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A select field that controls dependent fields.  When the selected value is
 * <b>not</b> in a given set, dependent fields will be disabled and ignored
 * during validation.  If no dependent fields are specified, the select box
 * controls all other fields in the form.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.CONTROLLING_SELECT)
@Constraint("com.zutubi.tove.validation.ControllingSelectValidator")
@Handler(className = DefaultAnnotationHandlers.OPTION)
public @interface ControllingSelect
{
    String optionProvider() default "";

    String[] enableSet() default {};

    String[] dependentFields() default {};
}
