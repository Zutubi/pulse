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

package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ConstraintProperty annotation can be used in conjunction with the
 * {@code com.zutubi.validation.annotations.Constraint} to configure the underlying
 * validator.
 * <p>
 * In the following example, the Max annotation is marked with the Constraint annotation
 * and has its value mapped to the validator's max property.
 *
 * <pre><code> &#064;Constraint("com.zutubi.validation.validators.NumericValidator")
 * public &#064;interface Max
 * {
 *    &#064;ConstraintProperty("max") int value();
 * }</code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConstraintProperty
{
    /**
     * The name of the property to which the annotated value will be mapped.
     *
     * @return property name.
     */
    public String value();
}
