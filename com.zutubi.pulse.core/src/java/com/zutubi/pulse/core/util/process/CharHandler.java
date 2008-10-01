package com.zutubi.pulse.core.util.process;

import java.io.IOException;

/**
 */
public interface CharHandler
{
    void handle(char[] buffer, int n, boolean error) throws IOException;
}
