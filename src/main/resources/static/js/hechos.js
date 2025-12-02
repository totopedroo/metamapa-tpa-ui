(function () {

    console.log("🟢 hechos.js cargado");

    // VARIABLES GLOBALES (inyectadas por Thymeleaf)
    const PICK_MODE = window.PICK_MODE ?? false;
    const RETURN_TO = window.RETURN_TO ?? null;
    const HECHOS_INICIALES = window.HECHOS_INICIALES ?? "";

    /* ================== URLS BACK ================== */
    const urlBaseFuenteDinamica = "http://localhost:8080/fuente-dinamica";
    const urlCrearHecho = `${urlBaseFuenteDinamica}/hechos/crear`;
    const urlEditarHechoBase = `${urlBaseFuenteDinamica}/hechos/editar/`;
    const urlCrearSolicitud = "http://localhost:8080/solicitudes";
    const urlDetalleHechoBase = "http://localhost:8080/api/hechos/";

    /* ===========================================================
       MODALES: crear, editar, solicitar eliminación
       =========================================================== */

    function modal(id) {
        return document.getElementById(id);
    }

    /* ------------------ MODAL CREAR ------------------ */

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

        const payload = {
            titulo: titulo.value.trim(),
            descripcion: descripcion.value.trim(),
            categoria: categoria.value.trim(),
            latitud: parseFloat(latitud.value),
            longitud: parseFloat(longitud.value),
            fechaAcontecimiento: fecha.value,
            contenidoMultimedia: urlMultimedia.value ? { url: urlMultimedia.value.trim() } : { url: null },
            etiquetas: parseEtiquetas(etiquetas.value)
        };

        try {
            const resp = await fetch(urlCrearHecho, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!resp.ok) throw new Error(await resp.text());
            cerrarCrearHecho();
            window.location.reload();

        } catch (err) {
            document.getElementById("errorCrearHecho").textContent = err.message;
        }
    });

    function parseEtiquetas(valor) {
        if (!valor.trim()) return [];
        return valor.split(",")
            .map(t => t.trim())
            .filter(t => t.length > 0)
            .map(nombre => ({ nombre }));
    }

    // ===============================
    // Abrir modal si viene en la URL
    // ===============================
    const params = new URLSearchParams(window.location.search);
    if (params.get("abrirModalCrear") !== null) {
        modalCrear.style.display = "block";

        // OPCIONAL: limpiar la URL (saca ?abrirModalCrear sin recargar)
        window.history.replaceState({}, "", "/hechos");
    }

    /* ------------------ MODAL SOLICITAR ELIMINACIÓN ------------------ */

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

        const payload = {
            idHecho: parseInt(document.getElementById("idHechoSolicitud").value, 10),
            justificacion: document.getElementById("justificacionSolicitud").value.trim()
        };

        try {
            const resp = await fetch(urlCrearSolicitud, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!resp.ok) throw new Error(await resp.text());

            cerrarSolicitud();
            alert("Solicitud enviada correctamente.");

        } catch (err) {
            document.getElementById("errorSolicitudEliminacion").textContent = err.message;
        }
    });

    /* ------------------ ADMIN: ELIMINAR HECHO ------------------ */

    document.querySelectorAll(".btn-eliminar-admin").forEach(btn => {
        btn.addEventListener("click", async function () {
            const fila = btn.closest("tr");
            const id = fila.dataset.hechoId;   // ← ESTA ES LA VARIABLE CORRECTA

            if (!confirm("¿Seguro que deseas eliminar este hecho?")) return;

            try {
                const resp = await fetch(`http://localhost:8080/api/hechos/eliminar/${id}`, {
                    method: "DELETE",
                    headers: {
                        "Authorization": "Bearer " + JWT
                    }
                });

                if (!resp.ok) {
                    const msg = await resp.text();
                    throw new Error(msg);
                }

                alert("Hecho eliminado correctamente.");
                window.location.reload();

            } catch (err) {
                alert("Error eliminando hecho: " + err.message);
            }
        });
    });

    /* ------------------ MODAL EDITAR ------------------ */

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

        document.getElementById("tituloEditar").value = fila.querySelector(".col-titulo")?.textContent.trim() || "";
        document.getElementById("descripcionEditar").value = fila.querySelector(".col-descripcion")?.textContent.trim() || "";
        document.getElementById("categoriaEditar").value = fila.dataset.hechoCategoria || "";
        document.getElementById("latitudEditar").value = fila.dataset.hechoLatitud || "";
        document.getElementById("longitudEditar").value = fila.dataset.hechoLongitud || "";

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

        const id = document.getElementById("idHechoEditar").value;

        const payload = {
            titulo: document.getElementById("tituloEditar").value.trim() || null,
            descripcion: document.getElementById("descripcionEditar").value.trim() || null,
            categoria: document.getElementById("categoriaEditar").value.trim() || null,
            latitud: parseNullable(document.getElementById("latitudEditar").value),
            longitud: parseNullable(document.getElementById("longitudEditar").value),
            fechaAcontecimiento: document.getElementById("fechaEditar").value || null,
            contenidoMultimedia: parseUrl(document.getElementById("urlMultimediaEditar").value),
            etiquetas: parseEtiquetas(document.getElementById("etiquetasEditar").value)
        };

        try {
            const resp = await fetch(urlEditarHechoBase + id, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!resp.ok) throw new Error(await resp.text());
            cerrarEditar();
            window.location.reload();

        } catch (err) {
            document.getElementById("errorEditarHecho").textContent = err.message;
        }
    });

    function parseNullable(v) {
        return v === "" ? null : parseFloat(v);
    }

    function parseUrl(url) {
        url = url.trim();
        return url ? { url } : null;
    }

    /* ------------------ LIMPIAR FILTROS ------------------ */

    document.getElementById("btnLimpiarFiltros")?.addEventListener("click", () =>
        window.location.href = "/hechos"
    );

    /* ------------------ MAPA ------------------ */

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


    /* ------------------ MODAL VER DETALLE ------------------ */

    const modalDetalle = modal("modalDetalleHecho");
    const detalleBody = document.getElementById("detalleHechoBody");
    const errorDetalle = document.getElementById("errorDetalleHecho");

    // Abrir modal usando los datos de la fila
    document.querySelectorAll(".btn-ver-detalle").forEach(btn => {
        btn.addEventListener("click", () => {
            const fila = btn.closest("tr");
            abrirDetalleDesdeFila(fila);
        });
    });

    function abrirDetalleDesdeFila(fila) {
        if (!modalDetalle || !detalleBody) return;

        modalDetalle.style.display = "block";
        errorDetalle.textContent = "";

        const ds = fila.dataset;

        const id = ds.hechoId || "";
        const titulo = ds.hechoTitulo || "";
        const descripcion = ds.hechoDescripcion || "";
        const categoria = ds.hechoCategoria || "";
        const latitud = ds.hechoLatitud || "";
        const longitud = ds.hechoLongitud || "";
        const fechaAcontecimiento = ds.hechoFechaAcontecimiento || "";
        const horaAcontecimiento = ds.hechoHoraAcontecimiento || "";
        const fechaCarga = ds.hechoFechaCarga || "";
        const etiquetas = ds.hechoEtiquetas || "";
        const consensos = ds.hechoConsensos || "";
        const consensuado = ds.hechoConsensuado === "true" ? "Sí" : "No";
        const fuentes = ds.hechoFuentes || "";
        const contenido = ds.hechoContenido || "";

        detalleBody.innerHTML = `
            <p><strong>ID:</strong> ${id || "-"}</p>
            <p><strong>Título:</strong> ${titulo || "-"}</p>
            <p><strong>Descripción:</strong> ${descripcion || "-"}</p>
            <p><strong>Categoría:</strong> ${categoria || "-"}</p>
           
            <p><strong>Latitud:</strong> ${latitud || "-"}</p>
            <p><strong>Longitud:</strong> ${longitud || "-"}</p>
            <p><strong>Fecha acontecimiento:</strong> ${fechaAcontecimiento || "-"}</p>
            <p><strong>Hora acontecimiento:</strong> ${horaAcontecimiento || "-"}</p>
            <p><strong>Fecha carga:</strong> ${fechaCarga || "-"}</p>
            <p><strong>Etiquetas:</strong> ${etiquetas || "-"}</p>
            <p><strong>Consensuado:</strong> ${consensuado}</p>
            <p><strong>Consensos:</strong> ${consensos || "-"}</p>
            <p><strong>Fuentes:</strong> ${fuentes || "-"}</p>
            <p><strong>Contenido multimedia:</strong>
                ${contenido
            ? `<a href="${contenido}" target="_blank" class="text-blue-600 underline">Ver recurso</a>`
            : "-"
        }
            </p>
        `;
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

        /** 1. Marcar los checkboxes al cargar */
        function marcarCheckBoxes() {
            const seleccionados = obtenerSeleccionados();
            seleccionados.forEach(id => {
                const cb = document.querySelector(`.pick-checkbox[value="${id}"]`);
                if (cb) cb.checked = true;
            });
        }
        marcarCheckBoxes();

        /** 2. Cuando cambia un checkbox, actualizar el hidden */
        document.addEventListener("change", e => {
            if (!e.target.classList.contains("pick-checkbox")) return;

            const prev = new Set(obtenerSeleccionados());

            if (e.target.checked) {
                prev.add(e.target.value);
            } else {
                prev.delete(e.target.value);
            }

            hidden.value = [...prev].join(",");
        });

        /** 3. Propagar HECHOS a todos los links de paginación */
        document.querySelectorAll("a.page-link").forEach(a => {
            a.addEventListener("click", e => {
                const seleccionados = obtenerSeleccionados();
                if (seleccionados.length === 0) return;

                const url = new URL(a.href);
                url.searchParams.set("hechos", seleccionados.join(","));
                a.href = url.toString();
            });
        });

        /** 4. Propagar HECHOS al enviar el formulario de filtros */
        const formFiltros = document.getElementById("filtrosForm");
        formFiltros.addEventListener("submit", () => {
            const seleccionados = obtenerSeleccionados();
            document.getElementById("hechosHidden").value = seleccionados.join(",");
        });

        /** 5. Propagar HECHOS al cambiar el selector "Mostrar X por página" */
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

        /** 6. Confirmar selección */
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