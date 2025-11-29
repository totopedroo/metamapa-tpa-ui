document.addEventListener('DOMContentLoaded', function() {
    const btnImportarCSV = document.getElementById('btnImportarCSV');

    if (btnImportarCSV) {
        btnImportarCSV.addEventListener('click', function() {
            console.log(">>> ¡Click detectado en el botón!");
            // 1. Referencias al DOM
            const fileInput = document.getElementById('inputCSV');
            const progressBar = document.getElementById('csvProgressBar');
            const progressContainer = document.getElementById('csvProgressContainer');
            const statusMsg = document.getElementById('csvStatusMessage');
            const form = document.getElementById('formCSV'); // Referencia al formulario

            // 2. Validación
            if (fileInput.files.length === 0) {
                fileInput.classList.add('is-invalid');
                return;
            } else {
                fileInput.classList.remove('is-invalid');
            }

            // 3. Preparar datos
            const file = fileInput.files[0];
            const formData = new FormData();
            formData.append('file', file); // 'file' debe coincidir con el Controller del Front

            // 4. UI: Mostrar carga
            progressContainer.classList.remove('d-none');
            progressBar.style.width = '30%';
            progressBar.innerText = 'Subiendo...';
            statusMsg.className = 'mt-2 text-center text-muted'; // Limpiar colores anteriores
            statusMsg.innerText = 'Procesando archivo...';
            this.disabled = true; // Deshabilitar botón

            // 5. FETCH al Controller del FRONTEND
            // Nota: No necesitamos headers de auth aquí, la cookie de sesión hace el trabajo.
            fetch('/admin/importar-csv-upload', {
                method: 'POST',
                body: formData,
                credentials: "include"
            })
                .then(async response => {
                    progressBar.style.width = '100%';
                    progressBar.innerText = '100%';

                    const textoRespuesta = await response.text(); // Leemos mensaje del servidor

                    if (response.ok) {
                        // ÉXITO
                        statusMsg.className = 'mt-2 text-center text-success fw-bold';
                        statusMsg.innerText = '¡Importación exitosa!';

                        setTimeout(() => {
                            // Cerrar modal
                            const modalEl = document.getElementById('modalCSV');
                            const modal = bootstrap.Modal.getInstance(modalEl);
                            modal.hide();

                            // Resetear formulario
                            form.reset();
                            progressContainer.classList.add('d-none');
                            statusMsg.innerText = '';
                            this.disabled = false;
                            progressBar.style.width = '0%';

                            // Opcional: Recargar página para ver nuevos hechos
                            // window.location.reload();
                        }, 2000);

                    } else {
                        // ERROR DEL SERVIDOR (400, 500, etc)
                        throw new Error(textoRespuesta || 'Error desconocido del servidor');
                    }
                })
                .catch(error => {
                    // ERROR DE RED O LÓGICA
                    console.error('Error:', error);
                    progressBar.className = 'progress-bar bg-danger'; // Barra roja
                    statusMsg.className = 'mt-2 text-center text-danger fw-bold';
                    statusMsg.innerText = 'Error: ' + error.message;
                    this.disabled = false;
                });
        });
    }

    /* =========================================================
 * LÓGICA PARA MANTENER LA PESTAÑA ACTIVA (LocalStorage)
 * ========================================================= */
    document.addEventListener('DOMContentLoaded', function() {

        const STORAGE_KEY = 'activeTabSolicitudes';

        // --- 1. RECUPERAR ESTADO ---
        const activeTabTarget = localStorage.getItem(STORAGE_KEY);

        if (activeTabTarget) {
            // A. Buscamos el botón y el contenido correspondientes
            const tabButton = document.querySelector(`button[data-bs-target="${activeTabTarget}"]`);
            const tabContent = document.querySelector(activeTabTarget); // ej: #modificacion

            if (tabButton && tabContent) {
                // B. LIMPIEZA: Quitamos 'active' y 'show' de TODAS las pestañas y contenidos
                document.querySelectorAll('.nav-link').forEach(btn => btn.classList.remove('active'));
                document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('show', 'active'));

                // C. ACTIVACIÓN MANUAL: Forzamos las clases en el elemento guardado
                tabButton.classList.add('active');
                tabButton.setAttribute('aria-selected', 'true');

                tabContent.classList.add('show', 'active');

                console.log(">>> Pestaña restaurada:", activeTabTarget);
            }
        }

        // --- 2. GUARDAR ESTADO AL CAMBIAR ---
        const tabButtons = document.querySelectorAll('button[data-bs-toggle="tab"]');
        tabButtons.forEach(btn => {
            btn.addEventListener('shown.bs.tab', function (event) {
                const target = event.target.getAttribute('data-bs-target');
                localStorage.setItem(STORAGE_KEY, target);
                console.log(">>> Pestaña guardada:", target);
            });
        });

    });
});