// Fase 4.4: modo oscuro. Se aplica ANTES de DOMContentLoaded (este script se
// incluye temprano en <head>, antes del CSS) para evitar el flash de tema
// claro al cargar la página con el oscuro ya guardado.
(function () {
    var STORAGE_KEY = "decididor-theme";

    function readStoredTheme() {
        try {
            return localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            // localStorage puede no estar disponible (modo privado estricto,
            // políticas de cookies/storage) -- el modo oscuro es un extra
            // opcional, así que simplemente no se recuerda entre visitas.
            return null;
        }
    }

    function applyTheme(theme) {
        if (theme === "dark") {
            document.documentElement.setAttribute("data-theme", "dark");
        } else {
            document.documentElement.removeAttribute("data-theme");
        }
    }

    var stored = readStoredTheme();
    var prefersDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
    applyTheme(stored || (prefersDark ? "dark" : "light"));

    document.addEventListener("DOMContentLoaded", function () {
        var toggle = document.getElementById("dark-mode-toggle");
        if (!toggle) return;
        toggle.addEventListener("click", function () {
            var isDark = document.documentElement.getAttribute("data-theme") === "dark";
            var next = isDark ? "light" : "dark";
            applyTheme(next);
            try {
                localStorage.setItem(STORAGE_KEY, next);
            } catch (e) {
                // Ver comentario en readStoredTheme().
            }
        });
    });
})();
