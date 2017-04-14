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
 * This annotation marks the field as a numeric field.  Optionally, the
 * numeric value can be constrained by the min and max constraints.
 * <p>
 * This annotation can be applied to integral and string fields.
 * <p>
 * For example:
 *
 * <pre><code>&#064;Numeric(min = 0, max = 100)
 * public String getNumber()
 * {
 *     return "093";
 * }</code></pre>
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric
{
    String DEFAULT_defaultKeySuffix = "";

    boolean DEFAULT_shortCircuit = true;

    int DEFAULT_max = Integer.MAX_VALUE;

    int DEFAULT_min = Integer.MIN_VALUE;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    int max() default DEFAULT_max;

    int min() default DEFAULT_min;
}


