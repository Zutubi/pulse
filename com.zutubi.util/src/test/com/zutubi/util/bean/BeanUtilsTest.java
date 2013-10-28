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
