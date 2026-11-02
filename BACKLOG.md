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
