# NOTES: learned the hard way

- **Limelight Marker Size:** the pipeline keeps last season's value. For BIOBUZZ it must be 82.55 mm
  (3.25 in.). With 165.1 mm every distance came out twice too big (2026-11-01).
- **Robot needs every device:** `new Robot(hardwareMap)` fails if any device (intake, drive motors,
  pinpoint, limelight) is missing from the active configuration. Test OpModes for one mechanism should
  create only that subsystem.

## First test at HQ, 2026-11-08 (hand-held Limelight, practice HIVE)

- **Vision Test:** distances "spot on" against a tape measure (Marker Size 82.55 mm). Angles not checked.
- **Tag Map Check**, camera typed at x 72, y 120, height 14 in., looking toward the audience:
  - Red tags 30, 31, 36: camera found within 0.7, 1.0 and 4.1 in., height within 0.3 in., heading -91 deg
    and tilt 29 deg up. So the map geometry, the ID order and the angle convention (`EULER_ZYX = true`)
    work for those, in both HIVE positions (one red cell up, one down).
  - Red tags 32, 33 (same cluster as 30, 31): 37 to 44 in. off. In the Limelight picture their 3D boxes
    overlap and point the wrong way: single-tag angle readings can be wrong (glare, edge of the picture).
    Do not trust one tag's angle alone: use the median of a cluster.
  - Blue tags 38 to 43: all wrong the same way, camera at about (97, 74) and height -12 in., tilt 88 deg.
    In simulation, a blue HIVE whose real state is the opposite of the typed `BLUE_FAR_CELL_UP` gives y 70,
    height -13, tilt 89 (matches) but x 72, not the +26 seen. To check: flip `BLUE_FAR_CELL_UP`, and
    measure the distance between the red and blue pivots on the practice rig (the model says 25.5 in.).

## Second test at HQ, 2026-11-08: the blue IDs on the practice rig

- Pivot distance between the red and blue HIVEs on the practice rig: 25.5 in. (as modeled).
- `BLUE_FAR_CELL_UP` was true on the rig (real: blue far cell up). With it set to false the blue tags gave
  the camera at about (97, 24), height 12 to 18 in., heading about 90 deg, tilt about 30 deg.
  A simulation reproduces this exactly (x 97, y 24, h 14, tilt 29, heading 90) if **on the practice rig the
  blue IDs are swapped: 38-41 sit on the FAR cell and 42-45 on the AUDIENCE cell**, the opposite of the
  manual's text (38-41 audience, 42-45 far). Red matches the manual (30-33 far). The rig is therefore
  not 180-degree symmetric as the manual says the field is, or its blue HIVE is turned around.
- To settle which one is right for the real field: look at Figure 9-17 (manual p. 75, PDF) and the
  official CAD for the blue stickers.
- The camera sees the tags of BOTH HIVEs and both cells of each HIVE at once, because the HIVE
  structure is open underneath. Idea for piece 3: compare the two cells' heights seen together (higher =
  up) instead of needing the mount height.
- Single tags still give wrong readings now and then (tag 31 this time, 32/33 the first time): always take
  the median over a cluster.

- **Figure 9-17 checked (user, 2026-11-08):** it agrees with the manual's text (blue audience 38-41 at the
  bottom, blue scoring/far 42-45 at the top; red scoring 33-30 and red audience 34-37 left to right),
  and our map reproduces that left-to-right order. So the practice rig is the odd one out. A rig-only
  switch was added and then removed the same day: the user is rebuilding the rig's blue HIVE to match the
  figure. After the rebuild, rerun "Tag Map Check" with the default settings (nothing to switch).

## Hive State Test at HQ, 2026-11-08

- Seen from the blue side with 2 tags per cluster: blue audience up, red far up were both read correctly
  (red from the last known state, since its tags were out of view). The two blue cells read 11.2 in. apart
  (34.4 and 23.2 in. above the camera) against about 14 in. in the CAD, and the tag angles 25 to 27 deg
  against 30: a small camera tilt error is likely, so set the real tilt when the Limelight is mounted.
- A "wrong state from the side" seen once could not be reproduced.
