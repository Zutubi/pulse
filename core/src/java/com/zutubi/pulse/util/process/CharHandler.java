package com.zutubi.pulse.util.process;

import java.io.IOException;

/**
 */
public interface CharHandler
{
    void handle(char[] buffer, int n, boolean error) throws IOException;
}
