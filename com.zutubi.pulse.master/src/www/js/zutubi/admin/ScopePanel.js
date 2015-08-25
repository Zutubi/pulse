// dependency: ./namespace.js
// dependency: ./ajax.js
// dependency: ./HierarchySelector.js

(function($)
{
    var Observable = kendo.Observable,
        PATHSELECT = 'pathselect';

    Zutubi.admin.ScopePanel = Observable.extend({
        init: function (containerSelector)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="outer-split" style="height: 100%; width: 100%">' +
                    '<div id="left-pane">' +
                        '<div id="hierarchy-tree" class="pane-content">' +
                        '</div>' +
                    '</div>' +
                    '<div id="center-pane">' +
                        '<div id="center-pane-content" class="pane-content">' +
                        '</div>' +
                    '</div>' +
                    '<div id="right-pane">' +
                        '<div class="pane-content">' +
                        '</div>' +
                    '</div>' +
                '</div>', {wrap: false});

            that.view.render($(containerSelector));

            $("#outer-split").kendoSplitter({
                panes: [
                    { collapsible: true, size: "250px" },
                    { collapsible: false },
                    { collapsible: true, size: "250px" }
                ]
            });

            that.hierarchySelector = $("#hierarchy-tree").kendoZaHierarchySelector().data("kendoZaHierarchySelector");
            that.hierarchySelector.bind("nodeselect", function(e)
            {
                that._loadContentPanes(e.name);
            });
        },

        events: [
            PATHSELECT
        ],

        destroy: function()
        {
            // FIXME kendo need we do more?
            this.view.destroy();
        },

        setScope: function(scope)
        {
            this.scope = scope;
            this.hierarchySelector.setScope(scope);
        },

        setItem: function(name)
        {
            this.hierarchySelector.selectItem(name);
            this._loadContentPanes(name);
        },

        _loadContentPanes: function(name)
        {
            $("#center-pane-content").html('<a href="' + window.baseUrl + '/admina/config/' + this.scope + '/' + name + '">' + name + '</a>');
        }

    });
}(jQuery));
