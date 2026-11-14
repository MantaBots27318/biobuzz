# Piece 2: tag positions

## Goal

Give the code a list ("tag map") that says where every AprilTag (IDs 30 to 45) is on the field, for
each of the 2 positions of its HIVE, in our field coordinates. Add a "Tag Map Check" test OpMode to
prove the list is right by comparing it with what the Limelight really sees. The robot's position is
NOT computed here (piece 4), and the HIVE's state is set by hand (piece 3 makes it automatic).

## In scope

- `vision/TagMap.java` (new package `vision`): for each tag ID and each HIVE state, its field position
  (x, y, height, inches) and the direction it faces. Pure Java, no hardware.
- `opmodes/test/TagMapCheck.java`: `@TeleOp(name = "Tag Map Check", group = "Test")`. Uses `Vision` only
  (not the whole `Robot`, like Vision Test).
- Settings in Panels (`@Configurable`): where the camera really is (x, y, height); which CELL is up on
  the blue HIVE and on the red HIVE. No tilt or heading setting: they are worked out from the tags.
- `Vision.Tag` also gives the camera's pose in the tag's own frame (position and angle), read from the
  Limelight's `getCameraPoseTargetSpace()` (no change to what Vision Test shows, except it may add flat
  distance later).
- `TagMap` turns that into the camera's pose on the field: using the tag's known field pose (position,
  facing direction, sticker "up"). Check the Limelight's axis convention on real readings; do not guess.
- Tag Map Check shows, per tag: the camera position (x, y, height) computed from that tag, the difference
  from the position typed in Panels, and flat distance measured vs predicted. Also shows the camera's
  heading and tilt that came out (information only).
- Update the file map in CLAUDE.md section 3 (Vision, VisionTest, TagMap, TagMapCheck).

## Out of scope

- Computing the robot's position from tags (piece 4), detecting HIVE state from the camera (piece 3),
  aiming (5, 6), any change to A. TeleOp / A. Auto.

## Decisions (see PLAN.md)

- 2026-11-01: positions from the official CAD only, read by the user in Onshape.
- 2026-11-01: field coordinates = Pedro, (0, 0) at the audience/red corner, +x toward blue, +y away from
  the audience, heading 0 = facing the blue wall, z = up from the tile top.
- 2026-11-01: a HIVE state = which of its 2 CELLS is up (FAR or AUDIENCE).
- 2026-11-01: the other 3 cases are computed from one measured sticker and the pivot.
- 2026-11-01: "Tag Map Check" is part of this piece.

## Facts the builder needs

- Tag IDs: blue far cell 42-45, blue audience cell 38-41, red far cell 30-33, red audience cell 34-37.
- Tags: 36h11, 3.25 in. Inside a cluster the 4 tags sit in a row, centers at -6.50, -2.75, +2.75, +6.50 in.
  from the middle of the row (SDK `getBioBuzzTagLibrary()`). Do NOT use the SDK's y = 7.1874 and
  z = -5.622 offsets: they are measured from a cluster origin that is not the sticker. Assume the tag row
  is centered on the sticker; the test will show if that is wrong.
- Our first CAD readings came from vertices about 1.2 to 1.4 in. inside the true field corner, so use them
  only relative to the pivot (see PLAN.md). Measured sticker center relative to the pivot:
  (-0.2, +12.6, +5.7).
- Measured in the CAD (blue far cell, FAR cell UP, from PLAN.md): sticker center, from the pivot, at
  about (84.55, 84.6, 49.7) on the field; face tilted 30 deg, rising toward +y; facing direction (0, +0.5, -0.866);
  sticker "up" (away from the field center) (0, +0.866, +0.5). The sticker is 16.8 in. wide (along x)
  and about 5 in. tall along its slope.
- Pivot axis (blue HIVE): (84.75, 72, 43.95), measured against the field's center marker: 12.76 in. to
  the blue side of the center line, exactly on it in y. Axis runs along x.
- Geometry in the (y, z) plane, relative to the pivot: a HIVE bar is tilted by theta = +30 deg (FAR cell
  up) or -30 deg (AUDIENCE cell up); b = (cos t, sin t), u = (-sin t, cos t). From the measured sticker
  at theta = +30: a = along b, p = along u (computed from the measured center minus the pivot).
  FAR cell sticker = pivot + a*b + p*u; AUDIENCE cell sticker = pivot - a*b + p*u. It faces -u. Its "up"
  is +b (FAR cell) or -b (AUDIENCE cell). x stays the same (the pivot's x) for all of them.
- Red HIVE = blue rotated 180 deg around the field center (72, 72): (x, y) -> (144 - x, 144 - y),
  directions' x and y flipped, heights unchanged. Blue FAR cell <-> red AUDIENCE cell.
- Tag order along the sticker's row (which ID is at which end) is NOT known yet: make it one setting and
  let the check show the right choice. Seen from below with "up" away from the center, IDs probably run
  left to right.
- Camera frame from `Vision.Tag`: x right, y down, z forward, inches.
- Rules: no `hardwareMap` in the OpMode, `TelemetryUtil.withPanels(telemetry)` first, Java 8, settings are
  `public static` and `@Configurable`.

## How the user tests it

1. Do a full install, select "Tag Map Check", INIT, START.
2. Put the Limelight (or the robot) somewhere where it sees a cluster. Measure its position with a tape
   measure from the audience/red corner (x, y) and its height. Type them into Panels. The direction does
   not matter.
3. In Panels, say which CELL is up on each HIVE (it must match the real practice HIVE).
4. For every tag in view the screen shows: measured flat distance and the computed camera position, the typed ones, and the
   difference. Position within about 2 in. = good. A single far tag may be noisier than a close one.
5. Check at least 2 positions, and also the HIVE tipped the other way (change the setting).
6. If the IDs look swapped left to right (a tag shows a big angle error that matches its neighbor), tell
   me; it is the tag order setting.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no warnings from our code.
- A scratch calculation reproduces the measured sticker (center, facing, "up") exactly for FAR-up blue,
  and gives plausible numbers for the other 7 cases (heights between 36 and 51 in.).
- All 16 IDs have a position for both states. No `sleep`, no `hardwareMap` in OpModes, Java 8 only.
- Say plainly what was checked: compiled and calculated, NOT tested on the robot.
- Nothing from pieces 3 to 6 added.
