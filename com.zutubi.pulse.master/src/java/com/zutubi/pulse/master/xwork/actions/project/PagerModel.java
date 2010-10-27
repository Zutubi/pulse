package com.zutubi.pulse.master.xwork.actions.project;

/**
 * Supplies JSON data for the Pager JS component.
 */
public class PagerModel
{
    private int totalItems;
    private int itemsPerPage;
    private int currentPage;

    public PagerModel(int totalItems, int itemsPerPage, int currentPage)
    {
        this.totalItems = totalItems;
        this.itemsPerPage = itemsPerPage;
        this.currentPage = currentPage;
    }

    public int getTotalItems()
    {
        return totalItems;
    }

    public int getItemsPerPage()
    {
        return itemsPerPage;
    }

    public int getCurrentPage()
    {
        return currentPage;
    }
}
