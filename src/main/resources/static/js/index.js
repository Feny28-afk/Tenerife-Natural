let USUARIO_SESION = { id: 1, nivel: 1 };
const marcadoresSenderos = L.layerGroup();
// --- NUEVO: Grupo para las paradas de guagua ---
const capaGuaguas = L.layerGroup();

const map = L.map('map').setView([28.2915, -16.6291], 10);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

marcadoresSenderos.addTo(map);
// No añadimos capaGuaguas al mapa todavía, lo hará la función de zoom

function cambiarNivelFisico(nuevoNivel) {
    USUARIO_SESION.nivel = parseInt(nuevoNivel);
    cargarSenderos();
}

// --- FUNCIÓN DE CONTROL DE ZOOM (Lógica de visibilidad) ---
function gestionarVisibilidadParadas() {
    const zoomActual = map.getZoom();
    const ZOOM_UMBRAL = 15; // Ajusta este número si quieres que salgan antes o después

    if (zoomActual >= ZOOM_UMBRAL) {
        if (!map.hasLayer(capaGuaguas)) {
            map.addLayer(capaGuaguas);
        }
    } else {
        if (map.hasLayer(capaGuaguas)) {
            map.removeLayer(capaGuaguas);
        }
    }
}

// Escuchar el movimiento del zoom
map.on('zoomend', gestionarVisibilidadParadas);

// --- SECCIÓN 1: CARGA DE SENDEROS (Sin cambios) ---
// --- SECCIÓN 1: CARGA DE SENDEROS (Con botones de Favorito y Pymes) ---
function cargarSenderos() {
    marcadoresSenderos.clearLayers();
    fetch('http://localhost:8080/api/senderos')
        .then(response => response.json())
        .then(senderos => {
            senderos.forEach(s => {
                if(s.latitud && s.longitud) {
                    // Dibujar líneas de ruta (igual que antes)
                    if(s.latitudFin && s.longitudFin) {
                        const coordenadasRuta = [[s.latitud, s.longitud], [s.latitudFin, s.longitudFin]];
                        L.polyline(coordenadasRuta, { color: '#2e7d32', weight: 3, opacity: 0.5 }).addTo(marcadoresSenderos);
                        L.circleMarker([s.latitudFin, s.longitudFin], { radius: 4, color: '#c62828' }).addTo(marcadoresSenderos);
                    }

                    // Lógica de dificultad
                    const niveles = { "Baja": 1, "Media": 2, "Alta": 3 };
                    const nivelRuta = niveles[s.dificultad] || 1;
                    let msgRec = (USUARIO_SESION.nivel >= nivelRuta) ? "✅ Apto para tu nivel" : "⚠️ Exceso de esfuerzo";
                    let colorRec = (USUARIO_SESION.nivel >= nivelRuta) ? "#e8f5e9; color: #2e7d32" : "#ffebee; color: #c62828";

                    const marker = L.marker([s.latitud, s.longitud]).addTo(marcadoresSenderos);

                    // POPUP CORREGIDO: Con Favoritos y Pymes
                    let popupContent = `
                        <div style="text-align:center; min-width: 200px; font-family: sans-serif;">
                            <b style="font-size:1.2em; color:#2e7d32;">${s.nombreSendero}</b><br>
                            <div style="background: ${colorRec}; border: 1px solid; padding:4px; border-radius:5px; font-weight:bold; margin: 5px 0;">${msgRec}</div>
                            <span style="font-size: 0.9em;">🌡️ ${s.estadoMeteorologico || 'Cargando...'}</span>
                            <hr style="border: 0; border-top: 1px solid #eee;">

                            <div id="bus-info-${s.id}" style="font-size: 0.85em; margin-bottom: 8px;">⌛ Buscando guaguas...</div>

                            <div style="display: grid; gap: 5px;">
                                <a href="/senderos/detalle/${s.id}" class="btn-detalle" style="background:#2e7d32; color:white; padding:6px; text-decoration:none; border-radius:4px; font-size:0.9em;">📖 Ver Detalles y PYMEs</a>
                                <button onclick="marcarFavorito(${s.id})" style="background:#ffca28; border:none; padding:6px; border-radius:4px; cursor:pointer; font-weight:bold;">⭐ Guardar Favorito</button>
                            </div>
                        </div>
                    `;

                    marker.bindPopup(popupContent);

                    // Carga de guaguas en tiempo real al abrir popup
                    marker.on('popupopen', () => {
                        fetch(`http://localhost:8080/api/senderos/${s.id}/transporte`)
                            .then(res => res.json())
                            .then(guaguas => {
                                const container = document.getElementById(`bus-info-${s.id}`);
                                if(guaguas && guaguas.length > 0) {
                                    let html = "<b>🚌 Guaguas cerca:</b><br>";
                                    guaguas.forEach(g => { html += `L-${g.linea}: ${g.tiempoLlegada} min<br>`; });
                                    container.innerHTML = html;
                                }

                            });
                    });
                }
            });
        });
}

// --- FUNCIÓN PARA MARCAR FAVORITO (La que faltaba) ---
function marcarFavorito(senderoId) {
    // Usamos el ID de usuario 1 por defecto para las pruebas
    const usuarioId = 1;
    const params = new URLSearchParams();
    params.append('usuarioId', usuarioId);

    fetch(`/senderos/favorito/${senderoId}`, {
        method: 'POST',
        body: params
    })
    .then(res => res.text())
    .then(data => {
        // Podrías cambiar el color del botón o lanzar un aviso
        alert(data + " UwU");
    })
    .catch(err => console.error("Error al guardar favorito:", err));
}
cargarSenderos();

