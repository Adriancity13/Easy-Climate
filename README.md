# ☀️ El Tiempo - Android Weather App & Native Widget

Una aplicación meteorológica moderna, fluida y elegante para Android, desarrollada íntegramente en **Kotlin** y **Jetpack Compose (Material 3)**. Ofrece pronósticos precisos en tiempo real, fondos atmosféricos animados reactivos, métricas climáticas avanzadas y un **widget nativo 2x2** optimizado para Android y Nothing OS con sincronización automática en segundo plano mediante **WorkManager**.

---

## 📱 Captura de Pantalla y Características Principales

### 🌟 1. Experiencia Visual y Animaciones
* **Fondos Atmosféricos Dinámicos:** Partículas y degradados que cambian en tiempo real según las condiciones meteorológicas (soleado, despejado nocturno, nublado, lluvia, tormenta eléctrica, nieve, niebla).
* **Diseño Glassmorphism:** Componentes translúcidos con desenfoque de fondo sutil y bordes elegantes de alta legibilidad.
* **Soporte de Pantallas de Alta Tasa de Refresco (90Hz / 120Hz):** Configuración nativa para una navegación ultrasuave.

### 📊 2. Datos Meteorológicos Completos
* **Tiempo Actual:** Temperatura, sensación térmica, estado del cielo con icono animado, humedad, velocidad y ráfagas de viento, dirección, índice UV, visibilidad y presión atmosférica.
* **Pronóstico Horario (Carrusel 24h):** Gráfico y tarjetas interactivas con la evolución hora a hora de temperatura, probabilidad de precipitación e iconos de estado.
* **Pronóstico Extendido a 7 Días:** Acordeón interactivo con desglose de temperaturas máximas y mínimas, horas de amanecer y atardecer, y probabilidad de lluvia.
* **Recomendaciones Inteligentes:** Sugerencias automáticas de vestimenta y precauciones (paraguas, ropa de abrigo, protección solar) calculadas según las condiciones presentes.

### 📍 3. Geolocalización Micro-local y Búsqueda por Barrios
* **Búsqueda Micro-local de Barrios y Distritos:** Barra de búsqueda inteligente con soporte para nombres de barrios, distritos y zonas locales (ej. *Delicias*, *Casa de Campo*, *Malasaña*, *Triana*, *Gràcia*, etc.), además de ciudades y municipios.
* **Autocompletado y Formato Claro:** Sugerencias con distintivo `[Barrio]` indicando explícitamente el barrio y la ciudad a la que pertenece (ejemplo: `Delicias · Madrid, España`).
* **Geocodificación con OpenStreetMap (Nominatim):** Extracción granular de jerarquías territoriales (`neighbourhood`, `quarter`, `suburb`, `city_district`, `borough`) con respaldo continuo de Open-Meteo.
* **Coordenadas GPS de Alta Precisión:** Al seleccionar un barrio, se consulta el pronóstico micro-local con sus coordenadas exactas, reflejando el nombre en formato `Barrio, Ciudad` tanto en la cabecera como en el widget.

### 🧩 4. Widget Nativo 2x2 (Android & Nothing OS)
* **Diseño Minimalista y Equilibrado:** Cuadrícula simétrica 2x2 resistente a temas dinámicos del sistema para conservar su estética de cristal ahumado oscuro.
* **Información en Pantalla de Inicio:**
  * Nombre explícito de barrio y ciudad (`Delicias, Madrid` o `Casa de Campo, Madrid`) y temperaturas máxima / mínima del día (`↑28° ↓15°`).
  * Temperatura actual destacada con tipografía de alto contraste.
  * Icono de condición y descripción del tiempo.
  * Micro-cápsula con **sensación térmica**, **probabilidad de lluvia** y **velocidad del viento**.
* **Acceso Directo:** Tocar el widget abre inmediatamente la aplicación en pantalla completa.

### ⚙️ 5. Actualización Automática y Seguimiento en Segundo Plano
* **Actualización Dinámica al Moverte (Umbral de 300 Metros):** Mediante `LocationTrackingManager` y `FusedLocationProviderClient`, la aplicación detecta desplazamientos significativos entre barrios (umbral de 300 a 500 metros) aun con la aplicación cerrada.
* **Refresco Inmediato del Widget:** Al desplazarte de una zona a otra, se encola de inmediato una tarea de WorkManager (`WeatherUpdateWorker`) que consulta las nuevas coordenadas y actualiza el widget en tiempo real sin esperar al ciclo periódico de 1 hora.
* **Actualización por Cambio Climático:** Forzado automático de actualización de datos si se detecta un cambio notable en el estado meteorológico (despejado a lluvia/tormenta) o una variación térmica brusca (≥ 3 °C).
* **Independiente del Ciclo de Vida y Persistencia:** `BootCompletedReceiver` reactiva las actualizaciones periódicas y el rastreo de desplazamientos tras reiniciar el dispositivo (`RECEIVE_BOOT_COMPLETED`).

---

## 🏗️ Arquitectura del Proyecto

El proyecto sigue los principios de **Clean Architecture** y el patrón de diseño **MVVM (Model-View-ViewModel)**:

```
app/src/main/java/com/example/
├── MainActivity.kt                  # Punto de entrada de la actividad principal
├── data/
│   ├── api/
│   │   ├── ApiClient.kt             # Configuración de clientes HTTP (Ktor/OkHttp)
│   │   └── WeatherApiService.kt     # Definición de endpoints de APIs meteorológicas
│   ├── models/
│   │   └── WeatherModels.kt         # Modelos de datos y DTOs serializables
│   └── repository/
│       └── WeatherRepository.kt     # Orquestación de datos climáticos y geocodificación
├── ui/
│   ├── components/
│   │   ├── AtmosphericWeatherBackground.kt # Canvas con partículas dinámicas y gradientes
│   │   ├── DailyAccordion.kt        # Acordeón del pronóstico a 7 días
│   │   ├── GlassComponents.kt       # Tarjetas y contenedores con efecto Glassmorphism
│   │   ├── HourlyCarousel.kt        # Carrusel horario 24 horas
│   │   └── WeatherIcons.kt          # Iconos vectoriales temáticos
│   ├── screens/
│   │   └── WeatherScreen.kt         # Pantalla principal en Jetpack Compose
│   ├── theme/
│   │   ├── Color.kt                 # Paleta de colores M3
│   │   ├── Theme.kt                 # Tema principal de la aplicación
│   │   └── Type.kt                  # Tipografía
│   └── viewmodel/
│       └── WeatherViewModel.kt      # Gestión de estado (UIState) y flujos reactivos
├── utils/
│   └── WeatherUtils.kt              # Mapeo de códigos WMO, formatos de fecha y recomendaciones
└── widget/
    ├── BootCompletedReceiver.kt     # Receptor de reinicio del sistema
    ├── WeatherWidgetProvider.kt     # AppWidgetProvider del widget 2x2
    └── worker/
        ├── WeatherUpdateWorker.kt   # CoroutineWorker para consultas en segundo plano
        └── WeatherWorkScheduler.kt  # Planificador periódico de WorkManager
```

---

## 🌐 APIs y Servicios Utilizados

1. **[Open-Meteo Weather API](https://open-meteo.com/):**
   * Previsión horaria y diaria detallada (temperatura, código WMO, viento, lluvia, sensación térmica, UV, humedad).
   * Totalmente gratuita y de código abierto (no requiere claves de API privadas).
2. **[Open-Meteo Geocoding API](https://geocoding-api.open-meteo.com/):**
   * Búsqueda por nombre de ciudades y coordenadas mundiales.
3. **[BigDataCloud Reverse Geocoding API](https://www.bigdatacloud.com/):**
   * Traducción de latitud/longitud GPS a nombres de localidades legibles.

---

## 🛠️ Stack Tecnológico y Dependencias

| Tecnología | Propósito |
| :--- | :--- |
| **Kotlin** | Lenguaje de desarrollo principal |
| **Jetpack Compose** | Framework declarativo de interfaz de usuario |
| **Material Design 3** | Sistema de diseño de última generación |
| **AndroidX WorkManager** | Planificación de trabajos periódicos en segundo plano |
| **Coroutines & StateFlow** | Concurrencia reactiva y manejo de estado asíncrono |
| **Google Play Services Location** | Detección de ubicación por GPS de bajo consumo |
| **Ktor / OkHttp / Gson** | Consumo y parseo de servicios REST |

---

## 📋 Permisos Requeridos

En `AndroidManifest.xml` se declaran los siguientes permisos:

* `android.permission.INTERNET`: Necesario para descargar los datos del clima y geocodificación.
* `android.permission.ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`: Detección precisa de la ubicación actual del usuario en primer plano.
* `android.permission.ACCESS_BACKGROUND_LOCATION`: Seguimiento de desplazamientos de 300 a 500 metros entre barrios con la aplicación cerrada para actualizar el widget al instante.
* `android.permission.RECEIVE_BOOT_COMPLETED`: Reactivación de la sincronización del widget y del seguimiento de ubicación tras reiniciar el teléfono.

---

## 🚀 Cómo Compilar y Ejecutar

### Prerrequisitos
* **Android Studio** (versión Ladybug / Jellyfish o superior).
* **JDK 17** o superior.
* **Android SDK** con `compileSdk 35` y `minSdk 26`.

### Pasos para compilar:
1. Clona o descarga el repositorio del proyecto.
2. Abre la carpeta raíz en Android Studio.
3. Espera a que Gradle sincronice las dependencias del archivo `libs.versions.toml`.
4. Ejecuta el proyecto en un emulador o dispositivo físico con el botón **Run** (`Shift + F10`).

### Ejecución de tareas mediante Gradle:
```bash
# Compilar el APK en modo depuración
gradle assembleDebug

# Ejecutar las pruebas unitarias locales
gradle :app:testDebugUnitTest
```

---

## 📌 Cómo Usar el Widget en la Pantalla de Inicio

1. Ve a la pantalla de inicio de tu dispositivo Android (o Nothing OS).
2. Mantén presionado un espacio vacío y selecciona **Widgets**.
3. Busca **El Tiempo** en la lista de aplicaciones.
4. Arrastra el widget **El Tiempo (2x2)** a la pantalla de inicio.
5. El widget se sincronizará automáticamente y se actualizará cada hora en segundo plano.

---

## 📄 Licencia

Este proyecto está distribuido bajo la licencia libre para fines educativos, demostrativos y de uso personal.
