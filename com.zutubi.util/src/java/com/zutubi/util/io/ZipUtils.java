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

package com.zutubi.util.io;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Basic utilities for zip files.
 */
public class ZipUtils
{
    /**
     * Extracts the files from the given zip stream to into the given
     * destination directory.
     *
     * @param zin zip stream to extract files from
     * @param dir destination directory
     * @throws IOException on error
     */
    public static void extractZip(ZipInputStream zin, File dir) throws IOException
    {
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null)
        {
            File file = new File(dir, entry.getName());

            if (entry.isDirectory())
            {
                if (!file.isDirectory())
                {
                    FileSystemUtils.createDirectory(file);
                }
            }
            else
            {
                // Ensure that the file's parents already exist.
                if (!file.getParentFile().isDirectory())
                {
                    FileSystemUtils.createDirectory(file.getParentFile());
                }

                unzip(zin, file);
            }

            file.setLastModified(entry.getTime());
        }
    }

    private static void unzip(InputStream zin, File file) throws IOException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            ByteStreams.copy(zin, out);
        }
        finally
        {
            IOUtils.close(out);
        }
    }

    /**
     * Compresses a single file using gzip.
     *
     * @param in  file to compress
     * @param out file to store the compressed output in
     * @throws IOException on an error reading the input or writing the output
     *
     * @see #uncompressFile(java.io.File, java.io.File)
     */
    public static void compressFile(File in, File out) throws IOException
    {
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = new FileInputStream(in);
            os = new GZIPOutputStream(new FileOutputStream(out));
            ByteStreams.copy(is, os);
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }
    }

    /**
     * Uncompresses a single file using gzip.
     *
     * @param in  file to uncompress
     * @param out file to store the uncompressed output in
     * @throws IOException on an error reading the input or writing the output
     *
     * @see #compressFile(java.io.File, java.io.File)
     */
    public static void uncompressFile(File in, File out) throws IOException
    {
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = new GZIPInputStream(new FileInputStream(in));
            os = new FileOutputStream(out);
            ByteStreams.copy(is, os);
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }
    }
}
