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

import com.zutubi.pulse.core.api.PulseException;

/**
 * 
 *
 */
public class ParseException extends PulseException
{
    private int line;
    private int column;

    /**
     * @param errorMessage
     */
    public ParseException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public ParseException()
    {
        super();
    }

    /**
     * @param cause
     */
    public ParseException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public ParseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }

    public ParseException(int line, int column, String message)
    {
        super(message);
        this.line = line;
        this.column = column;
    }

    public ParseException(int line, int column, Throwable cause)
    {
        super(cause);
        this.line = line;
        this.column = column;
    }

    public int getLine()
    {
        return line;
    }

    public int getColumn()
    {
        return column;
    }
}
