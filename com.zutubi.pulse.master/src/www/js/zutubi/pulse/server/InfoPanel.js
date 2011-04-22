// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/ActivePanel.js
// dependency: zutubi/table/KeyValueTable.js
// dependency: zutubi/pulse/project/namespace.js

/**
 * The content of the server and agent info tabs.  Expects data of the form:
 *
 * {
 *     serverProperties: { key: value, ... },
 *     pulseProperties: { key: value, ... },
 *   [ jvmProperties: { key: value, ... },
 *     environment: { key: value, ... } ]
 * }
 *
 * JVM and environment properties are only visible to system administrators.
 */
Zutubi.pulse.server.InfoPanel = Ext.extend(Zutubi.ActivePanel, {
    border: false,
    autoScroll: true,
    
    dataKeys: ['serverProperties', 'pulseProperties', 'jvmProperties', 'environment'],
    
    initComponent: function(container, position)
    {
        var panel = this;
        Ext.apply(this, {
            layout: 'vtable',
            contentEl: 'center',
            items: [{
                xtype: 'container',
                layout: 'htable',
                items: [{
                    xtype: 'xzkeyvaluetable',
                    id: this.id + '-pulseProperties',
                    title: 'pulse configuration'
                }, {
                    xtype: 'box'
                }, {
                    xtype: 'xzkeyvaluetable',
                    id: this.id + '-serverProperties',
                    title: 'server information'
                }]
            }, {
                xtype: 'container',
                layout: 'vtable',
                style: 'padding: 0 17px',
                items: [{
                    xtype: 'xzkeyvaluetable',
                    id: this.id + '-environment',
                    title: 'environment variables'
                }, {
                    xtype: 'xzkeyvaluetable',
                    id: this.id + '-jvmProperties',
                    title: 'jvm system properties'
                }]
            }]
        });

        Zutubi.pulse.server.InfoPanel.superclass.initComponent.apply(this, arguments);
}});
