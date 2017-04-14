/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
