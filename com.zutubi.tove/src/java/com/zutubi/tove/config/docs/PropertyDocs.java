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

package com.zutubi.tove.config.docs;

import com.opensymphony.util.TextUtils;
import com.zutubi.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds documentation for a single field of a type.  Only simple fields are
 * documented: complex/nested fields are represented by their own
 * {@link TypeDocs}.
 */
public class PropertyDocs implements Docs
{
    private static final int BRIEF_LENGTH_LIMIT = 100;

    private String name;
    private String label;
    private String brief;
    private String verbose;
    private List<Example> examples;

    public PropertyDocs(String name)
    {
        this.name = name;
    }

    public PropertyDocs(String name, String label, String brief, String verbose)
    {
        this.name = name;
        this.label = label;
        this.brief = brief;
        this.verbose = verbose;
    }

    public static PropertyDocs createFromUnencodedString(String name, String label, String verbose)
    {
        return new PropertyDocs(name, label, TextUtils.htmlEncode(StringUtils.trimmedString(verbose, BRIEF_LENGTH_LIMIT)), TextUtils.htmlEncode(verbose));
    }

    public String getName()
    {
        return name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getBrief()
    {
        return brief;
    }

    public void setBrief(String brief)
    {
        this.brief = brief;
    }

    public String getVerbose()
    {
        return verbose;
    }

    public void setVerbose(String verbose)
    {
        this.verbose = verbose;
    }

    public List<Example> getExamples()
    {
        return examples;
    }

    public void addExample(Example example)
    {
        if (examples == null)
        {
            examples = new ArrayList<>();
        }

        examples.add(example);
    }
}
