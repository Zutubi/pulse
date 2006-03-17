package com.cinnamonbob.util;

import java.util.Iterator;

/**
 * Not a classic ring buffer, just a fixed size buffer that information can
 * keep scrolling through until we are keen to see what's inside.
 */
public class CircularBuffer<T> implements Iterable<T>
{
    private T[] buffer;
    private int index;
    private int count;

    public CircularBuffer(int capacity)
    {
        buffer = (T[]) new Object[capacity + 1];
        index = capacity > 0 ? 1 : 0;
        count = 0;
    }

    public int getCount()
    {
        return count;
    }

    public int getCapacity()
    {
        return buffer.length - 1;
    }

    public void append(T t)
    {
        buffer[index++] = t;
        if (index >= buffer.length)
        {
            index = 0;
        }

        if (count < getCapacity())
        {
            count++;
        }
    }

    public T getElement(int i)
    {
        int actualIndex;

        if (count < getCapacity())
        {
            actualIndex = i + 1;
        }
        else
        {
            actualIndex = (index + i + 1) % buffer.length;
        }

        return buffer[actualIndex];
    }

    public Iterator<T> iterator()
    {
        return new CircularBufferIterator();
    }

    private class CircularBufferIterator implements Iterator<T>
    {
        /**
         * Index of next element to read.
         */
        private int current;
        /**
         * Index of element one past the last element to read.
         */
        int last;

        public CircularBufferIterator()
        {
            if (count < getCapacity())
            {
                current = 1;
                last = count + 1;
            }
            else
            {
                current = (index + 1) % buffer.length;
                last = index;
            }
        }

        public boolean hasNext()
        {
            return current != last;
        }

        public T next()
        {
            T result = buffer[current++];
            if (current == buffer.length)
            {
                current = 0;
            }

            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
