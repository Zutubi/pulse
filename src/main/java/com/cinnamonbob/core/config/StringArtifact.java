package com.cinnamonbob.core.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 
 *
 */
public class StringArtifact extends AbstractArtifact
{
    private String content;

    public StringArtifact(String name, String content)
    {
        this.name = name;
        this.content = content;
    }

    public InputStream getContent()
    {
        return new ByteArrayInputStream(content.getBytes());
    }

}
