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

package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * <p>
 * Support base class for post processors that read text files.  Handles
 * setup of a reader and IOExceptions.
 * </p>
 * <p>
 * Note that processors which deal with text files line-by-line may be better
 * served by the extra features in {@link LineBasedPostProcessorSupport}.
 * </p>
 *
 * @see LineBasedPostProcessorSupport
 */
public abstract class TextFilePostProcessorSupport extends OutputPostProcessorSupport
{
    protected TextFilePostProcessorSupport(TextFilePostProcessorConfigurationSupport config)
    {
        super(config);
    }

    public final void processFile(File artifactFile, PostProcessorContext ppContext)
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(artifactFile));
            process(reader, ppContext);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    /**
     * Called once for each file to be processed by this post-processor.  The
     * given reader is ready to read at the beginning of the file and will be
     * closed by this implementation after this call completes.  Any
     * information found during processing should be reported using the given
     * context.
     *
     * @param reader    a reader intialised with the file to process
     * @param ppContext context in which the processing is executing
     * @throws IOException if there is an error interacting with the reader
     */
    protected abstract void process(BufferedReader reader, PostProcessorContext ppContext) throws IOException;
}
