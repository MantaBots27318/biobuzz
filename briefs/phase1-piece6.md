# Piece 6: the Auto, step 1: "Far start" (the user's Auto 1)

## Goal

The first real Auto, built as if the robot were finished. The robot starts on the side where the PARTNER shoots
first, waits and watches our HIVE with the Limelight, and when the partner has tipped it, shoots our load, goes to
a FLOWER, comes back, shoots again, and parks. Intaking and shooting are placeholders (just waits) for now; the
Limelight steps are real. Every step is a small reusable function ("building block") so the other Autos can be
put together from the same pieces.

## The Auto, in order (written for RED as the user and the co-coder think about it; the code is written in
BLUE coordinates and `alliance.poses()` rotates it)

Red far start = (60, 133, 270) from the co-coder's `Auto3`. In blue coordinates the same Auto starts on the audience
side. Because the field has central symmetry, the code is written once.

1. Start with 4 POLLEN loaded (placeholder: nothing to do). Drive to the shoot spot ((60, 106) facing the HIVE).
2. **Aim and wait for our target CELL to be up, with a timeout.** The partner tips our HIVE first, so the cell
   nearest to our start becomes the up cell. Aiming runs while we wait (it falls back to any tag of our HIVE).
   Time out: skip the shots and go to PARK (the points are worth more than nothing).
3. **Shoot** (placeholder: wait `SHOOT_TIME_MS`, telemetry says "SHOOT (placeholder)").
4. Go to the FLOWER on our side at (47, 129) (confirmed 2026-11-14), on the far wall: front toward the wall (heading 90
   in red coordinates; the co-coder's route backed in facing 270). **Intake** (placeholder: wait `INTAKE_TIME_MS`).
5. Back to the shoot spot, front toward the HIVE. Our shot in step 3 tipped the HIVE, so the target cell is down
   now: **aim and wait for it to be up again** (the partner has to tip it back). Same block, same timeout.
6. Shoot (placeholder). 7. PARK from where we actually are (a timeout can cut the sequence anywhere).
   PARK is (11, 118) (confirmed 2026-11-14, in the LOADING ZONE). The whole Auto also has an overall limit, so PARK always happens before the 30 s end.

## In scope

- New OpMode "A. Auto: far start" (Match group), based on `MainAuto`'s structure (alliance chosen at INIT).
- Building blocks (one small class in `opmodes/auto/`): `follow(path)`, `aimAndWaitForCell(cluster, timeout)`
  (a new small command: every loop drives `turnPower()`, finishes when the cell is up and aimed, or times out,
  and remembers whether it succeeded), `shoot()` and `intake()` placeholders, `parkFromHere()`.
- A skip step: if the wait timed out, the shot is skipped (never shoot without having seen the cell up).
- `robot.vision.start()` at START, `stop()` at the end; `robot.aim.setAlliance(...)`; the pose is marked known.
- Settings (`@Configurable`): timeouts, placeholder times, shoot spot, poses.
- The co-coder's four routes are saved in `docs/AUTO-PLANNING-NOTES.md`; this piece uses the `Auto3` one,
  converted to blue and with headings fixed for a robot whose **shooter and intake are on the same side (the front)**:
  front toward the HIVE at the shoot spot, front toward the wall at the FLOWER (no backing-in `reverseTangent`).

## Out of scope

- The user's Autos 2 and 3, an Auto chooser at INIT, the human player step, GARDEN pickups.
- The real shooter and intake (and the `Outtake` / `ShootingTest` decision), the servo-mounted Limelight.
- Tuning to the final robot (camera mount, gains, true timings). Not tested on the final robot.
- Changing `MainAuto` (it stays as it is for now).

## Decisions (see PLAN.md)

- 2026-11-14: build Auto 1 (far start) first, because it uses the tag wait the most.
- 2026-11-14: pretend the robot is finished; shoot and intake are waits for now.
- 2026-11-14: the code is written in blue coordinates; the co-coder's red paths are rotated by 180 degrees.
- 2026-11-14: the shooter is at the front with the intake, so headings are fixed (see above).

## Facts the builder needs

- Read `docs/AUTO-HANDOVER.md` (what exists, the 7 things that must be done) and `docs/AUTO-PLANNING-NOTES.md`.
- Rotation red -> blue: (144 - x, 144 - y, heading + 180); `alliance.poses()` does it for you if the poses are
  written in blue. Red start (60, 133, 270) = blue (84, 11, 90).
- Which cluster is "our target": the CELL nearest to the start side. Red far start = `RED_FAR`. For blue the same
  Auto starts on the audience side = `BLUE_AUDIENCE`. Take it from the alliance, do not hard-code it.
- Aim needs a tag of our HIVE in view from the shoot spot: check it at the spot (the camera mount is a placeholder).
- PARK: at least partly in our LOADING ZONE (23 x 11 in., at the wall by our ALLIANCE AREA). Park pose (11, 118),
  from the end of `Auto3`, confirmed by the user 2026-11-14. FLOWER = (47, 129), confirmed.
- Pedro 3 API, Java 8, no blocking (CLAUDE.md). `follower.update()` only in `Drivetrain.periodic()`.
- Branch: a new `feat/auto-far-start` made from the current branch (it contains the Shooting Test merge).

## Confirmed by the user 2026-11-14

- (47, 129) is the FLOWER; (11, 118) is a park spot; the brief is OK.

## How the user tests it (on the test robot, in an open space about 120 x 60 in.)

1. Full install (or Sloth). Put the practice HIVE where the red/blue one would be, tags in view of the shoot spot.
2. Select "A. Auto: far start", choose the alliance with X / B, press INIT then START. Telemetry shows the step name.
3. Do NOT tip the HIVE: the robot should drive to the shoot spot, turn toward the tags, wait, and after the timeout
   go and park without any "SHOOT" line.
4. Run it again and tip the HIVE by hand while it waits: it should show "SHOOT (placeholder)", go to the FLOWER spot
   (front toward the wall), "INTAKE (placeholder)", come back, wait again, and (tipped by hand again) shoot, then park.
5. Repeat with the other alliance: the route is rotated, nothing else changes.
6. Tell me where it ended up, how long each step took, and anything odd.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no new warnings.
- No `sleep`, no `hardwareMap` in the OpMode, no Java 9+, `follower.update()` not called outside `Drivetrain`.
- Limelight started and stopped; alliance given to `aim` and drivetrain.
- Poses in blue; a small check (scratch test) that the red conversion of each pose matches the co-coder's numbers.
- Headings: shoot spot faces the HIVE, FLOWER pose faces the wall, nothing backs in.
- The shot is skipped on timeout, and PARK always happens.
- What is checked is said plainly: "it compiles" is not "it works on the robot". Nothing from later pieces added.

## What was built (closed 2026-11-22)

Smaller than planned: drive 8 in. ahead, aim and wait for the target cell, shoot (placeholder), park in the LOADING
ZONE. The FLOWER pickup and second shot were left out for now. The park spot is computed from the measured zone and
robot size. The Limelight stops before the park (it slowed the loop and broke the park heading). Tested on the test
robot with a taped Limelight; all values are to retune on the final robot.
