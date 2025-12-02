document.addEventListener('DOMContentLoaded', function() {

    /* =========================================================
     * 1. LÓGICA DE IMPORTACIÓN CSV
     * ========================================================= */
    const btnImportarCSV = document.getElementById('btnImportarCSV');

    if (btnImportarCSV) {
        btnImportarCSV.addEventListener('click', function() {
            // Referencias al DOM
            const fileInput = document.getElementById('inputCSV');
            const progressBar = document.getElementById('csvProgressBar');
            const progressContainer = document.getElementById('csvProgressContainer');
            const statusMsg = document.getElementById('csvStatusMessage');
            const form = document.getElementById('formCSV');

            // Validación
            if (fileInput.files.length === 0) {
                fileInput.classList.add('is-invalid');
                return;
            } else {
                fileInput.classList.remove('is-invalid');
            }

            // Preparar datos
            const file = fileInput.files[0];
            const formData = new FormData();
            formData.append('file', file);

            // UI Carga
            progressContainer.classList.remove('d-none');
            progressBar.style.width = '30%';
            progressBar.innerText = 'Subiendo...';
            statusMsg.className = 'mt-2 text-center text-muted';
            statusMsg.innerText = 'Procesando archivo...';
            this.disabled = true;

            // Fetch
            fetch('/admin/importar-csv-upload', {
                method: 'POST',
                body: formData
            })
                .then(async response => {
                    progressBar.style.width = '100%';
                    progressBar.innerText = '100%';

                    // Intentar leer texto plano si el back devuelve string
                    const texto = await response.text();

                    if (response.ok) {
                        statusMsg.className = 'mt-2 text-center text-success fw-bold';
                        statusMsg.innerText = '¡Importación exitosa! ' + texto;

                        setTimeout(() => {
                            const modalEl = document.getElementById('modalCSV');
                            const modal = bootstrap.Modal.getInstance(modalEl);
                            modal.hide();

                            form.reset();
                            progressContainer.classList.add('d-none');
                            statusMsg.innerText = '';
                            this.disabled = false;
                            progressBar.style.width = '0%';

                            // Opcional: Recargar para ver los nuevos hechos
                            window.location.reload();
                        }, 2000);

                    } else {
                        throw new Error(texto || 'Error desconocido');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    progressBar.classList.add('bg-danger');
                    statusMsg.className = 'mt-2 text-center text-danger fw-bold';
                    statusMsg.innerText = 'Error: ' + error.message;
                    this.disabled = false;
                });
        });
    }

    /* =========================================================
     * 2. LÓGICA DE PESTAÑAS (Persistencia Manual)
     * ========================================================= */

    // Solo ejecutamos si existen las pestañas en esta página
    const tabsContainer = document.getElementById('solicitudesTabs');

    if (tabsContainer) {
        const STORAGE_KEY = 'activeTabSolicitudesAdmin'; // Clave única para Admin
        const tabButtons = tabsContainer.querySelectorAll('button[data-bs-toggle="tab"]');
        const tabPanes = document.querySelectorAll('.tab-pane');

        // Función para forzar el cambio visual
        function activarPestana(targetId) {
            // A. Apagar todo
            tabButtons.forEach(btn => {
                btn.classList.remove('active');
                btn.setAttribute('aria-selected', 'false');
            });
            tabPanes.forEach(pane => {
                pane.classList.remove('show', 'active');
            });

            // B. Prender el seleccionado
            const selectedBtn = document.querySelector(`button[data-bs-target="${targetId}"]`);
            const selectedPane = document.querySelector(targetId);

            if (selectedBtn && selectedPane) {
                selectedBtn.classList.add('active');
                selectedBtn.setAttribute('aria-selected', 'true');
                selectedPane.classList.add('show', 'active');
            }

            // C. Guardar en memoria
            localStorage.setItem(STORAGE_KEY, targetId);
        }

        // Event Listeners (Click manual)
        tabButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const targetId = this.getAttribute('data-bs-target');
                activarPestana(targetId);
            });
        });

        // Recuperar estado al cargar
        const savedTab = localStorage.getItem(STORAGE_KEY);
        if (savedTab) {
            activarPestana(savedTab);
        } else {
            // Default: Eliminación
            activarPestana('#eliminacion');
        }
    }
});