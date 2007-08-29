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
    
    onRender: function(ct, position)
    {
        //Ext.form.Field.superclass.onRender.call(this, ct, position);
        this.wrap = ct.createChild({tag: 'div', cls: 'x-string-list'});
        this.el = this.wrap.createChild({tag: 'input', type: 'hidden'});
        this.input = this.wrap.createChild({tag: 'input', type: 'text'});
        this.input.setWidth(this.width - 16);
        this.input.on('keypress', this.onKeypress, this);
        this.addButton = this.wrap.createChild({tag: 'img', src: '/images/add.gif', style: 'width: 16px; height: 16px;'});
        this.addButton.on('click', this.onAdd, this);
        this.addButton.alignTo(this.input, 'l-r');
        
        this.list = this.wrap.createChild({tag: 'div', cls: 'x-string-list-list'});
        this.list.setWidth(this.width - 16);
        this.list.setHeight(this.height);

        this.removeButton = this.wrap.createChild({tag: 'img', src: '/images/delete.gif', style: 'width: 16px; height: 16px;'});
        this.removeButton.alignTo(this.list, 'tl-tr');
        this.removeButton.on('click', this.onRemove, this);

        this.upButton = this.wrap.createChild({tag: 'img', src: '/images/resultset_up.gif', style: 'width: 16px; height: 16px;'});
        this.upButton.alignTo(this.removeButton, 't-b');
        this.upButton.on('click', this.onUp, this);

        this.downButton = this.wrap.createChild({tag: 'img', src: '/images/resultset_down.gif', style: 'width: 16px; height: 16px;'});
        this.downButton.alignTo(this.upButton, 't-b');
        this.downButton.on('click', this.onDown, this);

        var cls = 'x-string-list';

        if(!this.tpl)
        {
            this.tpl = '<div class="' + cls + '-item">{' + this.fieldName + '}</div>';
        }

        this.view = new Ext.View(this.list, this.tpl, {
            singleSelect:true, store: this.store, selectedClass: this.selectedClass
        });

        this.ValueRecord = Ext.data.Record.create({name: 'value'});
    },

    afterRender: function()
    {
        ZUTUBI.StringList.superclass.afterRender.call(this);
        if(this.value)
        {
            this.setValue(this.value);
            this.originalValue = this.value;
        }
    },

    onKeypress: function(evt)
    {
        if (evt.getKey() == evt.RETURN)
        {
            this.onAdd(evt);
        }
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
        var selected = this.view.getSelectedIndexes();
        if(selected.length > 0)
        {
            var i = selected[0];
            this.store.remove(this.store.getAt(i));
            this.hiddenFields[i].remove();
            this.hiddenFields.splice(i, 1);
            this.fireEvent('change', this, evt);
        }
    },

    onUp: function(evt)
    {
        var selected = this.view.getSelectedIndexes();
        if(selected.length > 0 && selected[0] > 0)
        {
            var i = selected[0];
            var record = this.store.getAt(i);
            this.store.remove(record);
            this.store.insert(i - 1, record);
            this.view.select(i - 1);

            var hidden = this.hiddenFields.splice(i, 1)[0];
            this.hiddenFields.splice(i - 1, 0, hidden);
            hidden.insertBefore(this.hiddenFields[i]);

            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    onDown: function(evt)
    {
        var selected = this.view.getSelectedIndexes();
        if(selected.length > 0 && selected[0] < this.store.getCount() - 1)
        {
            var i = selected[0];
            var record = this.store.getAt(i);
            this.store.remove(record);
            this.store.insert(i + 1, record);
            this.view.select(i + 1);

            var hidden = this.hiddenFields.splice(i, 1)[0];
            this.hiddenFields.splice(i + 1, 0, hidden);
            hidden.insertAfter(this.hiddenFields[i]);

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

    doWithSelection: function(fn, cond)
    {
        var selected = this.view.getSelectedIndexes();
        if(selected.length > 0)
        {
            if(!cond || cond(selected[0]))
            {
                fn(selected[0]);
            }
        }
    },

    getValue: function()
    {
        var value = [];
        this.store.each(function(r) { value.push(r.value); });
    },

    setValue: function(value)
    {
        this.store.removeAll();
        for(var i = 0; i < value.length; i++)
        {
            this.appendItem(value[i]);
        }
    }
});

Ext.form.Checkbox.prototype.onResize = function()
{
    Ext.form.Checkbox.superclass.onResize.apply(this, arguments);
}
