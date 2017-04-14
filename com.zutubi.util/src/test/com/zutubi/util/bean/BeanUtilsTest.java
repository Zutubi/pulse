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

package com.zutubi.util.bean;

import com.zutubi.util.RandomUtils;
import com.zutubi.util.junit.ZutubiTestCase;

public class BeanUtilsTest extends ZutubiTestCase
{
    public void testGetProperty() throws BeanException
    {
        String value = RandomUtils.insecureRandomString(5);

        Bean target = new Bean();
        target.setProperty(value);

        assertEquals(value, BeanUtils.getProperty("property", target));
    }

    public void testSetProperty() throws BeanException
    {
        String value = RandomUtils.insecureRandomString(5);
        Bean target = new Bean();
        BeanUtils.setProperty("property", value, target);
        assertEquals(value, target.getProperty());
    }

    private class Bean
    {
        private String property;

        public String getProperty()
        {
            return property;
        }

        public void setProperty(String property)
        {
            this.property = property;
        }
    }
}
