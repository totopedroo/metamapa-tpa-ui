#  MetaMapa - UI & Maquetado Web

##  Descripción general

**MetaMapa** es una plataforma colaborativa de gestión y visualización de hechos geolocalizados, desarrollada como parte del Trabajo Práctico Anual de **Diseño y Desarrollo de Sistemas de Información (DDSI) - UTN FRBA**.

Esta entrega corresponde al **diseño y maquetado de la interfaz de usuario (UI)**, con el objetivo de aplicar principios de usabilidad, accesibilidad y diseño responsive utilizando **HTML5 y CSS3**.

---

##  Objetivos de la entrega

- Incorporar nociones de **diseño UI/UX** aplicadas al sistema MetaMapa.  
- Implementar el **maquetado web** de las interfaces principales en **HTML5**.  
- Aplicar **estilos y principios de diseño responsivo** mediante **CSS3**.  
- Representar las **interacciones, jerarquías y flujos de navegación** definidos en el diseño de interfaz.  

---

##  Alcance funcional

Esta entrega incluye el diseño y maquetado de las siguientes interfaces:

###  **Landing Page (Acceso público)**
- Presentación del propósito y objetivos de MetaMapa.  
- Ejemplos destacados de colecciones y hechos.  
- Enlaces de acceso a:
  - Visualización anónima de hechos.
  - Registro e inicio de sesión (colaboradores y administradores).
  - Información legal y de privacidad.

---

### 🔍 **Visualizador (Usuario anónimo o registrado)**
- Listado de colecciones disponibles.  
- Filtros de hechos por **fecha, ubicación, categoría y fuente**.  
- Alternancia entre **modo curado** e **irrestricto**.  
- Visualización detallada de un hecho (mapa interactivo, multimedia y fuente).  
- Solicitud de eliminación de hechos (mínimo 500 caracteres).  

---

###  **Contribuyente**
- Subida de nuevos hechos (título, descripción, categoría, ubicación, fecha y multimedia).  
- Edición de hechos propios dentro de los 7 días posteriores a su creación.  
- Generación de solicitudes de eliminación de hechos existentes.  

---

###  **Administrador**
- Panel de control con resumen de actividad (hechos, fuentes, solicitudes).  
- CRUD de colecciones y fuentes (estáticas, dinámicas y proxy).  
- Configuración del algoritmo de consenso por colección.  
- Aprobación, rechazo o modificación de hechos.  
- Importación de hechos desde archivos **CSV (10.000+ entradas)**.

---

##  Arquitectura de navegación

La interfaz se estructura bajo una jerarquía de **máximo tres niveles de profundidad**, siguiendo principios de usabilidad y consistencia visual:
