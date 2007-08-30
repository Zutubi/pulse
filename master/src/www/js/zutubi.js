// setup the namespace for the zutubi components.
var ZUTUBI = window.ZUTUBI || {};

// if it is already defined, great, otherwise create it.
ZUTUBI.widget = ZUTUBI.widget || {};

/**
 * @class ZUTUBI.Select
 * @extends Ext.form.Field
 * Basic select box.  Supports multi-select, which is not available in Ext
 * currently.
 * @constructor
 * Creates a new TextField
 * @param {Object} config Configuration options
 */
ZUTUBI.Select = function(config)
{
    ZUTUBI.Select.superclass.constructor.call(this, config);
    this.viewRendered = false;
};

Ext.extend(ZUTUBI.Select, Ext.form.Field, {
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
    size : undefined,
    /**
     * @cfg {String} displayField The store field to use as item text.
     */
    displayField : 'text',
    /**
     * @cfg {String} valueField The store field to use as item values.
     */
    valueField : 'value',

    onRender : function(ct, position){
        if(!this.size)
        {
            if(this.multiple)
            {
                var total = this.store.getTotalCount();
                // Make the min size 3 (looks like a select) and max size 10
                this.size = total > 10 ? 10 : (total < 3 ? 3: total);
            }
            else
            {
                this.size = 1;
            }
        }
        if(!this.el)
        {
            this.defaultAutoCreate = {tag: 'select', size: this.size };
            if(this.multiple)
            {
                this.defaultAutoCreate.multiple = 'true';
            }
        }
        ZUTUBI.Select.superclass.onRender.call(this, ct, position);

        if(!this.tpl){
            this.tpl = '<option value="{' + this.valueField + '}">{' + this.displayField + '}</option>';
        }

        this.view = new Ext.View(this.el, this.tpl, {
            singleSelect :!this.multiple,
            store: this.store
        });

        this.viewRendered = true;
        this.initValue();

        //this.view.on('click', this.onViewClick, this);
        //this.store.on('beforeload', this.onBeforeLoad, this);
        //this.store.on('load', this.onLoad, this);
        //this.store.on('loadexception', this.collapse, this);
    },

    initValue: function()
    {
        if(!this.viewRendered)
        {
            return;
        }

        this.setValue(this.value);
    },

    findRecord : function(prop, value){
        var record;
        if(this.store.getCount() > 0){
            this.store.each(function(r){
                if(r.data[prop] == value){
                    record = r;
                    return false;
                }
            });
        }
        return record;
    },

    setValue: function(value)
    {
        // Clear current selection
        this.el.dom.selectedIndex = -1;
        this.view.clearSelections();

        // Now select values in the view and DOM
        var options = this.el.dom.options;
        for(var i = 0; i < value.length; i++)
        {
            var record = this.findRecord(this.valueField, value[i]);
            if (record)
            {
                this.view.select(this.store.indexOf(record));
                for(var j = 0; j < options.length; j++)
                {
                    if(options[j].value == value[i])
                    {
                        options[j].selected = true;
                    }
                }
            }
        }
    }
});

ZUTUBI.ConfigTreeLoader = function(base)
{
    ZUTUBI.ConfigTreeLoader.superclass.constructor.call(this, {
        dataUrl: base,
        requestMethod: 'get'
    });
};

Ext.extend(ZUTUBI.ConfigTreeLoader, Ext.tree.TreeLoader, {
    getNodePath : function(node)
    {
        var tree = node.getOwnerTree();
        return this.dataUrl + '/' + tree.getNodeConfigPath(node);
    },

    requestData : function(node, callback)
    {
        if(this.fireEvent("beforeload", this, node, callback))
        {
            var params = this.getParams(node);
            var cb = {
                success: this.handleResponse,
                failure: this.handleFailure,
                scope: this,
                argument: {callback: callback, node: node}
            };

            this.transId = Ext.lib.Ajax.request(this.requestMethod, this.getNodePath(node) + '?ls', cb, params);
        }
        else
        {
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if(typeof callback == "function")
            {
                callback();
            }
        }
    }
});


ZUTUBI.ConfigTree = function(id, config)
{
    ZUTUBI.ConfigTree.superclass.constructor.call(this, id, config);
}

