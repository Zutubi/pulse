package com.zutubi.pulse.prototype.config;

/**
 *
 *
 */
public class FisheyeConfiguration extends ChangeViewerConfiguration
{
    private String baseurl;
    private String repository;

    public String getBaseurl()
    {
        return baseurl;
    }

    public void setBaseurl(String baseurl)
    {
        this.baseurl = baseurl;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }
}