// --- SECCIÓN 2: PARADAS TITSA (Modificada para usar la capaGuaguas) ---
fetch('http://localhost:8080/api/guaguas/paradas')
.then(response => response.json())
.then(paradas => {
    paradas.forEach(p => {
        if(p.latitude && p.longitude) {
            // Añadimos a capaGuaguas en vez de al map directamente
            const busMarker = L.circleMarker([p.latitude, p.longitude], {
                radius: 6, fillColor: "#1565c0", color: "#fff", weight: 2, fillOpacity: 0.9
            }).addTo(capaGuaguas);

            busMarker.bindTooltip(`🚌 Parada: ${p.name}`);

            busMarker.on('click', function(e) {
                L.DomEvent.stopPropagation(e);
                const urlTitsaOficial = `https://www.titsa.com/correspondencias.php?idc=${p.id}`;
                const popupBus = `
                    <div style="min-width: 220px; text-align:center;">
                        <b style="color:#1565c0; font-size:1.1em;">🚏 ${p.name}</b><br>
                        <div id="real-time-${p.id}" style="text-align:left; margin-bottom:10px;"></div>
                        <a href="${urlTitsaOficial}" target="_blank" style="background-color: #1565c0; color: white; padding: 10px; border-radius: 8px; text-decoration: none; display: block; font-weight: bold;">
                           🚌 Ver en TITSA
                        </a>
                    </div>
                `;
                busMarker.bindPopup(popupBus).openPopup();

                fetch(`http://localhost:8080/api/horarios/parada/${p.id}`)
                    .then(res => res.json())
                    .then(tiempos => {
                        const div = document.getElementById(`real-time-${p.id}`);
                        if(tiempos && tiempos.length > 0) {
                            let html = "";
                            tiempos.forEach(t => {
                                html += `<div style="display: flex; justify-content: space-between; margin-bottom: 5px; background: #f5f5f5; padding: 5px; border-radius: 4px;">
                                            <span><b>${t.linea}</b></span><span>${t.tiempoLlegada} min ⏱️</span>
                                         </div>`;
                            });
                            div.innerHTML = html;
                        }
                    });
            });
        }
    });
    // Al cargar por primera vez, verificamos si deben verse
    gestionarVisibilidadParadas();
});

// --- SECCIÓN 3: CREACIÓN DE RUTAS ---
let puntosRuta = [];
let marcadoresTemporales = [];
let lineaTemporal = null;

map.on('click', function(e) {
    if (puntosRuta.length >= 2) {
        limpiarRutaTemporal();
    }

    const lat = e.latlng.lat;
    const lng = e.latlng.lng;

    const esInicio = puntosRuta.length === 0;
    const icono = esInicio ? '🟢' : '🏁';
    const punto = L.marker([lat, lng]).addTo(map);
    punto.bindTooltip(`${icono} ${esInicio ? 'Inicio' : 'Fin'}`, { permanent: true, direction: 'top' });

    puntosRuta.push([lat, lng]);
    marcadoresTemporales.push(punto);

    if (puntosRuta.length === 2) {
        lineaTemporal = L.polyline(puntosRuta, {
            color: '#2e7d32',
            weight: 4,
            opacity: 0.7,
            dashArray: '10, 10'
        }).addTo(map);

        // Calculamos distancia aproximada para sugerir dificultad
        const dist = Math.sqrt(Math.pow(puntosRuta[1][0]-puntosRuta[0][0], 2) + Math.pow(puntosRuta[1][1]-puntosRuta[0][1], 2));
        const dificultadSugerida = dist > 0.05 ? "Alta" : "Media";

        setTimeout(() => confirmarNuevaRuta(puntosRuta, dificultadSugerida), 500);
    }
});

function limpiarRutaTemporal() {
    marcadoresTemporales.forEach(m => map.removeLayer(m));
    if (lineaTemporal) map.removeLayer(lineaTemporal);
    marcadoresTemporales = [];
    puntosRuta = [];
    lineaTemporal = null;
}

function confirmarNuevaRuta(puntos, dificultadSugerida) {
    const nombre = prompt(`Has conectado los puntos. ¿Nombre del sendero? UwU\n(Dificultad sugerida: ${dificultadSugerida})`);
    if (nombre) {
        const nuevoSendero = {
            nombreSendero: nombre,
            latitud: puntos[0][0],
            longitud: puntos[0][1],
            latitudFin: puntos[1][0],
            longitudFin: puntos[1][1],
            dificultad: dificultadSugerida,
            acceso: true
        };

        fetch('http://localhost:8080/senderos/api/nuevo', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(nuevoSendero)
        })
        .then(res => res.text())
        .then(msg => {
            alert(msg);
            location.reload();
        });
    } else {
        limpiarRutaTemporal();
    }
}

function marcarFavorito(senderoId) {
    const params = new URLSearchParams();
    params.append('usuarioId', USUARIO_SESION.id);
    fetch(`/senderos/favorito/${senderoId}`, { method: 'POST', body: params })
    .then(res => res.text()).then(data => alert(data));
}