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
 * This annotation indicates that the marked property should
 * itself be validated.
 * <p>
 * For example, by marking the door property with the validate annotation,
 * each of the properties on the door will also be validated as part of the
 * validation of the house. That is, the house will not be considered valid
 * unless the door is also valid.  Without the validate annotation, the
 * door would be ignored.
 * </p>
 * 
 * <pre><code> public class House
 * {
 *     &#064;Validate
 *     public Door getDoor()
 *     {
 *
 *     }
 * }</code></pre>
 */
@Constraint("com.zutubi.validation.validators.DelegateValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;
}
