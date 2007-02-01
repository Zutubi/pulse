package com.zutubi.pulse.upgrade.tasks;

import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.engine.Mapping;
import org.hibernate.type.Type;
import org.hibernate.MappingException;

/**
 *
 *
 */
public class MutableConfiguration extends Configuration
{
    public void addTable(Table table)
    {
        String key = getTableKey(table);
        if (tables.containsKey(key))
        {
            throw new IllegalArgumentException();
        }
        tables.put(key, table);
    }

    private String getTableKey(Table table)
    {
        return table.getSubselect() == null ?
                Table.qualify(table.getCatalog(), table.getSchema(), table.getName()) :
                table.getSubselect();
    }

    public void removeTable(Table table)
    {
        String key = getTableKey(table);
        if(tables.remove(key) == null)
        {
            throw new IllegalArgumentException();
        }
    }

    public Mapping getMapping() {
        return new Mapping() {
            /**
             * Returns the identifier type of a mapped class
             */
            public Type getIdentifierType(String persistentClass) throws MappingException
            {
                PersistentClass pc = ( (PersistentClass) classes.get( persistentClass ) );
                if ( pc == null ) {
                    throw new MappingException( "persistent class not known: " + persistentClass );
                }
                return pc.getIdentifier().getType();
            }

            public String getIdentifierPropertyName(String persistentClass) throws MappingException {
                final PersistentClass pc = (PersistentClass) classes.get( persistentClass );
                if ( pc == null ) {
                    throw new MappingException( "persistent class not known: " + persistentClass );
                }
                if ( !pc.hasIdentifierProperty() ) return null;
                return pc.getIdentifierProperty().getName();
            }

            public Type getReferencedPropertyType(String persistentClass, String propertyName) throws MappingException {
                final PersistentClass pc = (PersistentClass) classes.get( persistentClass );
                if ( pc == null ) {
                    throw new MappingException( "persistent class not known: " + persistentClass );
                }
                Property prop = pc.getReferencedProperty( propertyName );
                if ( prop == null ) {
                    throw new MappingException(
                            "property not known: " +
                            persistentClass + '.' + propertyName
                        );
                }
                return prop.getType();
            }
        };
    }

}
