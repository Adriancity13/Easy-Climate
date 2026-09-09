# 🌤️ Easy-Climate: Guía Técnica de Ingeniería Bioclimática, IA Local-First y Arquitectura Android Jetpack

[![Licencia: MIT](https://img.shields.io/badge/Licencia-MIT%20Open%20Source-green.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Android Jetpack](https://img.shields.io/badge/Android%20Jetpack-Compose%20M3-3DDC84.svg?style=for-the-badge&logo=android)](https://developer.android.com/jetpack)
[![Open-Meteo](https://img.shields.io/badge/API-Open--Meteo%20v1-orange.svg?style=for-the-badge)](https://open-meteo.com/)
[![Groq Cloud](https://img.shields.io/badge/LLM-Groq%20Llama%203.3%2070B-F55036.svg?style=for-the-badge)](https://groq.com/)
[![Jetpack Glance](https://img.shields.io/badge/Widget-Glance%20%2F%20RemoteViews-4285F4.svg?style=for-the-badge)](https://developer.android.com/jetpack/compose/glance)
[![Nothing OS Ready](https://img.shields.io/badge/Nothing%20OS-Monochrome%20Ready-000000.svg?style=for-the-badge)](https://nothing.tech)

---

## 📑 Tabla de Contenidos

1. [Visión General y Fundamento Fisiológico](#1-visión-general-y-fundamento-fisiológico)
2. [Arquitectura del Sistema (Hybrid Local-First + LLM)](#2-arquitectura-del-sistema-hybrid-local-first--llm)
   - [Diagrama de Flujo de Datos](#diagrama-de-flujo-de-datos)
   - [Gestión de Estado Unidireccional (UDF)](#gestión-de-estado-unidireccional-udf)
   - [Caché de Doble Barrera en RAM (`Double-Barrier Cache`)](#caché-de-doble-barrera-en-ram-double-barrier-cache)
   - [Estrategia Offgrid / Zero-Latency](#estrategia-offgrid--zero-latency)
   - [Geocodificación Multinivel y Resolución de Barrios](#geocodificación-multinivel-y-resolución-de-barrios)
3. [Desglose Matemático y Biomecánico del Algoritmo Local (`BioclimaticClothingEngine.kt`)](#3-desglose-matemático-y-biomecánico-del-algoritmo-local-bioclimaticclothingenginekt)
   - [A. Ecuación de Magnus-Tetens y Punto de Rocío ($T_d$)](#a-ecuación-de-magnus-tetens-y-punto-de-rocío-t_d)
   - [B. Índice Humidex y Tasa de Evaporación Cutánea](#b-índice-humidex-y-tasa-de-evaporación-cutánea)
   - [C. Aceleración por Efecto Venturi en Cañones Urbanos](#c-aceleración-por-efecto-venturi-en-cañones-urbanos)
   - [D. Delta de Wind Chill Pectoral y Prevención de Broncoconstricción](#d-delta-de-wind-chill-pectoral-y-prevención-de-broncoconstricción)
   - [E. Factor de Protección de Mucosa Respiratoria](#e-factor-de-protección-de-mucosa-respiratoria)
   - [F. Irradiancia Solar Directa vs. Sombra ($W/m^2$)](#f-irradiancia-solar-directa-vs-sombra-wm2)
   - [G. Alerta de Caída Térmica Vespertina (*Sunset Drop*) y Sudor Frío](#g-alerta-de-caída-térmica-vespertina-sunset-drop-y-sudor-frío)
   - [H. Matriz Estandarizada de Aislamiento Térmico CLO](#h-matriz-estandarizada-de-aislamiento-térmico-clo)
   - [I. Sistema Modular de 3 Capas (Método Cebolla)](#i-sistema-modular-de-3-capas-método-cebolla)
   - [J. Motor Dinámico de Selección de Calzado](#j-motor-dinámico-de-selección-de-calzado)
   - [K. Reglas Estrictas de Selección y Veto de Materiales Textiles](#k-reglas-estrictas-de-selección-y-veto-de-materiales-textiles)
4. [Integración con Groq Cloud LLM (`GroqBioclimaticAdvisor.kt`)](#4-integración-con-groq-cloud-llm-groqbioclimaticadvisorkt)
   - [Modelo e Inferencia de Ultrabaja Latencia](#modelo-e-inferencia-de-ultrabaja-latencia)
   - [System Prompt Bioclimático y Directiva Causal](#system-prompt-bioclimático-y-directiva-causal)
   - [Esquema de Inyección de Contexto JSON](#esquema-de-inyección-de-contexto-json)
   - [Mecanismo de Fallback Transparente](#mecanismo-de-fallback-transparente)
5. [Diseño de Interfaz Jetpack Compose M3 y Renderizado en Canvas](#5-diseño-de-interfaz-jetpack-compose-m3-y-renderizado-en-canvas)
   - [Fondo Dinámico Reactivo Procedural (`AtmosphericWeatherBackground.kt`)](#fondo-dinámico-reactivo-procedural-atmosphericweatherbackgroundkt)
   - [Arquitectura Visual Anti-Matrioska y Glassmorphism](#arquitectura-visual-anti-matrioska-y-glassmorphism)
   - [Componentes UI Clave](#componentes-ui-clave)
6. [Widget de Escritorio Nativo de 3 Filas (Jetpack Glance & RemoteViews)](#6-widget-de-escritorio-nativo-de-3-filas-jetpack-glance--remoteviews)
   - [Estructura de Información en 3 Filas Compactas](#estructura-de-información-en-3-filas-compactas)
   - [Ciclo de Vida y Actualización con WorkManager](#ciclo-de-vida-y-actualización-con-workmanager)
7. [Compatibilidad Adaptativa con Nothing OS & Material You](#7-compatibilidad-adaptativa-con-nothing-os--material-you)
8. [Guía de Compilación, Pruebas y Despliegue](#8-guía-de-compilación-pruebas-y-despliegue)
9. [Licencia de Uso 100% Gratuita y Open Source (MIT)](#9-licencia-de-uso-100-gratuita-y-open-source-mit)

---

## 📌 1. Visión General y Fundamento Fisiológico

Las aplicaciones meteorológicas convencionales se limitan a proyectar la temperatura termométrica ($^\circ\text{C}$) o un valor estático de sensación térmica (*Apparent Temperature*). No obstante, **la sensación corporal y la salud del usuario no dependen exclusivamente de los grados centígrados medidos en una garita meteorológica a 2 metros de altura en un descampado**, sino del balance térmico e hidrodinámico entre el organismo y el microclima inmediato:

$$\text{Balance Térmico Humano } (S) = M - W - (R + C + E) - (R_{\text{res}} + C_{\text{res}})$$

Donde:
- $M$: Tasa metabólica de producción de calor.
- $W$: Trabajo mecánico realizado.
- $R, C$: Pérdidas o ganancias por radiación electromagnética y convección del aire.
- $E$: Pérdida de calor por evaporación de sudor en la superficie dérmica.
- $R_{\text{res}}, C_{\text{res}}$: Pérdidas por calor latente y sensible en el tracto respiratorio.

**Easy-Climate** nace para cerrar esta brecha fundamental. Es un sistema integral que traduce variables físicas complejas (irradiancia solar en $W/m^2$, punto de rocío $T_d$, efecto cañón urbano por aceleración Venturi, gradientes térmicos tras el ocaso y humedad absoluta) en **estrategias textiles precisas, selección de calzado técnico, pautas de ventilación/abrigo modular en 3 capas y advertencias fisiológicas preventivas** (evitación de broncoconstricción por choque térmico/aire seco y prevención de hipotermia local por sudor frío acumulado).

---

## 🏗️ 2. Arquitectura del Sistema (Hybrid Local-First + LLM)

Easy-Climate implementa un patrón **Hybrid Local-First**. La totalidad de los cálculos matemáticos, deducciones biomecánicas, matrices de aislamiento y pronósticos para los 7 días se computan **de forma síncrona en memoria RAM local en 0 ms**, mientras que una capa secundaria de Inteligencia Artificial Generativa (*Llama 3.3 70B vía Groq Cloud*) enriquece la explicación con síntesis causal en lenguaje natural cuando hay conectividad disponible.

### Diagrama de Flujo de Datos

```mermaid
flowchart TD
    subgraph SENSORES_Y_LOCALIZACION [Capa de Localización y Entorno]
        GPS[FusedLocationProviderClient / GPS]
        GEO[Geocoding Reverse: Open-Meteo + OSM Nominatim]
        MANUAL[Búsqueda Manual con Autocompletado de Barrios]
    end

    subgraph CACHE_ENGINE [Motor de Datos y Caché en RAM]
        CACHE[Double-Barrier Cache en RAM<br>• TTL: 30 min<br>• Distancia: 2.0 km<br>• Térmica: ΔT ≥ 3°C]
        API_METEO[Open-Meteo v1 REST API<br>168 Horas + Radiación + Calidad del Aire]
    end

    subgraph BIOCLIMATIC_CORE [Motor Físico Local en RAM (BioclimaticClothingEngine.kt)]
        PHYS[Cálculo Físico Instantáneo (0 ms)<br>• Magnus-Tetens / Punto de Rocío<br>• Humidex & Evaporación Cutánea<br>• Corrección Venturi Urbana<br>• Delta Wind Chill Pectoral<br>• Protección de Mucosa Respiratoria<br>• Ganancia Solar W/m² vs Sombra<br>• Matriz de Aislamiento CLO (0.3 a 1.2+)<br>• Estrategia Modular 3 Capas<br>• Selección de Calzado y Alertas]
        LOCAL_REC[Entidad Local de Recomendación Completa]
    end

    subgraph AI_LAYER [Capa LLM de Síntesis Causal (Groq Cloud)]
        PROMPT[Inyección Contexto JSON + System Prompt Fisiológico]
        GROQ[Groq Ingestion API<br>Llama 3.3 70B Versatile (~300 tok/s)]
        AI_REC[Síntesis Causal 20-30 Palabras]
    end

    subgraph UI_OUTPUT [Presentación y Widgets]
        STATE[WeatherUIState (Jetpack Compose Flow)]
        SCREEN[WeatherScreen M3 + Canvas Atmosférico Reactivo]
        WIDGET[Widget 3 Filas Jetpack Glance / RemoteViews]
    end

    GPS --> CACHE
    GEO --> CACHE
    MANUAL --> CACHE
    CACHE -->|Hit: 0 ms| PHYS
    CACHE -->|Miss: Network Request| API_METEO
    API_METEO --> CACHE
    API_METEO --> PHYS
    PHYS --> LOCAL_REC
    LOCAL_REC -->|Conexión Activa| PROMPT
    PROMPT --> GROQ
    GROQ -->|Éxito| AI_REC
    AI_REC --> STATE
    LOCAL_REC -->|Offline / Fallback / Timeout| STATE
    LOCAL_REC -->|Zero Cost / No API Calls| WIDGET
    STATE --> SCREEN
```

### Gestión de Estado Unidireccional (UDF)

El flujo de información en la aplicación se adhiere rígidamente al patrón **Model-View-ViewModel (MVVM)** con **Unidirectional Data Flow**:

1. **`WeatherViewModel`**: Expone un `StateFlow<WeatherUIState>` inmutable consumido por la interfaz de usuario con `collectAsState()`.
2. **`WeatherUIState`**: Clase sellada (*sealed interface*) que modela de forma determinista los estados:
   - `Loading`: Pantalla de carga con retroalimentación contextual.
   - `PermissionDenied`: Estado de permisos con alternativas manuales de selección de ciudad.
   - `Error(message)`: Manejo controlado de excepciones con mecanismo de reintento.
   - `Success(...)`: Modelo enriquecido con la previsión horaria, diaria, calidad del aire, ciclo solar y la entidad `ClothingRecommendation`.

### Caché de Doble Barrera en RAM (`Double-Barrier Cache`)

Para maximizar la autonomía de batería del dispositivo móvil y evitar peticiones de red superfluas tanto en la UI activa como en ejecuciones en segundo plano (`WorkManager`), el repositorio implementa una **Caché Multicriterio en Memoria RAM**:

1. **Barrera Temporal ($TTL = 30\text{ minutos}$):** Los datos en memoria son válidos de forma predeterminada durante media hora si no se detectan alteraciones en los sensores físicos.
2. **Barrera Espacial ($\Delta\text{Distancia} \ge 2.0\text{ km}$):** Mediante el cálculo del semiverseno (*Haversine*), las coordenadas reportadas por el hardware GPS no invalidan los pronósticos a menos que el desplazamiento exceda los 2000 metros:
   $$d = 2r \arcsin\left(\sqrt{\sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)}\right)$$
3. **Barrera de Disrupción Meteorológica ($\Delta T \ge \pm 3^\circ\text{C}$ o cambio WMO):** Si el sensor o la lectura meteorológica detecta una caída térmica repentina o transición súbita de estado (ej. de seco a precipitación convectiva), la barrera temporal se rompe de inmediato para refrescar la estrategia bioclimática.

### Estrategia Offgrid / Zero-Latency

- **0 ms en Renderizado:** Al abrir la aplicación o cambiar de pestaña horaria, la respuesta visual es inmediata gracias a la ejecución en memoria RAM.
- **Previsión de 7 Días Local:** El desglose completo de los 7 días y sus 24 horas (`DailyItem`, `HourlyItem`) se computa en su totalidad mediante el motor físico `BioclimaticClothingEngine.kt` con un coste medio menor a **0.05 ms por día**.
- **Independencia de Servidores:** La aplicación funciona al 100% de sus capacidades técnicas sin conexión a internet en modo de caché previa o calculando datos directamente sin requerir servidores intermedios de terceros.

### Geocodificación Multinivel y Resolución de Barrios

El motor de búsqueda y localización implementa un sistema híbrido de resolución geográfica:
1. **Detección de Barrio/Distrito:** Prioriza el topónimo más granular (ej. *Malasaña*, *Chamberí*, *Delicias*, *Sarrià*, *Ruzafa*) mediante `Open-Meteo Geocoding API` con soporte de fallback a `OpenStreetMap Nominatim`.
2. **Identificador Jerárquico:** Muestra en interfaz `Barrio · Ciudad · País` para evitar ambigüedades en áreas metropolitanas densas donde el clima y el efecto cañón varían entre distritos.

---

## ⚙️ 3. Desglose Matemático y Biomecánico del Algoritmo Local (`BioclimaticClothingEngine.kt`)

El núcleo `BioclimaticClothingEngine.kt` procesa los datos brutos de la atmósfera y ejecuta en microsegundos una serie de ecuaciones termodinámicas y reglas biomecánicas:

```
                                  ENTRADA METEOROLÓGICA BRUTA
               [ Temperatura (T), Humedad Relativa (HR), Viento (V), Radiación (W/m²), Nubes (C%) ]
                                               │
               ┌───────────────────────────────┴───────────────────────────────┐
               ▼                                                               ▼
    [ TERMODINÁMICA CUTÁNEA ]                                      [ DINÁMICA DE CONVECCIÓN ]
   • Magnus-Tetens: E_s(T), E(T,HR)                               • Corrección Venturi: V_urbano = 1.2 · V
   • Punto de Rocío (T_d)                                         • Wind Chill Jagti: Sensación Pectoral
   • Humidex Masterson & Richardson                               • Delta Viento: ΔT_viento = T - WindChill
               │                                                               │
               └───────────────────────────────┬───────────────────────────────┘
                                               ▼
                                  [ RADIACIÓN Y GRADIENTES ]
                                 • Ganancia Solar: ΔT_solar
                                 • Gradiente Caída Ocaso (Sunset Drop)
                                 • Balance de Humedad Mucosa
                                               │
                                               ▼
                              [ MATRIZ BIOCLIMÁTICA DE DECISIÓN ]
           ┌───────────────────────────────────┼───────────────────────────────────┐
           ▼                                   ▼                                   ▼
   [ MATRIZ CLO (0.3 - 1.2+) ]      [ MÉTODO CEBOLLA 3 CAPAS ]           [ VETOS TEXTILES ]
   • Cálculo de Aislamiento         • Capa 1: Contacto Transpirable      • VETO DEL ALGODÓN
   • Nivel Térmico Asignado         • Capa 2: Aislamiento Modular        • Malla 3D / Poliéster
   • Alerta Viento/Pectoral         • Capa 3: Escudo Cortavientos        • Calzado y Membrana
```

---

### A. Ecuación de Magnus-Tetens y Punto de Rocío ($T_d$)

El punto de rocío ($T_d$) es la temperatura a la que el aire debe enfriarse a presión constante para que el vapor de agua se sature y comience la condensación. A diferencia de la humedad relativa, es la medida termodinámica absoluta de la cantidad de vapor en el ambiente y determina la tasa máxima de evaporación del sudor humano.

1. **Presión de Vapor de Saturación $E_s(T)$ en hectopascales ($\text{hPa}$):**
   $$E_s(T) = 6.112 \cdot \exp\left(\frac{17.67 \cdot T}{T + 243.5}\right)$$

2. **Presión de Vapor Real $E(T, \text{HR})$:**
   $$E(T, \text{HR}) = E_s(T) \cdot \left(\frac{\text{HR}}{100}\right)$$

3. **Cálculo del Punto de Rocío ($T_d$):**
   $$T_d = \frac{243.5 \cdot \ln\left(\frac{E(T, \text{HR})}{6.112}\right)}{17.67 - \ln\left(\frac{E(T, \text{HR})}{6.112}\right)}$$

---

### B. Índice Humidex y Tasa de Evaporación Cutánea

Para evaluar el estrés térmico por bochorno y la incapacidad fisiológica de disipar calor por sudoración, se aplica la fórmula de Masterson y Richardson:

$$\text{Humidex} = T + \frac{5}{9} \cdot (E(T, \text{HR}) - 10)$$

- **$T_d \ge 16.0^\circ\text{C}$ o $\text{Humidex} \ge 30$:** El gradiente de presión de vapor entre la piel húmeda y el aire ambiental se colapsa. El sudor no se evapora, sino que se acumula en estado líquido.
- **Respuesta Biomecánica:** Se activa la alerta de **Alta Tasa de Transpiración**, recomendando prendas de alta capilaridad y vetando totalmente las fibras hidrofílicas retenedoras de agua.

---

### C. Aceleración por Efecto Venturi en Cañones Urbanos

En entornos de ciudad (*Urban Street Canyons*), los edificios altos y las calles estrechas provocan un estrechamiento de las líneas de flujo del viento, acelerando la velocidad local en superficie según el principio de conservación de la masa y el efecto Venturi:

$$V_{\text{urbano}} = V_{\text{estación}} \cdot k_v \quad \text{donde } k_v = 1.20$$

Si la estación base mide $20\text{ km/h}$, el motor bioclimático evalúa la exposición corporal a $24\text{ km/h}$ en la acera.

---

### D. Delta de Wind Chill Pectoral y Prevención de Broncoconstricción

La pérdida convectiva forzada de calor a través de la caja torácica se calcula mediante la fórmula estandarizada de sensación por viento (Jagti / Siple-Passel actualizada):

$$\text{WindChill} = 13.12 + 0.6215 \cdot T - 11.37 \cdot (V_{\text{urbano}})^{0.16} + 0.3965 \cdot T \cdot (V_{\text{urbano}})^{0.16}$$

El impacto térmico diferencial sobre el pecho se define como:

$$\Delta T_{\text{viento}} = T - \text{WindChill}$$

- **Regla Pectoral Crítica:** Si $\Delta T_{\text{viento}} \ge 3.0^\circ\text{C}$ o $V_{\text{urbano}} \ge 18\text{ km/h}$ con $T < 18^\circ\text{C}$:
  - **Consecuencia Médica:** El enfriamiento brusco del tórax induce vasoconstricción cutánea e irritación refleja vagal de las vías aéreas, provocando **broncospasmos y crisis en personas con asma o hiperreactividad bronquial**.
  - **Acción Textil Mandatoria:** Se prescribe **cortavientos con cierre estanco y cuello alto**, prohibiendo prendas con apertura frontal sin protección pectoral.

---

### E. Factor de Protección de Mucosa Respiratoria

Cuando el aire inhalado es frío y seco, las células epiteliales de la tráquea y los bronquios transfieren calor y agua rápidamente para atemperarlo y humidificarlo al 100% antes de los alvéolos. Si la pérdida de humedad supera la tasa de reposición ciliar:

$$\text{Riesgo de Deshidratación Mucosa} \iff T \le 12^\circ\text{C} \quad \land \quad \text{HR} < 40\% \quad (\text{o ráfagas } V_{\text{urbano}} \ge 22\text{ km/h})$$

- **Acción:** Emite la recomendación de **braga técnica de cuello / tubular poroso transpirable**, creando una microcámara de recirculación de vapor al exhalar que previene la desecación del epitelio respiratorio.

---

### F. Irradiancia Solar Directa vs. Sombra ($W/m^2$)

A partir de la cobertura de nubes ($C\% \in [0, 100]$), la altitud solar y la irradiancia global horizontal, el motor calcula la ganancia térmica neta al sol frente a la sombra:

$$\text{Atenuación por Nubosidad } (f_c) = 1.0 - 0.75 \cdot \left(\frac{C}{100}\right)^{3.4}$$

$$\Delta T_{\text{solar}} = \text{SolarBoost}_{\text{max}} \cdot f_c \quad \text{donde } \Delta T_{\text{solar}} \in [0.0^\circ\text{C}, +4.5^\circ\text{C}]$$

$$T_{\text{percibida, sol}} = \text{FeelsLike} + \Delta T_{\text{solar}}$$
$$T_{\text{percibida, sombra}} = \text{FeelsLike} - 0.5^\circ\text{C}$$

- **Recomendación Dinámica:** Si $\Delta T_{\text{solar}} \ge 2.5^\circ\text{C}$, se alerta de **Oscilación Sol/Sombra Severa**, sugiriendo prendas con cremallera frontal completa (*Full-Zip*) para desabrochar al sol y cerrar de inmediato al entrar en calles en sombra.

---

### G. Alerta de Caída Térmica Vespertina (*Sunset Drop*) y Sudor Frío

El motor analiza la serie temporal horaria entre las 17:00 y las 22:00 horas, detectando la caída rápida de temperatura vinculada al ocaso:

$$\Delta T_{\text{caída}} = T_{\text{tarde}} - T_{\text{noche}} > 5.0^\circ\text{C} \quad \lor \quad (T_{\text{noche}} - T_{d,\text{noche}}) \le 2.0^\circ\text{C}$$

- **Riesgo Fisiológico:** Si el usuario camina o realiza actividad física por la tarde, sus prendas de contacto absorberán transpiración. Al caer la noche y descender la temperatura, el agua acumulada en la ropa incrementa su conductividad térmica en un factor de $\times 25$, conduciendo el calor corporal hacia el exterior a gran velocidad (*Sudor Frío* e hipotermia local).
- **Acción:** Genera la alerta preventiva **"Sunset Drop: Lleva cortavientos ligero en mochila para la vuelta tras el ocaso"**.

---

### H. Matriz Estandarizada de Aislamiento Térmico CLO

La unidad CLO define la resistencia térmica que mantiene a un sujeto en equilibrio térmico en reposo ($1\text{ CLO} = 0.155\text{ m}^2\cdot\text{K/W}$):

| Nivel Térmico | Rango Temp. Operativa | Índice CLO | Configuración Textil Prescrita |
| :--- | :--- | :--- | :--- |
| **GÉLIDO** | $< 0^\circ\text{C}$ | **$\ge 1.30\text{ CLO}$** | Capa base térmica sintética/merino + Forro polar denso + Anorak plumón/sintético con membrana cortavientos e impermeable. |
| **FRÍO INTENSO** | $0^\circ\text{C} - 7^\circ\text{C}$ | **$1.10 - 1.25\text{ CLO}$** | Capa térmica + Capa intermedia aislante + Abrigo o chaqueta técnica cortavientos con sellado de cuello. |
| **FRÍO MODERADO** | $8^\circ\text{C} - 13^\circ\text{C}$ | **$0.90 - 1.05\text{ CLO}$** | Capa base transpirable + Sudadera técnica/jersey + Cortavientos frontal abrochado. |
| **FRESCO ENTRETIEMPO** | $14^\circ\text{C} - 18^\circ\text{C}$ | **$0.70 - 0.85\text{ CLO}$** | Camiseta técnica manga corta/larga + Chaqueta ligera modular / Cortavientos desabrochable. |
| **TEMPLADO SUAVE** | $19^\circ\text{C} - 23^\circ\text{C}$ | **$0.50 - 0.65\text{ CLO}$** | Manga corta transpirable. Capa exterior opcional ultracompactable si hay viento o lluvia. |
| **CÁLIDO** | $24^\circ\text{C} - 29^\circ\text{C}$ | **$0.35 - 0.45\text{ CLO}$** | Tejido técnico microperforado ultraligero de alta capilaridad. Ropa holgada de tonos claros. |
| **CALOR INTENSO** | $\ge 30^\circ\text{C}$ | **$\le 0.30\text{ CLO}$** | Malla técnica de máxima evacuación de calor. Protección solar obligatoria (gorra con ventilación y gafas UV400). |

---

### I. Sistema Modular de 3 Capas (Método Cebolla)

Para temperaturas inferiores a $19^\circ\text{C}$ o con oscilación diaria superior a $7^\circ\text{C}$, el motor estructura la vestimenta en 3 capas desacopladas:

1. **Capa 1 (Base / Contacto):** Función exclusiva de **evacuación hidrodinámica del sudor**. Fibras hidrófobas (poliéster microperforado, polipropileno o lana merino fina). Prohibición del algodón.
2. **Capa 2 (Intermedia / Aislamiento):** Función de **retención del aire caliente generado por el metabolismo**. Estructura alveolar (forro polar, microfibra técnica, felpa sintética transpirable con cremallera frontal).
3. **Capa 3 (Exterior / Protección Mecánica y Convectiva):** Función de **barrera frente a viento, lluvia y nieve**. Membrana técnica cortavientos transpirable (tipo cortavientos ripstop o membrana hidrofóbica permeable al vapor) que impide la intrusión del aire frío exterior sin retener la humedad interna.

---

### J. Motor Dinámico de Selección de Calzado

El calzado se selecciona evaluando la interacción entre precipitación acumulada, charcos, velocidad del viento y humedad en superficie:

```kotlin
// Lógica de deducción en BioclimaticClothingEngine.kt
val footwear = when {
    isSnowing -> Footwear(
        icon = "🥾",
        title = "Botas térmicas con suela de tracción",
        reason = "Aislamiento contra el suelo helado y taqueado antideslizante."
    )
    isRainy && (rainProb >= 60 || precip >= 2.0) -> Footwear(
        icon = "👟",
        title = "Zapatillas con membrana hidrófuga / impermeables",
        reason = "Protección frente al agua estancada y salpicaduras continuas."
    )
    isWarmAndHumid -> Footwear(
        icon = "👟",
        title = "Zapatillas técnicas con empeine de malla abierta (Mesh 3D)",
        reason = "Permite la circulación de aire para disipar el sudor plantar y evitar rozaduras."
    )
    else -> Footwear(
        icon = "👟",
        title = "Calzado cómodo estándar cerrado",
        reason = "Protección térmica equilibrada para terreno urbano."
    )
}
```

---

### K. Reglas Estrictas de Selección y Veto de Materiales Textiles

| Material | Propiedad Fisiológica | Dictamen del Motor | Condición de Activación / Proscripción |
| :--- | :--- | :--- | :--- |
| **Algodón (Cotton)** | Hidrofílico por enlaces de hidrógeno. Retiene hasta un **2700% de su peso en agua**, secado ultra-lento, colapso de cámaras de aire. | ⛔ **VETADO Y PROSCRITO** | Vetado cuando $T_d \ge 16^\circ\text{C}$ o $\text{HR} > 70\%$ con actividad, o en $T < 14^\circ\text{C}$ con riesgo de sudor frío. |
| **Poliéster Microperforado** | Hidrófobo (absorbe $< 0.4\%$ de agua). Estructura capilar que transporta la humedad al exterior por acción mecánica. | ✅ **PRESCRITO (Capa 1 Base)** | Obligatorio en calor húmedo, alta transpiración o como primera capa en frío. |
| **Lana Merino (17.5 a 19.5 $\mu\text{m}$)** | Fibra queratínica natural. Regula la temperatura corporal incluso en húmedo sin sensación fría. No retiene olores. | ✅ **PRESCRITO (Capa 1/2 en Frío)** | Recomendado en rangos de $0^\circ\text{C}$ a $12^\circ\text{C}$ en actividades de intensidad variable. |
| **Poliamida / Ripstop Nylon** | Alta resistencia al desgarro, densidad de tejido estanca al paso convectivo del aire ($0\text{ CFM}$). | ✅ **PRESCRITO (Capa 3 Cortavientos)** | Mandatorio si $V_{\text{urbano}} \ge 18\text{ km/h}$ o $\Delta T_{\text{viento}} \ge 3.0^\circ\text{C}$. |

---

## 🧠 4. Integración con Groq Cloud LLM (`GroqBioclimaticAdvisor.kt`)

### Modelo e Inferencia de Ultrabaja Latencia

Para enriquecer la recomendación matemática local con una redacción natural, fluida y persuasiva, la aplicación integra el modelo **Llama 3.3 70B Versatile** alojado en la infraestructura de **Groq Cloud**. La tasa de generación supera los **300 tokens por segundo**, logrando tiempos de respuesta completos inferiores a **400 ms**.

### System Prompt Bioclimático y Directiva Causal

El modelo opera bajo una directiva de ingeniería biomecánica estricta que prohíbe divagaciones o listas genéricas:

```text
Eres el Asesor Bioclimático de Easy-Climate, un experto en fisiología térmica humana y textil técnico.
Tu misión es recibir variables meteorológicas y biomecánicas procesadas en JSON y generar una recomendación
de vestimenta de 20 a 30 palabras en español fluido y profesional.

REGLAS OBLIGATORIAS:
1. EXPLICA LA CAUSA Y LA CONSECUENCIA: Menciona la razón física (ej. punto de rocío alto, viento urbano o
   caída tras el ocaso) y la acción textil preventiva precisa (ej. poliéster técnico, cortavientos cerrado).
2. NUNCA menciones marcas comerciales ni des saludos genéricos como "¡Hola!".
3. PRIORIDAD AL PECHO Y VÍAS RESPIRATORIAS: Si deltaWindChill >= 3.0°C o viento >= 18 km/h con < 18°C,
   exige protección pectoral cerrada para prevenir broncospasmos y enfriamiento torácico.
4. PROSCRIBE EL ALGODÓN si hay riesgo de humedad o sudoración alta.
```

### Esquema de Inyección de Contexto JSON

El repositorio empaqueta las variables físicas calculadas y las inyecta en el prompt:

```json
{
  "currentTemp": 18.5,
  "apparentTemp": 17.2,
  "humidity": 82,
  "dewPoint": 15.4,
  "urbanWindSpeed": 22.8,
  "deltaWindChill": 3.6,
  "solarGain": 2.8,
  "cloLevel": "0.75 CLO",
  "isHighSweatRisk": true,
  "isMandatoryChestProtection": true,
  "isSunsetDropRisk": true
}
```

### Mecanismo de Fallback Transparente

Si el dispositivo no cuenta con conexión a internet, la API Key no está configurada o se produce un timeout en la red ($> 3000\text{ ms}$), el sistema realiza un **fallback instantáneo y silencioso a `BioclimaticClothingEngine.generateLocalRecommendation()`**, garantizando que el usuario jamás visualice una pantalla vacía o un error de conexión.

---

## 🎨 5. Diseño de Interfaz Jetpack Compose M3 y Renderizado en Canvas

### Fondo Dinámico Reactivo Procedural (`AtmosphericWeatherBackground.kt`)

La aplicación no utiliza vídeos ni GIFs estáticos que degraden el rendimiento. El fondo visual se dibuja directamente en un **`Canvas` procedural reactivo a 60/120 FPS**:

- **Gradientes Atmosféricos Reactivos:** Interpolación continua de colores basada en la temperatura, código WMO y el estado del sol (día luminoso, crepúsculo/ocaso `isSunsetPeriod`, noche profunda y tormenta).
- **Cuerpo Solar con Resplandor Pulsante:** Gradientes radiales concéntricos con modulación sinusoidal de radio y alfa (`infiniteRepeatable`).
- **Bóveda Celeste Nocturna:** Generación procedural de estrellas con centelleo aleatorio y luna en fase según la hora solar.
- **Simulación Física de Lluvia Angular:** Trazos lineales con cálculo trigonométrico de inclinación vinculado a la velocidad del viento y partículas de salpicadura en la base.
- **Bancos de Niebla y Nieve en Deriva:** Capas horizontales con desplazamiento sinusoidal continuo y copos de nieve con movimiento Browniano simulado.

### Arquitectura Visual Anti-Matrioska y Glassmorphism

Siguiendo los principios más estrictos de diseño de interfaces modernas:
1. **Contenedor Unificado (Anti-Matrioska):** Eliminación total de tarjetas anidadas con bordes amarillos/morados redundantes. Cada sección principal se estructura en un único `GlassCard` de radio amplio (`20.dp` / `24.dp`).
2. **Jerarquía por Espacio Negativo:** Separación elegante mediante padding generoso (`16.dp`), espaciado vertical (`12.dp`) y divisores lineales ultrafinos (`1.dp` con `Color.White.copy(alpha = 0.08f)`).
3. **Paleta Cromática Funcional y Accesible:**
   - **Blanco Puro (`#FFFFFF`):** Reservado para títulos, temperaturas principales y nombres de prendas clave.
   - **Blanco al 70% (`Color.White.copy(alpha = 0.70f)`):** Utilizado en textos explicativos secundarios y etiquetas de unidades.
   - **Acentos Funcionales:** Amarillo ámbar (`#FDE047`) para advertencias térmicas y azul celeste (`#93C5FD`) para métricas de lluvia e hidratación.

### Componentes UI Clave

- **`BioclimaticRecommendationCard.kt`:** Muestra la estrategia de vestimenta, el nivel térmico, el delta sol/sombra, el calzado recomendado, los 4 hitos horarios del día y el desglose de 3 capas.
- **`HourlyCarousel.kt`:** Carrusel horizontal con respuesta háptica táctil (`HapticFeedbackType.TextHandleMove`), escala activa animada y barra de progreso temporal interactiva.
- **`DailyAccordion.kt`:** Tarjeta unificada para los 7 días con acordeones expandibles que muestran el desglose horario de 24 horas y los ciclos de amanecer/atardecer.
- **`WeatherScreen.kt`:** Pantalla principal con cabecera de fecha en español, barra de búsqueda desplegable con autocompletado en tiempo real y soporte para permisos de ubicación en segundo plano.

---

## 📱 6. Widget de Escritorio Nativo de 3 Filas (Jetpack Glance & RemoteViews)

Easy-Climate incluye un widget de escritorio de alta densidad informativa optimizado para formato de rejilla 2x2 y 4x2:

```
┌──────────────────────────────────────────────────────────┐
│  📍 Delicias, Madrid                         ↑22° · ↓12° │  ◄── Fila 1: Ubicación y Rango Térmico
├──────────────────────────────────────────────────────────┤
│  ⛅ 19°  Despejado con sol suave            Sens. 18°    │  ◄── Fila 2: Temperatura y Condición
├──────────────────────────────────────────────────────────┤
│  👕 Manga corta técnica + Cortavientos modular (✨ Groq) │  ◄── Fila 3: Recomendación Bioclimática
└──────────────────────────────────────────────────────────┘
```

### Estructura de Información en 3 Filas Compactas

1. **Fila 1 (Cabecera Territorial y Extremos):** Nombre de la localidad/barrio (`maxLines = 1`, `ellipsize = "end"`) con indicador de temperaturas máxima y mínima del día.
2. **Fila 2 (Estado Meteorológico Central):** Icono de condición climática, temperatura actual destacada a gran escala y sensación térmica percibida.
3. **Fila 3 (Cápsula Bioclimática de Vestimenta):** Icono de prenda principal (`👕`, `🧥`, `🎽`, `🛡️`), consejo sintetizado de 3 a 5 palabras y distintivo de origen (`✨ Groq` o `⚙️ Local`).

### Ciclo de Vida y Actualización con WorkManager

- **`WeatherWidgetWorker`:** Tarea periódica en segundo plano programada con restricciones de batería (`BatteryNotLow`) y red conectada.
- **`BootCompletedReceiver`:** Restaura inmediatamente el widget tras reiniciar el dispositivo utilizando la última caché válida de la base de datos o memoria, sin generar llamadas costosas de red.

---

## ⚪ 7. Compatibilidad Adaptativa con Nothing OS & Material You

Easy-Climate incluye compatibilidad nativa con los estándares de diseño monocromático de **Nothing OS** y los esquemas dinámicos de **Android 12+ (Material You)**:

### Icono Adaptativo Monocromo (`res/drawable/ic_launcher_monochrome.xml`)
El icono de la aplicación cuenta con una capa monocromática vectorial optimizada:
- **Lienzo Total:** Lienzo estándar de $108 \times 108\text{ dp}$.
- **Zona Segura:** Elementos gráficos confinados dentro del círculo central de $66 \times 66\text{ dp}$ para prevenir recortes en launchers circulares o hexagonales.
- **Contraste Puro:** Blanco sólido (`#FFFFFF`) sobre transparencia para integrarse con los temas monocromáticos y la matriz de puntos característica de Nothing OS.

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

---

## 🛠️ 8. Guía de Compilación, Pruebas y Despliegue

### Prerrequisitos de Entorno
- **Android Studio:** Ladybug (2024.2.1) o superior.
- **JDK:** OpenJDK 17 o 21.
- **Gradle:** Kotlin DSL con soporte para Android Gradle Plugin 8.7+.
- **Configuración SDK:** `minSdk = 26` (Android 8.0 Oreo), `targetSdk = 35` (Android 15), `compileSdk = 35`.

### Inyección de Credenciales de Groq Cloud
Para habilitar el asesor IA con Llama 3.3 70B, crea un archivo `.env` en la raíz del proyecto o introduce la variable en el panel de compilación:

```properties
GROQ_API_KEY=gsk_tu_clave_de_groq_aqui
```

*(Nota: Si no se proporciona una API Key de Groq, la aplicación funcionará de forma ininterrumpida utilizando el motor bioclimático local).*

### Comandos de Compilación y Verificación

```bash
# 1. Compilar el APK en modo depuración (Debug)
gradle assembleDebug

# 2. Ejecutar la suite completa de pruebas unitarias y biomecánicas en la JVM (Robolectric / JUnit)
gradle :app:testDebugUnitTest

# 3. Validar las pruebas de captura de pantalla (Robarazzi Screenshot Tests)
gradle :app:verifyRoborazziDebug

# 4. Generar el bundle de producción para distribución (Release AAB)
gradle bundleRelease
```

---

## 📄 9. Licencia de Uso 100% Gratuita y Open Source (MIT)

Este proyecto está publicado bajo la **Licencia MIT**, una licencia de software libre y de código abierto (Open Source) sumamente permisiva.

Cualquier persona, estudiante, desarrollador, empresa o institución tiene la libertad absoluta de:
- **Usar y Ejecutar:** Utilizar la aplicación y su motor matemático para cualquier fin personal o comercial de manera 100% gratuita.
- **Estudiar y Modificar:** Analizar el código fuente, alterar los algoritmos bioclimáticos, adaptar los modelos de capas o personalizar la interfaz gráfica.
- **Redistribuir y Compartir:** Realizar copias, redistribuir versiones modificadas o integrar partes del motor físico en proyectos propios sin restricciones de royalties ni pagos de licencias.

```text
MIT License

Copyright (c) 2026 Easy-Climate Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
