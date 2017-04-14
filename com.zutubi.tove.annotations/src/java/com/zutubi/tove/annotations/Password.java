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

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

// This annotation is a form field of type PASSWORD.
@Field(type = FieldType.PASSWORD)

/**
 * The password annotation allows you to mark a property for display as a simple form password field.
 * 
 */
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface Password
{
    /**
     * The DEFAULT_size indicates that, by default, no size value will be rendered.
     */
    int DEFAULT_size = 0;

    /**
     * The size of the rendered password field.
     *
     * @return number of columns to be displayed.
     */
    int size() default DEFAULT_size;

    /**
     * Indicates whether or not the contents of the password field should be shown as '*'s.
     *
     * This field defaults to true.
     *
     * @return the true if the password should be shown, false otherwise.
     */
    boolean showPassword() default true;

}
