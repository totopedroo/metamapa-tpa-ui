document.addEventListener("DOMContentLoaded", () => {

    // ============================================================
    //   FUNCIÓN AUXILIAR cargar títulos
    // ============================================================
    async function cargarTitulos(ids) {
        try {
            const q = ids.join(",");
            const resp = await fetch(`/colecciones/ui/hechos/titulos?ids=${q}`);
            if (!resp.ok) return {};
            return await resp.json();
        } catch {
            return {};
        }
    }

    async function cargarTodosLosCriterios() {
        try {
            const resp = await fetch(`/colecciones/ui/criterios`);
            if (!resp.ok) return [];
            return await resp.json();
        } catch {
            return [];
        }
    }

    function activarBotonesQuitarCriterio() {
        document.querySelectorAll(".quitar-criterio").forEach(btn => {
            btn.onclick = () => {
                btn.parentElement.remove();
                guardarEstado();
            };
        });
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

            criteriosIds: Array.from(
                document.querySelectorAll("input[name='criteriosIds']")
            ).map(i => Number(i.value))
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

        if (est.algoritmoId) {
            const sel = document.getElementById("algoritmoSelect");
            const hid = document.getElementById("algoritmoIdHidden");

            if (sel) sel.value = est.algoritmoId;
            if (hid) hid.value = est.algoritmoId;
        }

        if (est.fuenteId) {
            const sel = document.getElementById("fuenteSelect");
            const hid = document.getElementById("fuenteIdHidden");

            if (sel) sel.value = est.fuenteId;
            if (hid) hid.value = est.fuenteId;
        }

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
        //const critContainer = document.getElementById("criteriosIdsContainer");

        critUl.innerHTML = "";
        //critContainer.innerHTML = "";

        const criteriosIds = est.criteriosIds ?? [];

        cargarTodosLosCriterios().then(lista => {

            criteriosIds.forEach(id => {
                const crit = lista.find(c => String(c.id_criterio) === String(id));
                if (!crit) return;

                let texto = (crit.tipo === "texto") ? `Texto: "${crit.valor}"` : `Rango: ${crit.desde} → ${crit.hasta}`;

                critUl.insertAdjacentHTML("beforeend", `
                <li class="list-group-item d-flex justify-content-between align-items-center"
                    data-id="${crit.id_criterio}">
                    ${texto}
                    <input type="hidden" name="criteriosIds" value="${crit.id_criterio}">
                    <button class="btn btn-sm btn-danger quitar-criterio">✕</button>
                </li>
            `);
            });

            activarBotonesQuitarCriterio();
        });
    }

    // ============================================================
    //   INICIALIZACIÓN AL CARGAR
    // ============================================================

    if (sessionStorage.getItem(KEY)) {
        restaurarEstado();
    }

    // --- Sincronizar hidden inputs después de restaurar ---
    const syncHidden = (selectId, hiddenId) => {
        const sel = document.getElementById(selectId);
        const hid = document.getElementById(hiddenId);
        if (sel && hid) hid.value = sel.value;
    };

    syncHidden("algoritmoSelect", "algoritmoIdHidden");
    syncHidden("fuenteSelect", "fuenteIdHidden");

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