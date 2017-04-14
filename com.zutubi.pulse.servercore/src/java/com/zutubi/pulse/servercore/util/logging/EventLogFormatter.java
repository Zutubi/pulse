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

package com.zutubi.pulse.servercore.util.logging;

import com.zutubi.util.SystemUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * The formatter that is used to control the formatting of what is
 * written to the systems event log.
 *
 * @see Formatter
 */
public class EventLogFormatter extends Formatter
{
    private final static String FORMAT = "{0,date} {0,time}";

    private Date date = new Date();
    private MessageFormat formatter = new MessageFormat(FORMAT);

    private Object args[] = new Object[1];

    public String format(LogRecord record)
    {
        StringBuilder builder = new StringBuilder();

        // Minimize memory allocations here.
        date.setTime(record.getMillis());
        args[0] = date;

        StringBuffer formatBuffer = new StringBuffer();
        formatter.format(args, formatBuffer, null);
        builder.append(formatBuffer);
        builder.append(": ");
        String message = formatMessage(record);
        builder.append(message);
        builder.append(SystemUtils.LINE_SEPARATOR);
        return builder.toString();
    }
}
