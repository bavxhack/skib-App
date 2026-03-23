# SKIBin Android-App lokal starten

Dieses Repository enthält die Android-App im Ordner `SKIBin_old/`.

Die App ist für Organisationen und Betreuer gedacht und greift über die bestehende API auf verwaltete Kinder zu. Die Verbindung der App erfolgt über die QR-Codes aus der bestehenden Webanwendung.

---

## Projektüberblick

- **Android-Projektordner:** `SKIBin_old/`
- **App-ID / Package Name:** `com.h2Invent.skibin`
- **Programmiersprache:** Kotlin
- **Build-System:** Gradle
- **Minimale Android-Version:** Android 5.0 (API 21)
- **Ziel-SDK:** Android 15 / API 35
- **Wichtige Bibliotheken:** Material Components, Volley, Fresco, Biometric, ZXing Embedded

---

## Voraussetzungen

Bevor du die App auf deinem Handy startest, solltest du Folgendes installiert bzw. vorbereitet haben:

### 1. Android Studio installieren
Empfohlen ist eine aktuelle Android-Studio-Version mit eingebettetem **JDK 17**.


> **Wichtig bei deinem Fehlerbild:**
> Wenn Android Studio beim Sync meldet, dass die **Gradle JVM inkompatibel** ist, musst du in Android Studio die **Gradle JDK / Gradle JVM auf Java 17** umstellen.
>
> Pfad in Android Studio:
> `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`
>
> Dort wählst du am besten:
> - **Embedded JDK (17)**, oder
> - ein lokal installiertes **JDK 17**

### 2. Android SDK installieren
In Android Studio sollten mindestens diese Komponenten installiert sein:

- **Android SDK Platform 35**
- **Android SDK Build-Tools**
- **Android SDK Platform-Tools**
- **Android Emulator** (optional, falls du zusätzlich einen Emulator nutzen willst)

Das findest du in Android Studio unter:

- `Android Studio > Settings > Android SDK`

### 3. Handy vorbereiten
Auf deinem Android-Handy:

- **Entwickleroptionen aktivieren**
- **USB-Debugging aktivieren**
- Das Handy per USB mit dem Rechner verbinden
- Die Debugging-Freigabe auf dem Handy bestätigen

Falls du die Entwickleroptionen noch nicht aktiviert hast:

1. `Einstellungen > Über das Telefon`
2. Mehrfach auf **Build-Nummer** tippen
3. Danach unter `Einstellungen > System > Entwickleroptionen` **USB-Debugging** einschalten

---

## Projekt in Android Studio öffnen

### Variante A: Direkt den App-Ordner öffnen
Das ist die einfachste Variante.

1. Android Studio starten.
2. Auf **Open** klicken.
3. Den Ordner **`SKIBin_old/`** auswählen.
4. Warten, bis Gradle synchronisiert ist.

### Variante B: Falls du das ganze Repo geöffnet hast
Wenn du versehentlich den Repo-Root `skib-App/` geöffnet hast, öffne stattdessen den Unterordner **`SKIBin_old/`**, weil dort das eigentliche Android-Projekt liegt.

---

## Was du in Android Studio ausführen musst

Wenn das Projekt geöffnet ist, führe in Android Studio diese Schritte aus:

### 1. Gradle-Sync starten
Normalerweise startet Android Studio den Sync automatisch.

Falls nicht:

- Klicke oben auf **Sync Project with Gradle Files**
- oder nutze:
  - `File > Sync Project with Gradle Files`

### 2. Prüfen, ob die Build-Variante stimmt
Unten links bzw. im linken Toolfenster:

- **Build Variants** öffnen
- sicherstellen, dass für das Modul `app` die Variante **`debug`** ausgewählt ist

### 3. Projekt einmal bauen
Empfohlen vor dem ersten Start:

- `Build > Make Project`

Wenn das erfolgreich ist, sind SDK, Gradle und Abhängigkeiten korrekt eingerichtet.

### 4. Handy als Zielgerät auswählen
Oben in Android Studio:

- Das angeschlossene Handy in der Geräteliste auswählen
- Falls es nicht erscheint:
  - USB-Kabel prüfen
  - USB-Debugging prüfen
  - ADB-Zugriff auf dem Handy bestätigen

### 5. App starten
Dann entweder:

- auf den **grünen Play-Button** klicken
- oder:
  - `Run > Run 'app'`

Android Studio installiert die App dann auf deinem Handy und startet sie.

---

## Was genau gestartet werden soll

In Android Studio wird normalerweise automatisch die **Run Configuration `app`** verwendet.

Falls du sie manuell auswählen oder neu anlegen musst:

1. Oben auf die Run-Konfiguration klicken
2. **Edit Configurations** öffnen
3. Prüfen, dass folgendes gesetzt ist:
   - **Module:** `app`
   - **Launch:** Default Activity

Die Start-Aktivität ist die App-eigene Authentifizierungsoberfläche mit Fingerprint/PIN.

---

## App auf dem Handy einrichten

Nach dem ersten Start:

