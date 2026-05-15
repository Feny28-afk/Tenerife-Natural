// --- 1. VARIABLES GLOBALES ---
// --- 1. VARIABLES GLOBALES ---
// Si USUARIO_REAL existe (lo enviamos desde HTML), lo usamos. Si no, usamos uno por defecto.
let USUARIO_SESION = typeof USUARIO_REAL !== 'undefined' ? USUARIO_REAL : { id: 1, nivel: 1 };
let modoCreacionRuta = true;
const marcadoresSenderos = L.layerGroup();
const capaGuaguas = L.layerGroup();

const map = L.map('map').setView([28.2915, -16.6291], 10);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
marcadoresSenderos.addTo(map);

// Simulación de temperatura
async function obtenerTemperatura(lat, lng) {
    const tempSimulada = (15 + Math.random() * 10).toFixed(1);
    return `${tempSimulada}°C`;
}

// --- CAMBIO DE NIVEL FÍSICO (UNIFICADO) ---
function cambiarNivelFisico(nuevoNivel) {
    USUARIO_SESION.nivel = parseInt(nuevoNivel);
    const nombres = {1: "Principiante", 2: "Aficionado", 3: "Experto"};
    mostrarNotificacion("Nivel actualizado a: " + nombres[nuevoNivel] + " 🏃‍♂️");

    // Refrescamos el mapa para que cambien los mensajes de Apto/Esfuerzo
    cargarSenderos();
}

function conmutarModoCreacion() {
    modoCreacionRuta = !modoCreacionRuta;
    const btn = document.getElementById('btnModoCrear');
    if (btn) {
        btn.className = modoCreacionRuta ? "btn btn-danger" : "btn btn-success";
        btn.innerText = modoCreacionRuta ? "Cancelar Creación ❌" : "Añadir Nuevo Sendero 🌿";
    }
}

// --- 2. GESTIÓN DE NOTIFICACIONES Y MODAL ---
function mostrarNotificacion(mensaje, esError = false) {
    const toastElement = document.getElementById('notificacionRuta');
    const mensajeElement = document.getElementById('mensajeToast');
    if (!toastElement) return;
    toastElement.classList.remove('bg-success', 'bg-danger');
    toastElement.classList.add(esError ? 'bg-danger' : 'bg-success');
    mensajeElement.innerText = mensaje;
    new bootstrap.Toast(toastElement, { delay: 3000 }).show();
}

function abrirModalRuta(distancia, dificultad, temp, callback) {
    const modalElement = document.getElementById('modalNombreSendero');
    const infoText = modalElement.querySelector('.text-muted');
    const input = document.getElementById('inputNombreSendero');
    const btnGuardar = document.getElementById('btnGuardarNombre');

    infoText.innerHTML = `
        📏 Distancia: <b>${distancia.toFixed(2)} km</b><br>
        🌡️ Temp. actual: <b>${temp}</b><br>
        🏔️ Dificultad: <b>${dificultad}</b><br>
        <span style="font-size: 0.9em;">¿Qué nombre le pondrás a esta aventura? </span>
    `;

    input.value = "";
    input.classList.remove('is-invalid');
    const modal = new bootstrap.Modal(modalElement);
    modal.show();

    const nuevoBtn = btnGuardar.cloneNode(true);
    btnGuardar.parentNode.replaceChild(nuevoBtn, btnGuardar);

    nuevoBtn.addEventListener('click', () => {
        const nombre = input.value.trim();
        if (nombre) {
            modal.hide();
            callback(nombre);
        } else {
            input.classList.add('is-invalid');
        }
    });
}

// --- 3. LÓGICA DE GUAGUAS ---
function gestionarVisibilidadParadas() {
    const zoomActual = map.getZoom();
    const ZOOM_UMBRAL = 15;
    if (zoomActual >= ZOOM_UMBRAL) {
        if (!map.hasLayer(capaGuaguas)) map.addLayer(capaGuaguas);
        const nuevoRadio = 6 + (zoomActual - ZOOM_UMBRAL) * 3;
        capaGuaguas.eachLayer(layer => {
            if (layer instanceof L.CircleMarker) layer.setRadius(nuevoRadio);
        });
    } else {
        if (map.hasLayer(capaGuaguas)) map.removeLayer(capaGuaguas);
    }
}
map.on('zoomend', gestionarVisibilidadParadas);

// --- 4. CARGA DE SENDEROS (Con Lógica de Niveles Activa) ---
async function cargarSenderos() {
    marcadoresSenderos.clearLayers();
    try {
        const response = await fetch('http://localhost:8080/api/senderos');
        const senderos = await response.json();

        senderos.forEach(async (s) => {
            if(s.latitud && s.longitud) {
                const dificultadRuta = { "Baja": 1, "Media": 2, "Alta": 3 };
                const nivelRequerido = dificultadRuta[s.dificultad] || 1;

                // Lógica de validación según nivel del usuario
                let esApto = USUARIO_SESION.nivel >= nivelRequerido;

                let msgRec = esApto ? "✅ Ruta apta para tu nivel" : "⚠️ Exceso de esfuerzo";
                let colorRec = esApto ? "#e8f5e9; color: #2e7d32" : "#ffebee; color: #c62828";

                const markerInicio = L.marker([s.latitud, s.longitud]).addTo(marcadoresSenderos);
                markerInicio.bindTooltip("🟢 Inicio: " + s.nombreSendero);
                markerInicio.bindPopup(generarHtmlPopup(s, s.estadoMeteorologico || '18.0°C', msgRec, colorRec, "Inicio"));

                if(s.latitudFin && s.longitudFin) {
                    L.polyline([[s.latitud, s.longitud], [s.latitudFin, s.longitudFin]], {
                        color: esApto ? '#2e7d32' : '#c62828',
                        weight: 4,
                        opacity: 0.6
                    }).addTo(marcadoresSenderos);

                    const marcadorFin = L.marker([s.latitudFin, s.longitudFin]).addTo(marcadoresSenderos);
                    marcadorFin.bindTooltip("🏁 Fin: " + s.nombreSendero);

                    const tempB = await obtenerTemperatura(s.latitudFin, s.longitudFin);
                    marcadorFin.bindPopup(generarHtmlPopup(s, tempB, msgRec, colorRec, "Final"));
                }
            }
        });
    } catch (error) {
        console.error("Error cargando senderos:", error);
    }
}

function generarHtmlPopup(s, temp, msg, color, etiqueta) {
    return `
        <div style="text-align:center; min-width: 180px;">
            <b style="color:#2e7d32;">${s.nombreSendero} (${etiqueta})</b>
            <div style="background: ${color}; border: 1px solid; padding:2px; border-radius:4px; font-size:0.8em; margin:5px 0;">${msg}</div>
            <div style="font-size: 0.9em; font-weight: bold;">🌡️ Temp: ${temp}</div>
            <hr style="margin:8px 0;">
            <div style="display: grid; gap: 4px;">
                <a href="/senderos/detalle/${s.id}" class="btn btn-sm btn-dark" style="color:white; text-decoration:none;">Ver Detalles</a>
                <button onclick="marcarFavorito(${s.id})" class="btn btn-sm btn-warning">⭐ Favorito</button>
            </div>
        </div>`;
}

// --- 5. PARADAS TITSA ---
fetch('http://localhost:8080/api/guaguas/paradas')
.then(res => res.json())
.then(paradas => {
    paradas.forEach(p => {
        const busMarker = L.circleMarker([p.latitude, p.longitude], {
            radius: 6,
            fillColor: "#1565c0",
            color: "#fff",
            weight: 2,
            fillOpacity: 0.9
        }).addTo(capaGuaguas);

        // Definimos el contenido del popup con el enlace dinámico
        // Asumo que tu objeto parada tiene un 'id' o 'idc'
        const urlTitsa = `https://www.titsa.com/correspondencias.php?idc=${p.id}`;

        const popupContent = `
            <div style="text-align:center;">
                <b>🚏 ${p.name}</b><br>
                <hr style="margin:5px 0;">
                <a href="${urlTitsa}" target="_blank" class="btn btn-sm btn-primary" style="color:white; text-decoration:none;">
                    Ver correspondencias 🚌
                </a>
            </div>
        `;

        // Vinculamos el popup directamente al marcador
        busMarker.bindPopup(popupContent);

        // Opcional: Asegurar que el click funcione si hay solapamiento
        busMarker.on('click', function(e) {
            L.DomEvent.stopPropagation(e);
            this.openPopup();
        });
    });
});

