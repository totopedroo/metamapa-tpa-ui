document.addEventListener('DOMContentLoaded', () => {
    // -------------------------------------------------------------------------
    // 1. Lógica del Mapa (si existe)
    // -------------------------------------------------------------------------
    const mapElement = document.getElementById('map');
    if (mapElement) {
        // Inicialización del mapa (manteniendo la lógica existente)
        const hechosJson = mapElement.getAttribute('data-hechos-json');
        let hechos = [];

        try {
            if (hechosJson && hechosJson !== '[]') {
                hechos = JSON.parse(hechosJson);
            }
        } catch (e) {
            console.error("Error al parsear hechos JSON:", e);
        }

        // Configuración inicial de Leaflet (ejemplo básico)
        const map = L.map('map').setView([-34.6037, -58.3816], 13); // Buenos Aires

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);

        // Añadir marcadores de hechos
        hechos.forEach(hecho => {
            if (hecho.latitud && hecho.longitud) {
                L.marker([hecho.latitud, hecho.longitud])
                    .addTo(map)
                    .bindPopup(`<b>${hecho.titulo}</b><br>${hecho.descripcion || ''}`);
            }
        });
    }

    // -------------------------------------------------------------------------
    // 2. Lógica del Modal "Crear Hecho"

    // -------------------------------------------------------------------------
    const modal = document.getElementById('modal-crear-hecho');
    const btnAbrir = document.getElementById('btn-abrir-modal-crear');
    const btnCerrar = document.getElementById('btn-cerrar-modal');
    const btnCancelar = document.getElementById('btn-cancelar-crear');
    const formCrear = document.getElementById('form-crear-hecho');
    const modalMessage = document.getElementById('modal-message');

    // Función para abrir el modal
    const openModal = () => {
        modal.classList.remove('hidden');
        modalMessage.classList.add('hidden'); // Ocultar mensajes previos
        formCrear.reset(); // Limpiar formulario
    };

    // Función para cerrar el modal
    const closeModal = () => {
        modal.classList.add('hidden');
    };

    // Event listeners para abrir y cerrar
    if (btnAbrir) btnAbrir.addEventListener('click', openModal);
    if (btnCerrar) btnCerrar.addEventListener('click', closeModal);
    if (btnCancelar) btnCancelar.addEventListener('click', closeModal);

    // Cerrar modal al hacer click fuera
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });

    // -------------------------------------------------------------------------
    // 3. Envío del Formulario (POST /crear al Backend)
    // -------------------------------------------------------------------------

    // La base URL de la API debe ser inyectada por Thymeleaf o definida aquí
    // Asumimos que la API está en el puerto 8080 según tu configuración.
    const API_BASE_URL = 'http://localhost:8080';

    const showMessage = (message, isError = false) => {
        modalMessage.textContent = message;
        modalMessage.classList.remove('hidden', 'bg-red-100', 'text-red-700', 'bg-green-100', 'text-green-700');
        if (isError) {
            modalMessage.classList.add('bg-red-100', 'text-red-700');
        } else {
            modalMessage.classList.add('bg-green-100', 'text-green-700');
        }
    };

    formCrear.addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(formCrear);
        const hechoData = {};

        // Convertir FormData a objeto JSON
        for (let [key, value] of formData.entries()) {
            if (key === 'latitud' || key === 'longitud') {
                hechoData[key] = parseFloat(value);
            } else if (key === 'fechaAcontecimiento') {
                // El backend espera la fecha en formato yyyy-MM-dd
                hechoData[key] = value;
            } else {
                hechoData[key] = value;
            }
        }

        const btnGuardar = document.getElementById('btn-guardar-crear');
        btnGuardar.disabled = true;
        btnGuardar.textContent = 'Guardando...';
        showMessage('Guardando hecho...', false);


        try {
            const response = await fetch(`http://localhost:8080/fuente-dinamica/hechos/crear`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(hechoData),
            });

            if (!response.ok) {
                // Intenta leer el cuerpo del error si es posible
                let errorText = await response.text();
                try {
                    const errorJson = JSON.parse(errorText);
                    errorText = errorJson.message || errorText;
                } catch (e) {
                    // Si no es JSON, usa el texto crudo
                }

                throw new Error(`Error ${response.status}: ${errorText || 'Error desconocido'}`);
            }

            // Si es exitoso
            showMessage('✅ Hecho creado exitosamente.', false);

            // Esperar un momento antes de cerrar y recargar
            setTimeout(() => {
                closeModal();
                // Recargar la página para ver el nuevo hecho y actualizar el mapa
                window.location.reload();
            }, 1000);

        } catch (error) {
            console.error('Error al crear hecho:', error);
            showMessage(`❌ Error al guardar: ${error.message}`, true);
        } finally {
            btnGuardar.disabled = false;
            btnGuardar.textContent = 'Guardar Hecho';
        }
    });

});