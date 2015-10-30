// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        PATHSELECT = "pathselect",
        READY = "ready",

    PathInfo = function(found, item, path, embedded)
    {
        this.found = found;
        this.item = item;
        this.path = path;
        this.embedded = embedded;
    };

    function _embedCollections(item)
    {
        var i;
        if (item.nested)
        {
            if (item.kind === "collection" && !item.type.keyed)
            {
                item.embedded = item.nested;
                delete item.nested;
            }
            else if (Zutubi.admin.hasCollapsedCollection(item))
            {
                item.collapsed = item.nested[0];
                item.nestedName = item.collapsed.key;
                item.nested = item.collapsed.nested;
            }

            if (item.nested)
            {
                for (i = 0; i < item.nested.length; i++)
                {
                    _embedCollections(item.nested[i]);
                }
            }
        }
    }

    function _cloneAndEmbed(data)
    {
        if (data)
        {
            data = jQuery.extend(true, {}, data);
            _embedCollections(data);
        }

        return data;
    }

    Zutubi.admin.ConfigTree = TreeView.extend({
        init: function(element, options)
        {
            TreeView.fn.init.call(this, element, options);

            this.bound = false;

            if (options && options.rootPath)
            {
                this.setRootPath(options.rootPath);
            }
        },

        events: [
            // FIXME kendo: to subscribe to our own select event we need to have this here, is there a better way?
            "dataBound",
            "select",
            PATHSELECT,
            READY
        ],

        options: {
            name: "ZaConfigTree",
            dataTextField: "label",
            loadOnDemand: false,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node. It is also called when we
                // later update the datasource, so once we've bound we don't trigger again.
                if (!this.bound && !e.node)
                {
                    kendo.ui.progress(this.element, false);
                    this.bound = true;
                    this.trigger(READY);
                    this._selectConfigNode();
                }
            },
            select: function(e)
            {
                var that = this;
                that.configPath = that._pathForNode(e.node, true);
                that.trigger(PATHSELECT, {path: that._pathForNode(e.node)});
            }
        },

        _pathForNode: function(node, relative)
        {
            var nodeData = this.dataItem(node),
                path = nodeData.key,
                parent = this.parent(node),
                parentItem,
                parentPath;

            if (parent.length)
            {
                parentPath = this._pathForNode(parent, relative);
                parentItem = this.dataItem(parent);
                if (parentItem.nestedName)
                {
                    if (parentPath.length > 0)
                    {
                        parentPath += '/';
                    }

                    parentPath += parentItem.nestedName;
                }

                if (parentPath.length > 0)
                {
                    parentPath += '/';
                }

                path =  parentPath + path;
            }
            else if (relative)
            {
                path = '';
            }
            else
            {
                path = this.rootPath;
            }

            return path;
        },

        getRootPath: function()
        {
            return this.rootPath;
        },

        setRootPath: function(rootPath)
        {
            var dataSource;

            if (this.rootPath && this.rootPath === rootPath)
            {
                return;
            }

            this.rootPath = rootPath;
            this.bound = false;

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: {
                        url: window.baseUrl + "/api/config/" + Zutubi.admin.encodePath(rootPath) + "?depth=-1&filter=nested&filter=type",
                        dataType: "json",
                        headers: {
                            Accept: "application/json; charset=utf-8",
                            "Content-Type": "application/json; charset=utf-8"
                        }
                    }
                },
                schema: {
                    parse: function(response) {
                        return jQuery.map(response, function(item) {
                            _embedCollections(item);
                            return item;
                        });
                    },
                    model: {
                        children: "nested"
                    }
                }
            });

            dataSource.bind('error', function(e) {
                Zutubi.admin.reportError('Could not load configuration tree: ' + Zutubi.admin.ajaxError(e.xhr));
            });

            kendo.ui.progress(this.element, true);
            this.setDataSource(dataSource);
        },

        _infoForConfigPath: function(configPath)
        {
            var that = this,
                root = that.wrapper.find(".k-item:first"),
                dataItem = that.dataItem(root),
                path = that.rootPath,
                keys = [],
                i, j,
                key,
                children;

            if (!dataItem)
            {
                return new PathInfo(false, null, null, false);
            }

            if (configPath)
            {
                keys = configPath.split("/");
            }

            for (i = 0; i < keys.length; i++)
            {
                key = keys[i];

                if (dataItem.nestedName)
                {
                    if (dataItem.nestedName !== key)
                    {
                        return new PathInfo(false, dataItem, path, false);
                    }

                    path += "/" + key;

                    if (i < keys.length - 1)
                    {
                        i++;
                        key = keys[i];
                    }
                    else
                    {
                        return new PathInfo(true, dataItem, path, false);
                    }
                }
                else if (dataItem.embedded)
                {
                    // We've hit list of non-named items, the end of the line.  If this is the last
                    // path element we might hit it in the embedded collection.
                    if (i === keys.length - 1)
                    {
                        for (j = 0; j < dataItem.embedded.length; j++)
                        {
                            if (dataItem.embedded[j].key === key)
                            {
                                return new PathInfo(true, dataItem, path, true);
                            }
                        }
                    }

                    return new PathInfo(false, dataItem, path, false);
                }

                children = typeof dataItem.children.data === "function" ? dataItem.children.data() : [];
                for (j = 0; j < children.length; j++)
                {
                    if (children[j].key === key)
                    {
                        break;
                    }
                }

                if (j === children.length)
                {
                    return new PathInfo(false, dataItem, path, false);
                }

                dataItem = children[j];
                path += "/" + key;
            }

            return new PathInfo(true, dataItem, path, false);
        },

        _selectConfigNode: function()
        {
            var that = this,
                root = that.wrapper.find(".k-item:first"),
                info = that._infoForConfigPath(that.configPath);

            that.expand(root);
            if (info.item)
            {
                that.expandTo(info.item);
                that.select(that.findByUid(info.item.uid));
            }
            else
            {
                that.select(root);
            }
        },

        getConfigPath: function()
        {
            return this.configPath;
        },

        selectConfig: function(configPath)
        {
            this.configPath = configPath;
            if (this.bound)
            {
                this._selectConfigNode();
            }
        },

        selectAbsolutePath: function(path)
        {
            this.selectConfig(this._absoluteToConfigPath(path));
        },

        longestMatchingSubpath: function(path)
        {
            var info = this._infoForConfigPath(this._absoluteToConfigPath(path)),
                collapsed = Zutubi.admin.baseName(info.path) === info.item.nestedName;
            return collapsed ? Zutubi.admin.parentPath(info.path) : info.path;
        },

        _absoluteToConfigPath: function(path)
        {
            if (path.indexOf(this.rootPath) === 0)
            {
                path = path.substring(this.rootPath.length);
            }

            if (path.length > 0 && path[0] === "/")
            {
                path = path.substring(1);
            }

            return path;
        },

        _addModel: function(model, parentDataItem, index)
        {
            var parentType, i, data, item;

            if (parentDataItem.embedded)
            {
                parentDataItem.embedded.push(model);
            }
            else
            {
                if (parentDataItem.collapsed)
                {
                    parentType = parentDataItem.collapsed.type;
                }
                else
                {
                    parentType = parentDataItem.type;
                }

                if (!parentType.ordered)
                {
                    // Order by label.
                    data = parentDataItem.children.data();
                    for (i = 0; i < data.length; i++)
                    {
                        item = data[i];
                        if (item.label.localeCompare(model.label) > 0)
                        {
                            break;
                        }
                    }

                    index = i;
                }
                else if (index === undefined || index < 0)
                {
                    index = parentDataItem.children.data().length;
                }

                parentDataItem.children.insert(index, model);
            }
        },

        updatePath: function(path, data)
        {
            var info;

            data = _cloneAndEmbed(data);
            path = this._absoluteToConfigPath(path);
            info = this._infoForConfigPath(path);

            if (info.found)
            {
                if (data)
                {
                    this._updateItem(path, info.item, data);
                }
                else
                {
                    this.dataSource.remove(info.item);
                }
            }
            else if (data)
            {
                path = Zutubi.admin.parentPath(path);
                info = this._infoForConfigPath(path);
                if (info.found)
                {
                    this._addModel(data, info.item);
                }
            }
        },

        _updateItem: function(path, item, data)
        {
            var parentItem = item.parentNode(),
                dataSource,
                index;

            if (parentItem)
            {
                dataSource = parentItem.children;
            }
            else
            {
                dataSource = this.dataSource;
            }

            index = dataSource.indexOf(item);
            if (index >= 0)
            {
                dataSource.remove(item);
                // FIXME kendo it would be nicer to maintain all expansions, not just this one.
                data.expanded = item.expanded;
                dataSource.insert(index, data);
                if (path === this.configPath)
                {
                    this.selectConfig(path);
                }
            }
        },

        _removeEmbedded: function(item, key)
        {
            var i;

            for (i = 0; i < item.embedded.length; i++)
            {
                if (item.embedded[i].key === key)
                {
                    break;
                }
            }

            if (i < item.embedded.length)
            {
                item.embedded.splice(i, 1);
            }
        },

        applyDelta: function(delta)
        {
            var that = this,
                i,
                model,
                info,
                path,
                index = -1;

            if (delta.addedPaths)
            {
                for (i = 0; i < delta.addedPaths.length; i++)
                {
                    path = that._absoluteToConfigPath(delta.addedPaths[i]);
                    info = that._infoForConfigPath(Zutubi.admin.parentPath(path));
                    if (info.found)
                    {
                        that._addModel(_cloneAndEmbed(delta.models[delta.addedPaths[i]]), info.item);
                    }
                }
            }

            if (delta.deletedPaths)
            {
                for (i = 0; i < delta.deletedPaths.length; i++)
                {
                    path = that._absoluteToConfigPath(delta.deletedPaths[i]);
                    info = that._infoForConfigPath(path);
                    if (info.found)
                    {
                        if (info.embedded)
                        {
                            info = that._infoForConfigPath(Zutubi.admin.parentPath(path));
                            this._removeEmbedded(info.item, Zutubi.admin.baseName(path));
                        }
                        else
                        {
                            that.dataSource.remove(info.item);
                        }
                    }
                }
            }

            if (delta.renamedPaths)
            {
                jQuery.each(delta.renamedPaths, function(oldPath, newPath)
                {
                    model = delta.models[newPath];
                    oldPath = that._absoluteToConfigPath(oldPath);
                    newPath = that._absoluteToConfigPath(newPath);

                    info = that._infoForConfigPath(oldPath);
                    if (info.found)
                    {
                        index = info.item.parentNode().children.indexOf(info.item);
                        that.dataSource.remove(info.item);
                    }

                    info = that._infoForConfigPath(Zutubi.admin.parentPath(newPath));
                    if (info.found)
                    {
                        that._addModel(_cloneAndEmbed(model), info.item, index);
                    }

                    if (oldPath === that.configPath)
                    {
                        that.selectConfig(newPath);
                    }
                });
            }

            if (delta.updatedPaths)
            {
                for (i = 0; i < delta.updatedPaths.length; i++)
                {
                    path = delta.updatedPaths[i];
                    model = delta.models[path];
                    path = that._absoluteToConfigPath(path);
                    info = that._infoForConfigPath(path);
                    if (info.found)
                    {
                        // Update to a node, e.g. collection reorder. Refresh.
                        that._updateItem(path, info.item, _cloneAndEmbed(model));
                    }
                }
            }
        }
    });

    ui.plugin(Zutubi.admin.ConfigTree);
}(jQuery));
