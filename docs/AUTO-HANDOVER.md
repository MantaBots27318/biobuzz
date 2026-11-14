# Handover: building the Auto on the Limelight (piece 6)

For a fresh session. Read `CLAUDE.md` (team rules), `PLAN.md` (decisions, dated), `PROGRESS.md`, `BACKLOG.md`,
`docs/NOTES.md` (what the tests showed), then this file. Written 2026-11-14, branch `feat/apriltags`, pieces 1 to 5
done and tested on the test robot. Piece 6 waits for the final robot (decided 2026-11-08).

## The goal of piece 6

An Auto that (1) knows where it is, using the Limelight to correct the Pinpoint, and (2) shoots only when the
HIVE cell it aims at is up and still: aim step, then "wait until our target cell is up" with a time limit, then
shoot; if it never happens, keep the balls and go PARK (AUTO PARK: at least partly in our LOADING ZONE, 5 points,
manual 10.5). Structure: our usual `SequentialCommandGroup` + `FollowPathCommand` list (CLAUDE.md section 8), the
wait is one more step, the time limit is a setting like `MainAuto.SCORING_TIMEOUT_MS`. Never shoot without having
seen the cell up.

## What already exists (all in `TeamCode/.../teamcode/`, registered in `Robot` in this order)

| Class | What it gives the Auto |
|---|---|
| `subsystems/Vision` | `start()` / `stop()` the Limelight, `getTags()` (camera frame x right, y down, z forward, inches, plus roll/pitch/yaw), `getAgeMs()`, `getFrameId()`, `getLatencyMs()` |
| `subsystems/Hives` | per cell (`Cluster.BLUE_FAR/BLUE_AUDIENCE/RED_FAR/RED_AUDIENCE`): `getState()` UP / DOWN / MOVING / NOT_SEEN, `getLastKnownState()`, `isCellUp(cluster)` (UP, settled, seen a moment ago: the shooting condition), `getSideUp(hive)` / `getLastKnownSideUp(hive)` (FAR / AUDIENCE / UNKNOWN) |
| `subsystems/HiveAim` | `setAlliance()`, `hasTarget()`, `turnPower()` (counter-clockwise positive, 0 when aimed or no target), `isAimed()`, `getErrorDeg()`, `getTargetName()`. It does NOT move the robot |
| `subsystems/TagLocalizer` | nudges the Pinpoint's x and y toward the tags (heading never changed); `setPoseKnown()`, `ENABLED`, status text. Runs by itself in `periodic()` |
| `vision/TagMap` | where each of the 16 tags is on the field for each HIVE position; `cameraFromTag(...)` |
| `vision/HiveLogic`, `AimLogic`, `LocalizerLogic` | the maths, no hardware, easy to check on their own |
| `subsystems/Drivetrain` | `follower` (Pedro), `pose()`, `setPose()`, `driveFieldCentric(forward, lateral, turn)` |
| `util/MatchState` | the Auto saves alliance and pose every loop for the TeleOp |

Test OpModes (group "Test"): Vision Test, Tag Map Check, Hive State Test, Aim Test, Localization Test. Each builds
only the subsystems it needs, so it runs with a partial configuration (the full `Robot` needs every device).

## Things the next session MUST do or it will silently not work

1. **`MainAuto` never starts the Limelight.** Call `robot.vision.start()` in `preRun()` and `robot.vision.stop()`
   in `end()` (`MainTeleOp` already does). Without it no tag is read, so no aiming, no up detection, no correction.
2. **Set the alliance on `robot.aim`** (`robot.aim.setAlliance(alliance)`), not only on the drivetrain: that is how
   it knows which HIVE is ours.
3. **There is no launcher subsystem yet.** The robot has only `Intake`; `MainAuto`'s scoring step is a placeholder
   (`intake.eject`). A shooting mechanism and its timing have to be built (the team's job, then ask the user how it
   works) before the "shoot" step means anything.
4. **`MainAuto`'s poses are old placeholders** (start (9, 111, -90) etc., blue side). Replace them with real BIOBUZZ
   start and park poses from the manual (`TeamCode/docs/manual/`), blue-side coordinates, `alliance.poses().of(...)`.
5. **There is no step that turns by itself.** `HiveAim.turnPower()` only gives a number. Write a small command (for
   example `AimCommand`) that, every loop, calls `drivetrain.driveFieldCentric(0, 0, aim.turnPower())`, finishes
   when `aim.isAimed()` has held a short time (or on a time limit), and stops the robot at the end.
6. **The wait step** is `new WaitUntilCommand(() -> robot.hives.isCellUp(targetCluster) && robot.aim.isAimed())`
   with `.withTimeout(...)`. The target cluster comes from `hives.getSideUp(ourHive)`: FAR means the far cell
   (`BLUE_FAR` / `RED_FAR`), AUDIENCE the other one; if UNKNOWN, do not shoot.
7. **Park after the timeout from the actual pose** with `DeferredCommand`, as `MainAuto` already does, because the
   timeout can cut the sequence anywhere.

