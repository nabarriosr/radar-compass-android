# Radar Compass

Brújula Android nativa y liviana. Combina acelerómetro y magnetómetro para compensar la inclinación del equipo, y usa la ubicación GPS para corregir la declinación magnética y apuntar al norte verdadero.

La interfaz 3D incorpora una burbuja de nivel que muestra `pitch` y `roll`: al quedar centrada y verde, el teléfono está aproximadamente paralelo al plano tangente local. El fondo alienígena, el radar energético y sus nodos son ilustraciones originales dibujadas directamente con Canvas, sin imágenes pesadas.

En tienda, UI y Play Store el producto se llama **Orbital Compass** (`com.orbitalcompass.app`). El directorio del repo puede seguir llamándose `radar-compass-android`.

## Requisitos

- Android 6.0 o posterior.
- Acelerómetro y magnetómetro.
- Ubicación opcional; sin permiso GPS continúa con norte magnético.
- Internet para el banner de AdMob (opcional en la práctica: si no carga, la brújula sigue).

## Compilar

Abrir el directorio en Android Studio (JDK 17) y ejecutar **Build > Build APK(s)**. Para generar una versión firmada: **Build > Generate Signed Bundle / APK**.

Cada envío a la rama `main` también ejecuta GitHub Actions. La APK de prueba queda disponible en **Actions > Build Android APK > Artifacts > RadarCompass-debug-apk** durante 30 días.

La interfaz es una creación original de anime de artes marciales y no contiene personajes, nombres, logotipos ni recursos de Dragon Ball.

## Documentación

| Documento | Para |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Pipeline de sensores, patrones y riesgos |
| [docs/DOMAIN.md](docs/DOMAIN.md) | Reglas de negocio, fórmulas y constantes |
| [docs/USE_CASES.md](docs/USE_CASES.md) | Flujos de usuario y matriz QA |
| [docs/PLAY_STORE.md](docs/PLAY_STORE.md) | Publicar en Google Play (AAB, AdMob, Data safety) |
| [docs/privacy-policy.html](docs/privacy-policy.html) | Política de privacidad para alojar en HTTPS |
| [docs/AI_CONTEXT.md](docs/AI_CONTEXT.md) | Cómo está partido el contexto para agentes |
| [AGENTS.md](AGENTS.md) | Instrucciones cortas para coding agents |
