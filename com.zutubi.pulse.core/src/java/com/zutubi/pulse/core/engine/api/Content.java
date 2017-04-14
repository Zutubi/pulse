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
 * Used to mark a text property as settable using the content contained within
 * XML tags.  When binding XML to objects, if text content is found between
 * tags it will be used to set a property with this annotation (if any).
 *
 * <p/>
 *
 * For example, given this configuration class:
 * <pre>{@code
 * public class MyConfig extends AbstractConfiguration
 * {
 *     \@Content
 *     private String text;
 * }
 * }</pre>
 *
 * The given XML would set the "text" property to "value":
 * 
 * <pre>{@code
 * <myconfig>value</myconfig>
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Content
{
}