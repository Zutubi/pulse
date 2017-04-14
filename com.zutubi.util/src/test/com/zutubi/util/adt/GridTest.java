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

package com.zutubi.util.adt;

import com.zutubi.util.Point;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.junit.ZutubiTestCase;

public class GridTest extends ZutubiTestCase
{
    public void testGetCell()
    {
        Grid<String> grid = new Grid<String>(3, 2);
        assertNotNull(grid.getCell(2, 1));
    }

    public void testGetCellByPoint()
    {
        Grid<String> grid = new Grid<String>(3, 3);
        assertSame(grid.getCell(1, 1), grid.getCell(new Point(1, 1)));
    }

    public void testGetRow()
    {
        Grid<String> grid = makeGrid(3);

        Iterable<GridCell<String>> rowIt = grid.getRow(1);
        int i = 0;
        for (GridCell<String> cell: rowIt)
        {
            assertEquals(String.format("%d,1", i), cell.getData());
            i++;
        }
    }

    public void testIterator()
    {
        Grid<String> grid = makeGrid(3);
        int y = 0;
        for (Iterable<GridCell<String>> rowsIt: grid)
        {
            int x = 0;
            for (GridCell<String> cell: rowsIt)
            {
                assertEquals(getCellData(x, y), cell.getData());
                x++;
            }

            y++;
        }
    }

    public void testFlipHorizontalOdd()
    {
        flipTest(3);
    }

    public void testFlipHorizontalEven()
    {
        flipTest(4);
    }

    public void testFlipHorizontalFlipFn()
    {
        Grid<String> grid = makeGrid(3);
        final int[] count = {0};
        grid.flipHorizontal(new UnaryProcedure<String>()
        {
            public void run(String s)
            {
                count[0]++;
            }
        });

        // Make sure all cells are visited.
        assertEquals(9, count[0]);
    }

    private void flipTest(int size)
    {
        Grid<String> grid = makeGrid(size);
        grid.flipHorizontal(null);
        int x = size - 1;
        for (GridCell<String> cell: grid.getRow(0))
        {
            assertEquals(getCellData(x, 0), cell.getData());
            x--;
        }
    }

    private Grid<String> makeGrid(int size)
    {
        Grid<String> grid = new Grid<String>(size, size);
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                grid.getCell(x, y).setData(getCellData(x, y));
            }
        }

        return grid;
    }

    private String getCellData(int x, int y)
    {
        return String.format("%d,%d", x, y);
    }
}
