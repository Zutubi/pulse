package com.zutubi.pulse.core.util.process;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 */
public interface CharHandler
{
    /**
     * Indicates the character set to use for byte to character conversion.
     * 
     * @return the character set used to convert bytes to characters
     */
    Charset getCharset();
    
    void handle(char[] buffer, int n, boolean error) throws IOException;
}
