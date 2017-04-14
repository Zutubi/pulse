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

package com.zutubi.tove.config.api;

/**
 * Interface to be implemented by classes used to classify configuration
 * instances.  A classifier by convention has the same name as the
 * configuration class it works on, with a "Classifier" suffix added.  In many
 * cases the {@link com.zutubi.tove.annotations.Classification} annotation may
 * be used instead of a classifier.  However, when you need to return different
 * classes for instances of the same type, based on their state, you need to
 * implement a classifier.
 */
public interface Classifier<T extends Configuration>
{
    /**
     * Returns the class that the given instance belongs to.  This class can be
     * used by user interfaces to distinguish the instance (e.g. give it a
     * unique graphical representation).
     *
     * @param instance instance to classify
     * @return name of the class that the instance belongs to
     */
    String classify(T instance);
}
