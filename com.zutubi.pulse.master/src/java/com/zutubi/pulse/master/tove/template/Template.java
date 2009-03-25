package com.zutubi.pulse.master.tove.template;

import java.io.Writer;

public interface Template
{
    void process(Object context, Writer writer) throws Exception;
}
