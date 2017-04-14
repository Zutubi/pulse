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

package com.zutubi.pulse.core.resources.api;

import java.util.LinkedList;
import java.util.List;

/**
 * A resource locator that combines the resources of multiple child locators.
 */
public class CompositeResourceLocator implements ResourceLocator
{
    private ResourceLocator[] locators;

    /**
     * Creates a new locator with the given children.
     * 
     * @param locators set of locators to run
     */
    public CompositeResourceLocator(ResourceLocator... locators)
    {
        this.locators = locators;
    }

    public List<ResourceConfiguration> locate()
    {
        List<ResourceConfiguration> result = new LinkedList<ResourceConfiguration>();
        for (ResourceLocator locator: locators)
        {
            result.addAll(locator.locate());
        }

        return result;
    }
}
