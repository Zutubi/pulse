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

package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 * Abstract support class for implementing {@link CharHandler}.
 */
public abstract class CharHandlerSupport implements CharHandler
{
    private Charset charset;

    /**
     * Creates a handler with the default character set.
     */
    protected CharHandlerSupport()
    {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a handler with the specified character set for output byte to character conversion.
     * 
     * @param charset the character set that should be used to convert output before it is passed to
     *                this handler
     */
    protected CharHandlerSupport(Charset charset)
    {
        this.charset = charset;
    }

    /**
     * @return the character set that should be used to convert output before it is passed to this
     *         handler
     */
    public Charset getCharset()
    {
        return charset;
    }
}
