# Piece 10: Auto 1, part 2: FLOWER pickup, then a second shot or park with the balls

## Goal

Finish the user's Auto 1 ("A. Auto: far start"). After the first shot, if it came early enough, the robot drives to
the FLOWER on our side and picks up balls (placeholder wait). Then a fixed cut-off time decides: if there is time, it
drives back to the shoot spot, aims with the camera and shoots the second load (which tips the HIVE), then parks;
otherwise it parks straight away with the balls loaded for TELEOP. Pickup and shooting stay placeholders; the
driving, aiming and timing are real. The new steps are building blocks the user's Autos 2 and 3 can reuse.

## In scope

- In `FarStartAuto`, after the first shot:
  1. **Go to the FLOWER only if the first shot happened and it ended before `FLOWER_CUTOFF_S`.** No first shot (the
     HIVE never tipped) or too late: park straight away, as today (we still hold our 4 POLLEN).
  2. Drive to the FLOWER stop pose (placeholder), front toward the far wall. **Intake** placeholder: wait
     `INTAKE_TIME_MS`, telemetry "INTAKE (placeholder)".
  3. **If the Auto clock is below `SECOND_SHOT_CUTOFF_S`:** drive back to the shoot spot facing the HIVE, **aim with
     the camera without waiting for the cell** (turn until aimed or `AIM_TIMEOUT_MS`), shoot (placeholder), park.
     **Otherwise:** park from the FLOWER with the balls loaded.
- Building blocks (small methods returning a command): `goToFlowerAndIntake()`, `ifTimeLeft(cutoffS, then, else)`,
  `aimNoWait(timeoutMs)`, reusing `follow`, `shoot`, `parkLeg`.
- Placeholder settings in code (not Panels): FLOWER pose, `INTAKE_TIME_MS`, `FLOWER_CUTOFF_S`, `SECOND_SHOT_CUTOFF_S`,
  `AIM_TIMEOUT_MS`. The overall time limit before parking is kept, so PARK always happens before 30 s.
- The log and telemetry show the new steps and which branch was taken (and the Auto clock at each decision).

## Out of scope

- Autos 2 and 3, an Auto chooser at INIT, the real intake and shooter, the human player.
- Measuring the real FLOWER position (placeholder until the final robot / real field).

## Decisions (see PLAN.md, 2026-11-22, user)

- 8 POLLEN tip the HIVE: our first 4 do not, so the cell is still up for the second load.
- Second shot: aim with the camera, no waiting for the cell.
- Shoot again or park: one fixed cut-off time.
- FLOWER stop: placeholder.
- Defaults I chose (tell me if wrong): no first shot = park directly (keep the balls); a second cut-off decides
  whether to go to the FLOWER at all.

## Facts the builder needs

- FLOWER placeholder: co-coder's red (47, 129), front toward the far wall = blue (97, 15), heading 270 in blue
  (front toward the audience wall in blue). Written in blue, rotated by `alliance.poses()`.
- Placeholder times (rough, from the logs: first leg 0.8 s, park about 3 s at 50 in/s): `INTAKE_TIME_MS` 2000,
  `FLOWER_CUTOFF_S` 18 (pickup about 4 s + park about 4 s + margin), `SECOND_SHOT_CUTOFF_S` 19 (back about 2 s, aim
  up to 1 s, shoot 1.5 s, park about 4 s, margin 2 s), `AIM_TIMEOUT_MS` 1000. All to time on the robot.
- The aim reuses `AimAndWaitForCell` with a new "don't wait for the cell" option (its Auto gains turn the robot;
  `HiveAim`'s TeleOp minimum power was too weak in piece 6). With no tag in view it faces the HIVE from the Pinpoint.
- Speeds: legs 40 in/s, park 50 in/s. The tag fix stays off in this Auto (`USE_TAG_POSITION_FIX`).
- Java 8, no `sleep`, commands only; the Auto clock is the existing `runTime`.
- Built after the phase 1 review fixes (added after this brief was first written): the scoring part, now with the
  FLOWER and the second shot inside it, is cut at `PARK_START_BY_S` (26 s) and the park then runs with no speed limit;
  everything stops at 29.5 s. The old 9 s tip wait is gone: with no tip the robot waits until 26 s.

## How the user tests it (test robot, the field used for piece 6, a tape mark for the FLOWER)

Rough times with the placeholder values (first shot ends about 1.5 s after the tip; FLOWER trip plus pickup about
3.5 s). The "Route" line on the Driver Hub and the `route` log column list each choice with the clock.

1. Full install. Put a tape mark where the FLOWER placeholder is. Run "A. Auto: far start".
2. **Tip the HIVE early** (about 2 s after START): shoot, drive to the tape mark (front toward the wall behind the
   start), "INTAKE (placeholder)", drive back, turn a little to aim, "SHOOT (placeholder)", park parallel in the zone.
   Route: `shot 1, FLOWER, shot 2, parking`.
3. **Tip it at about 14 s:** shoot, FLOWER, then park with the balls (no second shot).
   Route: `shot 1, FLOWER, shot 2 skipped, parking`.
4. **Tip it at about 17 s:** shoot, then park straight away. Route: `shot 1, FLOWER skipped, parking`.
5. **Do not tip it:** it waits until 26 s, then parks fast. Route: `parking (late)`.
6. Watch for bumps into the wall at the FLOWER and pull the log after each run.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no new warnings.
- Each branch traced in the code: early tip, late tip, no tip; PARK happens in all three, before 30 s even in the
  slowest case (sum the time limits).
- FLOWER pose converted: red (47, 129, 90 deg facing the far wall) matches blue after rotation.
- No waiting step in the second shot; the aim has its own time limit.
- Nothing from Autos 2 and 3 added. "It builds" is not "it works on the robot".
