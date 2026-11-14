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
- Limelight on the test setup, measured by the user 2026-11-08: tilt about 28 deg, height 14 in. Defaults in
  `Hives` set to these. Both change on the final robot: measure again then.

## Aim Test at HQ, 2026-11-08

- User: "it works". Seen: when the camera sees tags that are not the target cell's, the robot does not turn
  (by design). Recorded in BACKLOG.md as an idea.

## Tag Map Check on the rebuilt rig, 2026-11-08 (camera looking toward +y from the audience side)

- Typed in Panels: x 0.0 (meant relative to the HIVE center, should have been 72), y 27, height 14. States:
  `BLUE_FAR_CELL_UP` false, `RED_FAR_CELL_UP` false. So the "error" lines were meaningless (x in the wrong frame);
  the "camera at" lines are what count.
- Six tags from four clusters (red audience 34, 35; blue audience 38, 40; red far 31; blue far 44), both HIVEs,
  both cell positions (one up, one down each) agree on one spot: x 70.5 to 75.4 (mean 73.0), y 23.5 to 25.0
  (mean 24.3), height 11.9 to 18.2 (mean 14.1, typed 14), heading 86 to 92 deg (looking toward +y),
  camera tilt 26 to 31 deg (mean 28.4, measured 28). Tags 34 and 35, at the edge of the picture, are the worst.
- So the map is right for all four clusters on the rebuilt rig, to about 3 in. The only gap is y: 24.3 against
  the typed 27 (2.7 in.), likely the tape measure reference (lens against the point measured).

## Tag Map Check, second run on the rebuilt rig, 2026-11-08: piece 2 passes

- Run 1: typed (72, 15), height 14, both flags false, camera looking toward +y. Blue audience tags 38 to 41:
  errors 3.1, 2.2, 1.0, 2.2 in.; heights within 1.3 in.; tilt 26 to 29 deg.
- Run 2: typed (107, 126), height 14, both flags true (blue far cell up, red far cell up), camera looking
  toward the audience on the blue side. Tags 42, 43 (blue far) and 30 (red far): errors 1.7, 2.0, 3.1 in.;
  heights within 1.0 in.; tilt 28 to 29 deg.
- Together with the first run, every cluster and both positions of each HIVE have now been checked, to about
  1 to 3 in. In both runs the computed x is 1 to 3 in. larger than the typed one whatever way the camera
  points (red and blue alike), so it is most likely the tape measure reference, not the map.

## Localization Test at HQ, 2026-11-14

- Camera offset measured on the test robot: 6 in. forward, 0 in. left of the Pinpoint's reference point.
- Panels was down for a while: the test was run from the Driver Hub screen with the defaults in the code
  (start x 72, y 24, heading 90 deg). Causes to check if it happens again: the computer not on the robot's
  Wi-Fi, the page opened before the app was ready, no OpMode running, the app stuck (restart it).
- User: "it all works": the fake bump (14 in., above the 10 in. snap limit) recovers in a split second; the
  driving check, the unknown start, a moving HIVE and a covered camera were all fine. No numbers were kept.
