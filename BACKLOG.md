# BACKLOG

- **Fill in the Limelight's USB port** in the "Wiring" table of TeamCode/README.md (still `?`).
  Why: asked on 2026-11-01, not answered yet.
- **Confirm the 45º** (camera tilt?) with the build team before choosing the mounting.
  Why: noted 2026-11-01 as "best angle for seeing the tags", meaning not confirmed.

## Done

## When back at HQ (about 2026-11-08): everything below is built but never run on the robot

- Run "Vision Test" again: piece 1's `Vision` now also reads the tag angles (added in piece 2).
- Run "Tag Map Check" (piece 2): compare computed and typed camera positions. If every tag is wrong, try
  `TagMap.EULER_ZYX` and `TagMap.IDS_LEFT_TO_RIGHT` in Panels. Added 2026-11-01.
- Measure the real height and angle to mount the Limelight (starting idea: 45 deg tilt, to confirm).

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
