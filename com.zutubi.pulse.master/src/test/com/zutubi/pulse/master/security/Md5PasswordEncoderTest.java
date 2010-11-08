package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

public class Md5PasswordEncoderTest extends PulseTestCase
{
    private Md5PasswordEncoder encoder;

    protected void setUp() throws Exception
    {
        super.setUp();

        encoder = new Md5PasswordEncoder();
    }

    /**
     * CIB-548: ensure that password verification behaves as expected when dealing with I18N characters.
     */
    public void testI18NPasswordEncoding()
    {
        String password = "Iñtërnâtiônàlizætiøn";
        String encodedPassword = encoder.encodePassword(password, null);
        assertTrue(encoder.isPasswordValid(encodedPassword, password, null));
    }

    public void testEncode()
    {
        String encoded = encoder.encodePassword("admin", null);
        System.out.println(encoded);
    }
}
