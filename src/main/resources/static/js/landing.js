/* Configurable: endpoints del Proxy del Frontend */
const API = {
    // Apunta al controlador intermedio (HechoUiApiController) que crearemos/verificaremos luego
    hechosIrrestrictos: "/api-proxy/hechos?modo=irrestricto",
    hechosCurados:      "/api-proxy/hechos?modo=curado"
};

const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

/* Mobile nav toggle */
const navBtn = $(".nav-toggle");
const nav = $("#menu-principal");
if (navBtn) {
    navBtn.addEventListener("click", () => {
        const open = nav.classList.toggle("open");
        navBtn.setAttribute("aria-expanded", String(open));
        navBtn.setAttribute("aria-label", open ? "Cerrar menú" : "Abrir menú");
    });
}

/* ---- LÓGICA DE CARGA DINÁMICA (SOLO PARA TABS) ---- */

// Helper para formatear fechas (dd/mm/yyyy)
function formatDate(fechaStr) {
    if (!fechaStr) return "—";
    // Asumiendo formato ISO yyyy-mm-dd
    const [y, m, d] = fechaStr.split('-');
    return `${d}/${m}/${y}`;
}

function badge(label) { return `<span class="badge">${label}</span>`; }

function cardHecho({ titulo, descripcion, categoria, lugar, fecha }) {
    const fechaFmt = formatDate(fecha);
    return `
    <article class="card" role="listitem">
        <div class="card-header">
            <h3>${titulo}</h3>
        </div>
        <div class="card-meta">
            ${categoria ? badge(categoria) : ""} 
            ${lugar ? badge(lugar) : ""} 
            ${badge(fechaFmt)}
        </div>
        <div class="card-body">
            <p>${descripcion}</p>
        </div>
    </article>`;
}

// Fetch seguro
async function getJSON(url) {
    try {
        const r = await fetch(url, { headers: { Accept: "application/json" } });
        if (!r.ok) throw new Error("HTTP " + r.status);
        return await r.json();
    } catch (e) {
        console.error("Error JS:", e);
        return null;
    }
}

// Carga hechos vía AJAX (Solo se usa al cambiar de Tab)
async function cargarHechos(modo) {
    const el = $("#hechos-grid");
    if (!el) return;

    el.innerHTML = "<p style='padding:1rem'>Cargando...</p>";

    const url = modo === "curado" ? API.hechosCurados : API.hechosIrrestrictos;
    const data = await getJSON(url);

    // Validamos estructura { items: [...] }
    if (!data || !Array.isArray(data.items) || data.items.length === 0) {
        el.innerHTML = "<p style='padding:1rem'>No hay hechos para mostrar en esta categoría.</p>";
        return;
    }

    el.innerHTML = data.items.map(cardHecho).join("");
}

/* ---- EVENT LISTENERS ---- */

// Tabs de modo de navegación
$$(".tab").forEach(btn => {
    btn.addEventListener("click", () => {
        if (btn.classList.contains("is-active")) return;

        $$(".tab").forEach(b => {
            b.classList.remove("is-active");
            b.setAttribute("aria-selected", "false");
        });
        btn.classList.add("is-active");
        btn.setAttribute("aria-selected", "true");

        // Solo cargamos por AJAX si el usuario hace clic
        cargarHechos(btn.dataset.mode);
    });
});

/* IMPORTANTE:
   Eliminamos el "DOMContentLoaded" que llamaba a cargarColecciones() y cargarHechos().
   Ahora dejamos que Thymeleaf muestre el contenido inicial (SSR).
*/