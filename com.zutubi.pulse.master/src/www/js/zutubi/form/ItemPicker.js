// dependency: ./namespace.js
// dependency: ext/package.js

Zutubi.form.ItemPicker = function(config)
{
    Zutubi.form.ItemPicker.superclass.constructor.call(this, config);
    this.addEvents({
        'change': true
    });
    this.hiddenName = config.name;
};

Ext.extend(Zutubi.form.ItemPicker, Ext.form.Field, {
    width: 100,
    height: 130,
    displayField: 'text',
    valueField: 'value',
    selectedClass: 'x-item-picker-selected',
    hiddenFields: [],
    allowReordering: true,
    value: [],
    optionStore: undefined,
    defaultAutoCreate: {tag: 'div', cls: 'x-item-picker', tabindex: '0', style:"outline:none;border:0px"},

    onRender: function(ct, position)
    {
        Zutubi.form.ItemPicker.superclass.onRender.call(this, ct, position);
        this.el.on('click', this.onClick, this);

        if(this.optionStore)
        {
            this.combo = new Ext.form.ComboBox({
                store: this.optionStore,
                mode: 'local',
                forceSelection: true,
                name: 'combo.' + this.name,
                displayField: this.displayField,
                valueField: this.valueField,
                triggerAction: 'all',
                id: this.id + '.choice'
            });
            this.choice = this.combo;
        }
        else
        {
            this.input = new Ext.form.TextField({
                tag: 'input',
                type: 'text',
                cls: 'x-form-text',
                id: this.id + '.choice'
            });
            this.choice = this.input;
        }

        var cls = 'x-item-picker';
        if(!this.tpl)
        {
            if(Ext.isIE || Ext.isIE7)
            {
                this.tpl = '<tpl for="."><div unselectable="on" class="' + cls + '-item">{' + this.displayField + '}</div></tpl>';
            }
            else
            {
                this.tpl = '<tpl for="."><div class="' + cls + '-item  x-unselectable">{' + this.displayField + '}</div></tpl>';
            }
        }

        this.view = new Ext.DataView({
            cls: 'x-item-picker-list',
            tpl: this.tpl,
            itemSelector: '.x-item-picker-item',
            singleSelect: true,
            store: this.store,
            selectedClass: this.selectedClass
        });

        this.ValueRecord = Ext.data.Record.create({name: 'text'}, {name: 'value'});

        // spacing for a gap between the buttons and the widget:
        // 1) buttons are 22 px wide.
        // 2) give them 26 px and place them in the middle, giving 2px padding on the left
        // 3) place them such that the right edge of the button lines up with the desired width.
        // note: align right in the icon panel would be nice but does not work.

        var icons = new Ext.Panel({
            border:false,
            header:false,
            width:26,
            layout:"vbox",
            layoutConfig:
            {
                align:"center"
            }
        });

        this.removeButton = new Ext.Button({
            id: 'x-item-picker-remove',
            icon: window.baseUrl + '/images/buttons/sb-delete.gif'
        });
        this.removeButton.on('click', this.onRemove, this);
        icons.add(this.removeButton);
        icons.add({xtype:'spacer',flex:1});

        if (this.allowReordering)
        {
            this.upButton = new Ext.Button({
                id: 'x-item-picker-up',
                icon: window.baseUrl + '/images/buttons/sb-up.gif'
            });
            this.upButton.on('click', this.onUp, this);
            icons.add(this.upButton);
            icons.add({xtype:'spacer',flex:1});
            this.downButton = new Ext.Button({
                id: 'x-item-picker-down',
                icon: window.baseUrl + '/images/buttons/sb-down.gif'
            });
            this.downButton.on('click', this.onDown, this);
            icons.add(this.downButton);
            icons.add({xtype:'spacer',flex:1});
        }

        this.addButton = new Ext.Button({
            id: 'x-item-picker-add',
            icon: window.baseUrl + '/images/buttons/sb-add.gif'
        });
        this.addButton.on('click', this.onAdd, this);
        icons.add(this.addButton);

        this.view.flex = 1;
        var internalPanel = new Ext.Container({
            width:this.width - 24,
            layout:"vbox",
            layoutConfig:{align:"stretch"},
            items:[this.view,this.choice]
        });

        this.panel = new Ext.Panel({
            border:false,
            height:this.height,
            layout:"hbox",
            layoutConfig:{align:"stretch"},
            items:[internalPanel,icons]
        });
        this.panel.render(this.el);

        this.nav = new Ext.KeyNav( (this.input) ? this.input.el : this.el, {
            "up": function(evt)
            {
                this.navUp(evt.ctrlKey);
            },

            "down": function(evt)
            {
                this.navDown(evt.ctrlKey);
            },

            "enter": function(evt)
            {
                if(this.input)
                {
                    this.onAdd(evt);
                }
            },

            scope: this
        });
    },

    doLayout: function()
    {
        if(this.rendered)
        {
            this.panel.doLayout();
        }
    },

    afterRender: function()
    {
        Zutubi.form.ItemPicker.superclass.afterRender.call(this);
        if(this.value)
        {
            this.setValue(this.value);
            this.originalValue = this.value;
        }
    },

    onClick: function(evt)
    {
        if (!this.disabled && this.input)
        {
            this.input.focus();
        }
    },

    navUp: function(ctrl)
    {
        if(this.allowReordering && ctrl)
        {
            this.onUp();
        }
        else
        {
            var selected = this.getSelection();
            if(selected > 0)
            {
                this.view.select(selected - 1);
            }
        }

        this.ensureSelectionVisible();
    },

    navDown: function(ctrl)
    {
        if(this.allowReordering && ctrl)
        {
            this.onDown();
        }
        else
        {
            var selected = this.getSelection();
            if(selected >= 0 && selected < this.store.getCount() - 1)
            {
                this.view.select(selected + 1);
            }
        }

        this.ensureSelectionVisible();
    },

    onAdd: function(evt)
    {
        var text = '';
        var value;

        if(this.input)
        {
            text = this.input.getValue();
            this.input.setValue('');
            value = text;
        }
        else
        {
            var selectedIndexes = this.combo.view.getSelectedIndexes();
            if(selectedIndexes.length > 0)
            {
                var record = this.combo.store.getAt(selectedIndexes[0]);
                if(record)
                {
                    text = record.get(this.displayField);
                    value = record.get(this.valueField);
                }
            }
        }

        if(text.length > 0)
        {
            var index = this.appendItem(text, value);
            this.view.select(index);
            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    appendItem: function(text, value)
    {
        var r = new this.ValueRecord({text: text, value: value});
        if (this.allowReordering)
        {
            this.store.add(r);
        }
        else
        {
            this.store.addSorted(r);
        }

        this.hiddenFields.push(this.el.createChild({tag: 'input', type: 'hidden', name: this.name, value: Ext.util.Format.htmlEncode(value)}));
        return this.store.indexOf(r);
    },

    onRemove: function(evt)
    {
        var selected = this.getSelection();
        if(selected >= 0)
        {
            this.store.remove(this.store.getAt(selected));
            this.hiddenFields[selected].remove();
            this.hiddenFields.splice(selected, 1);
            this.fireEvent('change', this, evt);
        }
    },

    onUp: function(evt)
    {
        var selected = this.getSelection();
        if(selected > 0)
        {
            var record = this.store.getAt(selected);
            this.store.remove(record);
            this.store.insert(selected - 1, record);
            this.view.select(selected - 1);

            var hidden = this.hiddenFields.splice(selected, 1)[0];
            this.hiddenFields.splice(selected - 1, 0, hidden);
            hidden.insertBefore(this.hiddenFields[selected]);

            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    onDown: function(evt)
    {
        var selected = this.getSelection();
        if(selected >= 0 && selected < this.store.getCount() - 1)
        {
            var record = this.store.getAt(selected);
            this.store.remove(record);
            this.store.insert(selected + 1, record);
            this.view.select(selected + 1);

            var hidden = this.hiddenFields.splice(selected, 1)[0];
            this.hiddenFields.splice(selected + 1, 0, hidden);
            hidden.insertAfter(this.hiddenFields[selected]);

            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    ensureSelectionVisible: function()
    {
        var nodes = this.view.getSelectedNodes();
        if(nodes.length > 0)
        {
            var selectedEl = Ext.get(nodes[0]);
            selectedEl.scrollIntoView(this.el);
        }
    },

    getValue: function()
    {
        var value = [];
        this.store.each(function(r) { value.push(r.get('value')); });
        return value;
    },

    setValue: function(value)
    {
        if(!this.ValueRecord)
        {
            // Superclass onRender calls before we are ready.
            return;
        }

        this.store.removeAll();
        var i;
        for(i = 0; i < this.hiddenFields.length; i++)
        {
            this.hiddenFields[i].remove();
        }
        this.hiddenFields = [];

        for(i = 0; i < value.length; i++)
        {
            var text = this.getTextForValue(value[i]);
            if(text)
            {
                this.appendItem(text, value[i]);
            }
        }
    },

    /**
     * Clear / reset the values of the item picker to an empty list.
     */
    clear: function()
    {
        if(!this.ValueRecord)
        {
            // Superclass onRender calls before we are ready.
            return;
        }

        this.store.removeAll();
        for(i = 0; i < this.hiddenFields.length; i++)
        {
            this.hiddenFields[i].remove();
        }
        this.hiddenFields = [];
    },

    /**
     * Use the response data to define the options.
     * @param response  data to be used as options
     * @param append    if true, the data will be appended to the existing options.
     * If false, the existing options will be replaced.
     */
    loadOptions: function(response, append)
    {
        this.optionStore.loadData(response, append);
    },

    /**
     * Get a list of the current option keys.
     */
    getOptionValues: function()
    {
        var optionValues = [];
        this.optionStore.each(function(record)
        {
            optionValues.push(record.data.value);
        });
        return optionValues;
    },

    getTextForValue: function(value)
    {
        if(this.input)
        {
            return value;
        }
        else
        {
            var record = this.optionStore.data.find(function(r) { return r.get(this.valueField) == value; }, this);
            if(record)
            {
                return record.get(this.displayField);
            }
            else
            {
                return null;
            }
        }
    },

    getSelection: function()
    {
        var selections = this.view.getSelectedIndexes();
        if(selections.length > 0)
        {
            return selections[0];
        }
        else
        {
            return -1;
        }
    },

    onDisable: function()
    {
        Zutubi.form.ItemPicker.superclass.onDisable.call(this);
        if(this.input)
        {
            this.input.disable();
        }
        else
        {
            this.combo.disable();
        }

        this.addButton.disable();
        this.removeButton.disable();
        this.upButton.disable();
        this.downButton.disable();
        this.view.disabled = true;
    },

    onEnable: function()
    {
        Zutubi.form.ItemPicker.superclass.onEnable.call(this);
        if(this.input)
        {
            this.input.enable();
        }
        else
        {
            this.combo.enable();
        }

        this.addButton.enable();
        this.removeButton.enable();
        this.upButton.enable();
        this.downButton.enable();
        this.view.disabled = false;
    }
});
