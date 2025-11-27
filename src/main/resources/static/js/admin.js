// Esperamos a que el DOM cargue completamente
document.addEventListener('DOMContentLoaded', function() {

    // Referencias a los elementos del DOM
    const btnImportarCSV = document.getElementById('btnImportarCSV');

    // Verificamos si el botón existe antes de agregar el evento (para evitar errores en otras pantallas)
    if (btnImportarCSV) {

        btnImportarCSV.addEventListener('click', function() {
            const fileInput = document.getElementById('inputCSV');
            const progressBar = document.getElementById('csvProgressBar');
            const progressContainer = document.getElementById('csvProgressContainer');
            const statusMsg = document.getElementById('csvStatusMessage');

            if (fileInput.files.length === 0) {
                fileInput.classList.add('is-invalid');
                return;
            } else {
                fileInput.classList.remove('is-invalid');
            }

            const file = fileInput.files[0];
            const formData = new FormData();
            formData.append('file', file);

            // UI: Mostrar barra de progreso
            progressContainer.classList.remove('d-none');
            progressBar.style.width = '20%';
            progressBar.innerText = 'Cargando...';
            statusMsg.innerText = '';
            this.disabled = true;
            fetch('/admin/importar-csv-upload', {
                method: 'POST',
                body: formData,
                credentials: "include"
            })
                .then(response => {
                    progressBar.style.width = '100%';
                    progressBar.innerText = '100%';

                    if (response.ok) {
                        statusMsg.className = 'mt-2 text-center text-success';
                        statusMsg.innerText = 'Importación completada con éxito.';
                        setTimeout(() => {
                            const modalEl = document.getElementById('modalCSV');
                            // Usamos la API de Bootstrap para cerrar el modal
                            const modal = bootstrap.Modal.getInstance(modalEl);
                            modal.hide();
                            document.getElementById('formCSV').reset();
                            progressContainer.classList.add('d-none');
                            statusMsg.innerText = '';
                            this.disabled = false;
                        }, 1500);
                    } else {
                        throw new Error('Error en la respuesta del servidor');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    statusMsg.className = 'mt-2 text-center text-danger';
                    statusMsg.innerText = 'Error al importar el archivo.';
                    this.disabled = false;
                });
        });
    }
});