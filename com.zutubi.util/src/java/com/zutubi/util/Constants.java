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

package com.zutubi.util;

import java.util.Date;

/**
 * A namespace for constant values.
 */
public class Constants
{
    public static final long MEGABYTE = 1048576;

    public static final long MILLISECOND = 1;
    public static final long SECOND      = 1000 * MILLISECOND;
    public static final long MINUTE      = 60 * SECOND;
    public static final long HOUR        = 60 * MINUTE;
    public static final long DAY         = 24 * HOUR;
    public static final long WEEK        = 7 * DAY;
    public static final long YEAR        = 365 * DAY;

    public static final Date DAY_0 = new Date(0);
}
