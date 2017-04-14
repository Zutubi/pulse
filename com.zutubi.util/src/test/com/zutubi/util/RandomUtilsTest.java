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

import com.zutubi.util.junit.ZutubiTestCase;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 *
 *
 */
public class RandomUtilsTest extends ZutubiTestCase
{
    public void testRandomnessOfSecureRandom() throws NoSuchAlgorithmException
    {
        Random a = new Random(0);
        Random b = new Random(0);
        assertTrue(a != b);
        assertEquals(RandomUtils.randomString(a, 10), RandomUtils.randomString(b, 10));

        Random sa = SecureRandom.getInstance("SHA1PRNG");
        sa.setSeed(0);
        Random sb = SecureRandom.getInstance("SHA1PRNG");
        sb.setSeed(0);
        
        assertTrue(sa != sb);
        assertTrue(RandomUtils.randomString(sa, 10).compareTo(RandomUtils.randomString(sb, 10)) != 0);
    }
}
