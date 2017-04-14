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

package com.zutubi.pulse.master.build.log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that writes directly to an output log.
 */
public class OutputLoggerOutputStream extends OutputStream
{
    private OutputLogger logger;

    public OutputLoggerOutputStream(OutputLogger logger)
    {
        this.logger = logger;
    }

    protected void sendEvent(byte[] sendBuffer)
    {
        logger.log(sendBuffer);
    }

    public void write(int b) throws IOException
    {
        logger.log(new byte[]{(byte)b});
    }

    public void write(byte[] source, int offset, int length) throws IOException
    {
        logger.log(source, offset, length);
    }
}