## Decisions already made (see PLAN.md for the dated list)

- Camera: Limelight 3A named `limelight`, fixed on the robot, points the way the launcher points; the robot turns,
  no turret. Pipeline: AprilTag 36h11, Marker Size **82.55 mm** (3.25 in.). The old 165.1 mm doubled every distance.
- Field frame = Pedro: (0, 0) at the corner where the audience wall meets the red wall, +x toward blue, +y away
  from the audience, heading 0 toward the blue wall. Red = blue turned 180 degrees about (72, 72).
- Aim: camera only (the Pinpoint position is not used to aim). Target = the up cell of our HIVE; if unknown the
  last known up cell; if unknown too any tag of our HIVE; no tag in view = no automatic turn.
- Up detection: angle must be about 30 degrees from straight down (both resting positions; about 0 halfway through a
  tip), then the higher cell of a HIVE is up when both are in view, a height threshold when only one is, plus a
  hold time. Not seen counts as "not up yet". The median over a cluster's tags is used (single tags can be wrong).
- Correction: x and y only, the Pinpoint's heading is trusted. Only tags of a settled HIVE; tags whose implied
  camera height is off are thrown out; each picture is compared with the Pinpoint pose from the moment it was
  taken; a gain pull for small differences; a disagreement above 10 in. is confirmed by 4 pictures in a row, then it
  jumps (a bump), otherwise ignored. If the pose is unknown the first good picture sets it. Readings are ignored
  when the robot moves faster than 30 in/s or turns faster than 90 deg/s, so in an Auto the correction mostly
  happens at the stops (for example while aiming and waiting), not during fast driving.
- The TeleOp start spot is an option (`TagLocalizer.TELEOP_START_ENABLED`, off, placeholders).

## Verified on the test robot (docs/NOTES.md has the numbers)

- Tag Map Check: every cluster and both HIVE positions give the camera within 1 to 3 in. and the height within
  about 1 in.; the Limelight's angle convention (`TagMap.EULER_ZYX = true`) and the tag order along a sticker are right.
- Hive State Test: both HIVEs read correctly from the side. A wrong state seen once could not be reproduced.
- Aim Test: it works. Localization Test: a 14 in. fake bump is fixed in a split second; driving, unknown start,
  HIVE moving and covered camera were fine.

## NOT verified or still placeholders (tune on the final robot)

- Camera mounting: tilt 28 deg and height 14 in. (`Hives`), offsets 6 in. forward, 0 left (`TagLocalizer`).
  They were measured on the test setup and change on the final robot.
- All gains and limits: `HiveAim` (turn strength, min and max power, tolerance, offset), `TagLocalizer` (gain,
  distances, speeds, height tolerance, snap), `Hives` (thresholds, hold time).
- The Limelight's real picture delay: `Vision.getLatencyMs()` is capture + targeting + staleness; keep
  `TagLocalizer.EXTRA_LATENCY_MS` in mind.
- Whether the correction should be ON during the Auto's path following (it is on by default). It is safe at the stops
  because the speed gate stops it during fast driving; check that a correction in the middle of a path does not make
  Pedro react badly. `TagLocalizer.ENABLED` turns it off.
- The camera must actually see the HIVE tags from the spot where the robot waits and shoots: choose that spot, and
  the camera mounting, with this in mind.

## Pitfalls we hit

- A `Robot` needs every device in the active configuration (drive motors, `pinpoint`, `intake`, `limelight`); a missing
  one crashes at INIT. The test OpModes avoid this.
- Single tags give a wrong angle now and then (tags at the edge of the picture, glare): never trust one tag alone.
- The practice rig once had the blue stickers on the wrong cells; the real layout is manual Figure 9-17
  (blue audience 38-41, blue far 42-45, red far 30-33, red audience 34-37).
- Our first CAD measurements came from vertices about 1.2 to 1.4 in. inside the field corner: use CAD readings
  relative to the pivot (blue pivot (84.75, 72, 43.95)).
- Panels sometimes does not load (computer not on the robot's Wi-Fi, app stuck): the same telemetry is on the
  Driver Hub screen, and defaults in the code work without Panels. Values typed in Panels are lost on redeploy.
- A "-1 V" in Panels was not from our code (`BatteryVoltage` cannot produce it): check the battery and the
  Driver Hub first.
- Java 8 only; Pedro 3 API (CLAUDE.md section 2); never `sleep` or wait in a loop; `follower.update()` only in
  `Drivetrain.periodic()`.

## Questions to ask the user first (one at a time, recommendation first)

1. How does the shooting mechanism work and what is its timing (spin-up, feed, how many balls)?
2. What should the Auto do, in order: start spot, which balls to collect, where to shoot from, when to wait for the
   HIVE, where to park? (Check the manual for AUTO scoring and the LOADING ZONE.)
3. Should the tag correction be on during the Auto, and should aiming also be allowed to use other tags when the
   target's are out of view (BACKLOG idea)?
4. Is the final robot built, and are its Limelight mounting numbers measured?
