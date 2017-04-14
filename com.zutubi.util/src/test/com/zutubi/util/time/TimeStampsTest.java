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

package com.zutubi.util.time;

import com.zutubi.util.Constants;
import com.zutubi.util.junit.ZutubiTestCase;

/**
 *
 *
 */
public class TimeStampsTest extends ZutubiTestCase
{
    public void testPrettyEstimated()
    {
        assertEquals("< 1 minute", TimeStamps.getPrettyEstimated(1));
        assertEquals("About 1 minute", TimeStamps.getPrettyEstimated(Constants.MINUTE));
        assertEquals("About 1 minute", TimeStamps.getPrettyEstimated(Constants.MINUTE * 1 + 1));
        assertEquals("About 2 minutes", TimeStamps.getPrettyEstimated(Constants.MINUTE * 2 + 1));
        assertEquals("About 54 minutes", TimeStamps.getPrettyEstimated(Constants.MINUTE * 54));
        assertEquals("< 1 hour", TimeStamps.getPrettyEstimated(Constants.MINUTE * 56));
    }
}
