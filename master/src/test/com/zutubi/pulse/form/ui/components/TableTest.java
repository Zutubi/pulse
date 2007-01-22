package com.zutubi.pulse.form.ui.components;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TableTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        Table table = new Table();
        table.addColumn(new Column("a"));
        table.addColumn(new Column("b"));
        table.setName("data");

        List<Object> data = new ArrayList<Object>();
        data.add(new DummyData());
        data.add(new DummyData());

        context.push(new DataProvider(data));

        table.render(context, templateRenderer);

        String content = writer.toString();
        System.out.println(content);
    }

    private class DataProvider
    {
        List<Object> data;

        public DataProvider(List<Object> data)
        {
            this.data = data;
        }

        public List<Object> getData()
        {
            return data;
        }
    }

    private class DummyData
    {
        private String a = "a";
        private String b = "b";

        public String getA()
        {
            return a;
        }

        public void setA(String a)
        {
            this.a = a;
        }

        public String getB()
        {
            return b;
        }

        public void setB(String b)
        {
            this.b = b;
        }
    }
}