Ext.extend(ZUTUBI.ConfigTree, Ext.tree.TreePanel, {
    configPathToTreePath: function(configPath)
    {
        var treePath = configPath;

        if (this.pathPrefix && treePath.indexOf(this.pathPrefix) == 0)
        {
            treePath = treePath.substring(this.pathPrefix.length);
        }

        if(treePath.length > 0 && treePath.substring(0, 1) != '/')
        {
            treePath = '/' + treePath;
        }

        if (!this.rootVisible)
        {
            treePath = this.pathSeparator + this.root.id + treePath;
        }

        // Workaround odd Ext behaviour: selecting needs a leading slash
        // except when selecting the root.
        if (treePath.lastIndexOf('/') == 0)
        {
            treePath = treePath.substring(1);
        }

        //alert('cpttp(' + configPath + ') -> ' + treePath);
        return treePath;
    },

    treePathToConfigPath: function(treePath)
    {
        var configPath = treePath;
        
        if (!this.rootVisible)
        {
            configPath = configPath.substring(this.root.id.length + 1);
        }

        if(configPath.length > 0 && configPath.substring(0, 1) == '/')
        {
            configPath = configPath.substring(1);    
        }

        if (this.pathPrefix)
        {
            configPath = this.pathPrefix + this.pathSeparator + configPath;
        }

        //console.log('tptcp(' + treePath + ') -> ' + configPath);
        return configPath;
    },

    selectConfigPath: function(configPath)
    {
        this.getSelectionModel().clearSelections();
        this.selectPath(this.configPathToTreePath(configPath));
    },

    expandToPath: function(path)
    {
        this.expandPath(this.configPathToTreePath(path));        
    },

    getNodeConfigPath: function(node)
    {
        return this.treePathToConfigPath(node.getPath());
    },

    getNodeByConfigPath: function(configPath)
    {
        var treePath = this.configPathToTreePath(configPath);
        if(treePath.substring(0, 1) == '/')
        {
            treePath = treePath.substring(1);
        }
        
        var keys = treePath.split(this.pathSeparator);

        if (keys[0] != this.root.id)
        {
            return null;
        }

        var node = this.root;
        for(var i = 1; node && i < keys.length; i++)
        {
            node = node.findChild('id', keys[i]);
        }
        
        return node;
    },

    handleResponse: function(response)
    {
        var tree = this;

        if(response.addedFiles)
        {
            each(response.addedFiles, function(addition) { tree.addNode(addition.parentPath, {id: addition.baseName, text: addition.displayName, leaf: addition.leaf}); });
        }

        if(response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(rename.oldPath, rename.newName, rename.newDisplayName); });
        }

        if(response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(path) });
        }
    },

    redirectToNewPath: function(response)
    {
        if (response.newPath)
        {
            this.selectConfigPath(response.newPath);
        }
    },

    addNode: function(parentPath, config)
    {
        if (parentPath)
        {
            var parentNode = this.getNodeByConfigPath(parentPath);
            if (parentNode)
            {
                var newNode = this.getLoader().createNode(config);
                parentNode.appendChild(newNode);
            }
        }
    },

    renameNode: function(oldPath, newName, newDisplayName)
    {
        if(oldPath)
        {
            var node = this.getNodeByConfigPath(oldPath);
            if(node)
            {
                this.setNodeId(node, newName);
                node.setText(newDisplayName);
            }
        }
    },

    removeNode: function(path)
    {
        if (path)
        {
            var node = this.getNodeByConfigPath(path);
            if (node && !node.isRoot)
            {
                node.parentNode.removeChild(node);
            }
        }
    },

    setNodeId: function(node, id)
    {
        this.unregisterNode(node);
        node.id = id;
        node.attributes.id = id;
        this.registerNode(node);
    }
});


ZUTUBI.TemplateTree = function(scope, id, config)
{
    this.scope = scope;
    ZUTUBI.TemplateTree.superclass.constructor.call(this, id, config);
}

