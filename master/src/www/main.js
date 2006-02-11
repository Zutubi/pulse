// Function to toggle the enable state of a control based on the state of a
// checkbox.
function setEnableState(id, checkboxId)
{
    element = document.getElementById(id);
    element.disabled = !document.getElementById(checkboxId).checked;
}

function confirmUrl(message, url)
{
    if (confirm(message))
    {
        location.href = url;
    }
}