/*fetch('http://localhost:8080/api/guaguas/paradas')
  .then(res => res.json())
  .then(paradas => {
      paradas.forEach(p => {
          const busMarker = L.circleMarker([p.latitude, p.longitude], {
              radius: 6, fillColor: "#1565c0", color: "#fff", weight: 2, fillOpacity: 0.9
          }).addTo(capaGuaguas);
          busMarker.on('click', (e) => {
              L.DomEvent.stopPropagation(e);
              busMarker.bindPopup(`<b>🚏 ${p.name}</b>`).openPopup();
          });
      });
  });*/

// --- 6. CREACIÓN DE RUTAS (MANTENIDO EXACTAMENTE IGUAL) ---
let puntosRuta = [];
let marcadoresTemporales = [];
let lineaTemporal = null;

map.on('click', async function(e) {
    if (e.originalEvent.target.classList.contains('leaflet-marker-icon')) return;
    if (puntosRuta.length >= 2) limpiarRutaTemporal();

    const lat = e.latlng.lat;
    const lng = e.latlng.lng;
    const esInicio = puntosRuta.length === 0;

    const tempActual = await obtenerTemperatura(lat, lng);

    const punto = L.marker([lat, lng], { zIndexOffset: 1000 }).addTo(map);
    punto.bindTooltip(`${esInicio ? '🟢 Inicio' : '🏁 Fin'} | 🌡️ ${tempActual}`, { permanent: false, direction: 'top' });

    puntosRuta.push([lat, lng]);
    marcadoresTemporales.push(punto);

    if (puntosRuta.length === 2) {
        lineaTemporal = L.polyline(puntosRuta, { color: '#2e7d32', weight: 4, dashArray: '10, 10' }).addTo(map);

        const dist = L.latLng(puntosRuta[0]).distanceTo(L.latLng(puntosRuta[1])) / 1000;
        const tempNum = parseFloat(tempActual);

        let diff = dist > 7 ? "Alta" : dist > 3 ? "Media" : "Baja";
        if (tempNum > 25) {
            if (diff === "Baja") diff = "Media";
            else if (diff === "Media") diff = "Alta";
        }

        setTimeout(() => {
            abrirModalRuta(dist, diff, tempActual, (nombre) => {
                const nuevo = {
                    nombreSendero: nombre,
                    latitud: puntosRuta[0][0], longitud: puntosRuta[0][1],
                    latitudFin: puntosRuta[1][0], longitudFin: puntosRuta[1][1],
                    distancia: dist,
                    dificultad: diff,
                    estadoMeteorologico: tempActual,
                    acceso: true
                };
                fetch('http://localhost:8080/senderos/api/nuevo', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(nuevo)
                }).then(() => {
                    mostrarNotificacion("¡Sendero guardado! ");
                    cargarSenderos();
                    limpiarRutaTemporal();
                });
            });
        }, 400);
    }
});

function limpiarRutaTemporal() {
    marcadoresTemporales.forEach(m => map.removeLayer(m));
    if (lineaTemporal) map.removeLayer(lineaTemporal);
    marcadoresTemporales = []; puntosRuta = [];
}

function marcarFavorito(id) {
    const params = new URLSearchParams({ usuarioId: USUARIO_SESION.id });
    fetch(`/senderos/favorito/${id}`, { method: 'POST', body: params })
        .then(res => res.text()).then(data => mostrarNotificacion(data + " ⭐"));
}

cargarSenderos();