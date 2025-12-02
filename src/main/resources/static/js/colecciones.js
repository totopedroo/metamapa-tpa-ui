// =======================================================
//  COLECCIONES.JS – NUEVA Y EDITAR COLECCIONES (versión con títulos dinámicos)
// =======================================================

document.addEventListener("DOMContentLoaded", () => {

    // =======================================================
    // FUNCIONES AUXILIARES
    // =======================================================
    async function cargarTitulos(ids) {
        try {
            const resp = await fetch(`http://localhost:8080/api/hechos/titulos?ids=${ids.join(",")}`)
            if (!resp.ok) return {};
            return await resp.json(); // { "5": "Accidente", "8": "Incendio" }
        } catch (err) {
            console.error("Error consultando títulos:", err);
            return {};
        }
    }

    function renderHechosSeleccionados(hechosIds, titulos = {}) {

        const ul = document.getElementById("hechosSeleccionados");
        const cont = document.getElementById("hechosIdsContainer");

        if (!ul || !cont) return;

        ul.innerHTML = "";
        cont.innerHTML = "";

        hechosIds.forEach(id => {

            const titulo = titulos[String(id)] ?? "(sin título)";

            ul.innerHTML += `
            <li class="list-group-item hecho-item d-flex justify-content-between align-items-center" data-id="${id}">
                <span>Hecho #${id} — <strong>${titulo}</strong></span>
                <button type="button" class="btn btn-sm btn-danger hecho-remove" data-id="${id}">✕</button>
            </li>
        `;

            cont.innerHTML += `<input type="hidden" name="hechosIds" value="${id}">`;
        });

        // Botones X para quitar
        document.querySelectorAll(".hecho-remove").forEach(btn => {
            btn.onclick = () => {
                const id = btn.dataset.id;
                const filtrados = hechosIds.filter(x => String(x) !== id);
                renderHechosSeleccionados(filtrados, titulos);
                guardarEstado();
            };
        });
    }

    function renderTextoCriterio(c) {
        if (c.tipo === "texto") return `Texto contiene: "${c.valor}"`;
        if (c.tipo === "fechaDesde") return `Fecha desde: ${c.desde}`;
        if (c.tipo === "fechaHasta") return `Fecha hasta: ${c.hasta}`;
        if (c.tipo === "rango") return `Rango: ${c.desde} → ${c.hasta}`;
    }

    function activarBotonesQuitarCriterio() {
        document.querySelectorAll(".quitar-criterio").forEach(btn => {
            btn.onclick = () => {
                btn.parentElement.remove();
                guardarEstado();
            };
        });
    }

    // =======================================================
    // 1) NUEVA COLECCIÓN
    // =======================================================

    const modalNueva = document.getElementById("modalConfirmarNueva");

    if (modalNueva) {

        const params = new URLSearchParams(window.location.search);
        const hechosParam = params.get("hechos");

        if (hechosParam) {

            const hechos = hechosParam.split(",").map(Number);
            const modal = new bootstrap.Modal(modalNueva);

            // TRAER TÍTULOS SOLO DE ESTOS hechos
            cargarTitulos(hechos).then(titulos => {

                modal.show();

                // Render lista
                document.getElementById("listaHechosConfirmarNueva").innerHTML =
                    hechos.map(id => `
                    <li class="list-group-item">
                        Hecho #${id} —
                        <strong>${titulos[String(id)] ?? "(sin título)"}</strong>
                    </li>
                `).join("");

                // Hidden inputs
                renderHechosSeleccionados(hechos, titulos);

                // Editar selección
                document.getElementById("btnEditarSeleccionNueva").onclick = () => {
                    window.location.href =
                        `/hechos?pick=true&hechos=${hechosParam}&returnTo=/colecciones/nueva`;
                };

                // Confirmar
                document.getElementById("btnConfirmarNueva").onclick = () => {
                    const modal = bootstrap.Modal.getInstance(document.getElementById("modalConfirmarNueva"));
                    modal.hide();

                    // Usamos SIEMPRE la función centralizada
                    renderHechosSeleccionados(hechos, titulos);
                };
            });
        }
    }

    // =======================================================
    // 2) EDITAR COLECCIÓN
    // =======================================================

    const modalEditar = document.getElementById("modalConfirmarEditar");

    if (modalEditar) {

        const params = new URLSearchParams(window.location.search);
        const hechosParam = params.get("hechos");
        if (!hechosParam) return;

        const nuevos = hechosParam.split(",").map(Number);
        const originales = JSON.parse(document.getElementById("hechosOriginalesJson").value);

        const agregados = nuevos.filter(x => !originales.includes(x));
        const quitados = originales.filter(x => !nuevos.includes(x));

        if (agregados.length === 0 && quitados.length === 0) return;

        const modal = new bootstrap.Modal(modalEditar);

        // Cargar títulos SOLO de los hechos involucrados
        const idsAConsultar = [...new Set([...nuevos, ...originales])];

        cargarTitulos(idsAConsultar).then(titulos => {

            modal.show();

            // ------- TITULO DINÁMICO -------
            const tituloModal = modalEditar.querySelector(".modal-title");

            if (agregados.length > 0 && quitados.length === 0) {
                tituloModal.textContent = "Está por agregar los siguientes hechos:";
            }
            else if (quitados.length > 0 && agregados.length === 0) {
                tituloModal.textContent = "Está por quitar los siguientes hechos:";
            }
            else {
                tituloModal.textContent = "Confirmar cambios";
            }

            // ------- LISTA DE AGREGADOS -------
            document.getElementById("listaAgregadosEditar").innerHTML =
                agregados.length
                    ? agregados.map(id =>
                        `<li class="list-group-item">
                            Hecho #${id} — <strong>${titulos[String(id)] ?? "(sin título)"}</strong>
                        </li>`
                    ).join("")
                    : `<li class="list-group-item">Ninguno</li>`;

            // ------- LISTA DE QUITADOS -------
            document.getElementById("listaQuitadosEditar").innerHTML =
                quitados.length
                    ? quitados.map(id =>
                        `<li class="list-group-item">
                            Hecho #${id} — <strong>${titulos[id] ?? ""}</strong>
                        </li>`
                    ).join("")
                    : `<li class="list-group-item">Ninguno</li>`;

            // ------- RECONSTRUIR INPUTS -------
            renderHechosSeleccionados(nuevos, titulos);

            // ------- EDITAR SELECCIÓN -------
            const idColeccion = document.querySelector("[name='id']").value;

            document.getElementById("btnEditarSeleccionEditar").onclick = () => {
                window.location.href =
                    `/hechos?pick=true&hechos=${nuevos.join(",")}&returnTo=/colecciones/editar/${idColeccion}`;
            };

            // ------- CONFIRMAR -------
            document.getElementById("btnConfirmarEditar").onclick = () => {

                const modal = bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEditar"));
                modal.hide();

                // reconstruir lista visible
                const ul = document.getElementById("hechosSeleccionados");

                // dejar los nuevos en inputs hidden
                renderHechosSeleccionados(nuevos, titulos);
            };
        });
    }

    // Botón "Seleccionar hechos" en NUEVA
    const pickNueva = document.getElementById("btnPickHechos");
    if (pickNueva) {
        pickNueva.onclick = () => {
            const ids = Array.from(document.querySelectorAll("input[name='hechosIds']"))
                .map(i => i.value)
                .join(",");

            if (typeof guardarEstado === "function") guardarEstado();

            window.location.href = `/hechos?pick=true&hechos=${ids}&returnTo=/colecciones/nueva`;
        };
    }

    // ===============================
// CAMBIO DE CAMPOS SEGÚN TIPO
// ===============================
    const criterioTipo = document.getElementById("criterioTipo");
    const campoTexto = document.getElementById("campoTexto");
    const campoFechaDesde = document.getElementById("campoFechaDesde");
    const campoFechaHasta = document.getElementById("campoFechaHasta");

    if (criterioTipo) {
        criterioTipo.onchange = () => {
            const tipo = criterioTipo.value;

            campoTexto.classList.add("d-none");
            campoFechaDesde.classList.add("d-none");
            campoFechaHasta.classList.add("d-none");

            if (tipo === "texto") {
                campoTexto.classList.remove("d-none");
            }
            if (tipo === "rango") {
                campoFechaDesde.classList.remove("d-none");
                campoFechaHasta.classList.remove("d-none");
            }
        };
    }

    // ===============================
// AGREGAR CRITERIO
// ===============================
    const btnGuardarCriterio = document.getElementById("btnGuardarCriterio");

    if (btnGuardarCriterio) {
        btnGuardarCriterio.onclick = () => {

            const tipo = criterioTipo.value;

            // VALIDACIÓN DE RANGO ÚNICO
            if (tipo === "rango") {
                const yaExisteRango = Array.from(
                    document.querySelectorAll("#criteriosSeleccionados li")
                ).some(li => JSON.parse(li.dataset.json).tipo === "rango");

                if (yaExisteRango) {
                    alert("Solo puede existir un criterio de rango de fechas.");
                    return;
                }
            }

            // ARMADO DEL OBJETO CRITERIO
            let criterio = { tipo };

            if (tipo === "texto") {
                criterio.valor = document.getElementById("criterioValorTexto").value.trim();
                if (!criterio.valor) {
                    alert("Debe ingresar un texto");
                    return;
                }
            }

            // VALIDACION DE TEXTO REPETIDO
            if (tipo === "texto") {
                const textoNuevo = criterio.valor.trim().toLowerCase();
                const repetido = Array.from(document.querySelectorAll("#criteriosSeleccionados li"))
                    .some(li => {
                        const c = JSON.parse(li.dataset.json);
                        return c.tipo === "texto" && c.valor.trim().toLowerCase() === textoNuevo;
                    });

                if (repetido) {
                    alert("Este criterio de texto ya fue ingresado.");
                    return;
                }
            }

            if (tipo === "rango") {
                criterio.desde = document.getElementById("criterioFechaDesde").value;
                criterio.hasta = document.getElementById("criterioFechaHasta").value;

                if (!criterio.desde || !criterio.hasta) {
                    alert("Debe completar fecha desde y fecha hasta.");
                    return;
                }
                if (criterio.desde > criterio.hasta) {
                    alert("La fecha desde no puede ser mayor que la fecha hasta.");
                    return;
                }
            }

            // INSERTAR EL CRITERIO EN EL UL
            const li = `
        <li class="list-group-item d-flex justify-content-between align-items-center"
            data-json='${JSON.stringify(criterio)}'>
            ${renderTextoCriterio(criterio)}
            <button class="btn btn-sm btn-danger quitar-criterio">✕</button>
        </li>
    `;

            document.getElementById("criteriosSeleccionados")
                .insertAdjacentHTML("beforeend", li);

            activarBotonesQuitarCriterio();
            guardarEstado();

            bootstrap.Modal.getInstance(document.getElementById("modalCriterios")).hide();
        }
    }

});