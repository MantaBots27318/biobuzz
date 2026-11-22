#!/usr/bin/env bash
# Fails if our code uses the Xbox button names (a, b, x, y). We drive with PlayStation controllers, so the code
# uses cross, circle, square, triangle (the same physical buttons). Run by the "Build" check on every pull request.
# Usage: scripts/check-button-names.sh   (on Windows: run it from Git Bash)
set -euo pipefail

cd "$(dirname "$0")/.."
SRC="TeamCode/src/main/java/org/firstinspires/ftc/teamcode"

# Plain character classes instead of \b so the same pattern works with GNU grep (GitHub) and macOS grep
NOT_WORD='[^A-Za-z0-9_]'
PATTERN="(^|$NOT_WORD)(gamepad[12]?\.(a|b|x|y)(WasPressed|WasReleased)?|Button\.(A|B|X|Y))($NOT_WORD|$)"

# pedro/procedures/ is copied from the Pedro Quickstart and never edited by us
if found="$(grep -rnE "$PATTERN" "$SRC" --include='*.java' | grep -v "/pedro/procedures/")"; then
    echo "$found"
    echo
    echo "Xbox button names found above. Use the PlayStation names: cross, circle, square, triangle"
    echo "(A = cross, B = circle, X = square, Y = triangle; Button.A = Button.CROSS, and so on)."
    exit 1
fi
echo "Button names OK (PlayStation names only)."