Ext.extend(ZUTUBI.TemplateTree, ZUTUBI.ConfigTree, {
    handleResponse: function(response)
    {
        var tree = this;

        if (response.addedFiles)
        {
            each(response.addedFiles, function(addition) {
                if (addition.parentTemplatePath && addition.parentPath == tree.scope)
                {
                    tree.addNode(addition.parentTemplatePath, {id: addition.baseName, text: addition.displayName, leaf: addition.leaf});
                }
            });
        }

        if (response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(tree.translatePath(rename.oldPath), rename.newName, rename.newDisplayName); });
        }

        if (response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(tree.translatePath(path)); } );
        }
    },

    redirectToNewPath: function(response)
    {
        if (response.newTemplatePath)
        {
            this.selectConfigPath(response.newTemplatePath);
        }
    },

    translatePath: function(path)
    {
        var pieces = path.split(this.pathSeparator);
        if (pieces.length == 2 && pieces[0] == this.scope)
        {
            var id = pieces[1];
            var node = this.getNodeById(id);
            if (node)
            {
                return this.getNodeConfigPath(node);
            }
        }
        
        return null;
    }
});

ZUTUBI.CheckForm = function(mainForm, options)
{
    ZUTUBI.CheckForm.superclass.constructor.call(this, options);
    this.mainForm = mainForm;
};

Ext.extend(ZUTUBI.CheckForm, Ext.form.Form, {
    isValid: function()
    {
        // Call both, they have side-effects.
        var mainValid = this.mainForm.isValid();
        var valid = ZUTUBI.CheckForm.superclass.isValid.call(this);
        return mainValid && valid;
    },

    markInvalid: function(errors)
    {
        for(var i = 0; i < errors.length; i++)
        {
            var fieldError = errors[i];
            var id = fieldError.id;
            var field;

            if(id.lastIndexOf('_check') == id.length - 6)
            {
                field = this.mainForm.findField(id.substr(0, id.length - 6));
            }
            else
            {
                field = this.findField(id);
            }
            if(field)
            {
                field.markInvalid(fieldError.msg);
            }
        }
    },

    submit: function(options)
    {
        var params = options.params || {};
        var mainParams = this.mainForm.getValues(false);

        for(var param in mainParams)
        {
           params[param + '_check'] = mainParams[param];
        }

        options.params = params;
        ZUTUBI.CheckForm.superclass.submit.call(this, options);
    }
});

ZUTUBI.ImageButton = function(renderTo, config)
{
    ZUTUBI.ImageButton.superclass.constructor.call(this, config);
    this.addEvents({
        'click': true,
        'mouseover': true,
        'mouseout': true,
        'mousedown': true
    });

    this.defaultAutoCreate = { tag: 'img', src: this.image };
    this.render(renderTo);
}

Ext.extend(ZUTUBI.ImageButton, Ext.BoxComponent, {
    onRender: function(ct, position)
    {
        ZUTUBI.ImageButton.superclass.onRender.call(this, ct, position);
        this.el = ct.createChild(this.getAutoCreate(), position);
    },

    afterRender: function()
    {
        this.el.on('click', this.onClick,  this);
        this.el.on("mouseover", this.onMouseOver, this);
        this.el.on("mouseout", this.onMouseOut, this);
        this.el.on("mousedown", this.onMouseDown, this);
    },

    onClick: function(evt)
    {
        this.fireEvent('click', evt);
    },

    onMouseOver : function(e){
        if(!this.disabled){
            this.el.set({src: this.overImage});
            this.fireEvent('mouseover', this, e);
        }
    },

    onMouseOut : function(e){
        if(!e.within(this.el,  true)){
            this.el.set({src: this.image});
            this.fireEvent('mouseout', this, e);
        }
    },

    onMouseDown : function(e){
        if(!this.disabled && e.button == 0){
            this.el.set({src: this.downImage});
            Ext.get(document).on('mouseup', this.onMouseUp, this);
        }
    },

    onMouseUp : function(e){
        if(e.button == 0){
            this.el.set({src: this.image});
            Ext.get(document).un('mouseup', this.onMouseUp, this);
        }
    }
});

ZUTUBI.StringList = function(config)
{
    ZUTUBI.StringList.superclass.constructor.call(this, config);
    this.addEvents({
        'change': true
    });
};

