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

package com.zutubi.pulse.master.xwork.actions;

/**
 */
public class PagingSupport
{
    private static final int SURROUNDING_PAGES = 10;

    private int totalItems;
    private int itemsPerPage;
    private int startPage;

    public PagingSupport(int itemsPerPage)
    {
        this.itemsPerPage = itemsPerPage;
        startPage = 0;
    }

    public PagingSupport(int totalPages, int itemsPerPage, int startPage)
    {
        this.totalItems = totalPages;
        this.itemsPerPage = itemsPerPage;
        this.startPage = startPage;
    }

    public int getTotalItems()
    {
        return totalItems;
    }

    public void setTotalItems(int totalItems)
    {
        this.totalItems = totalItems;
    }

    public int getItemsPerPage()
    {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage)
    {
        this.itemsPerPage = itemsPerPage;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public int getPageCount()
    {
        return (totalItems + itemsPerPage - 1) / itemsPerPage;
    }

    public int getPageRangeStart()
    {
        int offset = SURROUNDING_PAGES / 2;

        if (startPage + offset + 1 > getPageCount())
        {
            // show more to the left
            offset += startPage + offset + 1 - getPageCount();
        }

        int start = startPage - offset;
        if (start < 0)
        {
            start = 0;
        }

        return start;
    }

    public int getPageRangeEnd()
    {
        int offset = SURROUNDING_PAGES / 2;

        if (startPage - offset < 0)
        {
            // show more to the right
            offset += offset - startPage;
        }

        int end = startPage + offset;
        if (end >= getPageCount())
        {
            end = getPageCount() - 1;
        }

        return end;
    }

    public boolean isStartPageValid()
    {
        /* Page zero is always valid, it is up to the UI to handle this case gracefully. */
        return startPage == 0 || startPage * itemsPerPage < totalItems;
    }

    public int getStartOffset()
    {
        return startPage * itemsPerPage;
    }

    public int getEndOffset()
    {
        int offset = getStartOffset() + itemsPerPage;

        if (offset > totalItems)
        {
            offset = totalItems;
        }

        return offset;
    }

    public void clampStartPage()
    {
        if(startPage * itemsPerPage >= totalItems)
        {
            startPage = getPageCount() - 1;
        }

        if(startPage < 0)
        {
            startPage = 0;
        }
    }
}
