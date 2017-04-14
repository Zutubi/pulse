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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * A file locator that combines the results of multiple child locators.
 */
public class CompositeFileLocator implements FileLocator
{
    private FileLocator[] locators;

    /**
     * Creates a locator with the given children.
     * 
     * @param locators child locators to collect results from
     */
    public CompositeFileLocator(FileLocator... locators)
    {
        this.locators = locators;
    }

    public List<File> locate()
    {
        List<File> result = new LinkedList<File>();
        for (FileLocator locator: locators)
        {
            result.addAll(locator.locate());
        }

        return result;
    }
}
