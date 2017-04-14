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

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that wraps an existing stream and forwards all calls to
 * it except {@link #close}, which is ignored.
 */
public class IgnoreCloseOutputStream extends OutputStream
{
    private OutputStream delegate;

    public IgnoreCloseOutputStream(OutputStream delegate)
    {
        this.delegate = delegate;
    }


    public void write(int b) throws IOException
    {
        delegate.write(b);
    }

    public void write(byte[] b) throws IOException
    {
        delegate.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        delegate.write(b, off, len);
    }

    public void flush() throws IOException
    {
        delegate.flush();
    }

    public void close() throws IOException
    {
        // Ignored, of course.
    }
}
