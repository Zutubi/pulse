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
