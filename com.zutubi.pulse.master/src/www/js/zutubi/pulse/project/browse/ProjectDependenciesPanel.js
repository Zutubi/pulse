// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: zutubi/table/GridDiagram.js
// dependency: zutubi/pulse/SectionHeading.js

/**
 * The content of the project dependencies page.  Expects data of the form:
 * {
 *     transitiveMode: 'TRIM_DUPLICATES',
 *     upstream: [grid data],
 *     downstream: [grid data],
 * }
 */
Zutubi.pulse.project.browse.ProjectDependenciesPanel = Ext.extend(Ext.Panel, {
    border: false,
    autoScroll: true,
    
    dataKeys: ['upstream', 'downstream'],
    
    contentTemplate: new Ext.XTemplate(
        '<a id="{cellId}-{htmlName}" href="{url}">{name:htmlEncode}</a><br/> ' +
        '<img alt="health" src="{[window.baseUrl]}/images/health/{health}.gif"> {health} <span class="understated">//</span> ' +
        '<tpl if="building">' +
            '<img alt="building" src="{[window.baseUrl]}/images/inprogress.gif"/> ' +
        '</tpl>' +
        '{state}'
    ),
                
    cellRenderer: function(cellId, data) {
        if (data.name)
        {
            var args = Ext.apply({}, data, {cellId: cellId, building: data.state && data.state == 'building'});
            return Zutubi.pulse.project.browse.ProjectDependenciesPanel.prototype.contentTemplate.apply(args);
        }
        else
        {
            return '&nbsp;';
        }
    },
    
    initComponent: function(container, position) {
        var panel = this;
        Ext.apply(this, {
            items: [{
                xtype: 'panel',
                border: false,
                id: 'dependencies-inner',
                layout: 'vtable',
                contentEl: 'center',
                tbar: {
                    id: 'build-toolbar',
                    style: 'margin: 17px 17px 0 17px; padding: 2px 2px 2px 6px',
                    items: [{
                        xtype: 'label',
                        text: 'transitive dependencies:'
                    }, ' ', {
                        xtype: 'combo',
                        id: 'dependencies-transitive-mode',
                        editable: false,
                        forceSelection: true,
                        triggerAction: 'all',
                        width: 230,
                        store: [
                            ['FULL', 'show full cascade'],
                            ['TRIM_DUPLICATES', 'trim/shade duplicate subtrees'],
                            ['REMOVE_DUPLICATES', 'remove duplicate subtrees'],
                            ['NONE', 'show direct dependencies only']
                        ],
                        value: this.data.transitiveMode,
                        listeners: {
                            select: function(combo) {
                                panel.loading = true;
                                panel.el.mask('Updating...');
                                Ext.Ajax.request({
                                    url: window.baseUrl + '/ajax/setDependenciesTransitiveMode.action',
                                    params: {
                                        mode: combo.getValue()
                                    },
                                    success: function() {
                                        refresh(function() {
                                            panel.el.unmask();
                                            panel.loading = false;
                                        });
                                    },
                                    failure: function() {
                                        panel.el.unmask();
                                        showStatus('Could not update transitive dependencies mode.', 'failure');
                                        panel.loading = false;
                                    }
                                });
                            }
                        }
                    }, '->', {
                        xtype: 'label',
                        text: 'legend:'
                    }, ' ', {
                        xtype: 'box',
                        style: 'background: #fff; border: solid 1px #bbb; margin-left: 6px',
                        autoEl: {
                            tag: 'table',
                            html: '<tr>' +
                                      '<td class="glegend">' +
                                          '<table class="grid" style="display: inline"><tr><td class="gbox ghighlighted">this project</td></tr></table>' +
                                      '</td>' +
                                      '<td class="glegend">' +
                                          '<table class="grid" style="display: inline"><tr><td class="gbox">normal dependency</td></tr></table>' +
                                      '</td>' +
                                      '<td class="glegend">' +
                                          '<table class="grid" style="display: inline"><tr><td class="gunderstated">trimmed subtree root</td></tr></table>' +
                                      '</td>' +
                                  '</tr>'
                        }
                    }]
                },
                items: [{
                    xtype: 'xzsectionheading',
                    text: 'upstream'
                }, {
                    xtype: 'box',
                    autoEl: {
                        tag: 'p',
                        style: 'margin: 0 17px',
                        html: 'Projects that this project depends on:'
                    }
                }, {
                    id: 'dependencies-upstream',
                    xtype: 'xzgriddiagram',
                    style: 'margin-left: 17px',
                    cellRenderer: this.cellRenderer,
                    data: this.data.upstream
                }, {
                    xtype: 'xzsectionheading',
                    text: 'downstream'
                }, {
                    xtype: 'box',
                    autoEl: {
                        tag: 'p',
                        style: 'margin: 0 17px',
                        html: 'Projects that depend on this project:'
                    }
                }, {
                    id: 'dependencies-downstream',
                    xtype: 'xzgriddiagram',
                    style: 'margin-left: 17px',
                    cellRenderer: this.cellRenderer,
                    data: this.data.downstream
                }]
            }]
        });

        Zutubi.pulse.project.browse.ProjectDependenciesPanel.superclass.initComponent.apply(this, arguments);
    },
    
    update: function(data) {
        this.data = data;
        for (var i = 0, l = this.dataKeys.length; i < l; i++)
        {
            var key = this.dataKeys[i];
            Ext.getCmp('dependencies-' + key).update(data[key]);    
        }
    }
});
