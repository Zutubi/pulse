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

package com.zutubi.pulse.core.marshal.doc;

/**
 * Indicates what number of child elements may appear in a specific parent
 * element.
 */
public enum Arity
{
    ZERO_OR_ONE
    {
        public String shortForm()
        {
            return "0 or 1";
        }
    },

    EXACTLY_ONE
    {
        public String shortForm()
        {
            return "1";
        }
    },

    ONE_OR_MORE
    {
        public String shortForm()
        {
            return "1 or more";
        }
    },

    ZERO_OR_MORE
    {
        public String shortForm()
        {
            return "0 or more";
        }
    };
    
    public abstract String shortForm();
}
