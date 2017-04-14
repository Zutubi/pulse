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
 * Associates an explicit state display class for some configuration class.
 * Normally state display classes are looked up by the convention that they
 * use the same name as the configuration class with a "StateDisplay" suffix.
 * Where this is not desired, however, this annotation may be used to link an
 * arbitrary class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StateDisplay
{
    /**
     * Specifies the name of the state display class, either as a simple name
     * if the class is in the same package as the configuration class, or as a
     * fully-qualified name.
     *
     * @return name of the state displat class
     */
    String value();
}
