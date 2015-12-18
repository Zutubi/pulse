// dependency: ./namespace.js

(function()
{
    var Class = kendo.Class;

    Zutubi.config.Docs = Class.extend({
        init: function(data)
        {
            this.data = data;
            if (!this.data.properties)
            {
                this.data.properties = [];
            }
        },

        getBrief: function()
        {
            return this.data.brief;
        },

        getVerbose: function()
        {
            return this.data.verbose;
        },

        getPropertyDocs: function(propertyName)
        {
            var properties = this.data.properties,
                i;

            for (i = 0; i < properties.length; i++)
            {
                if (properties[i].name === propertyName)
                {
                    return properties[i];
                }
            }

            return null;
        }
    });
}());
