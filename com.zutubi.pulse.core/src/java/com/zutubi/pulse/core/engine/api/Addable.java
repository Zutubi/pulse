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
 * Used to mark a collection field as addable when binding text to
 * configuration objects.  For example, if your configuration type has a
 * property widgets of type Map&lt;String, Widget&gt;, you can annotate
 * that property as @Addable("widget"):
 *
 * <pre>{@code
 * public class MyConfig extends AbstractConfiguration
 * {
 *     \@Addable("widget")
 *     private Map<String, Widget> widgets;
 * }
 * }</pre>
 *
 * Then when binding from an XML form, a nested &lt;widget&gt; tag is
 * understood to define a widget instance which should be added to the widgets
 * collection:
 *
 * <pre>{@code
 * <myconfig>
 *     <widget>...</widget>
 *     <widget>...</widget>
 * </myconfig>
 * }</pre>
 *
 * <p/>
 *
 * If the collection field contains simple values (including references),
 * then {@link #attribute()} gives the name of the attribute that will
 * contain the value or referencing text.  For example, with reference()
 * set to "ref" (the default):
 *
 * <pre>{@code
 * public class MyConfig extends AbstractConfiguration
 * {
 *     \@Addable(value = "widget", ref = "ref") @Reference
 *     private Map<String, Widget> widgets;
 * }
 * }</pre>
 *
 * Then widget tags would refer to widgets like:
 *
 * <pre>{@code
 * <myconfig>
 *     <widget ref="${widget.one}"/>
 *     <widget ref="${widget.two}"/>
 * </myconfig>
 * }</pre>
 * 
 * If attribute() is empty, then the nested text between the tags is used:
 *
 * <pre>{@code
 * <myconfig>
 *     <widget>${widget.one}</widget>
 *     <widget>${widget.two}</widget>
 * </myconfig>
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Addable
{
    /**
     * Defines the name used to represent instances that can be added
     * to this collection property  (e.g. the XML tag name).
     * 
     * @return the name for defining an instance to be added
     */
    String value();

    /**
     * Defines the attribute used to refer to a widget when the target
     * collection holds references rather than values.  May be empty to
     * indicate that nested text content should be used.
     *
     * @return the name of the referencing attribute
     */
    String attribute() default "ref";
}
