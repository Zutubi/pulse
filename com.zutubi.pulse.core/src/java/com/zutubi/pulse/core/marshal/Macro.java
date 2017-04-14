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

package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.variables.api.Variable;
import nu.xom.Element;

/**
 * A macro in a pulse file, which associates a name with a fragment of XML.
 * This allows the same fragment to be referenced using macro-ref an
 * arbitrary number of times in the file.
 */
public class Macro implements Variable
{
    private String name;
    private Element element;

    public Macro(String name, Element element)
    {
        this.name = name;
        this.element = element;
    }

    public String getName()
    {
        return name;
    }

    public Element getValue()
    {
        return element;
    }
}
