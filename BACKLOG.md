# BACKLOG

## To do at the robot (nothing here has run on the robot yet unless it says so)

- **Measure the TeleOp start spot** and set `TELEOP_START_ENABLED`, `TELEOP_START_X_IN`, `TELEOP_START_Y_IN` and
  `TELEOP_START_HEADING_DEG` in `TagLocalizer`'s settings (they are placeholders, option off). Added 2026-11-08.
- **On the final robot:** measure the Limelight's camera offsets from the robot's reference point (now 6 in. forward,
  0 left in `TagLocalizer`) and its real tilt and height and set `Hives.CAMERA_TILT_DEG` and
  `Hives.CAMERA_HEIGHT_IN` (now 28 deg and 14 in. on the test setup); tune the aim in `HiveAim` (turn strength,
  maximum and minimum power, tolerance, launcher offset). Added 2026-11-08.
- **Fill in the Limelight's USB port** in the "Wiring" table of TeamCode/README.md (still `?`). Added 2026-11-01.
- **Piece 6, the Auto** (aim step, wait until our target cell is up with a time limit, then park if it never
  happens): waits for the final robot. Decided 2026-11-08.

## Done

- 2026-11-14: "Localization Test" run on the test robot: the 14 in. fake bump is fixed in a split second, driving,
  unknown start, HIVE moving and covered camera all work (piece 4). Camera offset measured: 6 in. forward, 0 left.
- 2026-11-01 to 11-08: Limelight set up (pipeline Marker Size 82.55 mm, name `limelight`), "Vision Test" run:
  distances spot on (piece 1, `c0098fc`).
- 2026-11-08: "Tag Map Check" run on the rebuilt rig: all clusters and both HIVE positions agree to about 1 to
  3 in. (piece 2, `5e42042`). The EULER_ZYX and IDS_LEFT_TO_RIGHT switches did not need changing.
- 2026-11-08: Limelight tilt measured on the test setup: about 28 deg, height 14 in.; the 45 deg idea is dropped
  for now (final robot: measure again).
- 2026-11-08: "Hive State Test" and "Aim Test" run on the test robot (pieces 3 and 5, `25f329f`, `d05ea99`).

## Found at HQ, 2026-11-08 ("Hive State Test"): not reproduced, closed 2026-11-08

The user could not recreate it and a later check from the blue side was fully correct. Kept in case it comes back.

- **Works well in general** (user: "pretty good"), but when the robot is at the SIDE of the HIVE and many tags
  are in view, a cell that is really down can be shown as UP. Cause not known yet. To get from the user when
  it happens: the per-cluster lines (height, angle, number of tags), the rule line, and the camera tilt and
  height settings used. Ideas: (1) wrong camera tilt setting hurts most when the two cells are at different
  distances; (2) a cluster with only 1 or 2 tags in view cannot reject a bad tag; (3) tags seen very obliquely
  have worse angle readings. Possible fixes: require at least 2 tags per cluster, or a spread check; or choose
  the HIVE state whose predicted tag positions agree best with each other (needs `TagMap`, no camera tilt,
  and Pinpoint can be a second check).

## Idea from the user, 2026-11-08 (while testing "Aim Test")

- **Aim even when the target's tags are not in view.** Sometimes the camera sees AprilTags (another cell, the
  other HIVE) and could tell its own heading and position, but the robot does not turn because it does not
  see its target tags. Why: piece 5 only uses the target cell's tags (camera only, by decision). Options:
  (A, cheap, no map) while the target's tags are out of view, turn toward any other tag of our HIVE as a
  rough aim, then the fine aim takes over (needs a switch with a little hysteresis so it does not jump
  between cells); (B, bigger) use the tag map to work out the camera's heading and position from any tag in
  view, then turn toward the target by position (needs the tag map confirmed on the rebuilt rig).

## Idea from the user, 2026-11-08: pair the Pinpoint and the Limelight

- The Pinpoint gives the quadrant the robot is in, so we know which AprilTags to expect in view. Use it to
  sanity-check tag readings (a tag the robot should not see from here, or a position far from the Pinpoint's,
  is probably wrong), and later to help aim when the target tags are out of view. Belongs with the postponed
  piece 4 (position correction) and the "aim when the target is out of view" idea above.
