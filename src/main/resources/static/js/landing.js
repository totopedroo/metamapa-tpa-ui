/* Configurable: endpoints de tu API real */
const API = {
    hechosIrrestrictos: "/api-proxy/hechos?modo=irrestricto",
    hechosCurados:      "/api-proxy/hechos?modo=curado"
};

const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

/* Mobile nav toggle (sin cambios) */
const navBtn = $(".nav-toggle");
const nav = $("#menu-principal");
if (navBtn) {
    navBtn.addEventListener("click", () => {
        const open = nav.classList.toggle("open");
        navBtn.setAttribute("aria-expanded", String(open));
        navBtn.setAttribute("aria-label", open ? "Cerrar menú" : "Abrir menú");
    });
}

/* ---- LÓGICA DE CARGA DINÁMICA (AJAX) PARA LAS PESTAÑAS ---- */

// Funciones para crear el HTML de las tarjetas que se cargan con AJAX
function badge(label) { return `<span class="badge">${label}</span>`; }

function cardHecho({ titulo, descripcion, categoria, lugar, fecha }) {
    const fechaFmt = fecha ? new Date(fecha).toLocaleDateString("es-AR") : "—";
    return `
    <article class="card" role="listitem">
        <div class="card-header">
            <h3>${titulo}</h3>
        </div>
        <div class="card-meta">
            ${categoria ? badge(categoria) : ""} ${lugar ? badge(lugar) : ""} ${badge(fechaFmt)}
        </div>
        <div class="card-body">
            <p>${descripcion}</p>
        </div>
    </article>`;
}

// Fetch que devuelve JSON o null si falla
async function getJSON(url) {
    try {
        const r = await fetch(url, { headers: { Accept: "application/json" } });
        if (!r.ok) throw new Error("HTTP " + r.status);
        return await r.json();
    } catch (e) {
        console.error("Error al hacer fetch:", e);
        return null; // Devuelve null en caso de error
    }
}

// Carga hechos de la API y los renderiza
async function cargarHechos(modo = "irrestricto") {
    const el = $("#hechos-grid");
    if (!el) return;
    el.innerHTML = "<p>Cargando...</p>"; // Feedback para el usuario

    const url = modo === "curado" ? API.hechosCurados : API.hechosIrrestrictos;
    const data = await getJSON(url);

    // Si la API falla o no devuelve items, muestra un mensaje
    if (!data || !Array.isArray(data.items) || data.items.length === 0) {
        el.innerHTML = "<p>No se pudieron cargar los hechos en este momento.</p>";
        return;
    }

    el.innerHTML = data.items.map(cardHecho).join("");
}

// Event listeners para las pestañas
$$(".tab").forEach(btn => {
    btn.addEventListener("click", () => {
        // No hacer nada si el tab ya está activo
        if (btn.classList.contains("is-active")) return;

        $$(".tab").forEach(b => {
            b.classList.remove("is-active");
            b.setAttribute("aria-selected", "false");
        });
        btn.classList.add("is-active");
        btn.setAttribute("aria-selected", "true");

        // Carga los hechos correspondientes al modo del botón
        cargarHechos(btn.dataset.mode);
    });
});