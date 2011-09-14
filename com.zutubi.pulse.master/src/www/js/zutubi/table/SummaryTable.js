// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./ContentTable.js
// dependency: zutubi/KeyValue.js
// dependency: zutubi/toolbar/package.js

/**
 * A table that summarises a collection of items, one item per row.  Multiple fields
 * from each item are shown across multiple columns.
 *
 * @cfg {String}  cls             Class to use for the table (defaults to 'content-table')
 * @cfg {Array}   columns         An array of {Zutubi.KeyValue} configs for all available columns.
 * @cfg {Array}   selectedColumns An array of names of columns that should be displayed (by default,
 *                                all columns are shown in the order they appear in columns).
 * @cfg {Boolean} customisable    If true, the table columns will be customisable.
 * @cfg {String}  saveUrl         Url used to save customised columns.  The chosen columns will be
 *                                sent as a comma-separated string in a parameter named 'columns'.
 * @cfg {String}  saveParams      Optional extra parameters to pass on the Ajax call to save
 *                                customised columns.
 * @cfg {String}  id              Id to use for the table.
 * @cfg {String}  title           Title for the table heading row.
 * @cfg {String}  emptyMessage    Message to show when the table has no rows to display (if not
 *                               specified, the table is hidden in this case).
 */
Zutubi.table.SummaryTable = Ext.extend(Zutubi.table.ContentTable, {
    indexRegex: new RegExp('idx-(\\d+)', ''),
    
    initComponent: function()
    {
        var i, keyValue;

        Ext.applyIf(this, {
            cellTemplate: new Ext.XTemplate(
                '<td class="idx-{index} ' +
                '<tpl if="first">leftmost </tpl>' +
                '<tpl if="last">rightmost </tpl>' +
                '{cls}">{value}</td>'
            ),
            
            headerTemplate: new Ext.XTemplate(
                '<th class="idx-{index} xz-summary-sortable {cls}<tpl if="first"> leftmost</tpl><tpl if="last"> rightmost</tpl>">' +
                    '<span class="xz-summary-remove"><img ext:qtip="remove this column" src="{[window.baseUrl]}/images/delete.gif"/></span>' +
                    '{value}' +
                '</th>'
            )
        });
        
        
        this.keyValues = new Array(this.columns.length);
        for (i = 0; i < this.columns.length; i++)
        {
            keyValue = new Zutubi.KeyValue(this.columns[i]);
            keyValue.component = this;
            this.keyValues[i] = keyValue;
        }
        
        if (this.selectedColumns)
        {
            this.activeColumns = new Array(this.selectedColumns.length);
            for (i = 0; i < this.selectedColumns.length; i++)
            {
                this.activeColumns[i] = this.getKeyValueByName(this.selectedColumns[i]);
            }
        }
        else
        {
            this.activeColumns = new Array(this.keyValues.length);
            for (i = 0; i < this.keyValues.length; i++)
            {
                this.activeColumns[i] = this.keyValues[i];
            }
        }
        
        this.columnCount = this.activeColumns.length;
        
        Zutubi.table.SummaryTable.superclass.initComponent.apply(this, arguments);
        
        if (this.customisable)
        {
            this.on('render', function(table) {        
                table.dragZone = new Zutubi.table.SummaryTableDragZone(table);
                table.dropZone = new Zutubi.table.SummaryTableDropZone(table);
            });
        }
    },
    
    getKeyValueByName: function(name)
    {
        var i, kv;
        
        for (i = 0; i < this.keyValues.length; i++)
        {
            kv = this.keyValues[i];
            if (kv.name === name)
            {
                return kv;
            }
        }
        
        kv = new Zutubi.KeyValue(name);
        kv.component = this;
        this.keyValues.push(kv);
        return kv;
    },
    
    getColumnIndex: function(cellDom)
    {
        var match;
        
        match = cellDom.className.match(this.indexRegex);
        if (match && match[1]){
            return parseInt(match[1], 10);
        }
        
        return -1;
    },
    
    addColumn: function(name)
    {
        this.activeColumns.push(this.getKeyValueByName(name));
        this.updateColumnCount();
        this.renderDynamic();
    },
    
    moveColumn: function(fromIndex, toIndex)
    {
        var low, high, shift, i, l, newColumns;

        if (fromIndex < toIndex)
        {
            low = fromIndex;
            high = toIndex;
            shift = -1;
        }
        else
        {
            low = toIndex;
            high = fromIndex;
            shift = 1;
        }
        
        newColumns = new Array(this.activeColumns.length);
        for (i = 0, l = this.activeColumns.length; i < l; i++)
        {
            if (i < low || i > high)
            {
                newColumns[i] = this.activeColumns[i];
            }
            else if (i === fromIndex)
            {
                newColumns[toIndex] = this.activeColumns[i];
            }
            else
            {
                newColumns[i + shift] = this.activeColumns[i];
            }
        }
        
        this.activeColumns = newColumns;
        this.renderDynamic();
    },
    
    removeColumn: function(index)
    {
        this.activeColumns.splice(index, 1);
        this.updateColumnCount();
        this.renderDynamic();
    },
    
    updateColumnCount: function()
    {
        var heading;

        this.columnCount = this.activeColumns.length;
        heading = this.tbodyEl.child('th.heading');
        heading.dom.colSpan = this.columnCount;
    },
    
    dataExists: function()
    {
        return Zutubi.table.SummaryTable.superclass.dataExists.apply(this, arguments) && this.data.length > 0;
    },
    
    renderData: function()
    {
        var data, i, l, column, th;

        data = {};
        for (i = 0; i < this.activeColumns.length; i++)
        {
            column = this.activeColumns[i];
            data[column.name] = column.key;
        }
        
        this.tbodyEl.insertHtml('beforeEnd', this.generateHeader());
        this.tbodyEl.select('.xz-summary-remove').on('click', function(e) {
            th = e.getTarget().parentNode.parentNode;
            this.removeColumn(this.getColumnIndex(th));
         }, this);

        for (i = 0, l = this.data.length; i < l; i++)
        {
            this.tbodyEl.insertHtml('beforeEnd', this.generateRow(this.data[i]), true);
        }
    },
    
    generateHeader: function()
    {
        var html, i, columnCount, column;

        html = '<tr class="' + Zutubi.table.CLASS_DYNAMIC + '">';
        columnCount = this.activeColumns.length;
        for (i = 0; i < columnCount; i++)
        {
            column = this.activeColumns[i];
            html += this.headerTemplate.apply({
                cls: column.cls || '',
                first: i === 0,
                last: i === columnCount - 1,
                index: i,
                value: column.key
            });
        }
        
        html += '</tr>';
        return html;
    },
    
    generateRow: function(data)
    {
        var html, i, columnCount, column;

        html = '<tr class="' + Zutubi.table.CLASS_DYNAMIC + ' xz-summary-data">';
        columnCount = this.activeColumns.length;
        for (i = 0; i < columnCount; i++)
        {
            column = this.activeColumns[i];
            html += this.cellTemplate.apply({
                cls: column.cls || '',
                first: i === 0,
                last: i === columnCount - 1,
                index: i,
                value: column.getRenderedValue(data)
            });
        }
        
        html += '</tr>';
        return html;
    },

    getAvailableColumnStore: function()
    {
        var i, columnCount, store, column;

        columnCount = this.keyValues.length;
        store = new Array(columnCount);
        for (i = 0; i < columnCount; i++)
        {
            column = this.keyValues[i];
            store[i] = [column.name, column.key];
        }

        store.sort(function(a, b) {
            if (a[1] < b[1])
            {
                return -1;
            }
            else if (a[1] > b[1])
            {
                return 1;
            }
            else
            {
                return 0;
            }
        });
        return store;
    },
    
    getColumnNames: function()
    {
        var names, i, columnCount, column;

        names = '';
        columnCount = this.activeColumns.length;
        for (i = 0; i < columnCount; i++)
        {
            column = this.activeColumns[i];
            if (i > 0)
            {
                names += ',';
            }
            names += column.name;
        }

        return names;
    },
    
    onCustomise: function()
    {
        var headerRow;

        this.toolbar = new Zutubi.table.SummaryTableToolbar({
            table: this
        });
        
        this.el.addClass('xz-summary-customising');
        headerRow = this.tbodyEl.child('tr');
        headerRow.setDisplayed(false);
        this.toolbar.render(this.container, this.el);
    },
    
    onCustomiseComplete: function()
    {
        var headerRow;
        
        this.toolbar.destroy();
        this.el.removeClass('xz-summary-customising');
        headerRow = this.tbodyEl.child('tr');
        headerRow.setDisplayed(true);
        
        this.saveColumns();
    },
    
    saveColumns: function()
    {
        var columnsString, i, l, params;

        if (!this.saveUrl)
        {
            return;
        }
        
        columnsString = '';
        for (i = 0, l = this.activeColumns.length; i < l; i++)
        {
            if (columnsString.length > 0)
            {
                columnsString += ',';
            }
            
            columnsString += this.activeColumns[i].name;
        }
        
        params = this.saveParams || {};
        params.columns = columnsString;
        
        runAjaxRequest({
            url: this.saveUrl,
            params: params
        });
    }
});

