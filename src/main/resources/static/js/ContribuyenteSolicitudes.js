document.addEventListener('DOMContentLoaded', function() {

    const STORAGE_KEY = 'activeTabContribuyente';
    const tabButtons = document.querySelectorAll('#solicitudesTabs button[data-bs-toggle="tab"]');
    const tabPanes = document.querySelectorAll('.tab-pane');

    // --- FUNCIÓN PARA ACTIVAR UNA PESTAÑA ---
    function activarPestana(targetId) {
        // 1. Desactivar todos los botones
        tabButtons.forEach(btn => {
            btn.classList.remove('active');
            btn.setAttribute('aria-selected', 'false');
        });

        // 2. Ocultar todos los contenidos
        tabPanes.forEach(pane => {
            pane.classList.remove('show', 'active');
        });

        // 3. Activar el botón seleccionado
        const selectedBtn = document.querySelector(`button[data-bs-target="${targetId}"]`);
        if (selectedBtn) {
            selectedBtn.classList.add('active');
            selectedBtn.setAttribute('aria-selected', 'true');
        }

        // 4. Mostrar el contenido seleccionado
        const selectedPane = document.querySelector(targetId);
        if (selectedPane) {
            selectedPane.classList.add('show', 'active');
        }

        // 5. Guardar en memoria
        localStorage.setItem(STORAGE_KEY, targetId);
    }

    // --- EVENT LISTENER PARA CLICKS ---
    tabButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault(); // Evitamos comportamiento default por si acaso
            const targetId = this.getAttribute('data-bs-target');
            activarPestana(targetId);
        });
    });

    // --- RECUPERAR ESTADO AL CARGAR ---
    const savedTab = localStorage.getItem(STORAGE_KEY);

    if (savedTab) {
        activarPestana(savedTab);
    } else {
        // Si no hay nada guardado, activamos la primera por defecto (Eliminación)
        activarPestana('#eliminacion');
    }

});