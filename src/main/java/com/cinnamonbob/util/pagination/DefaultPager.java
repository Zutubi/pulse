package com.cinnamonbob.util.pagination;

import java.util.ArrayList;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultPager implements prototype.pagination.Pager
{
    private final List data;

    private int pageSize = 10;

    public DefaultPager(List data)
    {
        this.data = data;
    }

    public DefaultPager(Object[] data)
    {
        this.data = new ArrayList(data.length);
        for (Object aData : data)
        {
            this.data.add(aData);
        }
    }

    public List getPage(int index)
    {
        if (index < 0 || (index != 0 && index >= getPageCount()))
        {
            throw new IndexOutOfBoundsException("index " + index);
        }

        int startIndex = index * pageSize;
        int endIndex = (index + 1) * pageSize;
        if (endIndex > data.size())
            endIndex = data.size(); // final page.

        return data.subList(startIndex, endIndex);
    }

    public int getPageCount()
    {
        return data.size() / pageSize + (data.size() % pageSize == 0 ? 0 : 1);
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int size)
    {
        if (size <= 0)
            throw new IllegalArgumentException();

        this.pageSize = size;
    }

    public int getDataSize()
    {
        return data.size();
    }
}
