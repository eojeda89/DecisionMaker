// Fase 4.1 (animaciones) + 4.5 (confeti/sonido) para decision-result.html.
// El algoritmo usado (data-algorithm-code en <body>) decide si hay que
// animar antes de revelar el resultado, o revelarlo directamente.
document.addEventListener("DOMContentLoaded", function () {
    var body = document.body;
    var algorithmCode = body.dataset.algorithmCode;
    var resultContent = document.getElementById("result-content");

    function reveal() {
        if (resultContent) resultContent.classList.remove("hidden-until-reveal");
        launchConfetti();
        playRevealSound();
    }

    if (algorithmCode === "fortune-wheel") {
        animateWheel(reveal);
    } else if (algorithmCode === "dice-roll") {
        animateDice(reveal);
    } else {
        reveal();
    }

    function animateWheel(onDone) {
        var wrap = document.getElementById("wheel-animation");
        var wheel = document.getElementById("wheel");
        if (!wrap || !wheel) { onDone(); return; }

        var angle = parseFloat(wrap.dataset.angle);
        if (isNaN(angle)) { onDone(); return; }

        var spins = 5;
        var rotation = spins * 360 + (360 - angle) % 360;

        // requestAnimationFrame para asegurar que el navegador pintó el
        // estado "rotate(0)" antes de animar a la posición final -- si no,
        // a veces la transición se salta directo al final sin girar.
        requestAnimationFrame(function () {
            requestAnimationFrame(function () {
                wheel.style.transition = "transform 3.2s cubic-bezier(0.15, 0.8, 0.15, 1)";
                wheel.style.transform = "rotate(" + rotation + "deg)";
            });
        });

        wheel.addEventListener("transitionend", function onEnd() {
            wheel.removeEventListener("transitionend", onEnd);
            onDone();
        });
    }

    function animateDice(onDone) {
        var wrap = document.getElementById("dice-animation");
        var dice = document.getElementById("dice");
        if (!wrap || !dice) { onDone(); return; }

        var faces = ["⚀", "⚁", "⚂", "⚃", "⚄", "⚅"];
        var tickCount = 0;
        var maxTicks = 12;
        var interval = setInterval(function () {
            dice.textContent = faces[Math.floor(Math.random() * faces.length)];
            dice.classList.remove("dice-bounce");
            void dice.offsetWidth; // reinicia la animación CSS en cada tick
            dice.classList.add("dice-bounce");
            tickCount++;
            if (tickCount >= maxTicks) {
                clearInterval(interval);
                dice.textContent = "🎲";
                onDone();
            }
        }, 100);
    }

    function launchConfetti() {
        var colors = ["#f94144", "#f3722c", "#f9c74f", "#90be6d", "#577590", "#277da1"];
        var container = document.createElement("div");
        container.className = "confetti-container";
        document.body.appendChild(container);

        for (var i = 0; i < 60; i++) {
            var piece = document.createElement("div");
            piece.className = "confetti-piece";
            piece.style.left = (Math.random() * 100) + "vw";
            piece.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            piece.style.animationDelay = (Math.random() * 0.4) + "s";
            piece.style.animationDuration = (2 + Math.random() * 1.5) + "s";
            container.appendChild(piece);
        }

        setTimeout(function () { container.remove(); }, 4000);
    }

    function playRevealSound() {
        try {
            var AudioContextClass = window.AudioContext || window.webkitAudioContext;
            if (!AudioContextClass) return;
            var ctx = new AudioContextClass();
            var notes = [523.25, 659.25, 783.99]; // arpegio C5-E5-G5
            notes.forEach(function (freq, idx) {
                var start = ctx.currentTime + idx * 0.12;
                var osc = ctx.createOscillator();
                var gain = ctx.createGain();
                osc.type = "sine";
                osc.frequency.value = freq;
                gain.gain.setValueAtTime(0.0001, start);
                gain.gain.exponentialRampToValueAtTime(0.15, start + 0.02);
                gain.gain.exponentialRampToValueAtTime(0.0001, start + 0.3);
                osc.connect(gain).connect(ctx.destination);
                osc.start(start);
                osc.stop(start + 0.35);
            });
        } catch (e) {
            // Las políticas de autoplay de audio pueden bloquear esto sin
            // avisar -- el sonido es un extra opcional (Fase 4.5), no crítico
            // para ver el resultado de la decisión.
        }
    }
});
