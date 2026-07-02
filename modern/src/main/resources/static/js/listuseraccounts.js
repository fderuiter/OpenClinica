document.addEventListener("DOMContentLoaded", () => {
    fetch("/auth/api/v1/createuseraccount", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({})
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById("content").innerText = "Data loaded from DTO: " + JSON.stringify(data);
    })
    .catch(error => console.error("Error loading accounts:", error));
});
