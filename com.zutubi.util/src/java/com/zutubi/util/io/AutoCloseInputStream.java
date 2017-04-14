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
import java.io.InputStream;

/**
 * An input stream that wraps a delegate and closes it when there is no more data to read.
 */
public class AutoCloseInputStream extends InputStream
{
    private InputStream delegate;
    private boolean closed = false;

    public AutoCloseInputStream(InputStream delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException
    {
        int result = -1;
        if (!closed)
        {
            result = delegate.read();
            if (result == -1)
            {
                close();
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException
    {
        if (!closed)
        {
            closed = true;
            delegate.close();
        }
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return delegate.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException
    {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException
    {
        return delegate.available();
    }

    @Override
    public void mark(int readlimit)
    {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException
    {
        delegate.reset();
    }

    @Override
    public boolean markSupported()
    {
        return delegate.markSupported();
    }
}
