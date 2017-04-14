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

import java.io.OutputStream;
import java.io.IOException;

/**
 * An output stream that forwards all writes to multiple output streams.
 */
public class ForkOutputStream extends OutputStream
{
    private OutputStream[] streams;

    public ForkOutputStream(OutputStream... streams)
    {
        this.streams = streams;
    }

    public void write(int b) throws IOException
    {
        for(OutputStream o: streams)
        {
            o.write(b);
        }
    }

    @Override
    public void write(byte b[]) throws IOException
    {
        for(OutputStream o: streams)
        {
            o.write(b);
        }
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException
    {
        for(OutputStream o: streams)
        {
            o.write(b, off, len);
        }
    }

    @Override
    public void flush() throws IOException
    {
        for(OutputStream o: streams)
        {
            o.flush();
        }
    }

    @Override
    public void close() throws IOException
    {
        for(OutputStream o: streams)
        {
            o.close();
        }
    }
}
