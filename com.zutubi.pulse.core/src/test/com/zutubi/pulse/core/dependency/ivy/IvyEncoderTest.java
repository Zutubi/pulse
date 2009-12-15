package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.junit.ZutubiTestCase;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.util.HashMap;
import java.util.Map;

import static com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor.EXTRA_ATTRIBUTE_SOURCE_FILE;
import static com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor.EXTRA_ATTRIBUTE_STAGE;

public class IvyEncoderTest extends ZutubiTestCase
{
    private static final String KEY_VALUE_DELIMITER = ";";

    public void testGeneralEncoding()
    {
        assertGeneralEncoding("", "");
        assertGeneralEncoding("abc", "abc");
        assertGeneralEncoding("123", "123");
        assertGeneralEncoding("*", "$2a");
    }

    private void assertGeneralEncoding(String raw, String expected)
    {
        assertEquals(expected, IvyEncoder.encode(raw));
        assertEquals(raw, IvyEncoder.decode(expected));
    }

    public void testModuleRevisionEncoding()
    {
        ModuleRevisionId raw = ModuleRevisionId.newInstance("org", "name", "branch", "revision", createMap(pair("key", "value")));
        ModuleRevisionId expected = ModuleRevisionId.newInstance("org", "name", "branch", "revision", createMap(pair("key", "value")));
        assertModuleRevisionIdEncoding(raw, expected);

        raw = ModuleRevisionId.newInstance("or*g", "na*me", "bra*nch", "revi*sion", createMap("k*ey;v*alue"));
        expected = ModuleRevisionId.newInstance("or$2ag", "na$2ame", "bra*nch", "revi*sion", createMap("k*ey;v*alue"));
        assertModuleRevisionIdEncoding(raw, expected);
    }

    private void assertModuleRevisionIdEncoding(ModuleRevisionId raw, ModuleRevisionId expected)
    {
        ModuleRevisionId encoded = IvyEncoder.encode(raw);
        assertEquals(expected.getName(), encoded.getName());
        assertEquals(expected.getOrganisation(), encoded.getOrganisation());
        assertEquals(expected.getBranch(), encoded.getBranch());
        assertEquals(expected.getRevision(), encoded.getRevision());
        assertEquals(expected.getModuleId(), encoded.getModuleId());
        assertEquals(expected.getExtraAttributes(), encoded.getExtraAttributes());

        ModuleRevisionId decoded = IvyEncoder.decode(expected);
        assertEquals(raw.getName(), decoded.getName());
        assertEquals(raw.getOrganisation(), decoded.getOrganisation());
        assertEquals(raw.getBranch(), decoded.getBranch());
        assertEquals(raw.getRevision(), decoded.getRevision());
        assertEquals(raw.getModuleId(), decoded.getModuleId());
        assertEquals(raw.getExtraAttributes(), decoded.getExtraAttributes());
    }

    public void testExtraAttributeEncoding()
    {
        assertExtraAttributeEncoding(pair("a", "b"), pair("a", "b"));
        assertExtraAttributeEncoding(pair(EXTRA_ATTRIBUTE_SOURCE_FILE, "*"), pair(EXTRA_ATTRIBUTE_SOURCE_FILE , "*"));
        assertExtraAttributeEncoding(pair(EXTRA_ATTRIBUTE_STAGE , "*"), pair(EXTRA_ATTRIBUTE_STAGE, "$2a"));
    }

    private void assertExtraAttributeEncoding(String raw, String expected)
    {
        Map<String, String> rawMap = createMap(raw);
        Map<String, String> expectedMap = createMap(expected);
        assertEquals(expectedMap, IvyEncoder.encode(rawMap));
        assertEquals(rawMap, IvyEncoder.decode(expectedMap));
    }

    private String pair(String key, String value)
    {
        return key + KEY_VALUE_DELIMITER + value;
    }

    private Map<String, String> createMap(String... keyValues)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (String keyValue : keyValues)
        {
            String key = keyValue.substring(0, keyValue.indexOf(KEY_VALUE_DELIMITER));
            String value = keyValue.substring(keyValue.indexOf(KEY_VALUE_DELIMITER) + 1);
            map.put(key, value);
        }
        return map;
    }
}
