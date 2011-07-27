// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * @class Zutubi.form.Select
 * @extends Ext.form.Field
 * Basic select box.  Supports multi-select, which is not available in Ext
 * currently.
 * @constructor
 * Creates a new TextField
 * @param {Object} config Configuration options
 */
Zutubi.form.Select = function(config)
{
    Zutubi.form.Select.superclass.constructor.call(this, config);
    this.addEvents({
        'change': true
    });
    this.hiddenName = config.name;
};

Ext.extend(Zutubi.form.Select, Ext.form.Field, {
    /**
     * @cfg {Ext.data.Store} store The data defining select items.
     * selections.
     */
    store: undefined,
    /**
     * @cfg {Boolean} multiple True if this field should allow multiple
     * selections.
     */
    multiple: false,
    /**
     * @cfg {Number} size The size of the select element.
     */
    size: undefined,
    /**
     * @cfg {String} displayField The store field to use as item text.
     */
    displayField: 'text',
    /**
     * @cfg {String} valueField The store field to use as item values.
     */
    valueField: 'value',
    hiddenFields: {},
    entryHeight: 22,

    onRender: function(ct, position)
    {
        this.el = ct.createChild({tag: 'div', cls: 'x-select', id: this.id});
        if(!this.tpl){
            if(Ext.isIE || Ext.isIE7)
            {
                this.tpl = '<tpl for="."><div unselectable="on" class="x-select-item" tabindex="-1">{' + this.displayField + ':htmlEncode}</div></tpl>';
            }
            else
            {
                this.tpl = '<tpl for="."><div class="x-select-item  x-unselectable" tabindex="-1">{' + this.displayField + ':htmlEncode}</div></tpl>';
            }
        }

        this.view = new Ext.DataView({
            renderTo: this.el,
            tpl: this.tpl,
            itemSelector: '.x-select-item',
            multiSelect: this.multiple,
            store: this.store,
            selectedClass: 'x-select-selected'
        });

        var count = this.store.getCount();
        if(!this.size)
        {
            if(count < 3)
            {
                this.size = 3;
            }
            else if(count > 10)
            {
                this.size = 10;
            }
            else
            {
                this.size = count;
            }
        }

        if(count > 0)
        {
            this.entryHeight = this.el.child("div.x-select-item").getHeight() + 2;
        }

        this.el.setHeight(this.entryHeight * this.size);
        this.view.on('selectionchange', this.onSelectionChange, this);
    },

    initValue: function()
    {
        this.setValue(this.value);
        this.originalValue = this.getValue();
    },

    onSelectionChange: function()
    {
        this.updateHiddenFields();
        this.fireEvent('change');
    },

    updateHiddenFields: function()
    {
        var value = this.getValue();
        var valueMap = {};
        var i;
        var key;

        for(i = 0; i < value.length; i++)
        {
            var iv = value[i];
            valueMap[iv] = true;
            if(!this.hiddenFields[iv])
            {
                this.addHiddenField(iv);
            }
        }

        for(key in this.hiddenFields)
        {
            if(!valueMap[key])
            {
                this.hiddenFields[key].remove();
                delete this.hiddenFields[key];
            }
        }
    },

    addHiddenField: function(value)
    {
        this.hiddenFields[value] = this.el.createChild({tag: 'input', type: 'hidden', name: this.name, value: value});
    },

    findRecord: function(prop, value)
    {
        var record;
        if(this.store.getCount() > 0)
        {
            this.store.each(function(r) {
                if(r.data[prop] == value)
                {
                    record = r;
                    return false;
                }
            });
        }
        return record;
    },

    getValue: function()
    {
        var value = [];
        var selections = this.view.getSelectedIndexes();
        var i;
        for(i = 0; i < selections.length; i++)
        {
            value.push(this.store.getAt(selections[i]).get(this.valueField));
        }

        // Ordering does not matter, and always sorting helps simplify
        // dirty-checking.
        value.sort();
        return value;
    },

    setValue: function(value)
    {
        var key;
        var i;
        
        this.view.clearSelections();
        for(key in this.hiddenFields)
        {
            this.hiddenFields[key].remove();
        }
        this.hiddenFields = {};

        for(i = 0; i < value.length; i++)
        {
            var record = this.findRecord(this.valueField, value[i]);
            if (record)
            {
                this.view.select(this.store.indexOf(record), true, true);
                this.addHiddenField(value[i]);
            }
        }
    },

    onDisable: function()
    {
        Zutubi.form.Select.superclass.onDisable.call(this);
        this.view.disabled = true;
    },

    onEnable: function()
    {
        Zutubi.form.Select.superclass.onEnable.call(this);
        this.view.disabled = false;
    }
});
