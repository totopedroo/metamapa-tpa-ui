document.addEventListener("DOMContentLoaded", () => {

    const KEY = "coleccion_form_" + window.FORM_STATE_KEY;

    function guardarEstado() {
        const estado = {
            titulo: document.querySelector("[name='titulo']")?.value ?? "",
            descripcion: document.querySelector("[name='descripcion']")?.value ?? "",
            algoritmoId: document.getElementById("algoritmoSelect")?.value ?? "",
            fuenteId: document.getElementById("fuenteSelect")?.value ?? "",
            hechosIds: Array.from(document.querySelectorAll("input[name='hechosIds']")).map(i => i.value),
            criterios: Array.from(document.querySelectorAll("#criteriosSeleccionados li")).map(li => li.dataset.json)
        };

        sessionStorage.setItem(KEY, JSON.stringify(estado));
    }

    function restaurarEstado() {
        const raw = sessionStorage.getItem(KEY);
        if (!raw) return;
        const est = JSON.parse(raw);

        if (est.titulo) document.querySelector("[name='titulo']").value = est.titulo;
        if (est.descripcion) document.querySelector("[name='descripcion']").value = est.descripcion;

        if (est.algoritmoId)
            document.getElementById("algoritmoSelect").value = est.algoritmoId;

        if (est.fuenteId)
            document.getElementById("fuenteSelect").value = est.fuenteId;

        // === Hechos ===
        const hechosContainer = document.getElementById("hechosIdsContainer");
        hechosContainer.innerHTML = "";

        const ul = document.getElementById("hechosSeleccionados");
        ul.innerHTML = "";

        est.hechosIds.forEach(id => {
            hechosContainer.innerHTML += `<input type="hidden" name="hechosIds" value="${id}">`;
            ul.innerHTML += `<li class="list-group-item">Hecho #${id}</li>`;
        });

        // === Criterios ===
        const critUl = document.getElementById("criteriosSeleccionados");
        const critContainer = document.getElementById("criteriosIdsContainer");

        critUl.innerHTML = "";
        critContainer.innerHTML = "";

        est.criterios.forEach(json => {
            const crit = JSON.parse(json);
            critUl.innerHTML += `<li class='list-group-item' data-json='${json}'>
                ${crit.tipo}: ${crit.valor}
            </li>`;
            // si querés almacenar IDs, agregar aquí
        });
    }

    // RESTAURAR SI HAY ESTADO
    restaurarEstado();

    // GUARDAR ANTES DE IR A /hechos
    document.querySelectorAll("a[href*='hechos']").forEach(a => {
        a.addEventListener("click", guardarEstado);
    });

    // GUARDAR ANTES DE SUBMIT
    const form = document.getElementById("coleccionForm");
    if (form) {
        form.addEventListener("submit", () => {
            sessionStorage.removeItem(KEY); // limpiamos
        });
    }
});