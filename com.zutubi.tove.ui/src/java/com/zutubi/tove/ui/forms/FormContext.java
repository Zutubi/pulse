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

package com.zutubi.tove.ui.forms;

import com.zutubi.tove.config.api.Configuration;

/**
 * Context information available while building a form.  Note that sometimes no existing
 * instance is available, only a closest existing path.  For forms modealling transient
 * types the closest existing path is null too.
 */
public class FormContext
{
    private Configuration existingInstance;
    private String closestExistingPath;

    public FormContext(Configuration existingInstance)
    {
        this.existingInstance = existingInstance;
        closestExistingPath = existingInstance.getConfigurationPath();
    }

    public FormContext(String closestExistingPath)
    {
        this.closestExistingPath = closestExistingPath;
    }

    /**
     * @return the existing (i.e. already persistent) instance the form is modelling, null if the
     *         form is being used to create a new instance or configure a transient type.
     */
    public Configuration getExistingInstance()
    {
        return existingInstance;
    }

    /**
     * Indicates the closest existing record of configuration to what the form is modelling, or
     * null if the form is modelling a transient type.
     *
     * @return the longest subpath to an existing configuration instance containing what we are
     *         configuring with this form.  When there is an existing instance it is the path of
     *         that instance, otherwise it is a parent (or higher ancestor) path that we are about
     *         to insert something into.  When the form models a transient type the path is null.
     */
    public String getClosestExistingPath()
    {
        return closestExistingPath;
    }
}
