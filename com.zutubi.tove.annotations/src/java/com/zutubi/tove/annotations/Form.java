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

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

/**
 * The form annotation allows for some level of customisation of the rendering of the form
 * that is generated for the annotated object.
 */
public @interface Form
{
    /**
     * Specify a custom ordering of the forms fields.
     *
     * Any fields not included in this ordering will be ordered arbitrarily.
     *
     * @return an array of field names defining the order in which they layed out
     * in the form.
     */
    String[] fieldOrder();
}
