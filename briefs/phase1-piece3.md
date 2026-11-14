# Piece 3: HIVE is up detection

## Goal

The robot tells, for each of the 4 CELLS that carry tags, whether it is UP (ready to shoot into), DOWN,
MOVING (the HIVE is tipping) or NOT SEEN, and which CELL of a HIVE is up. We only shoot into a CELL that is
up and has stopped moving. It uses the Limelight only (no tag map, no robot position). The camera sees
the tags of both HIVEs and both cells of each HIVE at once, because the structure is open underneath: when
both cells of a HIVE are in view, the higher one is the up one. It is built and checked by calculation
now, and tuned on the robot at HQ.

## In scope

- `subsystems/Hives.java` (a subsystem, registered in `Robot` after `Vision`): reads the tags from `Vision`
  every loop and keeps the state of the 4 clusters (blue far 42-45, blue audience 38-41, red far 30-33,
  red audience 34-37).
- Per cluster state: `UP`, `DOWN`, `MOVING`, `NOT_SEEN`. Plus the last known state, kept when the tags are
  lost (for aiming). Plus `isCellUp(cluster)`: true only if UP **and** seen recently.
- Per HIVE: which cell is up (`FAR`, `AUDIENCE`, `UNKNOWN`).
- How a cell's state is decided, in this order:
  1. **Angle (is it settled?)**: the tags' angle from straight down must be near 30 deg (tolerance is a
     setting). If not, the cell is `MOVING`.
  2. **Height, both cells of the HIVE in view**: the higher cell is `UP`, the lower `DOWN` (they differ by
     about 14 in.; a minimum difference is a setting).
  3. **Height, only one cell in view (fallback)**: above a height threshold (about 43 in., a setting) = `UP`,
     below = `DOWN`. This needs the camera's mounting height and tilt.
  4. The new state must hold for a hold time (a setting) before it replaces the old one.
- Test OpMode `opmodes/test/HiveStateTest.java`, `@TeleOp(name = "Hive State Test", group = "Test")`:
  shows each cluster's state, which rule decided it, and the numbers behind it (height, angle). Creates
  only `Vision` and `Hives`.
- Settings in Panels (`@Configurable`), with a placeholder value each: camera mounting height and tilt,
  height threshold, minimum height difference, expected tag angle and its tolerance, hold time, time after
  which a cluster counts as not seen.

## Out of scope

- Aiming (piece 5), the Auto step (piece 6), the tag map, position correction (postponed).
- Any use of the robot's position.

## Decisions (see PLAN.md)

- 2026-11-02: no map; heights come from the camera reading and the camera's tilt.
- 2026-11-02 (user): the tag's angle counts too: right height and right 30 deg angle.
- 2026-11-02: we shoot only when our target CELL is up and settled; not seen counts as "not up yet".
- 2026-11-08 (user): both cells of a HIVE are visible from under the structure, so compare their heights
  first; the height threshold is the fallback.
- 2026-11-08: the median over the tags of a cluster is used (single tags give wrong angles now and then).
- 2026-11-08: the real field's tag layout is the manual's (Figure 9-17); the user rebuilds the practice
  rig to match, so there is no practice-rig switch.

## Facts the builder needs

- From the CAD (blue far cell): tag center about 49.7 in. high when up, about 35.9 in. when down. The
  sticker's tilt is 30 deg in both resting positions (the lean direction flips). Halfway through a tip the
  bar is level: the tags face straight down (0 deg) at about 42.6 in. Heights are for the sticker's
  center; the tags sit in a row on it, so about the same.
- Camera frame from `Vision.Tag`: x right, y down, z forward, inches, plus `rollDeg`, `pitchDeg`, `yawDeg`.
- Height above the camera of a tag = -y*cos(t) + z*sin(t), t = camera tilt up from level; add the mount
  height for the height above the floor. The comparison of two cells needs only the tilt.
- Tag angle: the tag's normal (toward the camera) in the camera frame comes from the Limelight's rotation
  (same convention as `TagMap.EULER_ZYX`, which worked at HQ on 2026-11-08; add a helper in `TagMap`); its
  vertical part is found with the camera tilt; the angle from "straight down" should be 30 deg.
- HQ test 2026-11-08 (docs/NOTES.md): the angle convention gave a camera tilt of 29 deg for a camera held
  about 30 deg up, so it works; single tags can still be wrong, hence the median.
- Rules: no `hardwareMap` outside subsystems, settings `public static` + `@Configurable`, nothing blocks,
  Java 8. `Robot` creates `Vision` before `Hives` and registers them in that order.

## How the user tests it (at HQ)

1. Mount the Limelight as it will be on the robot. Measure its upward tilt (and its height above the
   tiles, for the fallback). Type them in Panels (`Hives` settings).
2. Do a full install (or hot reload), select "Hive State Test", INIT, START.
3. With the practice HIVE set one way, look at it from where the robot will shoot, so that both cells of
   one HIVE are in view. The higher cell should read UP, the other DOWN, and the HIVE line should name
   the cell that is up.
4. Move so that only one cell is in view: the state should stay right (rule 3, the fallback).
5. Tip the HIVE by hand slowly: the cells should read MOVING, then settle on the new state.
6. Cover the camera: the cells go to NOT SEEN after the set time and the last known state stays shown.
7. Tell me the heights and angles you see; I tune the thresholds.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no warnings from our code.
- A scratch calculation with pretend camera readings (made from `TagMap`) gives: UP for the higher cell and
  DOWN for the lower one with both in view (both HIVES, both positions); the fallback gives the same with
  one cell in view; MOVING for a level bar; NOT SEEN after the time; the right "which cell is up".
  Include a wrong single-tag reading among the tags to show the median copes.
- No `sleep`, no `hardwareMap` in OpModes, Java 8 only.
- Say plainly: compiled and calculated, NOT tested on the robot.
- Nothing from pieces 5 and 6 added.
