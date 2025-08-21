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

document.querySelector('form').addEventListener('submit', function(e) {
    const inputs = document.querySelectorAll('input[name="options"]');
    let valid = true;
    inputs.forEach(input => {
        if (!input.value.trim()) {
            valid = false;
            input.classList.add('input-error');
        } else {
            input.classList.remove('input-error');
        }
    });
    if (!valid) {
        e.preventDefault();
        alert('Por favor, completa todas las opciones antes de decidir.');
    }
});