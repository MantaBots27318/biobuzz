#!/usr/bin/env bash
# Updates the Pedro tuning procedures (pedro/procedures/) from the official Quickstart.
# Usage: scripts/update-pedro-procedures.sh [revision]   (default: latest master)
# On Windows: run it from Git Bash.
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
    echo "Already up to date ($sha)."
    exit 0
fi

# Replace the whole folder: it must never be edited by hand
find "$PROCEDURES" -name '*.java' -delete
cp "$tmp/quickstart/$PROCEDURES"/*.java "$PROCEDURES/"
echo "$sha" > "$PROCEDURES/QUICKSTART_REV"

echo "Procedures updated to $sha."
echo
echo "Pedro versions used by the Quickstart (copy them into TeamCode/build.gradle if they differ):"
grep -h "com.pedropathing" "$tmp/quickstart/build.dependencies.gradle" "$tmp/quickstart/TeamCode/build.gradle" || true
echo
echo "Our current versions:"
grep "com.pedropathing" TeamCode/build.gradle
echo
echo "Next: build (./gradlew :TeamCode:assembleDebug), check pedro/Tuning.java, then make a separate commit."
