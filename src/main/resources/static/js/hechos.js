function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return token && header ? { token, header } : null;
}

(function () {

    console.log("🟢 hechos.js cargado");

    // VARIABLES GLOBALES (inyectadas por Thymeleaf)
    const PICK_MODE = window.PICK_MODE ?? false;
    const RETURN_TO = window.RETURN_TO ?? null;
    const HECHOS_INICIALES = window.HECHOS_INICIALES ?? "";

    /* ===========================================================
       HELPERS
       =========================================================== */

    function modal(id) {
        return document.getElementById(id);
    }

    function renderValue(v) {
        if (v === null || v === undefined) return "-";
        const s = String(v).trim();
        return s.length ? s : "-";
    }

    // Para no mostrar filas vacías en el modal detalle
    function addRow(label, value) {
        const v = renderValue(value);
        if (v === "-") return "";
        return `
      <div class="flex justify-between border-b pb-1">
        <span class="font-semibold">${label}:</span>
        <span class="text-right">${v}</span>
      </div>
    `;
    }

    // 👉 Etiquetas como array de strings
    function parseEtiquetas(valor) {
        if (!valor || !valor.trim()) return [];
        return valor
            .split(",")
            .map(t => t.trim())
            .filter(t => t.length > 0);
    }

    // Para editar: "" => null
    function parseNullableNumber(v) {
        if (v === null || v === undefined) return null;
        const s = String(v).trim();
        if (!s.length) return null;
        const n = parseFloat(s);
        return Number.isNaN(n) ? null : n;
    }

    /* ===========================================================
       MODAL CREAR
       =========================================================== */

    const modalCrear = modal("modalCrearHecho");
    const formCrear = document.getElementById("formCrearHecho");

    document.getElementById("abrirModalCrear")?.addEventListener("click", () =>
        (modalCrear.style.display = "block")
    );

    document.getElementById("cerrarModalCrear")?.addEventListener("click", cerrarCrearHecho);
    document.getElementById("cancelarModalCrear")?.addEventListener("click", cerrarCrearHecho);

    function cerrarCrearHecho() {
        modalCrear.style.display = "none";
        formCrear?.reset();
        document.getElementById("errorCrearHecho").textContent = "";
    }

    /* Submit crear hecho */
    formCrear?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const errorDiv = document.getElementById("errorCrearHecho");
        errorDiv.textContent = "";

        const tituloEl = document.getElementById("titulo");
        const descripcionEl = document.getElementById("descripcion");
        const categoriaEl = document.getElementById("categoria");
        const urlMultimediaEl = document.getElementById("urlMultimedia");
        const etiquetasEl = document.getElementById("etiquetas");
        const latitudEl = document.getElementById("latitud");
        const longitudEl = document.getElementById("longitud");

        // ✅ tu HTML usa ESTE id (no "fecha")
        const fechaAcontecimientoEl = document.getElementById("fechaAcontecimiento");

        const etiquetasStr = parseEtiquetas(etiquetasEl.value);
        const etiquetasObj = etiquetasStr.map(nombre => ({ nombre }));

        const payload = {
            titulo: tituloEl.value.trim(),
            descripcion: descripcionEl.value.trim(),
            categoria: categoriaEl.value.trim(),

            // Si tu backend lo requiere (vos habías puesto provincia obligatoria)
            provincia: "Buenos Aires",

            latitud: parseFloat(latitudEl.value),
            longitud: parseFloat(longitudEl.value),

            // ✅ como te lo pide el backend:
            fechaAcontecimiento: fechaAcontecimientoEl.value || null,

            // ✅ como te lo pide el backend:
            contenidoMultimedia: {
                url: urlMultimediaEl.value ? urlMultimediaEl.value.trim() : ""
            },

            // ✅ como te lo pide el backend:
            etiquetas: etiquetasObj,

            // backend dice que puede ser null
            contribuyente: null
        };

        // Validación de obligatorios
        if (!payload.titulo || !payload.categoria || !payload.fechaAcontecimiento ||
            Number.isNaN(payload.latitud) || Number.isNaN(payload.longitud)) {
            errorDiv.textContent = "Faltan datos obligatorios o hay valores inválidos.";
            return;
        }

        try {
            const csrf = getCsrf();
            const headers = { "Content-Type": "application/json" };
            if (csrf) headers[csrf.header] = csrf.token;

            // OJO: dejo tu endpoint tal cual lo tenías (/hechos/crear).
            // Si tu controller expone /hechos/ui/crear, cambialo acá.
            const resp = await fetch("/hechos/crear", {
                method: "POST",
                credentials: "same-origin",
                headers,
                body: JSON.stringify(payload)
            });

            if (!resp.ok) {
                const texto = await resp.text();
                console.error("Respuesta de error crear:", resp.status, texto);
                throw new Error(texto || `Error ${resp.status} al crear el hecho.`);
            }

            cerrarCrearHecho();
            window.location.reload();

        } catch (err) {
            console.error("Error al crear hecho:", err);
            errorDiv.textContent = err.message || "Error al crear el hecho.";
        }
    });

    // ===============================
    // Abrir modal si viene en la URL
    // ===============================
    const params = new URLSearchParams(window.location.search);
    if (params.get("abrirModalCrear") !== null) {
        modalCrear.style.display = "block";
        window.history.replaceState({}, "", "/hechos");
    }

    /* ===========================================================
       MODAL SOLICITAR ELIMINACIÓN
       =========================================================== */

    const modalSol = modal("modalSolicitudEliminacion");
    const formSol = document.getElementById("formSolicitudEliminacion");

    document.querySelectorAll(".btn-solicitar-eliminacion").forEach(btn => {
        btn.addEventListener("click", () => {
            const fila = btn.closest("tr");
            const id = fila.dataset.hechoId;
            const titulo = fila.querySelector(".col-titulo")?.textContent.trim();
            abrirSolicitud(id, titulo);
        });
    });

    function abrirSolicitud(id, titulo) {
        modalSol.style.display = "block";
        document.getElementById("idHechoSolicitud").value = id;
        document.getElementById("tituloHechoSolicitud").textContent = titulo;
    }

    document.getElementById("cerrarModalSolicitud")?.addEventListener("click", cerrarSolicitud);
    document.getElementById("cancelarModalSolicitud")?.addEventListener("click", cerrarSolicitud);

    function cerrarSolicitud() {
        modalSol.style.display = "none";
        formSol?.reset();
        document.getElementById("errorSolicitudEliminacion").textContent = "";
    }

    formSol?.addEventListener("submit", async (e) => {
        e.preventDefault();

        // 1. Preparamos los datos como FORMULARIO (No JSON)
        // Esto es vital para que tu controller Java entienda el objeto 'NuevaSolicitudForm'
        const params = new URLSearchParams();
        params.append('idHecho', document.getElementById("idHechoSolicitud").value);
        params.append('justificacion', document.getElementById("justificacionSolicitud").value.trim());
        params.append('tipo', 'ELIMINACION'); // <--- IMPORTANTE: El Java usa esto en el IF

        try {
            const csrf = getCsrf();
            const headers = {
                "Content-Type": "application/x-www-form-urlencoded"
            };
            if (csrf) headers[csrf.header] = csrf.token;

            const resp = await fetch("/contribuyente/nueva-solicitud", {
                method: "POST",
                headers: headers,
                body: params
            });

            if (!resp.ok) {
                throw new Error("Error al procesar la solicitud en el servidor.");
            }

            // 1. Cerramos el modal rojo
            cerrarSolicitud();

            // 2. Abrimos el modal verde (MANUALMENTE)
            const modalExito = document.getElementById('modalExitoSimple');
            if (modalExito) {
                modalExito.style.display = "block"; // <--- ESTO ES LO QUE ARREGLA TODO
            } else {
                alert("Solicitud enviada correctamente.");
                window.location.reload();
            }

        } catch (err) {
            console.error(err);
            document.getElementById("errorSolicitudEliminacion").textContent = "Error: " + err.message;
        }
    });
    /* ===========================================================
       ADMIN: ELIMINAR HECHO (MODAL CUSTOM)
       =========================================================== */

    const modalEliminar = document.getElementById("modalConfirmarEliminar");
    const btnConfirmarEliminar = document.getElementById("btnConfirmarEliminacion");
    const btnCancelarEliminar = document.getElementById("btnCancelarEliminacion");

    let idParaEliminar = null;

    document.querySelectorAll(".btn-eliminar-admin").forEach(btn => {
        btn.addEventListener("click", function (e) {
            e.preventDefault();
            const id = btn.getAttribute("data-id");

            if (!id) {
                alert("Error: No se encontró el ID.");
                return;
            }

            idParaEliminar = id;
            if (modalEliminar) modalEliminar.style.display = "block";
        });
    });

    function cerrarModalEliminar() {
        if (modalEliminar) modalEliminar.style.display = "none";
        idParaEliminar = null;
    }

    btnCancelarEliminar?.addEventListener("click", cerrarModalEliminar);

    window.addEventListener("click", (e) => {
        if (e.target === modalEliminar) cerrarModalEliminar();
    });

    btnConfirmarEliminar?.addEventListener("click", async () => {
        if (!idParaEliminar) return;

        try {
            btnConfirmarEliminar.innerText = "Eliminando...";
            btnConfirmarEliminar.disabled = true;

            const csrf = getCsrf();
            const headers = {};
            if (csrf) headers[csrf.header] = csrf.token;

            const resp = await fetch(`/hechos/ui/eliminar/${idParaEliminar}`, {
                method: "DELETE",
                credentials: "same-origin",
                headers
            });

            if (!resp.ok) {
                const msg = await resp.text();
                throw new Error(msg);
            }

            cerrarModalEliminar();
            window.location.reload();

        } catch (err) {
            console.error(err);
            alert("Error eliminando hecho: " + err.message);
            cerrarModalEliminar();
        } finally {
            btnConfirmarEliminar.innerText = "Sí, eliminar";
            btnConfirmarEliminar.disabled = false;
        }
    });

    /* ===========================================================
       MODAL EDITAR
       =========================================================== */

    const modalEditar = modal("modalEditarHecho");
    const formEditar = document.getElementById("formEditarHecho");

    document.querySelectorAll(".btn-editar-hecho").forEach(btn => {
        btn.addEventListener("click", () => {
            const fila = btn.closest("tr");
            abrirEditar(fila);
        });
    });

    function abrirEditar(fila) {
        modalEditar.style.display = "block";
        const id = fila.dataset.hechoId;
        document.getElementById("idHechoEditar").value = id;

        document.getElementById("tituloEditar").value =
            fila.querySelector(".col-titulo")?.textContent.trim() || "";
        document.getElementById("descripcionEditar").value =
            fila.querySelector(".col-descripcion")?.textContent.trim() || "";
        document.getElementById("categoriaEditar").value = fila.dataset.hechoCategoria || "";
        document.getElementById("latitudEditar").value = fila.dataset.hechoLatitud || "";
        document.getElementById("longitudEditar").value = fila.dataset.hechoLongitud || "";

        // Si el dataset tiene contenido, lo uso
        document.getElementById("urlMultimediaEditar").value = fila.dataset.hechoContenido || "";

        // Fecha acontecimiento desde dataset si existe
        document.getElementById("fechaEditar").value = fila.dataset.hechoFechaAcontecimiento || "";

        // Etiquetas desde spans
        const etiquetas = [...fila.querySelectorAll(".col-etiquetas span")]
            .map(s => s.textContent.trim())
            .join(", ");
        document.getElementById("etiquetasEditar").value = etiquetas;
    }

    document.getElementById("cerrarModalEditar")?.addEventListener("click", cerrarEditar);
    document.getElementById("cancelarModalEditar")?.addEventListener("click", cerrarEditar);

    function cerrarEditar() {
        modalEditar.style.display = "none";
        formEditar?.reset();
        document.getElementById("errorEditarHecho").textContent = "";
    }

    formEditar?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const errorDiv = document.getElementById("errorEditarHecho");
        errorDiv.textContent = "";

        const id = document.getElementById("idHechoEditar").value;

        const etiquetasStr = parseEtiquetas(document.getElementById("etiquetasEditar").value);
        const etiquetasObj = etiquetasStr.map(nombre => ({ nombre }));

        // Ajusto al contrato del back
        const payload = {
            titulo: document.getElementById("tituloEditar").value.trim() || null,
            descripcion: document.getElementById("descripcionEditar").value.trim() || null,
            categoria: document.getElementById("categoriaEditar").value.trim() || null,
            latitud: parseNullableNumber(document.getElementById("latitudEditar").value),
            longitud: parseNullableNumber(document.getElementById("longitudEditar").value),

            fechaAcontecimiento: document.getElementById("fechaEditar").value || null,

            contenidoMultimedia: {
                url: document.getElementById("urlMultimediaEditar").value
                    ? document.getElementById("urlMultimediaEditar").value.trim()
                    : ""
            },

            etiquetas: etiquetasObj
        };

        try {
            const csrf = getCsrf();
            const headers = { "Content-Type": "application/json" };
            if (csrf) headers[csrf.header] = csrf.token;

            const resp = await fetch(`/hechos/ui/editar/${id}`, {
                method: "PATCH",
                credentials: "same-origin",
                headers,
                body: JSON.stringify(payload)
            });

            if (!resp.ok) throw new Error(await resp.text());
            cerrarEditar();
            window.location.reload();

        } catch (err) {
            errorDiv.textContent = err.message;
        }
    });

    /* ===========================================================
       LIMPIAR FILTROS
       =========================================================== */

    document.getElementById("btnLimpiarFiltros")?.addEventListener("click", () =>
        window.location.href = "/hechos"
    );

    /* ===========================================================
       MAPA
       =========================================================== */

    const mapContainer = document.getElementById("mapHechos");

    if (mapContainer) {

        const map = L.map("mapHechos").setView([-34.6037, -58.3816], 11);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        const filas = document.querySelectorAll("tbody tr[data-hecho-id]");
        const bounds = [];

        filas.forEach(fila => {
            const lat = parseFloat(fila.dataset.hechoLatitud);
            const lng = parseFloat(fila.dataset.hechoLongitud);

            if (!isNaN(lat) && !isNaN(lng)) {
                const titulo = fila.querySelector(".col-titulo")?.textContent.trim();
                const id = fila.dataset.hechoId;

                const marker = L.marker([lat, lng])
                    .addTo(map)
                    .bindPopup(`<strong>#${id}</strong><br>${titulo}`);

                fila._marker = marker;
                bounds.push([lat, lng]);
            }
        });

        if (bounds.length > 0) map.fitBounds(bounds, { padding: [20, 20] });

        document.querySelectorAll(".btn-ver-mapa").forEach(btn => {
            btn.addEventListener("click", () => {
                const fila = btn.closest("tr");
                const lat = parseFloat(fila.dataset.hechoLatitud);
                const lng = parseFloat(fila.dataset.hechoLongitud);

                if (!isNaN(lat) && !isNaN(lng)) {
                    map.setView([lat, lng], 15);
                    fila._marker?.openPopup();
                }
            });
        });
    }

    /* ===========================================================
       MODAL VER DETALLE (FIX: NO MOSTRAR “-”)
       =========================================================== */

    const modalDetalle = modal("modalDetalleHecho");
    const detalleBody = document.getElementById("detalleHechoBody");
    const errorDetalle = document.getElementById("errorDetalleHecho");

    document.querySelectorAll(".btn-ver-detalle").forEach(btn => {
        btn.addEventListener("click", () => {
            const fila = btn.closest("tr");
            abrirDetalleDesdeFila(fila);
        });
    });

    function abrirDetalleDesdeFila(fila) {
        if (!modalDetalle || !detalleBody) return;

        modalDetalle.style.display = "block";
        if (errorDetalle) errorDetalle.textContent = "";

        const ds = fila.dataset;

        const etiquetas = (ds.hechoEtiquetas || "")
            .split(",")
            .map(s => s.trim())
            .filter(Boolean);

        const consensos = (ds.hechoConsensos || "")
            .split(",")
            .map(s => s.trim())
            .filter(Boolean);

        const fuentes = (ds.hechoFuentes || "")
            .split(",")
            .map(s => s.trim())
            .filter(Boolean);

        // consensuado: solo mostrar si existe el dato
        let consensuadoTxt = null;
        if (ds.hechoConsensuado === "true") consensuadoTxt = "Sí";
        else if (ds.hechoConsensuado === "false") consensuadoTxt = "No";

        let html = "";
        html += addRow("ID", ds.hechoId);
        html += addRow("Título", ds.hechoTitulo);
        html += addRow("Descripción", ds.hechoDescripcion);
        html += addRow("Categoría", ds.hechoCategoria);
        html += addRow("Latitud", ds.hechoLatitud);
        html += addRow("Longitud", ds.hechoLongitud);
        html += addRow("Fecha acontecimiento", ds.hechoFechaAcontecimiento);
        html += addRow("Fecha carga", ds.hechoFechaCarga);

        if (etiquetas.length) html += addRow("Etiquetas", etiquetas.join(", "));
        if (consensos.length) html += addRow("Consensos", consensos.join(", "));
        if (consensuadoTxt) html += addRow("Consensuado", consensuadoTxt);
        if (fuentes.length) html += addRow("Fuentes", fuentes.join(", "));

        // Contenido multimedia: solo si existe
        if (ds.hechoContenido && ds.hechoContenido.trim().length) {
            html += `
        <div class="flex justify-between border-b pb-1">
          <span class="font-semibold">Contenido multimedia:</span>
          <span class="text-right">
            <a href="${ds.hechoContenido}" target="_blank" class="text-blue-600 underline">Ver recurso</a>
          </span>
        </div>
      `;
        }

        detalleBody.innerHTML = html || `<div class="text-gray-500">Sin datos para mostrar.</div>`;
    }

    function cerrarDetalleHecho() {
        if (!modalDetalle) return;
        modalDetalle.style.display = "none";
        if (detalleBody) detalleBody.innerHTML = "";
        if (errorDetalle) errorDetalle.textContent = "";
    }

    document.getElementById("cerrarModalDetalle")?.addEventListener("click", cerrarDetalleHecho);
    document.getElementById("cancelarModalDetalle")?.addEventListener("click", cerrarDetalleHecho);

    /* ======================================================
       PICK MODE
       ====================================================== */
    if (PICK_MODE) {

        const hidden = document.getElementById("hechosHidden");

        function obtenerSeleccionados() {
            if (!hidden.value) return [];
            return hidden.value.split(",").filter(x => x);
        }

        function marcarCheckBoxes() {
            const seleccionados = obtenerSeleccionados();
            seleccionados.forEach(id => {
                const cb = document.querySelector(`.pick-checkbox[value="${id}"]`);
                if (cb) cb.checked = true;
            });
        }
        marcarCheckBoxes();

        document.addEventListener("change", e => {
            if (!e.target.classList.contains("pick-checkbox")) return;

            const prev = new Set(obtenerSeleccionados());

            if (e.target.checked) prev.add(e.target.value);
            else prev.delete(e.target.value);

            hidden.value = [...prev].join(",");
        });

        document.querySelectorAll("a.page-link").forEach(a => {
            a.addEventListener("click", () => {
                const seleccionados = obtenerSeleccionados();
                if (seleccionados.length === 0) return;

                const url = new URL(a.href);
                url.searchParams.set("hechos", seleccionados.join(","));
                a.href = url.toString();
            });
        });

        const formFiltros = document.getElementById("filtrosForm");
        formFiltros?.addEventListener("submit", () => {
            const seleccionados = obtenerSeleccionados();
            document.getElementById("hechosHidden").value = seleccionados.join(",");
        });

        document.querySelectorAll("form select[name='size']").forEach(sel => {
            sel.addEventListener("change", function () {
                const form = this.closest("form");
                const seleccionados = obtenerSeleccionados();

                const hiddenInput = document.createElement("input");
                hiddenInput.type = "hidden";
                hiddenInput.name = "hechos";
                hiddenInput.value = seleccionados.join(",");

                form.appendChild(hiddenInput);
                form.submit();
            });
        });

        const btn = document.getElementById("btnConfirmarPick");
        if (btn) {
            btn.addEventListener("click", () => {
                const ids = obtenerSeleccionados();
                if (!RETURN_TO) {
                    alert("Error: returnTo no definido");
                    return;
                }
                window.location.href = RETURN_TO + "?hechos=" + ids.join(",");
            });
        }
    }

})();
