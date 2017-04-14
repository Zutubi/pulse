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

package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * Describes how text file End-Of-Line characters should be managed.  Several
 * SCMs support translation to/from local system line endings.
 */
public enum EOLStyle
{
    /**
     * Treat the file as binary: i.e. do no translation.
     */
    BINARY
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            // Do nothing.
        }
    },
    /**
     * Force carriage return (Old Macintosh style) endings.
     */
    CARRIAGE_RETURN
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.CR_BYTES);
        }
    },
    /**
     * Force carriage return linefeed (Windows style) endings.
     */
    CARRIAGE_RETURN_LINEFEED
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.CRLF_BYTES);
        }
    },
    /**
     * Force linefeed (Unix style) endings.
     */
    LINEFEED
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.LF_BYTES);
        }
    },
    /**
     * Force the file line endings to match the target platform endings.
     */
    NATIVE
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.LINE_SEPARATOR_BYTES);
        }
    },
    /**
     * Use the target side configuration for text files to determine what
     * to do (e.g. use Perforce client setting for text files on the
     * target side).
     */
    TEXT
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            localEOL.apply(file, localEOL);
        }
    };

    /**
     * Translate all end-of-line byte sequences in the given file to the given
     * byte sequence.  Any byte sequence that could be an end-of-line (CR, LF,
     * CRLF) will be replaced.  If the file does not exist this request is
     * ignored.
     *
     * @param file               file to translate line endings in
     * @param lineSeparatorBytes new byte sequence to use for all line endings
     * @throws IOException if there is an error reading/writing from/to the
     *         file system
     */
    public void translateEOLs(File file, byte[] lineSeparatorBytes) throws IOException
    {
        if(file.exists())
        {
            FileSystemUtils.translateEOLs(file, lineSeparatorBytes, true);
        }
    }

    /**
     * Applies this end-of-line style to the given file.  All current line
     * endings in the file will be translated according to the style.  If the
     * file does not exist this request is ignored.
     *
     * @param file         the file to apply the line ending style to
     * @param localEOL     the local end-of-line policy, as determined from the
     *                     environment in which we are running
     * @throws IOException if there is an error reading/writing from/to the
     *         file system
     */
    public abstract void apply(File file, EOLStyle localEOL) throws IOException;
}
