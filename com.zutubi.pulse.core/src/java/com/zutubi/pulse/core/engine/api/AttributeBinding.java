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

package com.zutubi.pulse.core.engine.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used to control how an attribute binds to a property.  This
 * annotation is rarely necessary, as the default binding behaviour should
 * always be preferred.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AttributeBinding
{
    /**
     * When the annotated property is bound to a list, this setting determines
     * if the attribute value is split on spaces or not.  If true, the value is
     * split on spaces to give a list.  If false, the value is not split, but
     * treated as a single-element list.
     * 
     * The default, to split, should be used except where compatibility must be
     * maintained (e.g. the property used to be a single string rather than a
     * list of strings, and that single string may have contained spaces).
     * 
     * @return true to split the value when binding the attribute to a list
     */
    boolean split() default true;
}