/**
 * A toolbar for customising the table columns.
 */
Zutubi.table.SummaryTableToolbar = Ext.extend(Zutubi.toolbar.Toolbar, {
    initComponent: function(container, position)
    {
        var table;

        table = this.table;
        Ext.apply(this, {
            cls: 'xz-summary-tb',
            items: [{
                xtype: 'label',
                html: '<em>Drag and drop existing columns to reorder them.</em>'
            }, '->', {
                xtype: 'label',
                text: 'add column: '
            }, ' ', {
                xtype: 'combo',
                id: table.id + '-new-column',
                store: table.getAvailableColumnStore(),
                forceSelection: true,
                editable: false,
                triggerAction: 'all',
                enableKeyEvents: true,
                listeners: {
                    keypress: function(c, e) {
                        if (e.getKey() === e.RETURN)
                        {
                            var name;
                            if (c.getStore().findExact('field2', c.getRawValue()) < 0)
                            {
                                name = c.getRawValue();
                            }
                            else
                            {
                                name = c.getValue();
                            }

                            if (name)
                            {
                                table.addColumn(name);
                            }
                        }
                    }
                }
            }, {
                xtype: 'button',
                tooltip: 'add selected column',
                icon: window.baseUrl + '/images/add.gif',
                listeners: {
                    click: function() {
                        var name;

                        name = Ext.getCmp(table.id + '-new-column').getValue();
                        if (name)
                        {
                            table.addColumn(name);
                        }
                    }
                }
            }, ' ', {
                xtype: 'button',
                enableToggle: true,
                pressed: true,
                tooltip: 'finish customising',
                icon: window.baseUrl + '/images/pencil.gif',
                listeners: {
                    click: function() {
                        table.customiseComplete();
                    }
                }
            }]
        });
        
        Zutubi.table.SummaryTableToolbar.superclass.initComponent.apply(this, arguments);
    }
});
 
