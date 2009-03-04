package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A composite type with properties of all flavours.
 */
@SymbolicName("mixed")
public class MixedConfiguration extends AbstractNamedConfiguration
{
    private int intProperty;
    private String stringProperty;
    private TestEnum enumProperty;
    @Reference
    private TrivialConfiguration referenceProperty;
    private TrivialConfiguration compositeProperty;

    private List<String> stringList = new LinkedList<String>();
    @Addable(value = "addableStringListItem", attribute = "str")
    private List<String> addableStringList = new LinkedList<String>();
    @Addable(value = "contentStringListItem", attribute = "")
    private List<String> contentStringList = new LinkedList<String>();
    private List<TestEnum> enumList = new LinkedList<TestEnum>();
    @Reference @Addable("referenceListItem")
    private List<TrivialConfiguration> referenceList = new LinkedList<TrivialConfiguration>();
    @Reference @Addable(value = "contentReferenceListItem", attribute = "")
    private List<TrivialConfiguration> contentReferenceList = new LinkedList<TrivialConfiguration>();
    @Addable("compositeMapItem")
    private Map<String, TrivialConfiguration> compositeMap = new HashMap<String, TrivialConfiguration>();
    private Map<String, ExtendableConfiguration> extendableMap = new HashMap<String, ExtendableConfiguration>();

    public int getIntProperty()
    {
        return intProperty;
    }

    public void setIntProperty(int intProperty)
    {
        this.intProperty = intProperty;
    }

    public String getStringProperty()
    {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty)
    {
        this.stringProperty = stringProperty;
    }

    public TestEnum getEnumProperty()
    {
        return enumProperty;
    }

    public void setEnumProperty(TestEnum enumProperty)
    {
        this.enumProperty = enumProperty;
    }

    public TrivialConfiguration getReferenceProperty()
    {
        return referenceProperty;
    }

    public void setReferenceProperty(TrivialConfiguration referenceProperty)
    {
        this.referenceProperty = referenceProperty;
    }

    public TrivialConfiguration getCompositeProperty()
    {
        return compositeProperty;
    }

    public void setCompositeProperty(TrivialConfiguration compositeProperty)
    {
        this.compositeProperty = compositeProperty;
    }

    public List<String> getStringList()
    {
        return stringList;
    }

    public void setStringList(List<String> stringList)
    {
        this.stringList = stringList;
    }

    public List<String> getAddableStringList()
    {
        return addableStringList;
    }

    public void setAddableStringList(List<String> addableStringList)
    {
        this.addableStringList = addableStringList;
    }

    public List<String> getContentStringList()
    {
        return contentStringList;
    }

    public void setContentStringList(List<String> contentStringList)
    {
        this.contentStringList = contentStringList;
    }

    public List<TestEnum> getEnumList()
    {
        return enumList;
    }

    public void setEnumList(List<TestEnum> enumList)
    {
        this.enumList = enumList;
    }

    public List<TrivialConfiguration> getReferenceList()
    {
        return referenceList;
    }

    public void setReferenceList(List<TrivialConfiguration> referenceList)
    {
        this.referenceList = referenceList;
    }

    public List<TrivialConfiguration> getContentReferenceList()
    {
        return contentReferenceList;
    }

    public void setContentReferenceList(List<TrivialConfiguration> contentReferenceList)
    {
        this.contentReferenceList = contentReferenceList;
    }

    public Map<String, TrivialConfiguration> getCompositeMap()
    {
        return compositeMap;
    }

    public void setCompositeMap(Map<String, TrivialConfiguration> compositeMap)
    {
        this.compositeMap = compositeMap;
    }

    public Map<String, ExtendableConfiguration> getExtendableMap()
    {
        return extendableMap;
    }

    public void setExtendableMap(Map<String, ExtendableConfiguration> extendableMap)
    {
        this.extendableMap = extendableMap;
    }
}
