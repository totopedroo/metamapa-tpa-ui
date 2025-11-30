// =======================================================
//  COLECCIONES.JS – NUEVA Y EDITAR COLECCIONES (versión con títulos dinámicos)
// =======================================================

document.addEventListener("DOMContentLoaded", () => {

    // =======================================================
    // FUNCIÓN AUXILIAR: traer títulos desde backend
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
                const cont = document.getElementById("hechosIdsContainer");
                cont.innerHTML = "";
                hechos.forEach(id => {
                    cont.innerHTML += `<input type="hidden" name="hechosIds" value="${id}">`;
                });

                // Editar selección
                document.getElementById("btnEditarSeleccionNueva").onclick = () => {
                    window.location.href =
                        `/hechos?pick=true&hechos=${hechosParam}&returnTo=/colecciones/nueva`;
                };

                // Confirmar
                document.getElementById("btnConfirmarNueva").onclick = () => {
                    document.getElementById("coleccionForm").submit();
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
            const cont = document.getElementById("hechosIdsContainer");
            cont.innerHTML = "";
            nuevos.forEach(id => {
                cont.innerHTML += `<input type="hidden" name="hechosIds" value="${id}">`;
            });

            // ------- EDITAR SELECCIÓN -------
            const idColeccion = document.querySelector("[name='id']").value;

            document.getElementById("btnEditarSeleccionEditar").onclick = () => {
                window.location.href =
                    `/hechos?pick=true&hechos=${nuevos.join(",")}&returnTo=/colecciones/editar/${idColeccion}`;
            };

            // ------- CONFIRMAR -------
            document.getElementById("btnConfirmarEditar").onclick = () => {
                document.getElementById("coleccionForm").submit();
            };

        });
    }

});