package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 * @deprecated
 */
@SymbolicName("basics")
public class BasicTypes
{
    private Boolean booleanO;
    private boolean booleanP;
    private Byte byteO;
    private byte byteP;
    private Character characterO;
    private char characterP;
    private Double doubleO;
    private double doubleP;
    private Float floatO;
    private float floatP;
    private Integer integerO;
    private int integerP;
    private Long longO;
    private long longP;
    private Short shortO;
    private short shortP;
    private String string;

    public Boolean getBooleanO()
    {
        return booleanO;
    }

    public void setBooleanO(Boolean booleanO)
    {
        this.booleanO = booleanO;
    }

    public boolean isBooleanP()
    {
        return booleanP;
    }

    public void setBooleanP(boolean booleanP)
    {
        this.booleanP = booleanP;
    }

    public Byte getByteO()
    {
        return byteO;
    }

    public void setByteO(Byte byteO)
    {
        this.byteO = byteO;
    }

    public byte getByteP()
    {
        return byteP;
    }

    public void setByteP(byte byteP)
    {
        this.byteP = byteP;
    }

    public Character getCharacterO()
    {
        return characterO;
    }

    public void setCharacterO(Character characterO)
    {
        this.characterO = characterO;
    }

    public char getCharacterP()
    {
        return characterP;
    }

    public void setCharacterP(char characterP)
    {
        this.characterP = characterP;
    }

    public Double getDoubleO()
    {
        return doubleO;
    }

    public void setDoubleO(Double doubleO)
    {
        this.doubleO = doubleO;
    }

    public double getDoubleP()
    {
        return doubleP;
    }

    public void setDoubleP(double doubleP)
    {
        this.doubleP = doubleP;
    }

    public Float getFloatO()
    {
        return floatO;
    }

    public void setFloatO(Float floatO)
    {
        this.floatO = floatO;
    }

    public float getFloatP()
    {
        return floatP;
    }

    public void setFloatP(float floatP)
    {
        this.floatP = floatP;
    }

    public Integer getIntegerO()
    {
        return integerO;
    }

    public void setIntegerO(Integer integerO)
    {
        this.integerO = integerO;
    }

    public int getIntegerP()
    {
        return integerP;
    }

    public void setIntegerP(int integerP)
    {
        this.integerP = integerP;
    }

    public Long getLongO()
    {
        return longO;
    }

    public void setLongO(Long longO)
    {
        this.longO = longO;
    }

    public long getLongP()
    {
        return longP;
    }

    public void setLongP(long longP)
    {
        this.longP = longP;
    }

    public Short getShortO()
    {
        return shortO;
    }

    public void setShortO(Short shortO)
    {
        this.shortO = shortO;
    }

    public short getShortP()
    {
        return shortP;
    }

    public void setShortP(short shortP)
    {
        this.shortP = shortP;
    }

    public String getString()
    {
        return string;
    }

    public void setString(String string)
    {
        this.string = string;
    }


    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        BasicTypes that = (BasicTypes) o;

        if (booleanP != that.booleanP)
        {
            return false;
        }
        if (byteP != that.byteP)
        {
            return false;
        }
        if (characterP != that.characterP)
        {
            return false;
        }
        if (Double.compare(that.doubleP, doubleP) != 0)
        {
            return false;
        }
        if (Float.compare(that.floatP, floatP) != 0)
        {
            return false;
        }
        if (integerP != that.integerP)
        {
            return false;
        }
        if (longP != that.longP)
        {
            return false;
        }
        if (shortP != that.shortP)
        {
            return false;
        }
        if (booleanO != null ? !booleanO.equals(that.booleanO) : that.booleanO != null)
        {
            return false;
        }
        if (byteO != null ? !byteO.equals(that.byteO) : that.byteO != null)
        {
            return false;
        }
        if (characterO != null ? !characterO.equals(that.characterO) : that.characterO != null)
        {
            return false;
        }
        if (doubleO != null ? !doubleO.equals(that.doubleO) : that.doubleO != null)
        {
            return false;
        }
        if (floatO != null ? !floatO.equals(that.floatO) : that.floatO != null)
        {
            return false;
        }
        if (integerO != null ? !integerO.equals(that.integerO) : that.integerO != null)
        {
            return false;
        }
        if (longO != null ? !longO.equals(that.longO) : that.longO != null)
        {
            return false;
        }
        if (shortO != null ? !shortO.equals(that.shortO) : that.shortO != null)
        {
            return false;
        }
        if (string != null ? !string.equals(that.string) : that.string != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        long temp;
        result = (booleanO != null ? booleanO.hashCode() : 0);
        result = 31 * result + (booleanP ? 1 : 0);
        result = 31 * result + (byteO != null ? byteO.hashCode() : 0);
        result = 31 * result + (int) byteP;
        result = 31 * result + (characterO != null ? characterO.hashCode() : 0);
        result = 31 * result + (int) characterP;
        result = 31 * result + (doubleO != null ? doubleO.hashCode() : 0);
        temp = doubleP != +0.0d ? Double.doubleToLongBits(doubleP) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (floatO != null ? floatO.hashCode() : 0);
        result = 31 * result + floatP != +0.0f ? Float.floatToIntBits(floatP) : 0;
        result = 31 * result + (integerO != null ? integerO.hashCode() : 0);
        result = 31 * result + integerP;
        result = 31 * result + (longO != null ? longO.hashCode() : 0);
        result = 31 * result + (int) (longP ^ (longP >>> 32));
        result = 31 * result + (shortO != null ? shortO.hashCode() : 0);
        result = 31 * result + (int) shortP;
        result = 31 * result + (string != null ? string.hashCode() : 0);
        return result;
    }
}
