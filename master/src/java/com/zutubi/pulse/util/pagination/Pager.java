/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.util.pagination;

import java.util.List;

/**
 * <class-comment/>
 */
public interface Pager
{
    /**
     *
     */
    List getPage(int index);

    /**
     *
     */
    int getPageSize();

    /**
     *
     */
    void setPageSize(int size);

    /**
     *
     */
    int getPageCount();
}
