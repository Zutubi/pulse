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
 * A description of how text content nested within an element is used.  For
 * most elements, there is no such documentation as nested text is not
 * supported, but in some cases the text is bound to a simple property or
 * used in lieu of an attribute for addable simple lists.
 */
public class ContentDocs
{
    private String verbose;

    public ContentDocs(String verbose)
    {
        this.verbose = verbose;
    }

    public String getVerbose()
    {
        return verbose;
    }
}
