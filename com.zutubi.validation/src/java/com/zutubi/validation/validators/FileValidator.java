package com.zutubi.validation.validators;

import java.io.File;

/**
 * Checks a field value is the path to a file on the local file system.
 */
public class FileValidator extends StringFieldValidatorSupport
{
    private boolean verifyFile;
    private boolean verifyDirectory;
    private boolean verifyReadable;
    private boolean verifyWritable;

    public FileValidator()
    {
        super("not.found");
    }

    public void validateStringField(String value)
    {
        File f = new File(value);
        if(!f.exists())
        {
            addError();
            return;
        }

        if(verifyFile && !f.isFile())
        {
            addError("not.file");
            return;
        }

        if(verifyDirectory && !f.isDirectory())
        {
            addError("not.dir");
            return;
        }

        if(verifyReadable && !f.canRead())
        {
            addError("not.readable");
        }

        if(verifyWritable && !f.canWrite())
        {
            addError("not.writable");
        }
    }

    public boolean isVerifyFile()
    {
        return verifyFile;
    }

    public void setVerifyFile(boolean verifyFile)
    {
        this.verifyFile = verifyFile;
    }

    public boolean isVerifyDirectory()
    {
        return verifyDirectory;
    }

    public void setVerifyDirectory(boolean verifyDirectory)
    {
        this.verifyDirectory = verifyDirectory;
    }

    public boolean isVerifyReadable()
    {
        return verifyReadable;
    }

    public void setVerifyReadable(boolean verifyReadable)
    {
        this.verifyReadable = verifyReadable;
    }

    public boolean isVerifyWritable()
    {
        return verifyWritable;
    }

    public void setVerifyWritable(boolean verifyWritable)
    {
        this.verifyWritable = verifyWritable;
    }
}
