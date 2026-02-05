# MetaMapa — UI (Web)

UI web del proyecto **MetaMapa**, plataforma colaborativa para la gestión y visualización de hechos geolocalizados (TPA DDSI — UTN FRBA).

## Qué incluye
Entrega orientada a **diseño y maquetado** de interfaz aplicando principios de **UI/UX**, accesibilidad y diseño responsive usando **HTML5**, **CSS3** y un poco de **JavaScript**.

## Pantallas y flujos principales

### Acceso público
- Landing page con propósito del sistema y accesos a visualización/registro.
- Visualización anónima de colecciones y hechos.
- Enlaces a información legal y privacidad.

### Visualizador (anónimo o registrado)
- Listado de colecciones disponibles.
- Filtros de hechos por fecha, ubicación, categoría y fuente.
- Alternancia entre modo **Curado** e **Irrestricto**.
- Visualización detallada de un hecho (mapa, multimedia y fuente).
- Solicitud de eliminación de hechos (mín. 500 caracteres).

### Contribuyente
- Subida de nuevos hechos (título, descripción, categoría, ubicación, fecha y multimedia).
- Edición de hechos propios dentro de los 7 días posteriores a su creación.
- Generación de solicitudes de eliminación de hechos existentes.

### Administrador
- Panel de control con resumen de actividad (hechos, fuentes, solicitudes).
- CRUD de colecciones y fuentes (estáticas, dinámicas y proxy).
- Configuración del algoritmo de consenso por colección.
- Aprobación, rechazo o modificación de hechos.
- Importación masiva de hechos desde archivos CSV (10.000+ entradas).

## Arquitectura de navegación
La interfaz se diseñó con una jerarquía de hasta **3 niveles** de profundidad, priorizando consistencia visual, usabilidad y accesibilidad.

## Tecnologías
- HTML5
- CSS3 (responsive)
- JavaScript

## Ejecutar en local

### Opción 1: abrir el sitio
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/totopedroo/metamapa-tpa-ui.git
   cd metamapa-tpa-ui
2. Abrir index.html en el navegador.

### Opción 2 (recomendada): servidor local simple
Esto evita problemas con rutas relativas o carga de recursos.

Con Python:
1. python -m http.server 8000
2. Abrir http://localhost:8000

## Relación con el backend
Este repositorio contiene la UI/maquetado. El backend (API + lógica + persistencia) se encuentra en: https://github.com/totopedroo/metamapa-tpa

## Créditos
Trabajo Práctico Anual — Diseño y Desarrollo de Sistemas de Información (DDSI) — UTN FRBA — 2025.
