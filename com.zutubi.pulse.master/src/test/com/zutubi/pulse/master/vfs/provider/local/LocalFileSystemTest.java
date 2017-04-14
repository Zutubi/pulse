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

package com.zutubi.pulse.master.vfs.provider.local;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.SystemUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

import java.io.File;

public class LocalFileSystemTest extends PulseTestCase
{
    private DefaultFileSystemManager fileSystemManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        fileSystemManager = new DefaultFileSystemManager();
        fileSystemManager.addProvider("local", new DefaultLocalFileProvider());
        fileSystemManager.init();
    }

    public void testResolveRootPathOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/");
        assertTrue(obj.exists());
        assertTrue(obj.isReadable());
    }

    public void testRootPathListingOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/");
        FileObject[] listing = obj.getChildren();
        File[] expectedRoots = File.listRoots();

        assertEquals(expectedRoots.length, listing.length);
    }

    public void testResolveCDriveOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/C:");
        assertTrue(obj.exists());
    }

    public void testCDriveListingOnWindows() throws FileSystemException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        FileObject obj = fileSystemManager.resolveFile("local:/C:");
        FileObject[] listing = obj.getChildren();

        File cdrive = new File("C:/");
        File[] expectedListing = cdrive.listFiles();

        assertEquals(expectedListing.length, listing.length);
        for (int i = 0; i < expectedListing.length; i++)
        {
            assertEquals(expectedListing[i].getName(), listing[i].getName().getBaseName());
        }
    }
}
