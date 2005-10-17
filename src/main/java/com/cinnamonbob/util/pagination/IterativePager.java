package com.cinnamonbob.util.pagination;

import java.util.Iterator;
import java.util.List;

/**
 * <class-comment/>
 */
public class IterativePager extends DefaultPager implements Iterator
{
    private int currentPageIndex = -1;

    public IterativePager(List data)
    {
        super(data);
    }

    public IterativePager(Object[] data)
    {
        super(data);
    }

    public List firstPage()
    {
        currentPageIndex = firstPageIndex();
        return currentPage();
    }

    public List lastPage()
    {
        currentPageIndex = lastPageIndex();
        return currentPage();
    }

    public List nextPage()
    {
        if (hasNextPage())
        {
            currentPageIndex++;
        }
        return currentPage();
    }

    public List previousPage()
    {
        if (hasPreviousPage())
        {
            currentPageIndex--;
        }
        return currentPage();
    }

    public boolean hasNextPage()
    {
        return currentPageIndex < lastPageIndex();
    }

    public boolean hasPreviousPage()
    {
        return firstPageIndex() < currentPageIndex;
    }

    public boolean hasNext()
    {
        return hasNextPage();
    }

    public Object next()
    {
        return nextPage();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isLastPage()
    {
        return currentPageIndex == lastPageIndex();
    }

    public boolean isFirstPage()
    {
        return currentPageIndex == firstPageIndex();
    }

    public List currentPage()
    {
        return getPage(currentPageIndex);
    }

    private int lastPageIndex()
    {
        return (getPageCount() != 0 ? getPageCount() - 1 : 0);
    }

    private int firstPageIndex()
    {
        return 0;
    }
}
