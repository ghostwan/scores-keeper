#!/usr/bin/env bash
#
# Change la langue de l'application ScoresKeeper via ADB (debug uniquement).
# Ne touche PAS à la langue du système.
#
# Usage:
#   ./switch-lang.sh          -> bascule automatiquement (fr ↔ en)
#   ./switch-lang.sh fr       -> français
#   ./switch-lang.sh en       -> anglais
#   ./switch-lang.sh es       -> espagnol
#   ./switch-lang.sh de       -> allemand
#   ./switch-lang.sh system   -> revenir à la langue du téléphone
#

set -euo pipefail

PACKAGE="com.ghostwan.scoreskeeper"
RECEIVER="${PACKAGE}/.LanguageBroadcastReceiver"
ACTION="${PACKAGE}.SET_LANGUAGE"

if ! adb get-state &>/dev/null; then
    echo "Erreur : aucun appareil Android connecté."
    exit 1
fi

TARGET="${1:-}"

if [[ -z "$TARGET" ]]; then
    # Détecte la langue actuelle du système et bascule fr ↔ en
    CURRENT=$(adb shell "settings get system system_locales" 2>/dev/null || echo "")
    if [[ "$CURRENT" == fr* ]]; then
        TARGET="en"
    else
        TARGET="fr"
    fi
fi

case "$TARGET" in
    fr|en|es|de|system)
        adb shell am broadcast -n "$RECEIVER" -a "$ACTION" --es lang "$TARGET" > /dev/null 2>&1
        case "$TARGET" in
            fr)     echo "Langue de l'app → Français" ;;
            en)     echo "Langue de l'app → English" ;;
            es)     echo "Langue de l'app → Español" ;;
            de)     echo "Langue de l'app → Deutsch" ;;
            system) echo "Langue de l'app → Système" ;;
        esac
        ;;
    *)
        echo "Usage: ./switch-lang.sh [fr|en|es|de|system]"
        exit 1
        ;;
esac
