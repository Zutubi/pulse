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

package com.zutubi.tove.squeezer;

/**
 * Interface for squeezers: classes that can convert between objects and
 * strings.
 */
public interface TypeSqueezer
{
    /**
     * Converts the given object to a string representation.
     *
     * @param obj the object to convert
     * @return a string representation of the object
     * @throws SqueezeException if the object cannot be converted
     */
    String squeeze(Object obj) throws SqueezeException;

    /**
     * Converts the given string representation to the corresponding object.
     *
     * @param str the string to convert
     * @return the object represented by the string
     * @throws SqueezeException if the string cannot be converted
     */
    Object unsqueeze(String str) throws SqueezeException;
}
