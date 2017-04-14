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
 * Allows customisation of the ordering of nested properties when they are
 * listed.  The default is alphabetical listing by display name, but in some
 * cases it makes sense to prioritise differently.
 */
public @interface Listing
{
    /**
     * Specify a custom ordering of nested properties.
     *
     * Any properties not included in this ordering will be ordered
     * alphabetically by display name after the listed properties.
     *
     * @return an array of property names defining the order in which they
     *         are listed
     */
    String[] order();
}