Ext.extend(ZUTUBI.StringList, Ext.form.Field, {
    width: 100,
    height: 100,
    fieldName: 'value',
    selectedClass: 'x-string-list-selected',
    hiddenFields: [],
    value: [],

    onRender: function(ct, position)
    {
        this.wrap = ct.createChild({tag: 'div', cls: 'x-string-list'});
        this.wrap.on('click', this.onClick, this);
        this.el = this.wrap.createChild({tag: 'input', type: 'hidden'});

        this.input = this.wrap.createChild({tag: 'input', type: 'text', cls: 'x-form-text'});
        this.input.setWidth(this.width);
        this.nav = new Ext.KeyNav(this.input, {
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
                this.onAdd(evt);
            },

            scope: this
        });

        this.addButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-add-up.gif', overImage: '/images/buttons/sb-add-over.gif', downImage: '/images/buttons/sb-add-down.gif'});
        this.addButton.getEl().setWidth(21);
        this.addButton.getEl().setHeight(21);
        this.addButton.on('click', this.onAdd, this);

        this.list = this.wrap.createChild({tag: 'div', cls: 'x-string-list-list x-unselectable'});
        this.list.setWidth(this.width);
        this.list.setHeight(this.height);
        this.list.alignTo(this.input, 't-b');

        this.removeButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-delete-up.gif', overImage: '/images/buttons/sb-delete-over.gif', downImage: '/images/buttons/sb-delete-down.gif'});
        this.removeButton.getEl().setWidth(21);
        this.removeButton.getEl().setHeight(21);
        this.removeButton.on('click', this.onRemove, this);

        this.upButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-up-up.gif', overImage: '/images/buttons/sb-up-over.gif', downImage: '/images/buttons/sb-up-down.gif'});
        this.upButton.getEl().setWidth(21);
        this.upButton.getEl().setHeight(21);
        this.upButton.on('click', this.onUp, this);

        this.downButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-down-up.gif', overImage: '/images/buttons/sb-down-over.gif', downImage: '/images/buttons/sb-down-down.gif'});
        this.downButton.getEl().setWidth(21);
        this.downButton.getEl().setHeight(21);
        this.downButton.on('click', this.onDown, this);

        var cls = 'x-string-list';

        if(!this.tpl)
        {
            this.tpl = (Ext.isIE || Ext.isIE7 ? '<div' : '<div unselectable=on') + ' class="' + cls + '-item">{' + this.fieldName + '}</div>';
        }

        this.view = new Ext.View(this.list, this.tpl, {
            singleSelect:true, store: this.store, selectedClass: this.selectedClass
        });

        this.ValueRecord = Ext.data.Record.create({name: 'value'});
    },

    afterRender: function()
    {
        ZUTUBI.StringList.superclass.afterRender.call(this);
        this.alignButtons();
        if(this.value)
        {
            this.setValue(this.value);
            this.originalValue = this.value;
        }
    },

    onPosition: function()
    {
        this.alignButtons();
    },

    onResize: function()
    {
        this.alignButtons();
    },

    onClick: function()
    {
        this.input.focus();
    },

    alignButtons: function()
    {
        this.addButton.getEl().alignTo(this.input, 'l-r', [2, 0]);
        this.removeButton.getEl().alignTo(this.list, 'bl-br', [2, 0]);
        this.upButton.getEl().alignTo(this.list, 'l-r', [2, -this.input.getHeight()]);
        this.downButton.getEl().alignTo(this.upButton.getEl(), 't-b', [0, 2]);
    },

    navUp: function(ctrl)
    {
        if(ctrl)
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
        if(ctrl)
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
        var text = this.input.dom.value;
        if(text.length > 0)
        {
            this.input.dom.value = '';
            this.appendItem(text);
            this.view.select(this.store.getCount() - 1);
            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    appendItem: function(text)
    {
        this.store.add(new this.ValueRecord({value: text}));
        this.hiddenFields.push(this.wrap.createChild({tag: 'input', type: 'hidden', name: this.name, value: text}));
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
            selectedEl.scrollIntoView(this.list);
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
        this.store.removeAll();
        for(var i = 0; i < this.hiddenFields.length; i++)
        {
            this.hiddenFields[i].remove();
        }
        this.hiddenFields = [];
        
        for(var i = 0; i < value.length; i++)
        {
            this.appendItem(value[i]);
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
    }
});

Ext.form.Checkbox.prototype.onResize = function()
{
    Ext.form.Checkbox.superclass.onResize.apply(this, arguments);
}
