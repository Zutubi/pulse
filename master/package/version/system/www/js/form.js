function checkfield(event)
{
    var field = Event.element(event);
    if (!field.value || field.value == "")
    {
        field.value = "required";
    }
    return false;
}

function requiredfield(event)
{
    var field = Event.element(event);
    clearError(field);
    if (!field.value || field.value == "")
    {
        // show the error message.
        addError(field, 'todo: load the actual error message... somehow...');
    }
    else
    {
        clearError(field);
    }
    return false;
}

function clearErrorMessages(form)
{
    var table = form.childNodes[1];
    if (typeof table == "undefined")
    {
        table = form.childNodes[0];
    }

    // clear out any rows with an "errorFor" attribute
    var rows = table.rows;
    var rowsToDelete = new Array();
    if (rows == null)
    {
        return;
    }

    for (var i = 0; i < rows.length; i++)
    {
        var r = rows[i];
        if (r.getAttribute("errorFor"))
        {
            rowsToDelete.push(r);
        }
    }

    // now delete the rows
    for (var i = 0; i < rowsToDelete.length; i++)
    {
        var r = rowsToDelete[i];
        table.deleteRow(r.rowIndex);
    }
}

function clearErrorLabels(form)
{
    // set all labels back to the normal class
    var elements = form.elements;
    for (var i = 0; i < elements.length; i++)
    {
        var e = elements[i];
        var cells = e.parentNode.parentNode.cells;
        if (cells && cells.length >= 2)
        {
            var label = cells[0].getElementsByTagName("label")[0];
            if (label)
            {
                label.setAttribute("class", "label");
                label.setAttribute("className", "label");
                //ie hack cause ie does not support setAttribute
            }
        }
    }
}

/**
 * Dynamically add the error html to the field. The added error results in the following:
 *
 *  <tr errorFor='element.id'><th colspan='2' class='error-message'>errorText</th></tr>
 *  <tr><td class='error-label'>...</td><td class='error-field'>...</td></tr>
 *
 */
function addError(element, errorText)
{
    try
    {
        // clear out any rows with an "errorFor" of e.id
        var row = element.parentNode.parentNode;
        var table = row.parentNode;

        // create the error row and insert it immediately above the error field.
        var error = document.createTextNode(errorText);
        var tr = document.createElement("tr");
        var th = document.createElement("th");
        th.colSpan = 2;
        th.setAttribute("class", "error-message");
        th.appendChild(error);
        tr.appendChild(th);
        tr.setAttribute("errorFor", element.id);
        table.insertBefore(tr, row);

        // update the field labels class
        var label = row.cells[0].getElementsByTagName("label")[0];
        label.setAttribute("class", "error-label");
        label.parentNode.setAttribute("class", "error-label");

        // update the fields class
        element.parentNode.setAttribute("class", "error-field");

    }
    catch (e)
    {
        alert(e);
    }
}

function clearError(element)
{
    var hasError = Element.hasClassName(element, 'error-field');
    if (!hasError)
    {
        return;
    }
    
    // clear out any rows with an "errorFor" of e.id
    var row = element.parentNode.parentNode;
    var table = row.parentNode;

    var label = row.cells[0].getElementsByTagName("label")[0];
    label.setAttribute("class", "label");
    label.parentNode.setAttribute("class", "label");

    // update the fields class
    element.parentNode.setAttribute("class", "field");

    // Now delete the previous row containing the error message.
    table.deleteRow(row.rowIndex - 1);
}