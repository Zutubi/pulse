package com.cinnamonbob.model;

/**
 * 
 *
 */
public class Svn extends Scm
{
    
    private final String URL = "svn.url";
    private final String USERNAME = "svn.username";
    private final String PASSWORD = "svn.password";
    private final String KEYFILE = "svn.keyfile";
    private final String PASSPHRASE = "svn.passphrase";
    private final String PATH = "svn.path";
    
    public String getUrl()
    {
        return (String) properties.get(URL);
    }
    
    public void setUrl(String url)
    {
        properties.put(URL, url);
    }

    public String getUsername()
    {
        return (String) properties.get(USERNAME);
    }

    public void setUsername(String username)
    {
        properties.put(USERNAME, username);
    }

    public String getPassword()
    {
        return (String) properties.get(PASSWORD);
    }

    public void setPassword(String password)
    {
        properties.put(PASSWORD, password);
    }

    public String getKeyfile()
    {
        return (String) properties.get(KEYFILE);
    }

    public void setKeyfile(String keyfile)
    {
        properties.put(KEYFILE, keyfile);
    }

    public String getPassphrase()
    {
        return (String) properties.get(PASSPHRASE);
    }

    public void setPassphrase(String passphrase)
    {
        properties.put(PASSPHRASE, passphrase);
    }

    public String getPath()
    {
        return (String) properties.get(PATH);
    }

    public void setPath(String path)
    {
        properties.put(PATH, path);
    }
}
