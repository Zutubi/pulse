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

package com.zutubi.pulse.master.committransformers;

/**
 * Represents a single subtitution to be made by a commit message transformer.
 */
public interface Substitution
{
    /**
     * Returns the regular expression used to match the text to transform.
     * 
     * @return a regular expression that will match the text to transform
     */
    String getExpression();

    /**
     * Returns the text used to replaced matched input.
     * 
     * @return a string used to replace matched input, which will usually
     *         contain references to to the input and/or groups from the
     *         expression
     */
    String getReplacement();
}
