#!/usr/bin/env bash
#
# Prend une capture d'écran du téléphone connecté via ADB.
#
# Usage:
#   ./screenshot.sh              -> screenshot_01.png, screenshot_02.png, ...
#   ./screenshot.sh home         -> home.png
#   ./screenshot.sh session      -> session.png
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NAME="${1:-}"

# Vérifier qu'un appareil est connecté
if ! adb get-state &>/dev/null; then
    echo "Erreur : aucun appareil Android connecté."
    exit 1
fi

if [[ -n "$NAME" ]]; then
    FILENAME="${NAME}.png"
else
    # Auto-incrément : screenshot_01.png, screenshot_02.png, ...
    NUM=1
    while [[ -f "$SCRIPT_DIR/screenshot_$(printf '%02d' $NUM).png" ]]; do
        ((NUM++))
    done
    FILENAME="screenshot_$(printf '%02d' $NUM).png"
fi

OUTPUT="$SCRIPT_DIR/$FILENAME"

adb exec-out screencap -p > "$OUTPUT"
echo "Screenshot enregistré : screenshots/$FILENAME"
