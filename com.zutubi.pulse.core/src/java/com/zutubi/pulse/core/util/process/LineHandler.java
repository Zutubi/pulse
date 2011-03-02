package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 */
public interface LineHandler
{
    /**
     * Indicates the character set to use for byte to character conversion.
     * 
     * @return the character set used to convert bytes to characters
     */
    Charset getCharset();
    
    void handle(String line, boolean error);
}
