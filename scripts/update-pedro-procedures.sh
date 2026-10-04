#!/usr/bin/env bash
# Met à jour les outils de réglage Pedro (pedro/procedures/) depuis le Quickstart officiel.
# Usage : scripts/update-pedro-procedures.sh [révision]   (par défaut : la dernière version de master)
# Sous Windows : à lancer depuis Git Bash.
set -euo pipefail

REPO_URL="https://github.com/Pedro-Pathing/Quickstart.git"
PROCEDURES="TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/procedures"
REV="${1:-master}"

cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

git clone --quiet --filter=blob:none "$REPO_URL" "$tmp/quickstart"
git -C "$tmp/quickstart" checkout --quiet "$REV"
sha="$(git -C "$tmp/quickstart" rev-parse HEAD)"

if [ "$sha" = "$(cat "$PROCEDURES/QUICKSTART_REV")" ]; then
    echo "Déjà à jour ($sha)."
    exit 0
fi

# On remplace tout le dossier : il ne doit jamais être modifié à la main
find "$PROCEDURES" -name '*.java' -delete
cp "$tmp/quickstart/$PROCEDURES"/*.java "$PROCEDURES/"
echo "$sha" > "$PROCEDURES/QUICKSTART_REV"

echo "Procédures mises à jour vers $sha."
echo
echo "Versions Pedro utilisées par le Quickstart (à recopier dans TeamCode/build.gradle si elles diffèrent) :"
grep -h "com.pedropathing" "$tmp/quickstart/build.dependencies.gradle" "$tmp/quickstart/TeamCode/build.gradle" || true
echo
echo "Versions actuelles chez nous :"
grep "com.pedropathing" TeamCode/build.gradle
echo
echo "Ensuite : compiler (./gradlew :TeamCode:assembleDebug), vérifier pedro/Tuning.java, puis faire un commit à part."
