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

package com.zutubi.util.io;

import java.io.*;

/**
 * A file input stream that deletes the file when it is closed.
 */
public class TempFileInputStream extends FileInputStream
{
    private File file;

    public TempFileInputStream(String name) throws FileNotFoundException
    {
        super(name);
        file = new File(name);
    }

    public TempFileInputStream(File file) throws FileNotFoundException
    {
        super(file);
        this.file = file;
    }

    public TempFileInputStream(FileDescriptor fdObj)
    {
        super(fdObj);
        throw new UnsupportedOperationException("File name required for cleanup");
    }


    public void close() throws IOException
    {
        super.close();
        file.delete();
    }
}
