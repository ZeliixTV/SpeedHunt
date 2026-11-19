# SPEED HUNT – native Android-App

Echte Android-App (Java, **kein Flutter**, keine externen Bibliotheken).
Paket `com.speedhunt.game`, Android 8.0+, Hochformat, läuft komplett offline.

## APK bekommen – Weg A: GitHub (kein Android Studio nötig)
1. Auf github.com ein neues (privates) Repository anlegen.
2. Den **Inhalt** dieses Ordners hochladen (inkl. des versteckten Ordners `.github`).
3. Reiter **Actions** → „Build SPEED HUNT APK“ läuft automatisch (ca. 3–5 Min.).
4. Fertig: Reiter **Releases** → `SpeedHunt.apk` auf dem Handy antippen → installieren
   (Android fragt einmal nach „Installation aus unbekannten Quellen“ erlauben).

## APK bekommen – Weg B: Android Studio
1. Android Studio öffnen → **Open** → diesen Ordner wählen → Sync abwarten.
2. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   (oder Terminal: `gradle assembleRelease`)
3. APK: `app/build/outputs/apk/release/app-release.apk`

Alle Builds sind mit demselben Schlüssel (`app/speedhunt.keystore`) signiert –
neue Versionen lassen sich einfach über die alte installieren, Fortschritt bleibt erhalten.

## Inhalt
- Menü mit animiertem Skin, Münzen, Rekord, Statistik
- Shop mit 30 Skins, Filter nach Seltenheit, Kaufen/Ausrüsten
- Spiel: 5 Verstecke, 4 Pings, Jäger wählt zufällig, animierte Karte, Konfetti, Rüttel-Effekt
- Einstellungen: 3 Hintergründe, Sound an/aus, Vibration an/aus, Anleitung, Fortschritt zurücksetzen
- Sound wird im Code erzeugt (keine Audiodateien), Speicherstand lokal auf dem Gerät

Belohnung: 1 Ping = 2, 2 Pings = 25, 3 Pings = 50, Flucht = 100 Münzen.
