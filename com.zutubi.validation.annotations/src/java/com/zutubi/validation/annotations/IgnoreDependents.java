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

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The IgnoreDependents annotation should be added to fields that are used
 * to toggle the availability of a set of fields via the interface.  In doing
 * so, when the fields are 'disabled', the validation requirements of those
 * dependent fields can be ignored.
 */
@Constraint("com.zutubi.validation.validators.IgnoreDependentsFieldValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreDependents
{
    /**
     * @return field value which will cause dependents to <b>not</b> be
     *         ignored.  This value will be compared with the value of the
     *         field converted to a string with toString().
     */
    String nonIgnoreValue() default "true";

    String[] dependentFields() default {};
}
