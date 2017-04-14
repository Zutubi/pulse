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

package com.zutubi.tove.ui.model;

import java.util.Set;

/**
 * Model for validating configuration.
 */
public class ValidateModel
{
    private String baseName;
    private boolean concrete;
    private Set<String> ignoredFields;
    private CompositeModel composite;

    public String getBaseName()
    {
        return baseName;
    }

    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public void setConcrete(boolean concrete)
    {
        this.concrete = concrete;
    }

    public Set<String> getIgnoredFields()
    {
        return ignoredFields;
    }

    public void setIgnoredFields(Set<String> ignoredFields)
    {
        this.ignoredFields = ignoredFields;
    }

    public CompositeModel getComposite()
    {
        return composite;
    }

    public void setComposite(CompositeModel composite)
    {
        this.composite = composite;
    }
}
