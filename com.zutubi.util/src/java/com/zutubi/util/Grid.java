package com.zutubi.util;

import static java.util.Arrays.asList;
import java.util.Iterator;

/**
 * A grid is a basic abstraction over a two-dimensional space.  Each point in
 * the space is a cell that can hold arbitrary data.  Grids are row-centric:
 * iterating over the grid yields iterator over each row in succession.
 */
public class Grid<T> implements Iterable<Iterable<GridCell<T>>>
{
    private GridCell<T>[][] cells;

    /**
     * Create a grid of the given dimensions.
     *
     * @param width  horizontal size of the grid
     * @param height vertical size of the grid
     */
    @SuppressWarnings({"unchecked"})
    public Grid(int width, int height)
    {
        cells = new GridCell[height][width];
        for (GridCell[] row: cells)
        {
            for (int i = 0; i < width; i++)
            {
                row[i] = new GridCell();
            }
        }
    }

    /**
     * Returns the cell at the given zero-based coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return the cell at the given coordinates
     */
    public GridCell<T> getCell(int x, int y)
    {
        return cells[y][x];
    }

    /**
     * Returns the cell at the coordinates of the given point.
     *
     * @param point point specifying the position of the cell to retrieve
     * @return the cell at the given point
     */
    public GridCell<T> getCell(Point point)
    {
        return getCell(point.getX(), point.getY());
    }

    /**
     * Returns an iterator over the row at the given zero-based index.
     *
     * @param i index of the row to retrieve
     * @return an iterator over the cells in the given row
     */
    public Iterable<GridCell<T>> getRow(int i)
    {
        return asList(cells[i]);
    }

    /**
     * Returns an iterator over row iterators.
     *
     * @return an iterator which itself yields iterators, one for each row
     */
    public Iterator<Iterable<GridCell<T>>> iterator()
    {
        return new RowIterator();
    }

    /**
     * Flips the whole grid horizontally, by flipping each row horizontally.
     * To flip a row, the first cell is swapped with the last, the second with
     * the next to last and so on.
     *
     * @param dataFlipFn if not null, a procedure to apply to all cell data to
     *                   flip the data's own contents
     */
    @SuppressWarnings({"unchecked"})
    public void flipHorizontal(UnaryProcedure<T> dataFlipFn)
    {
        for (GridCell[] row: cells)
        {
            // Applying the flipFn and doing the swap is possible in a single
            // loop, but tricky as the swap is moving cells about.  Simpler to
            // just use separate loops.
            if (dataFlipFn != null)
            {
                for (GridCell<T> cell: row)
                {
                    if (cell.getData() != null)
                    {
                        dataFlipFn.process(cell.getData());
                    }
                }
            }

            for (int i = 0; i < row.length / 2; i++)
            {
                GridCell temp = row[i];
                row[i] = row[row.length - i - 1];
                row[row.length - i - 1] = temp;
            }
        }
    }

    public int getWidth()
    {
        return cells[0].length;
    }

    public int getHeigth()
    {
        return cells.length;
    }

    private class RowIterator implements Iterator<Iterable<GridCell<T>>>
    {
        private int nextRow = 0;

        public boolean hasNext()
        {
            return nextRow < cells.length;
        }

        public Iterable<GridCell<T>> next()
        {
            return getRow(nextRow++);
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Row removal not supported");
        }
    }
}
