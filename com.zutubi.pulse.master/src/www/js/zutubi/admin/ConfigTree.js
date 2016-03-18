// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./Tree.js

(function($)
{
    var ui = kendo.ui,
        Tree = Zutubi.admin.Tree,
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
                if (item.collapsed.type.keyed)
                {
                    item.nestedName = item.collapsed.key;
                    item.nested = item.collapsed.nested;
                }
                else
                {
                    // The collapsed collection is embedded (i.e. no names, thus no tree nodes).
                    item.embedded = item.collapsed;
                    delete item.nested;
                }
            }
        }
    }

    function _translateItem(item)
    {
        var i;

        _embedCollections(item);

        if (!item.deeplyValid)
        {
            item.spriteCssClass = "fa fa-exclamation";

            if (item.validationErrors)
            {
                item.spriteCssClass += " k-invalid";
            }

        }

        if (item.nested)
        {
            for (i = 0; i < item.nested.length; i++)
            {
                _translateItem(item.nested[i]);
            }
        }
    }

    function _cloneAndTranslate(data)
    {
        if (data)
        {
            data = jQuery.extend(true, {}, data);
            _translateItem(data);
        }

        return data;
    }

    Zutubi.admin.ConfigTree = Tree.extend({
        init: function(element, options)
        {
            Tree.fn.init.call(this, element, options);

            this.bound = false;

            if (options && options.rootPath)
            {
                this.setRootPath(options.rootPath);
            }
        },

        events: [
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
                    this._applyFilter();
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

        isReady: function()
        {
            return this.bound;
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
                        url: window.baseUrl + "/api/config/" + Zutubi.config.encodePath(rootPath) + "?depth=-1&filter=nested&filter=type.&filter=validationErrors",
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
                            _translateItem(item);
                            return item;
                        });
                    },
                    model: {
                        children: "nested"
                    }
                }
            });

            dataSource.bind('error', function(e) {
                Zutubi.core.reportError('Could not load configuration tree: ' + Zutubi.core.ajaxError(e.xhr));
            });

            kendo.ui.progress(this.element, true);
            this.setDataSource(dataSource);
        },

        _infoForConfigPath: function(configPath)
        {
            var that = this,
                root = that.getRoot(),
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
                root = that.getRoot(),
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

        filterTrivial: function(filter)
        {
            this.filtered = filter;
            if (this.bound)
            {
                this._applyFilter();
            }
        },

        _applyFilter: function()
        {
            this.setFilter(this.filtered ? jQuery.proxy(this._filter, this) : null);
        },

        _filter: function(node)
        {
            var item = this.dataItem(node),
                parentItem = item.parentNode();

            if (item.templateOriginator === item.templateOwner)
            {
                if (parentItem && parentItem.templateOriginator === item.templateOriginator)
                {
                    return this.FILTER_VISIBLE;
                }
                else
                {
                    return this.FILTER_REVEAL;
                }
            }
            else if (item.skeleton === false)
            {
                return this.FILTER_REVEAL;
            }
            else
            {
                return this.FILTER_HIDE;
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
                collapsed = info.path && Zutubi.config.baseName(info.path) === info.item.nestedName;
            return collapsed ? Zutubi.config.parentPath(info.path) : info.path;
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

            data = _cloneAndTranslate(data);
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
                path = Zutubi.config.parentPath(path);
                info = this._infoForConfigPath(path);
                if (info.found)
                {
                    this._addModel(data, info.item);
                }
            }

            this._applyFilter();
        },

        _parentDataSource: function(item)
        {
            var parentItem = item.parentNode();

            if (parentItem)
            {
                return parentItem.children;
            }
            else
            {
                return this.dataSource;
            }
        },

        _updateItem: function(path, item, data)
        {
            var dataSource = this._parentDataSource(item),
                index = dataSource.indexOf(item);

            if (index >= 0)
            {
                dataSource.remove(item);
                // TODO it would be nicer to maintain all expansions, not just this one.
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
                index = -1,
                reloaded = false;

            if (delta.addedPaths)
            {
                for (i = 0; i < delta.addedPaths.length; i++)
                {
                    path = that._absoluteToConfigPath(delta.addedPaths[i]);
                    info = that._infoForConfigPath(Zutubi.config.parentPath(path));
                    if (info.found)
                    {
                        that._addModel(_cloneAndTranslate(delta.models[delta.addedPaths[i]]), info.item);
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
                            info = that._infoForConfigPath(Zutubi.config.parentPath(path));
                            this._removeEmbedded(info.item, Zutubi.config.baseName(path));
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

                    if (oldPath === "")
                    {
                        // The root path has been renamed, reload fully. This is overkill but rare
                        // and it resets our paths properly.
                        that.setRootPath(newPath);
                        reloaded = true;
                        return false;
                    }
                    else
                    {
                        newPath = that._absoluteToConfigPath(newPath);

                        info = that._infoForConfigPath(oldPath);
                        if (info.found)
                        {
                            index = that._parentDataSource(info.item).indexOf(info.item);
                            that.dataSource.remove(info.item);
                        }

                        info = that._infoForConfigPath(Zutubi.config.parentPath(newPath));
                        if (info.found)
                        {
                            that._addModel(_cloneAndTranslate(model), info.item, index);
                        }

                        if (oldPath === that.configPath)
                        {
                            that.selectConfig(newPath);
                        }
                    }
                });

                if (reloaded)
                {
                    return;
                }
            }

            if (delta.updatedPaths)
            {
                for (i = 0; i < delta.updatedPaths.length; i++)
                {
                    path = delta.updatedPaths[i];
                    model = delta.models[path];
                    path = that._absoluteToConfigPath(path);
                    info = that._infoForConfigPath(path);
                    if (info.found && !info.embedded)
                    {
                        // Update to a node, e.g. collection reorder. Refresh.
                        that._updateItem(path, info.item, _cloneAndTranslate(model));
                    }
                }
            }

            this._applyFilter();
        }
    });

    ui.plugin(Zutubi.admin.ConfigTree);
}(jQuery));
