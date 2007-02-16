package com.zutubi.prototype.type;

import junit.framework.TestCase;

import java.util.List;

/**
 *
 *
 */
public class CompositeTypeTest extends TestCase
{
    public void testGetListProperties()
    {
        CompositeType type = new CompositeType(Mock.class);
        type.addProperty("list", new ListType(String.class), null, null);

        List<String> properties = type.getProperties(ListType.class);
        assertEquals(1, properties.size());
    }

    public static class Mock
    {
        private List<String> list;

        public List<String> getList()
        {
            return list;
        }

        public void setList(List<String> list)
        {
            this.list = list;
        }
    }
}
