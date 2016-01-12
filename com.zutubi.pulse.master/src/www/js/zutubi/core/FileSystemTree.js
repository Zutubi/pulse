// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView;

    Zutubi.core.FileSystemTree = TreeView.extend({
        init: function(element, options)
        {
            this.bound = false;

            TreeView.fn.init.call(this, element, options);

            if (options && options.fs)
            {
                this.setBase(options.fs, options.basePath);
            }
        },

        events: [
            "dataBound"
        ],

        options: {
            name: "ZaFileSystemTree",
            dataTextField: "name",
            showFiles: true,
            loadOnDemand: true,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node. It is also called when we
                // later update the datasource, so once we've bound we don't trigger again.
                if (!this.bound && !e.node)
                {
                    kendo.ui.progress(this.element, false);
                    this.bound = true;

                }
            }
        },

        setBase: function(fs, basePath)
        {
            var that = this,
                dataSource;

            if (that.fs === fs && that.basePath === basePath)
            {
                return;
            }

            that.fs = fs;
            that.basePath = basePath;
            that.bound = false;

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: function(options)
                    {
                        // By default Kendo wants to specify the node with its id as a query param.
                        // Instead we want to use the node's path in the URL.  The simplest way I
                        // have found to do this is take over the read function as below, and make
                        // sure it gets the data it needs by using the path as the id (only the id
                        // field appears to be available to the read function, this would be
                        // simpler if we could just access the node).  So we don't need to
                        // wastefully pass the path from the server, we construct it client side in
                        // our success callback.
                        var url = "/api/fs/" + that.fs + "/" + Zutubi.config.encodePath(that.basePath),
                            path = null;

                        if (options.data && options.data.path)
                        {
                            path = options.data.path;
                            url += "/" + Zutubi.config.encodePath(path);
                            delete options.data.path;
                        }

                        url += "?showFiles=" + that.options.showFiles;

                        Zutubi.core.ajax({
                            url: url,
                            success: function(result)
                            {
                                var i, name;

                                for (i = 0; i < result.length; i++)
                                {
                                    name = result[i].name;
                                    result[i].path = path ? path + "/" + name : name;
                                }
                                options.success(result);
                            },
                            error: function(result)
                            {
                                options.error(result);
                            }
                        });
                    }
                },
                schema: {
                    model: {
                        hasChildren: "directory",
                        id: "path"
                    }
                }
            });

            dataSource.bind('error', function(e) {
                Zutubi.core.reportError('Could not load file system tree: ' + Zutubi.core.ajaxError(e.xhr));
            });

            kendo.ui.progress(that.element, true);
            that.setDataSource(dataSource);
        }
    });

    ui.plugin(Zutubi.core.FileSystemTree);
}(jQuery));
