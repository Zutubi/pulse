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

package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.util.logging.Logger;

import java.io.OutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * <class comment/>
 */
public class LoggingOutputStream extends OutputStream
{
    private Logger delegate;
    private Level level;

    private StringBuilder builder;

    private final Object lock = new Object();

    public LoggingOutputStream(Logger delegate, Level level)
    {
        this.delegate = delegate;
        this.level = level;

        builder = new StringBuilder();
    }

    public void write(int b) throws IOException
    {
        synchronized(lock)
        {
            builder.append(new String(new byte[]{(byte)b}));
        }
    }

    public void flush() throws IOException
    {
        String msg;
        synchronized(lock)
        {
            msg = builder.toString();
            builder = new StringBuilder();
        }
        delegate.log(level, msg);
    }
}
