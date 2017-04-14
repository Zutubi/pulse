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

package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Text;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

import java.io.File;
import java.io.IOException;

/**
 * Used for the setup data page: i.e. the very first page shown when no data
 * directory is available.
 */
@SymbolicName("zutubi.setupDataConfig")
@Form(fieldOrder = { "data" })
public class SetupDataConfiguration extends AbstractConfiguration implements Validateable
{
    @Required
    @Text(size = 400)
    @FieldAction(template = "SetupDataConfiguration")
    private String data;

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public void validate(ValidationContext context)
    {
        File data = new File(this.data);

        if (!data.exists() && !data.mkdirs())
        {
            context.addFieldError("data", "data.create.failed");
        }

        // ensure that we have write access to the data directory.
        checkDirectoryIsWritable(data, context);
    }

    private void checkDirectoryIsWritable(File data, ValidationContext context)
    {
        File tmpFile = null;
        try
        {
            tmpFile = File.createTempFile("test", "tmp", data);
        }
        catch (IOException e)
        {
            context.addFieldError("data", "data.write.failed");
        }
        finally
        {
            if (tmpFile != null && tmpFile.isFile())
            {
                tmpFile.delete();
            }
        }
    }
}
