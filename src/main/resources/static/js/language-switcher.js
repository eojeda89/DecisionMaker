// Selector de idioma (Fase 4, fuera del plan): cambia el locale vía
// LocaleChangeInterceptor (?lang=xx), que WebConfig persiste en la sesión
// HTTP (SessionLocaleResolver). Los endpoints POST-only que muestran el
// resultado de una decisión (/decide, /decide/best-of-n, /decide/daily) no
// tienen equivalente GET, así que cambiar de idioma ahí vuelve al formulario
// en vez de repetir esa URL con ?lang= (daría 405).
function switchLanguage(lang) {
    var postOnlyPaths = ["/decide", "/decide/best-of-n", "/decide/daily"];
    var path = window.location.pathname;
    if (postOnlyPaths.indexOf(path) !== -1) {
        path = "/form";
    }
    window.location.href = path + "?lang=" + lang;
}
