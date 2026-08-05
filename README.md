# Radar Compass

Brújula Android nativa y liviana. Combina acelerómetro y magnetómetro para compensar la inclinación del equipo, y usa la ubicación GPS para corregir la declinación magnética y apuntar al norte verdadero.

## Requisitos

- Android 6.0 o posterior.
- Acelerómetro y magnetómetro.
- Ubicación opcional; sin permiso GPS continúa con norte magnético.

## Compilar

Abrir el directorio en Android Studio (JDK 17) y ejecutar **Build > Build APK(s)**. Para generar una versión firmada: **Build > Generate Signed Bundle / APK**.

Cada envío a la rama `main` también ejecuta GitHub Actions. La APK de prueba queda disponible en **Actions > Build Android APK > Artifacts > RadarCompass-debug-apk** durante 30 días.

La interfaz es una creación original de anime de artes marciales y no contiene personajes, nombres, logotipos ni recursos de Dragon Ball.
