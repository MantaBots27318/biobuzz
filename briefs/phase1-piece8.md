# Piece 8: fixes from the phase 1 review

## Goal

Close phase 1 cleanly before the next Autos. Make the far-start Auto always park before the 30 s end, whatever the
waits are set to; take the old placeholder "A. Auto" out of the Match list; make the alliance choice safe (a clear
warning, the same default everywhere); and bring the notes in line with the code (camera tilt 35, camera offset).
Then open a pull request of pieces 6 to 8 to `master`. Source: docs/reviews/phase-1.md.

## In scope

1. **Park before 30 s** (`FarStartAuto`), counted from START on the match clock (`runTime`, reset in `preRun()`):
   - New settings `PARK_START_BY_S = 26` and `AUTO_END_S = 29.5`.
   - The scoring part (drive, wait for the cell, shoot) ends at `PARK_START_BY_S` at the latest, whatever its own
     limits say; then the park starts.
   - **A late park** (started because the 26 s limit cut the scoring part) drives with no speed limit (user: "we power
     to the park faster, doesn't matter too much if we hit the wall"). A normal park keeps `PARK_SPEED_IN_PER_S`.
   - Everything (park tries included) stops at `AUTO_END_S`: the robot holds where it is.
   - The step name says "parking (late)" when it is a late park, and the log shows it.
2. **Old Auto to Test:** `MainAuto` becomes `@Autonomous(name = "Old Auto (placeholder)", group = "Test")`. Update
   `CLAUDE.md` (tree, sections 8 and 9) and `TeamCode/README.md` (tree, "how a match runs") so they point to
   "A. Auto: far start" as the match Auto.
3. **Alliance default = RED everywhere, with a warning:** `FarStartAuto`, `MainTeleOp` (when no Auto handed one over),
   `AimTest`, `MainAuto`. During INIT, until X or B is pressed, telemetry shows a first line
   `ALLIANCE NOT CHOSEN: RED by default (X = blue, B = red)`; once pressed, `Alliance: BLUE (chosen)`. In the
   TeleOp, an alliance from the Auto counts as chosen.
4. **Notes:** `Hives.CAMERA_TILT_DEG = 35` is the right value (user, 2026-11-22): update `BACKLOG.md`, `docs/NOTES.md`
   (add a dated line, old 28 kept as history) and `docs/AUTO-HANDOVER.md`. Add to the existing backlog item on the
   camera offsets that `CAMERA_FORWARD_IN = 6`, `CAMERA_LEFT_IN = 0` were measured from the old Pinpoint tracked
   point (before the 2026-11-15 and 11-21 pod offset changes), so they must be re-measured.
5. **Pull request** of `feat/loop-time` (pieces 6, 7, 8) to `MantaBots27318/biobuzz`, base `master`, following
   `.github/PULL_REQUEST_TEMPLATE.md`, only after the user has tested and approved (done with `/done`).

## Out of scope

- The "could improve" items of the review (in BACKLOG.md), the PlayStation button names (own piece, after this one),
  Autos 2 and 3, the real shooter, any retuning on the final robot, re-measuring the camera offset.
- Blocking START when no alliance is chosen (decided: warn and keep a default).

## Decisions (see PLAN.md)

- 2026-11-22 (user): camera tilt is 35 deg (the code was right, the notes were old).
- 2026-11-22 (user): latest park start 26 s from START; a late park drives at full power; the Auto stops at 29.5 s.
- 2026-11-22 (user): no tip-wait limit of its own: the robot waits for the tip until 26 s, then parks. The 9 s
  `FIRST_TIP_TIMEOUT_MS` and 20 s `SCORING_TIMEOUT_MS` were placeholders and are removed.
- 2026-11-22 (user): "A. Auto" moves to the Test group.
- 2026-11-22 (user): warn when no alliance is chosen and keep a default, RED everywhere.
- 2026-11-22 (user): everything from the review's must-fix and should-fix lists goes in this piece.

## Facts the builder needs

- Today: `scoring.withTimeout(SCORING_TIMEOUT_MS)` then 3 `parkLeg`s of up to `PARK_TIMEOUT_MS` each
  ([FarStartAuto.java:180-205](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/auto/FarStartAuto.java#L180-L205)).
  Worst case today 20 + 12 = 32 s.
- A clock limit can be a `ParallelRaceGroup(scoring, new WaitUntilCommand(() -> runTime.seconds() >= PARK_START_BY_S))`
  (SolversLib). Same idea around the park legs with `AUTO_END_S`. Remember which one ended (a flag set in an
  `InstantCommand` or read from the clock) to choose the park speed.
- Full speed: `speedLimit().set(ForesightConfig.Constraint.NONE)`, as in `end()`.
- When the race cuts `AimAndWaitForCell`, it is interrupted: `succeeded()` is false, so no shot. Good: keep it.
- A cut path leaves the follower busy: after the park race ends, call `follower.hold(pose())` so the robot stops.
- Settings at the top, `public static`, not final, `@Configurable` (CLAUDE.md rule 4). Java 8, no `sleep`.
- Telemetry and code in English.

## How the user tests it (test robot)

1. Full install (`./gradlew :TeamCode:removeSlothRemote installDebug`); check the new step names appear (new code runs).
2. Driver Hub list: "A. Auto: far start" and "A. TeleOp" under Match; "Old Auto (placeholder)" under Test.
3. Select "A. Auto: far start", press INIT, press nothing: the screen says `ALLIANCE NOT CHOSEN: RED by default`.
   Press X: it says `BLUE (chosen)`. Press B: `RED (chosen)`.
4. Normal run (tip the HIVE by hand): as before, it shoots and parks parallel in the zone, step "parking".
5. Late park: do not tip the HIVE (no Panels needed). The robot waits; at 26 s the step becomes
   "parking (late)" and the robot drives to the zone fast; by 29.5 s it has stopped ("done" or "time up"). Pull the
   log and check the times.
6. "A. TeleOp" right after an Auto: no warning (alliance from the Auto). After an app restart: the warning shows.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no new warnings.
- Read the new command flow and check by hand: scoring can never run past 26 s; nothing runs past 29.5 s; a cut wait
  never shoots; a normal park still uses `PARK_SPEED_IN_PER_S`; the speed limit is given back in `end()`.
- Every alliance default in `opmodes/` is RED; every INIT shows the warning until a button is pressed.
- No reference to "A. Auto" as the match Auto left in CLAUDE.md or the README.
- Notes say tilt 35 everywhere they give the current value.
- Nothing from the backlog or later pieces added. Say plainly: "it compiles", not "it works on the robot".
