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

package com.zutubi.pulse.master.logging;

import com.zutubi.util.SystemUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Provides a very lightweight formatting for the config audt logs.
 */
public class ConfigAuditLogFormatter extends Formatter
{
    private StringBuilder builder = new StringBuilder();
    private Date date = new Date();

    public synchronized String format(LogRecord record)
    {
        builder.delete(0, builder.length());

        date.setTime(record.getMillis());
        builder.append(DateFormat.getDateTimeInstance().format(date));
        builder.append(": ");
        builder.append(record.getMessage());
        builder.append(SystemUtils.LINE_SEPARATOR);
        return builder.toString();
    }
}
