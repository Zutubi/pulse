package com.zutubi.util;

import java.io.OutputStream;

/**
 * An output stream that just eats the bytes written to it.
 */
public class NullOutputStream extends OutputStream
{
    public void write(int b)
    {
    }
}
