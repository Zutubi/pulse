package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;

/**
 * <class-comment/>
 */
public class Md5PasswordEncoderTest extends PulseTestCase
{
    private Md5PasswordEncoder encoder;

    public Md5PasswordEncoderTest()
    {
    }

    public Md5PasswordEncoderTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        encoder = new Md5PasswordEncoder();
    }

    protected void tearDown() throws Exception
    {
        encoder = null;

        super.tearDown();
    }

    /**
     * CIB-548: ensure that password varification behaves as expected when dealing with I18N characters.
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
