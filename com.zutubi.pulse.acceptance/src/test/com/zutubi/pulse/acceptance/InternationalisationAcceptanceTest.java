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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.util.RandomUtils;

public class InternationalisationAcceptanceTest extends AcceptanceTestBase
{
    private static final String I18N = "Iñtërnâtiônàlizætiøn";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        getBrowser().loginAsAdmin();
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        getBrowser().logout();
        rpcClient.logout();

        super.tearDown();
    }

    public void testI18NCharactersInProjectName() throws Exception
    {
        String name = randomName();
        rpcClient.RemoteApi.insertSimpleProject(name, false);

        ProjectConfigPage projectPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, name, false);
        assertTrue(projectPage.isPresent());
    }

    protected String randomName()
    {
        return I18N + "-" + RandomUtils.insecureRandomString(3);
    }
}
