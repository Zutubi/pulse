// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./OverviewPanel.js

(function($)
{
    var OverviewPanel = Zutubi.admin.OverviewPanel;

    Zutubi.admin.AgentOverviewPanel = OverviewPanel.extend({
        init: function (options)
        {
            var that = this,
                agent = options.agent;

            options = jQuery.extend({
                scope: "agents",
                item: agent
            }, that.options, options);

            OverviewPanel.fn.init.call(this, options);
        },

        options: {
            id: "agent-overview"
        },

        _addSummary: function(tableEl)
        {
            var agent = this.options.agent;

            this._addRow(tableEl, "", "location", this.getSimple(agent, "location", "[unknown]"));
            this._addRow(tableEl, "", "priority", this.getSimple(agent, "priority", "[unset]"));
            this._addRow(tableEl, "resources", "resources", this._addResourcesSummary);
        },

        _addResourcesSummary: function(cellEl)
        {
            var resources = this.getNested(this.options.agent, "resources"),
                tableEl,
                i,
                resource;

            if (resources && resources.nested.length > 0)
            {
                tableEl = this._addSubtable(cellEl);
                for (i = 0; i < resources.nested.length; i++)
                {
                    resource = resources.nested[i];
                    this._addRow(tableEl, "resources/" + resource.properties.name, resource.properties.name, this._getResourceSummary(resource));
                }
            }
            else
            {
                cellEl.append('<span class="k-understated">none configured</span>');
            }
        },

        _getResourceSummary: function(resource)
        {
            var summary = "",
                defaultVersion = this.getSimple(resource, "defaultVersion", "/"),
                versions = this.getNested(resource, "versions"),
                version,
                i;

            if (versions && versions.nested.length > 0)
            {
                for (i = 0; i < versions.nested.length; i++)
                {
                    if (summary)
                    {
                        summary += ", ";
                    }

                    version = this.getSimple(versions.nested[i], "value", "[unnamed]");
                    if (version === defaultVersion)
                    {
                        version += " [default]";
                    }
                    summary += version;
                }

            }

            if (summary)
            {
                return kendo.htmlEncode(summary);
            }
            else
            {
                return '<span class="k-understated">no versions</span>';
            }
        }
    });
}(jQuery));
