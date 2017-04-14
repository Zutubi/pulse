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

package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.validation.ValidationException;

import java.awt.*;

public class ColourValidatorTest extends PulseTestCase
{
    public void testParseEmpty()
    {
        errorHelper("");
    }

    public void testParseUnknown()
    {
        errorHelper("no such colour");
    }

    public void testParseByName()
    {
        Color color = ColourValidator.parseColour("white");
        assertSame(Color.white, color);
    }

    public void testParseByValue()
    {
        Color color = ColourValidator.parseColour("20");
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(20, color.getBlue());
    }

    public void testParseByHexValue()
    {
        Color color = ColourValidator.parseColour("0xff2018");
        assertEquals(255, color.getRed());
        assertEquals(32, color.getGreen());
        assertEquals(24, color.getBlue());
    }

    public void testValidate() throws ValidationException
    {
        ColourValidator validator = new ColourValidator();
        validator.validateField("");
        validator.validateField("black");
        validator.validateField("0x000000");
        validator.validateField("0");
        try
        {
            validator.validateField("no such colour");
            fail();
        }
        catch (ValidationException e)
        {
        }
    }

    private void errorHelper(String colour)
    {
        try
        {
            ColourValidator.parseColour(colour);
            fail("Colour '" + colour + "' should not be parseable");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Unrecognised colour '" + colour + "'", e.getMessage());
        }
    }
}
