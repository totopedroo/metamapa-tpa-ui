// =======================================================
// FUNCIONES AUXILIARES
// =======================================================
async function cargarTitulos(ids) {
        try {
            const resp = await fetch(`/colecciones/ui/hechos/titulos?ids=${ids.join(",")}`);
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
    if (c.tipo === "fecha") return `Rango: ${c.desde} → ${c.hasta}`;
}

function activarBotonesQuitarCriterio() {
        document.querySelectorAll(".quitar-criterio").forEach(btn => {
            btn.onclick = () => {
                btn.parentElement.remove();
                guardarEstado();
            };
        });
    }

async function cargarCriteriosExistentes() {
    const resp = await fetch("/colecciones/ui/criterios");
    if (!resp.ok) return;
        const criterios = await resp.json();

        const sel = document.getElementById("criterioExistente");
        if (!sel) return;

        sel.innerHTML = `<option value="">-- Seleccionar existente --</option>`;

        criterios.forEach(c => {
            const texto =
                c.tipo === "texto"
                    ? `Texto: "${c.valor}"`
                    : `Fechas: ${c.desde} → ${c.hasta}`;

            sel.innerHTML += `<option value="${c.id_criterio}">${texto}</option>`;
        });
    }

function agregarCriterioExistente(id) {

        // evitar duplicados
        const yaExiste = Array.from(
            document.querySelectorAll("input[name='criteriosIds']")
        ).some(i => Number(i.value) === Number(id));

        if (yaExiste) {
            alert("Ese criterio ya está agregado.");
            return;
        }

        const sel = document.getElementById("criterioExistente");
        const texto = sel.options[sel.selectedIndex].text;

        // Agregar visual
        document.getElementById("criteriosSeleccionados").insertAdjacentHTML(
            "beforeend",
            `
            <li class="list-group-item d-flex justify-content-between align-items-center"
                data-id="${id}">
                ${texto}
                <button class="btn btn-sm btn-danger quitar-criterio">✕</button>
                <input type="hidden" name="criteriosIds" value="${id}">
            </li>
        `
        );

        activarBotonesQuitarCriterio();
        guardarEstado();

        sel.value = "";
        bootstrap.Modal.getInstance(document.getElementById("modalCriterios")).hide();
    }

document.addEventListener("DOMContentLoaded", () => {

    const algSel = document.getElementById("algoritmoSelect");
    const algHidden = document.getElementById("algoritmoIdHidden");
    if (algSel && algHidden) algHidden.value = algSel.value || "";

    const fuenteSel = document.getElementById("fuenteSelect");
    const fuenteHidden = document.getElementById("fuenteIdHidden");
    if (fuenteSel && fuenteHidden) fuenteHidden.value = fuenteSel.value || "";
    if (document.getElementById("criterioExistente")) {
        cargarCriteriosExistentes();
    }

    const criterioExistente = document.getElementById("criterioExistente");

    if (criterioExistente) {
        criterioExistente.onchange = () => {
            const id = criterioExistente.value;
            if (id) agregarCriterioExistente(id);
        };
    }

    // =======================================================
    // 1) NUEVA COLECCIÓN
    // =======================================================
    if (window.FORM_STATE_KEY === "nueva") {

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
    }

    // =======================================================
    // 2) EDITAR COLECCIÓN
    // =======================================================

    const modalEditar = document.getElementById("modalConfirmarEditar");

    if (modalEditar) {

        const params = new URLSearchParams(window.location.search);
        const hechosParam = params.get("hechos");

        if (hechosParam) {

            const nuevos = hechosParam.split(",").map(Number);
            const originales = JSON.parse(
                document.getElementById("hechosOriginalesJson").value || "[]"
            );

            const agregados = nuevos.filter(x => !originales.includes(x));
            const quitados = originales.filter(x => !nuevos.includes(x));

            if (agregados.length > 0 || quitados.length > 0) {

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
                                Hecho #${id} — <strong>${titulos[String(id)] ?? "(sin título)"}</strong>
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

                        const modalInst = bootstrap.Modal.getInstance(modalEditar);
                        modalInst.hide();

                        renderHechosSeleccionados(nuevos, titulos);
                    };
                });
            }
        }
    }

    // Botón "Seleccionar hechos"
    const btnPick = document.getElementById("btnPickHechos");

    if (btnPick) {
        btnPick.onclick = () => {

            const ids = Array.from(
                document.querySelectorAll("input[name='hechosIds']")
            ).map(i => i.value).join(",");

            if (typeof guardarEstado === "function") {
                guardarEstado();
            }

            if (!window.RETURN_TO) {
                alert("Error: RETURN_TO no definido");
                return;
            }

            window.location.href =
                `/hechos?pick=true&hechos=${ids}&returnTo=${encodeURIComponent(window.RETURN_TO)}`;
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
        btnGuardarCriterio.onclick = async () => {

            const tipo = criterioTipo.value;

            let criterio = {tipo};

            // Backend usa tipo = "fecha" para rangos
            if (tipo === "rango") {
                criterio.tipo = "fecha";
            }

            // VALIDACIÓN DE RANGO ÚNICO
            if (tipo === "rango") {
                const yaExisteRango = Array.from(
                    document.querySelectorAll("#criteriosSeleccionados li")
                ).some(li => {
                    if (!li.dataset.json) return false;
                    const c = JSON.parse(li.dataset.json);
                    return c.tipo === "rango";
                });

                if (yaExisteRango) {
                    alert("Solo puede existir un criterio de rango de fechas.");
                    return;
                }
            }

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
                        if (!li.dataset.json) return false;

                        const c = JSON.parse(li.dataset.json);
                        return c.tipo === "texto" &&
                            c.valor.trim().toLowerCase() === textoNuevo;
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

            // =======================
            // VALIDAR CONTRA CRITERIOS EXISTENTES EN BACKEND
            // =======================
            const existentesResp = await fetch("/colecciones/ui/criterios");
            const existentes = existentesResp.ok ? await existentesResp.json() : [];

            // --- Si es texto ---
            if (tipo === "texto") {
                const existeIgual = existentes.some(c =>
                    c.tipo === "texto" &&
                    c.valor.trim().toLowerCase() === criterio.valor.trim().toLowerCase()
                );

                if (existeIgual) {
                    alert("Ese criterio de texto ya existe en el sistema.");
                    return;
                }
            }

            // --- Si es rango ---
            if (tipo === "rango") {
                const existeRango = existentes.some(c =>
                    c.tipo === "fecha" &&
                    c.desde === criterio.desde &&
                    c.hasta === criterio.hasta
                );

                if (existeRango) {
                    alert("Ese rango de fechas ya existe en el sistema.");
                    return;
                }
            }

            // 1) CREAR EN BACKEND
            const resp = await fetch("/colecciones/ui/criterios", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(criterio)
            });

            const criterioId = await resp.json(); // backend devuelve el ID nuevo

            // 2) AGREGARLO A LA LISTA VISIBLE
            const li = `
            <li class="list-group-item d-flex justify-content-between align-items-center"
                data-id="${criterioId}"
                data-json='${JSON.stringify(criterio)}'>
                ${renderTextoCriterio(criterio)}
                <button class="btn btn-sm btn-danger quitar-criterio">✕</button>
                <input type="hidden" name="criteriosIds" value="${criterioId}">
            </li>`;

            document.getElementById("criteriosSeleccionados")
                .insertAdjacentHTML("beforeend", li);

            activarBotonesQuitarCriterio();
            guardarEstado();

            bootstrap.Modal.getInstance(document.getElementById("modalCriterios")).hide();
        };
    }

});