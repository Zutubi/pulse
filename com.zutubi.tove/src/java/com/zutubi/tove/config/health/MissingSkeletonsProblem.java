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

package com.zutubi.tove.config.health;

/**
 * Identifies that skeleton records are missing.  This occurs where records are
 * present in the template parent, but absent in the child (and not hidden).
 */
public class MissingSkeletonsProblem extends MismatchedTemplateStructureProblem
{
    /**
     * Creates a new problem indicating missing skeletons at the given key of
     * the given path.
     *
     * @param path               path of the record the skeletons should be
     *                           under
     * @param message            description of this problem
     * @param key                key where the skeletons should be
     * @param templateParentPath path of the template parent of the record the
     *                           skeletons should be under
     */
    public MissingSkeletonsProblem(String path, String message, String key, String templateParentPath)
    {
        super(path, message, key, templateParentPath);
    }

}
