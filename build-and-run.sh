#!/usr/bin/env bash
# ============================================================
#  build-and-run.sh
#  Compile, installe et lance ScoresKeeper sur le téléphone
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Couleurs -----------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

# ---- Config -------------------------------------------------
APP_ID="com.scoreskeeper"
ACTIVITY=".MainActivity"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
BUILD_VARIANT="assembleDebug"

# ---- Parsing des arguments ----------------------------------
CLEAN=false
RELEASE=false

for arg in "$@"; do
  case $arg in
    --clean)   CLEAN=true ;;
    --release) RELEASE=true; BUILD_VARIANT="assembleRelease"; APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk" ;;
    --help|-h)
      echo ""
      echo "Usage: $0 [options]"
      echo ""
      echo "Options:"
      echo "  --clean     Nettoie le build avant de compiler"
      echo "  --release   Compile en mode release (non signé)"
      echo "  --help      Affiche cette aide"
      echo ""
      exit 0
      ;;
    *) warn "Argument inconnu : $arg (ignoré)" ;;
  esac
done

echo ""
echo "  ╔══════════════════════════════╗"
echo "  ║    ScoresKeeper Builder      ║"
echo "  ╚══════════════════════════════╝"
echo ""

# ---- 1. Vérification des prérequis --------------------------
info "Vérification des prérequis..."

# Java
if ! command -v java &>/dev/null; then
  error "Java introuvable. Installe un JDK 17+ (ex: brew install --cask temurin@17)"
fi
JAVA_VERSION=$(java -version 2>&1 | head -1)
success "Java : $JAVA_VERSION"

# ADB
if ! command -v adb &>/dev/null; then
  error "adb introuvable. Ajoute le platform-tools Android SDK au PATH."
fi
success "adb : $(adb version | head -1)"

# gradlew — génération automatique si absent
if [ ! -f "./gradlew" ]; then
  warn "gradlew absent, tentative de génération via gradle system..."
  if command -v gradle &>/dev/null; then
    gradle wrapper --gradle-version=8.9 2>/dev/null || true
  fi
  if [ ! -f "./gradlew" ]; then
    error "gradlew introuvable et gradle système absent.\nInstalle Gradle : brew install gradle\nOu ouvre le projet dans Android Studio pour qu'il génère gradlew."
  fi
fi
chmod +x ./gradlew

# ---- 2. Vérification de l'appareil connecté -----------------
info "Recherche d'un appareil Android..."

DEVICES=$(adb devices | grep -v "List of" | grep "device$" | awk '{print $1}')
DEVICE_COUNT=$(echo "$DEVICES" | grep -c "." 2>/dev/null || echo 0)

if [ "$DEVICE_COUNT" -eq 0 ]; then
  error "Aucun appareil connecté.\n  - Branche ton téléphone en USB\n  - Active le mode développeur et le débogage USB\n  - Lance : adb devices"
fi

if [ "$DEVICE_COUNT" -gt 1 ]; then
  warn "Plusieurs appareils détectés :"
  echo "$DEVICES" | while read -r d; do
    MODEL=$(adb -s "$d" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    echo "    - $d  ($MODEL)"
  done
  DEVICE=$(echo "$DEVICES" | head -1)
  warn "Utilisation du premier : $DEVICE"
else
  DEVICE="$DEVICES"
fi

MODEL=$(adb -s "$DEVICE" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
ANDROID=$(adb -s "$DEVICE" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')
success "Appareil : $MODEL (Android $ANDROID) [$DEVICE]"

# ---- 3. Clean optionnel -------------------------------------
if [ "$CLEAN" = true ]; then
  info "Nettoyage du build précédent..."
  ./gradlew clean --quiet
  success "Clean terminé"
fi

# ---- 4. Compilation -----------------------------------------
info "Compilation ($BUILD_VARIANT)..."
echo ""

START_TIME=$(date +%s)

./gradlew "$BUILD_VARIANT" \
  --stacktrace \
  --warning-mode none \
  2>&1 | while IFS= read -r line; do
    # Filtre et colorise la sortie Gradle
    if echo "$line" | grep -qE "^> Task|BUILD SUCCESSFUL|BUILD FAILED"; then
      if echo "$line" | grep -q "BUILD SUCCESSFUL"; then
        echo -e "  ${GREEN}$line${NC}"
      elif echo "$line" | grep -q "BUILD FAILED"; then
        echo -e "  ${RED}$line${NC}"
      else
        echo -e "  ${BLUE}$line${NC}"
      fi
    elif echo "$line" | grep -qiE "error:|warning:|exception"; then
      echo -e "  ${RED}$line${NC}"
    else
      echo "  $line"
    fi
  done

# Vérifie le résultat réel de Gradle (le pipe cache le code de retour)
./gradlew "$BUILD_VARIANT" --quiet 2>/dev/null || error "La compilation a échoué. Consulte les logs ci-dessus."

END_TIME=$(date +%s)
BUILD_DURATION=$((END_TIME - START_TIME))
echo ""
success "Build terminé en ${BUILD_DURATION}s"

# ---- 5. Vérification de l'APK -------------------------------
if [ ! -f "$APK_PATH" ]; then
  error "APK introuvable : $APK_PATH"
fi

APK_SIZE=$(du -sh "$APK_PATH" | cut -f1)
success "APK : $APK_PATH ($APK_SIZE)"

# ---- 6. Installation ----------------------------------------
info "Installation sur $MODEL..."

adb -s "$DEVICE" install -r "$APK_PATH" 2>&1 | while IFS= read -r line; do
  echo "  $line"
done

# Vérifie que l'app est bien installée
if ! adb -s "$DEVICE" shell pm list packages 2>/dev/null | grep -q "$APP_ID"; then
  error "L'installation a échoué (package $APP_ID non trouvé)"
fi
success "Application installée"

# ---- 7. Lancement -------------------------------------------
info "Lancement de l'application..."

adb -s "$DEVICE" shell am start \
  -n "${APP_ID}/${APP_ID}${ACTIVITY}" \
  -a android.intent.action.MAIN \
  -c android.intent.category.LAUNCHER \
  2>&1 || error "Impossible de lancer l'application"

success "Application lancée sur $MODEL !"

echo ""
echo "  ╔══════════════════════════════════════╗"
echo "  ║  Build & Deploy terminé avec succes  ║"
echo "  ╚══════════════════════════════════════╝"
echo ""
info "Astuce : utilise 'adb -s $DEVICE logcat -s ScoresKeeper' pour voir les logs"
echo ""
