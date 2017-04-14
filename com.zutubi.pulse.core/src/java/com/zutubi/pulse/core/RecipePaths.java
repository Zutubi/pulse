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

package com.zutubi.pulse.core;

import java.io.File;
import java.util.Map;

/**
 * The RecipePaths interface provides an interface that provides access to a recipes
 * work and output directories.
 */
public interface RecipePaths
{
    /**
     * @return the directory where a bootstrap checkout should take place, or
     * null if no checkout should be performed
     */
    File getCheckoutDir();

    /**
     * The base directory is the root directory for execution of a recipe.
     *
     * @return the base directory
     */
    File getBaseDir();

    /**
     * The output directory is the directory that contains all of the recipe processing
     * output. This includes all of the recipe artifacts and various log files generated
     * by the recipe execution.
     * <p/>
     * Everything in the output directory is archived so that it can be used later.
     *
     * @return the output directory.
     */
    File getOutputDir();

    /**
     * @return a set of path properties to be exposed to the recipe, e.g. base.dir, data.dir
     */
    Map<String,String> getPathProperties();
}
