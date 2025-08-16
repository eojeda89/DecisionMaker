// src/main/resources/static/js/script-decision-option.js

document.addEventListener("DOMContentLoaded", function() {
    const agregarBtn = document.getElementById("agregar-opcion");
    const opcionesContainer = document.getElementById("opciones-container");
    let opcionCount = 2; // Ya tenemos dos opciones iniciales

    agregarBtn.addEventListener("click", function() {
        opcionCount++;
        const nuevaOpcionDiv = document.createElement("div");
        nuevaOpcionDiv.innerHTML = `
            <label for="opcion${opcionCount}">Opción ${opcionCount}:</label>
            <input type="text" id="opcion${opcionCount}" name="opciones">
        `;
        opcionesContainer.appendChild(nuevaOpcionDiv);
    });
});