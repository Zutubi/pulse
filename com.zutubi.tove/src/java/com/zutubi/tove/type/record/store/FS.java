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

package com.zutubi.tove.type.record.store;

import java.io.File;
import java.io.IOException;

/**
 * A simple file system interface used by the file system record store to allow the
 * underlying file system to be replaced during testing. 
 *
 */
public interface FS
{
    boolean exists(File file);

    boolean createNewFile(File file) throws IOException;

    boolean mkdirs(File file);

    boolean delete(File file);

    boolean renameTo(File source, File destination);
}