1. Die App öffnen
2. Falls noch keine Verbindung gespeichert ist, führt die App dich zur Konfiguration
3. Über **„Gerät verbinden“** den QR-Code aus der bestehenden Webanwendung scannen
4. Danach je nach QR-Code:
   - **Organisation verbinden**, oder
   - **Benutzer / Betreuer verbinden**
5. Bei Benutzer-Verknüpfungen ggf. den **E-Mail-Bestätigungscode** eingeben
6. Danach werden die Endpunkte und Tokens lokal gespeichert

Danach kannst du:

- als **Organisation** QR-Codes für Check-ins scannen
- als **Betreuer** die Kinderlisten und Kinderdetails abrufen

---

## Wichtige Berechtigungen auf dem Handy

Die App benötigt je nach Funktion diese Berechtigungen:

- **Kamera** – für QR-Code-Scans
- **Telefon** – um aus den Kinderdetails direkt anzurufen
- **Biometrie** – für Fingerprint-Login
- **Internet** – für den Zugriff auf die API
- **Vibration** – für Feedback beim Scannen

Wenn etwas nicht funktioniert, prüfe in den App-Einstellungen auf dem Handy, ob die Berechtigungen erlaubt sind.

---

## Falls der Build in Android Studio fehlschlägt

### Mögliche Ursache 1: SDK 35 fehlt
Dann in Android Studio unter `Settings > Android SDK` die Plattform **API 35** nachinstallieren.

### Mögliche Ursache 2: **Gradle JVM version incompatible**
Wenn du beim Sync diese oder eine ähnliche Meldung siehst:

```text
Gradle JVM version incompatible.
This project is configured to use an older Gradle JVM that supports up to version 8
but the current AGP requires a Gradle JVM that supports version 17.
```

Dann ist fast immer in Android Studio noch **Java 8** oder ein anderes zu altes JDK für Gradle eingestellt.

#### So behebst du das in Android Studio

1. Öffne:
   `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`
2. Suche die Einstellung **Gradle JDK**
3. Stelle sie auf:
   - **Embedded JDK (17)**, oder
   - ein installiertes **JDK 17**
4. Danach erneut:
   - `File > Sync Project with Gradle Files`

#### Falls du kein JDK 17 zur Auswahl hast

Dann kannst du in Android Studio über den JDK-Dialog ein JDK 17 herunterladen oder lokal installieren und danach erneut auswählen.

#### Falls du zusätzlich im Terminal baust

Dann prüfe auch deine Java-Version:

```bash
java -version
```

Wenn dort nicht **17** angezeigt wird, setze für dein Terminal bzw. deine Shell ein JDK 17 als `JAVA_HOME`.

### Mögliche Ursache 3: Gradle oder Abhängigkeiten werden nicht geladen
Dann nacheinander versuchen:

1. `File > Sync Project with Gradle Files`
2. `Build > Clean Project`
3. `Build > Rebuild Project`

### Mögliche Ursache 4: Proxy / Firmennetzwerk blockiert Downloads
Wenn du in einem eingeschränkten Netzwerk bist, können Gradle oder Abhängigkeiten blockiert werden. In dem Fall:

- anderes Netzwerk testen
- Proxy in Android Studio korrekt eintragen
- oder die Downloads außerhalb des Firmennetzes durchführen

### Mögliche Ursache 5: Handy wird nicht erkannt
Im Android-Studio-Terminal oder normalen Terminal prüfen:

```bash
adb devices
```

Wenn dein Gerät dort nicht auftaucht:

- USB-Kabel wechseln
- USB-Modus am Handy prüfen
- Debugging-Freigabe erneut bestätigen

---

## Nützliche Aktionen in Android Studio

Diese Menüpunkte wirst du am häufigsten brauchen:

- **Gradle-Sync:** `File > Sync Project with Gradle Files`
- **Projekt bauen:** `Build > Make Project`
- **Neu bauen:** `Build > Rebuild Project`
- **App starten:** `Run > Run 'app'`
- **Log-Ausgabe ansehen:** `View > Tool Windows > Logcat`

---

## Wenn du die App per Terminal bauen willst

Im Projektordner `SKIBin_old/` kannst du auch per Terminal arbeiten:

```bash
./gradlew assembleDebug
```

Die APK liegt danach typischerweise unter:

```text
SKIBin_old/app/build/outputs/apk/debug/
```

Zum Installieren auf ein angeschlossenes Gerät:

```bash
./gradlew installDebug
```

---

## Kurzfassung: Das musst du in Android Studio tun

Wenn du nur die Kurzversion willst:

1. **`SKIBin_old/` in Android Studio öffnen**
2. **Gradle Sync** ausführen
3. **Build > Make Project**
4. Handy per USB anschließen und **USB-Debugging** aktivieren
5. Gerät oben auswählen
6. **Run > Run 'app'** oder den grünen Play-Button drücken
7. In der App den **QR-Code aus der Webanwendung scannen**

---

## Hinweis

Wenn du möchtest, kann ich dir als Nächstes auch noch eine zweite Datei ergänzen, z. B.:

- eine **kurze Schnellstart-Anleitung für Nicht-Entwickler**, oder
- eine **technische Entwickler-Doku** mit Projektstruktur, wichtigen Klassen und API-Fluss.
