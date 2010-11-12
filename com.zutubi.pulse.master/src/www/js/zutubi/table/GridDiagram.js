// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * Renders a diagram on a grid using HTML tables.
 *
 * @cfg {String} cls          Class to use for the table (defaults to 'grid')
 * @cfg {String} id           Id to use for the table.
 * @cfg {Mixed}  data         Data used to populate the grid.  Should be an
 *                            array of rows, where each row is an array of
 *                            cells.  Cells have an optional data property,
 *                            which if present should include rowspan, dead
 *                            (i.e. superfluous due to earlier rowspan) and
 *                            classes (CSS).  Cell data is also passed to the
 *                            renderer, so may carry further information.
 * @cfg {String} cellRenderer Function used to render contents of non-dead
 *                            cells.  It is passed the cell id, and the cell
 *                            data, and should return an HTML snippet.
 */
Zutubi.table.GridDiagram = Ext.extend(Ext.BoxComponent, {
    initComponent: function()
    {
        Ext.applyIf(this, {
            cls: 'grid',
            
            template: new Ext.XTemplate(
                '<table id="{id}" class="{cls}">' +
                    '<tbody></tbody>' +
                '</table>'
            ),
            
            cellTemplate: new Ext.XTemplate(
                '<td id="{cellId}" class="{cls}" ' +
                    '<tpl if="rowspan &gt; 1">' +
                        'rowspan="{rowspan}"' +
                    '</tpl>' +
                '>' +
                    '{content}' +
                '</td>'
            )
        });
    
        Zutubi.table.GridDiagram.superclass.initComponent.apply(this, arguments);        
    },
    
    onRender: function(container, position)
    {
        if (position)
        {
            this.el = this.template.insertBefore(position, this, true);    
        }
        else
        {
            this.el = this.template.append(container, this, true);
        }

        this.tbodyEl = this.el.down('tbody');
        
        this.renderData();
        
        Zutubi.table.GridDiagram.superclass.onRender.apply(this, arguments);        
    },

    /**
     * Updates this grid with new data.
     */
    update: function(data)
    {
        this.data = data;
        if (this.rendered)
        {
            this.clear();
            this.renderData();
        }
    },
    
    /**
     * Removes all rows from the grid.
     */
    clear: function()
    {
        this.tbodyEl.select('tr').remove();
    },

    /**
     * Renders the contents of the grid.
     */
    renderData: function()
    {
        if (this.data)
        {
            for (var rowIndex = 0, rowCount = this.data.length; rowIndex < rowCount; rowIndex++)
            {
                // The dummy cell at the start of each row is used to workaround a table border bug
                // in FireFox.
                var rowSource = '<tr><td>&nbsp;</td>';
                var row = this.data[rowIndex];
                for (var colIndex = 0, colCount = row.length; colIndex < colCount; colIndex++)
                {
                    var cell = row[colIndex];
                    var cellId = this.id + '-' + colIndex + '-' + rowIndex;
                    var cellContent = null;
                    var rowspan = 1;
                    var cls = '';
                    var data = cell.data;
                    if (data)
                    {
                        if (!data.dead)
                        {
                            if (data.classes)
                            {
                                cls = data.classes;
                            }
                            
                            if (data.rowspan)
                            {
                                rowspan = data.rowspan;
                            }

                            cellContent = this.cellRenderer(cellId, data);
                        }
                    }
                    else
                    {
                        cellContent = '&nbsp;';
                    }
                    
                    if (cellContent)
                    {
                        rowSource += this.cellTemplate.apply({
                            cellId: cellId,
                            rowspan: rowspan,
                            cls: cls,
                            rowIndex: rowIndex,
                            colIndex: colIndex,
                            content: cellContent
                        });
                    }
                }
                
                rowSource += '</tr>';
                this.tbodyEl.insertHtml('beforeEnd', rowSource);
            }
        }
    }
});

Ext.reg('xzgriddiagram', Zutubi.table.GridDiagram);
