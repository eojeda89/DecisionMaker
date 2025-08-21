// src/main/resources/static/js/script-decision-option.js

document.addEventListener("DOMContentLoaded", function() {
    const addBtn = document.getElementById("add-option");
    const containerOptions = document.getElementById("container-options");
    let optionCount = 2; // Ya tenemos dos opciones iniciales

    addBtn.addEventListener("click", function() {
        optionCount++;
        const newOptionDiv = document.createElement("div");
        newOptionDiv.innerHTML = `
            <label for="opcion${optionCount}">Opción ${optionCount}:</label>
            <input type="text" id="option${optionCount}" name="options">
        `;
        containerOptions.appendChild(newOptionDiv);
    });
});