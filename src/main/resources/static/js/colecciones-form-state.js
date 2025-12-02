document.addEventListener("DOMContentLoaded", () => {

    // ============================================================
    //   FUNCIÓN AUXILIAR cargar títulos
    // ============================================================
    async function cargarTitulos(ids) {
        try {
            const resp = await fetch(`/api/hechos/titulos?ids=${ids.join(",")}`);
            if (!resp.ok) return {};
            return await resp.json();
        } catch {
            return {};
        }
    }

    // ============================================================
    //   CLAVE DE SESSION STORAGE
    // ============================================================
    const KEY = "coleccion_form_" + window.FORM_STATE_KEY;

    // ============================================================
    //   GUARDAR ESTADO DEL FORMULARIO
    // ============================================================
    window.guardarEstado = function () {
        const estado = {
            titulo: document.querySelector("[name='titulo']")?.value ?? "",
            descripcion: document.querySelector("[name='descripcion']")?.value ?? "",
            algoritmoId: document.getElementById("algoritmoSelect")?.value ?? "",
            fuenteId: document.getElementById("fuenteSelect")?.value ?? "",

            hechosIds: Array.from(
                document.querySelectorAll("input[name='hechosIds']")
            ).map(i => i.value),

            criterios: Array.from(
                document.querySelectorAll("#criteriosSeleccionados li")
            ).map(li => li.dataset.json ?? "")
        };

        sessionStorage.setItem(KEY, JSON.stringify(estado));
    }

    // ============================================================
    //   QUITAR HECHO
    // ============================================================
    function removerHecho(id) {
        // 1) remove LI
        document.querySelector(`#hecho-li-${id}`)?.remove();

        // 2) remove hidden inputs
        document.querySelectorAll(`input[name='hechosIds'][value='${id}']`)
            .forEach(e => e.remove());

        // 3) guardar estado actualizado
        guardarEstado();
    }

    // ============================================================
    //   RESTAURAR ESTADO
    // ============================================================
    window.restaurarEstado = function () {
        const raw = sessionStorage.getItem(KEY);
        if (!raw) return;
        const est = JSON.parse(raw);

        // Campos básicos
        if (est.titulo) document.querySelector("[name='titulo']").value = est.titulo;
        if (est.descripcion) document.querySelector("[name='descripcion']").value = est.descripcion;

        if (est.algoritmoId)
            document.getElementById("algoritmoSelect").value = est.algoritmoId;

        if (est.fuenteId)
            document.getElementById("fuenteSelect").value = est.fuenteId;


        // ===============================
        //   Restaurar hechos
        // ===============================
        const hechosContainer = document.getElementById("hechosIdsContainer");
        const ul = document.getElementById("hechosSeleccionados");

        hechosContainer.innerHTML = "";
        ul.innerHTML = "";

        if (est.hechosIds.length > 0) {
            cargarTitulos(est.hechosIds).then(titulos => {

                est.hechosIds.forEach(id => {
                    hechosContainer.insertAdjacentHTML(
                        "beforeend",
                        `<input type="hidden" name="hechosIds" value="${id}">`
                    );

                    const titulo = titulos[String(id)] ?? "(sin título)";

                    ul.insertAdjacentHTML(
                        "beforeend",
                        `
                        <li class="list-group-item d-flex justify-content-between align-items-center"
                            id="hecho-li-${id}">
                            
                            <span>
                                Hecho #${id} — <strong>${titulo}</strong>
                            </span>

                            <button class="btn btn-sm btn-danger quitar-hecho"
                                    data-id="${id}">
                                Quitar
                            </button>
                        </li>`
                    );
                });

                // Activar botones de quitar
                document.querySelectorAll(".quitar-hecho").forEach(btn => {
                    btn.addEventListener("click", () => {
                        removerHecho(btn.dataset.id);
                    });
                });

            });
        }

        // ===============================
        //   Restaurar criterios
        // ===============================
        const critUl = document.getElementById("criteriosSeleccionados");
        const critContainer = document.getElementById("criteriosIdsContainer");

        critUl.innerHTML = "";
        critContainer.innerHTML = "";

        est.criterios.forEach(json => {
            if (!json) return;
            const crit = JSON.parse(json);

            critUl.insertAdjacentHTML(
                "beforeend",
                `
    <li class="list-group-item d-flex justify-content-between align-items-center"
        data-json='${json}'>
        ${renderTextoCriterio(crit)}
        <button class="btn btn-sm btn-danger quitar-criterio">✕</button>
    </li>
    `
            );

            activarBotonesQuitarCriterio();
            // si manejás IDs, acá podés agregarlos
        });
    }

    // ============================================================
    //   INICIALIZACIÓN AL CARGAR
    // ============================================================
    restaurarEstado();

    // ============================================================
    //   GUARDAR ANTES DE IR A /hechos (pick mode)
    // ============================================================
    document.querySelectorAll("a[href*='hechos']").forEach(a => {
        a.addEventListener("click", guardarEstado);
    });

    // ============================================================
    //   LIMPIAR STORAGE AL CREAR / EDITAR
    // ============================================================
    const form = document.getElementById("coleccionForm");
    if (form) {
        form.addEventListener("submit", () => {
            sessionStorage.removeItem(KEY);
        });
    }
});