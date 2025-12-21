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

            hechosIds: [...new Set(
                Array.from(document.querySelectorAll("input[name='hechosIds']"))
                    .map(i => i.value)
            )],
            criteriosIds: [...new Set(
                Array.from(document.querySelectorAll("input[name='criteriosIds']"))
                    .map(i => Number(i.value))
            )]
        };

        sessionStorage.setItem(KEY, JSON.stringify(estado));
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
    }

    // ============================================================
    //   INICIALIZACIÓN AL CARGAR
    // ============================================================

    const params = new URLSearchParams(window.location.search);
    const vieneDePick = params.has("hechos");

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