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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.model.PersistentFeature;
import com.zutubi.pulse.core.model.PersistentPlainFeature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class ViewDecoratedArtifactAction extends FileArtifactActionBase
{
    private static final Logger LOG = Logger.getLogger(ViewDecoratedArtifactAction.class);

    private BufferedReader reader;
    private Map<Long, Feature.Level> lineLevels;
    private MasterConfigurationManager configurationManager;

    public ReaderIterator getReaderIterator()
    {
        return new ReaderIterator();
    }

    public Feature.Level getLineLevel(long line)
    {
        return lineLevels.get(line);
    }

    private void determineLineLevels()
    {
        lineLevels = new TreeMap<Long, Feature.Level>();
        List<PersistentFeature> features = getFileArtifact().getFeatures();
        for(PersistentFeature f: features)
        {
            if(f instanceof PersistentPlainFeature)
            {
                PersistentPlainFeature p = (PersistentPlainFeature) f;
                updateLevel(p.getLineNumber(), p.getLevel());
            }
        }
    }

    private void updateLevel(long n, Feature.Level level)
    {
        if(lineLevels.containsKey(n))
        {
            if(level.compareTo(lineLevels.get(n)) > 0)
            {
                return;
            }
        }

        lineLevels.put(n, level);
    }

    public String execute()
    {
        StoredFileArtifact artifact = getRequiredFileArtifact();
        getBuildResult().loadFeatures(configurationManager.getDataDirectory());
        File artifactFile = new File(getCommandResult().getAbsoluteOutputDir(configurationManager.getDataDirectory()), artifact.getPath());
        if(!artifactFile.isFile())
        {
            addActionError("Artifact file '" + artifactFile.getAbsolutePath() + "' does not exist");
            return ERROR;
        }

        try
        {
            reader = new BufferedReader(new FileReader(artifactFile.getAbsolutePath()));
        }
        catch (FileNotFoundException e)
        {
            addActionError("Unable to open artifact file: " + e.getMessage());
            return ERROR;
        }

        determineLineLevels();

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    class ReaderIterator implements Iterator
    {
        private boolean buffered = false;
        private String line;

        public boolean hasNext()
        {
            if(buffered)
            {
                return true;
            }
            else
            {
                try
                {
                    line = reader.readLine();

                    if(line != null)
                    {
                        buffered = true;
                        return true;
                    }
                }
                catch (IOException e)
                {
                    LOG.severe("I/O error reading artifact: " + e.getMessage());
                }
            }

            return false;
        }

        public Object next()
        {
            if(buffered)
            {
                buffered = false;
                String temp = line;
                line = null;
                return temp;
            }

            try
            {
                return reader.readLine();
            }
            catch (IOException e)
            {
                LOG.severe("I/O error reading artifact: " + e.getMessage());
            }

            return "";
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

}
