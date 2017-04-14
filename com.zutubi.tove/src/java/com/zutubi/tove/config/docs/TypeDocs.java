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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds documentation for a single type in a form that can be rendered for
 * various UIs.
 */
public class TypeDocs implements Docs
{
    private String title;
    private String brief;
    private String verbose;
    private List<PropertyDocs> properties;

    public TypeDocs()
    {
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
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

    public List<PropertyDocs> getProperties()
    {
        return properties == null ? null : Collections.unmodifiableList(properties);
    }

    public PropertyDocs getPropertyDocs(final String fieldName)
    {
        return Iterables.find(properties, new Predicate<PropertyDocs>()
        {
            @Override
            public boolean apply(PropertyDocs input)
            {
                return input.getName().equals(fieldName);
            }
        }, null);
    }

    public void addProperty(PropertyDocs property)
    {
        if (properties == null)
        {
            properties = new ArrayList<>();
        }

        properties.add(property);
    }
}