/**
 * A drag zone for sortable column headers.
 */
Zutubi.table.SummaryTableDragZone = Ext.extend(Ext.dd.DragZone, {
    constructor: function(table)
    {
        this.table = table;
        Zutubi.table.SummaryTableDragZone.superclass.constructor.apply(this, [table.getEl()]);
    },
    
    getDragData: function(e)
    {
        var target, d;

        if (!e.getTarget('.xz-summary-remove'))
        {
            target = e.getTarget('.xz-summary-sortable');
            if (target)
            {
                d = target.cloneNode(true);
                d.id = Ext.id();
                return {
                    ddel: d,
                    header: target,
                    repairXY: Ext.fly(target).getXY()
                };
            }
        }
    },
    
    getRepairXY: function()
    {
        return this.dragData.repairXY;
    }
});

/**
 * A drop zone for sortable column headers.
 */
Zutubi.table.SummaryTableDropZone = Ext.extend(Ext.dd.DropZone, {
    constructor: function(table)
    {
        this.table = table;
        
        // The arrows showing where we will drop are based on lightweight code
        // from Ext.grid.HeaderDropZone.
        this.proxyTop = Ext.DomHelper.append(document.body, {
            cls: 'col-move-top',
            html: '&#160;'
        }, true);
        
        this.proxyBottom = Ext.DomHelper.append(document.body, {
            cls: 'col-move-bottom',
            html: '&#160;'
        }, true);

        this.proxyTop.hide = this.proxyBottom.hide = function() {
            this.setLeftTop(-100,-100);
            this.setStyle('visibility', 'hidden');
        };
            
        Zutubi.table.SummaryTableDropZone.superclass.constructor.apply(this, [table.getEl()]);
    },
    
    getTargetFromEvent: function(e)
    {
        var x, sortables, dom;

        x = e.getPageX();
        sortables = this.table.getEl().select('.xz-summary-sortable');
        dom = null;
        sortables.each(function(el) {
            if (!dom && el.getLeft() <= x && el.getRight() >= x)
            {
                dom = el.dom;
            }
        });
        
        return dom;
    },
    
    isLeft: function(node, event)
    {
        var el, middle;

        el = Ext.get(node);
        middle = el.getLeft() + el.getWidth() / 2;
        return event.getPageX() < middle;
    },
    
    showPosition: function(node, event)
    {
        var side;

        side = this.isLeft(node, event) ? 'l' : 'r';
        this.proxyTop.alignTo(node, 'b-t' + side);
        this.proxyTop.show();
        this.proxyBottom.alignTo(node, 't-b' + side);
        this.proxyBottom.show();
    },
    
    onNodeEnter: function(node, source, event, data)
    {
        this.showPosition(node, event);
    },
    
    onNodeOver: function(node, source, event, data)
    {
        this.showPosition(node, event);
        return Ext.dd.DropZone.prototype.dropAllowed;
    },
    
    onNodeOut: function(node, source, event, data)
    {
        this.proxyTop.hide();
        this.proxyBottom.hide();
    },

    onNodeDrop: function(node, source, event, data)
    {
        var initialIndex, newIndex;

        initialIndex = this.table.getColumnIndex(data.header);
        newIndex = this.table.getColumnIndex(node);
        if (initialIndex >= 0 && newIndex >= 0)
        {
            if (!this.isLeft(node, event) && newIndex <= this.table.columnCount)
            {
                newIndex++;
            }
            
            if (initialIndex < newIndex)
            {
                newIndex--;
            }
            
            if (initialIndex === newIndex)
            {
                return false;
            }
            else
            {
                this.table.moveColumn(initialIndex, newIndex);
            }
            
            return true;
        }
        else
        {
            return false;
        }
    }
});

Ext.reg('xzsummarytable', Zutubi.table.SummaryTable);
