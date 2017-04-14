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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.TemplateRecord;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AddMetaPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "test name";
    private static final String PROPERTY_VALUE = "test value";
    private static final String NO_INHERIT_NAME = TemplateRecord.NO_INHERIT_META_KEYS[0];
    private static final String SCOPE = "scope";
    private static final String TEMPLATED_SCOPE = "templated scope";
    private static final String TEMPLATED_ANCESTOR_PATH = TEMPLATED_SCOPE + "/ancestor";
    private static final String TEMPLATED_NO_ANCESTOR_PATH = TEMPLATED_SCOPE + "/noancestor";

    private static final String PATH = SCOPE + "/path";

    private AddMetaPropertyRecordUpgrader upgrader;
    private PersistentScopes scopes;

    protected void setUp() throws Exception
    {
        super.setUp();
        upgrader = new AddMetaPropertyRecordUpgrader(PROPERTY_NAME, PROPERTY_VALUE);

        TemplatedScopeDetails templatedScopeDetails = mock(TemplatedScopeDetails.class);
        doReturn(true).when(templatedScopeDetails).hasAncestor(TEMPLATED_ANCESTOR_PATH);
        doReturn(false).when(templatedScopeDetails).hasAncestor(TEMPLATED_NO_ANCESTOR_PATH);

        scopes = mock(PersistentScopes.class);
        doReturn(new ScopeDetails(SCOPE)).when(scopes).findByPath(startsWith(SCOPE));
        doReturn(templatedScopeDetails).when(scopes).findByPath(startsWith(TEMPLATED_SCOPE));

        upgrader.setPersistentScopes(scopes);
    }

    public void testSimpleScope()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoAncestor()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(TEMPLATED_NO_ANCESTOR_PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoInheritNoAncestor()
    {
        upgrader = new AddMetaPropertyRecordUpgrader(NO_INHERIT_NAME, PROPERTY_VALUE);
        upgrader.setPersistentScopes(scopes);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(TEMPLATED_NO_ANCESTOR_PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(NO_INHERIT_NAME));
    }

    public void testTemplatedScopeHasAncestor()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(TEMPLATED_ANCESTOR_PATH, mutable);
        assertNull(mutable.getMeta(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoInheritHasAncestor()
    {
        upgrader = new AddMetaPropertyRecordUpgrader(NO_INHERIT_NAME, PROPERTY_VALUE);
        upgrader.setPersistentScopes(scopes);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(TEMPLATED_ANCESTOR_PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(NO_INHERIT_NAME));
    }

    public void testPropertyAlreadyExists()
    {
        final String EXISTING_VALUE = "another value";

        MutableRecordImpl mutable = new MutableRecordImpl();
        mutable.putMeta(PROPERTY_NAME, EXISTING_VALUE);
        upgrader.upgrade(PATH, mutable);
        assertEquals(EXISTING_VALUE, mutable.getMeta(PROPERTY_NAME));
    }
}
