// dependency: zutubi/namespace.js
// dependency: zutubi/core/package.js

if (window.Zutubi.config === undefined)
{
    window.Zutubi.config = (function($)
    {
        function _coerceInt(properties, name)
        {
            var value, newValue;
            if (properties.hasOwnProperty(name))
            {
                value = properties[name];
                if (value === "")
                {
                    newValue = null;
                }
                else
                {
                    newValue = Number(value);
                }

                properties[name] = newValue;
            }
        }

        return {
            normalisedPath: function(path)
            {
                if (!path)
                {
                    return "";
                }

                if (path.length > 0 && path[0] === "/")
                {
                    path = path.substring(1);
                }
                if (path.length > 0 && path[path.length - 1] === "/")
                {
                    path = path.substring(0, path.length - 1);
                }

                return path;
            },

            subPath: function(path, begin, end)
            {
                var elements = path.split("/");

                if (typeof end === "undefined")
                {
                    end = elements.length;
                }

                elements = elements.slice(begin, end);
                return elements.join("/");
            },

            parentPath: function(path)
            {
                var i = path.lastIndexOf("/");
                if (i >= 0)
                {
                    return path.substring(0, i);
                }

                return null;
            },

            baseName: function(path)
            {
                var i = path.lastIndexOf("/");
                if (i >= 0)
                {
                    return path.substring(i + 1);
                }

                return path;
            },

            encodePath: function(path)
            {
                var pieces, encodedPath, i;

                pieces = path.split('/');
                encodedPath = '';

                for (i = 0; i < pieces.length; i++)
                {
                    if (encodedPath.length > 0)
                    {
                        encodedPath += '/';
                    }

                    encodedPath += encodeURIComponent(pieces[i]);
                }

                return encodedPath;
            },

            coerceProperties: function(properties, propertyTypes)
            {
                var i,
                    propertyType;

                if (propertyTypes)
                {
                    for (i = 0; i < propertyTypes.length; i++)
                    {
                        propertyType = propertyTypes[i];
                        if (propertyType.shortType === "int")
                        {
                            _coerceInt(properties, propertyType.name);
                        }
                    }
                }
            },

            getValidationErrors: function(jqXHR)
            {
                var details;

                if (jqXHR.status === 422)
                {
                    try
                    {
                        details = JSON.parse(jqXHR.responseText);
                        if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                        {
                            return details;
                        }
                    }
                    catch (e)
                    {
                        // Do nothing.
                    }
                }

                return null;
            },

            checkConfig: function(path, type, form, checkForm, errorCb)
            {
                var properties = form.getValues(),
                    checkProperties = checkForm.getValues(),
                    url = path ? "/api/action/check/" + Zutubi.config.encodePath(path) : "/setup-api/setup/check";

                form.clearMessages();

                Zutubi.config.coerceProperties(properties, type.simpleProperties);
                Zutubi.config.coerceProperties(checkProperties, type.checkType.simpleProperties);

                Zutubi.core.ajax({
                    type: "POST",
                    maskAll: true,
                    url: url,
                    data: {
                        main: {kind: "composite", properties: properties, type: {symbolicName: type.symbolicName}},
                        check: {kind: "composite", properties: checkProperties}
                    },
                    success: function (data)
                    {
                        var message = data.success ? "configuration ok" : (data.message || "check failed");
                        checkForm.showStatus(data.success, message);
                    },
                    error: function (jqXHR)
                    {
                        var details = Zutubi.config.getValidationErrors(jqXHR);

                        if (details)
                        {
                            if (details.key === "main")
                            {
                                form.showValidationErrors(details.validationErrors);
                            }
                            else
                            {
                                checkForm.showValidationErrors(details.validationErrors);
                            }
                        }
                        else
                        {
                            errorCb("Could not check configuration: " + Zutubi.core.ajaxError(jqXHR));
                        }
                    }
                });
            }
        }
    }(jQuery));
}
