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

package com.zutubi.pulse.core.marshal;

import java.io.InputStream;

/**
 * Interface for resolution of a file path to an input stream yielding that
 * file's contents.
 */
public interface FileResolver
{
    /**
     * Resolves a path to a file to a stream that yields the file content.
     * The returned stream must be closed by the caller.
     *
     * @param path path of the file to resolve
     * @return an input stream open at the start of the file content
     * @throws Exception on any error
     */
    InputStream resolve(String path) throws Exception;
}
