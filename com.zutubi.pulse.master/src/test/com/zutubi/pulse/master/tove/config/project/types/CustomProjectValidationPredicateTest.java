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

package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.pulse.core.marshal.ImportingNotSupportedFileResolver;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;

import java.util.LinkedList;
import java.util.List;

public class CustomProjectValidationPredicateTest extends FileLoaderTestBase
{
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        typeRegistry.register(Holder.class);
        fileLoaderFactory.register("referer", Referer.class);
        fileLoaderFactory.register("referee", Referee.class);
        loader = fileLoaderFactory.createLoader();
    }

    public void testCustomProjectValidation() throws Exception
    {
        ProjectRecipesConfiguration prc = load();
        assertNotNull(prc.getRecipes().get("bar"));
    }

    public void testUnresolvedVariableInName() throws Exception
    {
        ProjectRecipesConfiguration prc = load();
        assertNotNull(prc.getRecipes().get("with $(project) variable"));
    }

    public void testUnresolvedSingleReference() throws Exception
    {
        Holder holder = new Holder();
        load(holder);
        
        Referer referer = holder.getErList().get(0);
        assertNull(referer.getRef());
        
        List<Referee> eeList = referer.getRefList();
        assertEquals(1, eeList.size());
        assertEquals("ee", eeList.get(0).getName());
    }

    private ProjectRecipesConfiguration load() throws PulseException
    {
        ProjectRecipesConfiguration prc = new ProjectRecipesConfiguration();
        load(prc);
        return prc;
    }

    private void load(Configuration root) throws PulseException
    {
        loader.load(getInput("xml"), root, new PulseScope(), new ImportingNotSupportedFileResolver(), new CustomProjectValidationInterceptor());
    }

    @SymbolicName("holder")
    public static class Holder extends AbstractConfiguration
    {
        private List<Referer> erList = new LinkedList<Referer>();

        public List<Referer> getErList()
        {
            return erList;
        }

        public void setErList(List<Referer> erList)
        {
            this.erList = erList;
        }
    }
    
    @SymbolicName("referer")
    public static class Referer extends AbstractConfiguration
    {
        @Reference
        private Referee ref;
        @Reference @Addable("ee")
        private List<Referee> refList = new LinkedList<Referee>();

        public Referee getRef()
        {
            return ref;
        }

        public void setRef(Referee ref)
        {
            this.ref = ref;
        }

        public List<Referee> getRefList()
        {
            return refList;
        }

        public void setRefList(List<Referee> refList)
        {
            this.refList = refList;
        }
    }

    @SymbolicName("referee") @Referenceable
    public static class Referee extends AbstractNamedConfiguration
    {
    }
}
