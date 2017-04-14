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

/**
 * Model for requesting available options for a property of a composite. Note that the composite
 * may not yet exist, in which case the baseName will not be set.
 */
public class OptionsModel
{
    private String baseName;
    private String symbolicName;
    private String propertyName;
    private String scopePath;

    public String getBaseName()
    {
        return baseName;
    }

    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

    /**
     * @return path of the scope instance to search for options from: used for looking up referenceable instances when
     *         a dependentOn field is specified in the {@link @Reference} (see
     *         {@link com.zutubi.tove.ui.forms.ReferenceAnnotationHandler} and the trackdependent script).
     */
    public String getScopePath()
    {
        return scopePath;
    }

    public void setScopePath(String scopePath)
    {
        this.scopePath = scopePath;
    }
}
