# Piece 9: PlayStation button names

## Goal

The team drives with PlayStation controllers, so the code, the Driver Hub text and the docs should name the buttons
Cross, Circle, Square and Triangle, not A, B, X and Y. These are the same physical buttons, so nothing about how the
robot behaves changes, only the names. A check in the GitHub "Build" check stops the old names from coming back
through other branches. Branch `feat/ps-button-names`, from an up-to-date `master` (pieces 6 to 8 already merged).

## In scope

1. **Code** (`TeamCode/src/.../teamcode/`, every file that uses them, `pedro/procedures/` excluded):
   - `gamepad1.a` / `.b` / `.x` / `.y` → `.cross` / `.circle` / `.square` / `.triangle` (same for `gamepad2`).
   - `GamepadKeys.Button.A` / `B` / `X` / `Y` → `CROSS` / `CIRCLE` / `SQUARE` / `TRIANGLE`.
   - Edge variables named after the button (`lastA`) follow (`lastCross`).
   - Telemetry text and comments: "A = zero" → "Cross = zero", "X = blue, B = red" → "Square = blue, Circle = red".
   - Files today: `MainTeleOp`, `util/AllianceChoice`, `LocalizationTest`, `LoopCheck`, `OdometryCheck`, `PathCheck`,
     `ShootingTest`, `SystemCheck`, plus any comment or telemetry line naming a button elsewhere.
2. **Docs:** the "Gamepad controls" table and the other button mentions in `TeamCode/README.md`; the examples and
   button list in `CLAUDE.md` section 6, plus a rule there: "we use PS controllers: use the PS names; the Build check
   refuses A/B/X/Y"; the alliance line in CLAUDE.md section 8. Add a matching line under "Rules" in the README.
3. **Build check** (`.github/workflows/build.yml`): a step before the compile that searches `TeamCode/src` (without
   `pedro/procedures/`) for `gamepad1.a` style fields (`a`, `b`, `x`, `y`, including `aWasPressed()` style) and for
   `Button.A/B/X/Y`. If it finds any, the check turns red and prints the file, the line and
   "Use the PlayStation names: cross, circle, square, triangle".
4. **Note** in `PLAN.md` (piece 9 done) and `PROGRESS.md`.

## Out of scope

- Changing which button does what (only names change).
- Touching other branches (`feat/outtake-test`, `movement-testing`, `shooting-test`). They are old (October) and may
  belong to teammates. After the PR is merged, I ask the user which, if any, should get `master` merged in. If a
  teammate opens a PR with the old names, the Build check tells them what to change.
- `pedro/procedures/` and `FtcRobotController/` (never edited).

## Decisions

- 2026-11-22: PS names in the code; Cross = A, Circle = B, Square = X, Triangle = Y; alliance = Square blue, Circle red.
- 2026-11-22: rule in CLAUDE.md and README **plus** an automatic Build check (user chose option 1).
- 2026-11-22: done after piece 8 and the merge of `feat/auto-far-start` and `feat/loop-time` (done).

## Facts the builder needs

- FTC SDK 12 `Gamepad` has `cross`, `circle`, `square`, `triangle` fields (checked in RobotCore 12.0.0); SolversLib
  0.3.6 `GamepadKeys.Button` has `CROSS`, `CIRCLE`, `SQUARE`, `TRIANGLE`. Compile to confirm.
- The old names still work on the robot; the Build check is the only thing that enforces the new ones.
- Build: `./gradlew :TeamCode:assembleDebug`. Full install after: `./gradlew :TeamCode:removeSlothRemote installDebug`.

## How the user tests it (test robot, PS controllers)

1. Full install. Check new code runs: the alliance line during INIT now says "Square = blue, Circle = red".
2. "A. Auto: far start", INIT: press Square → `BLUE (chosen)`, press Circle → `RED (chosen)`.
3. "A. TeleOp": gamepad 2 hold Cross → intake collects, hold Circle → intake ejects. Square / Circle pick the alliance
   during INIT when no Auto ran before.
4. "System Check": hold Cross / Circle spins a motor; Triangle sets a servo to 0.5.
5. Optional: one test OpMode (for example "Path Check" or "Shooting Test"): the screen names PS buttons and they do
   what it says.
6. On GitHub, the PR's "Build" check is green.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no new warnings.
- A search for the old names in `TeamCode/src` (without `pedro/procedures/`) finds nothing, in code, comments or
  telemetry.
- Run the Build check's search script locally: it passes on the new code, and fails with a clear message on a
  throw-away file using `gamepad1.a` (then delete that file).
- Each renamed button maps correctly (A→Cross, B→Circle, X→Square, Y→Triangle): read each diff line.
- README table and CLAUDE.md match the code. Say plainly: "it compiles", not "it works on the robot".
