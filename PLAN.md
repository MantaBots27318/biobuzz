# PLAN: AprilTags, hive aiming and localization (branch `feat/apriltags`)

## Goal

In TeleOp and in the Auto, the robot uses the AprilTags on the HIVES to:
1. **Aim:** turn to face our HIVE, at the press of a button (TeleOp) or as a step of the Auto.
2. **Localize:** correct the Pinpoint position with the AprilTags, so the robot always knows where it is.

## What the manual says (TU07, section 9.6 and 9.9)

Source: [TeamCode/docs/manual/](TeamCode/docs/manual/) (on `master` once PR #7 is merged).

- The HIVE frame is fixed in the center of the field. Each HIVE (one red, one blue) has 2 CELLS and
  tips on a pivot 43.95 in. above the tiles, between **2 stable positions** (one CELL up).
- Each CELL has an AprilTag Cluster (4 tags, 36h11 family, 3.25 in.) on its **bottom face, facing
  down** toward the tiles. So the tags are angled and not visible from everywhere.
- Tag IDs: red 30–33 (far side) and 34–37 (audience side); blue 38–41 (audience side) and 42–45
  (far side).
- Exact tag positions: the official CAD model and the Reference Holes on the sticker.

## Where the tag positions come from (checked 2026-11-01)

- **Inside a cluster: known.** The FTC SDK 12.0 (`AprilTagGameDatabase.getBioBuzzTagLibrary()`) gives
  each tag's offset from the cluster's center, in inches: x = -6.50, -2.75, +2.75, +6.50 (first ID to
  last), y = +7.1874, z = -5.622, tag size 3.25.
- **Clusters on the field: not given.** In the same file, all 4 clusters are at (0, 0, 0) with no
  rotation. For DECODE (last season) FIRST gave real field positions. Here they left them out,
  probably because the HIVES tip. We have to find them ourselves.
- A web search found no published BIOBUZZ tag positions or Limelight field map. Discord was not
  checked (Claude can't read it).
- **Plan:** take both positions of each cluster from the official CAD model (Playing Field
  Resources page), then check them on our practice HIVE.
- Legal cameras: Limelight 3A, UVC webcams. Not stereo cameras.

## Decisions

- **2026-11-01: Camera = Limelight 3A.**
- **2026-11-01: A HIVE has 2 stable positions plus a tipping moment in between.** It is not a
  simple true/false. While a HIVE is tipping, its tags are moving, so their readings must not be used
  to correct the robot's position.
- **2026-11-01: This work lives on the branch `feat/apriltags`**, started from `master`.
- **2026-11-01: Aiming uses both.** The robot turns toward the HIVE using its position on the field
  (works even with no tag in view), then fine-tunes with the tags when the camera sees them.
- **2026-11-01: Use every tag in view together, not one tag at a time.** All visible tags go into one
  calculation of the robot's position. Note: the 4 tags of a cluster sit close together on one sticker,
  so they help less than tags spread across the field would. They mostly steady the reading and the
  angle. Seeing both of our HIVE's clusters at once (or a tag far away) helps more.

- **2026-11-01: Aim point = the middle of the CELL of our HIVE that is facing up.** The robot works out
  which CELL is up by itself.
- **2026-11-01: We have a practice HIVE** to measure tag positions and check the Limelight's precision.
- **2026-11-01: Limelight mounted fixed, pointing the same way as the launcher, tilted up**, so the tags
  are in view when the robot faces the HIVE to shoot. Exact height and angle to choose with the build
  team.
- **2026-11-01: TeleOp aim = hold RIGHT_BUMPER on gamepad 1 (driver).** While held, the robot turns to
  face the HIVE by itself and stays aimed; the driver keeps moving with the left stick. On release,
  the right stick controls turning again.

## Idea to confirm: telling when a HIVE has finished tipping

A tag reading gives the tag's position and angle. Compare it to the 2 known stable positions of that
tag (from the CAD model or measured on the field):
- **Stable:** the reading matches one of the 2 positions (within a margin) **and** has stayed the same
  for a short time (several readings in a row). Only then is the HIVE state updated and the tags
  used to correct the robot's position.
- **Tipping:** the reading matches neither position, or is still changing from one reading to the next.
  Ignore it for localization; keep aiming from the last known state.
- **No tag in view:** keep the last known state.

The 2 positions are far apart (the CELLS are 18.8 in. apart), so telling one from the other should be
easy even with an imperfect angle. The real precision of the Limelight must be measured on our
robot. Margin and "how long is stable" are settings to tune in Panels.

- **2026-11-01: The Auto uses tags like the TeleOp.** Position correction is on all the time; before each
  shot, an "aim at the HIVE" step holds until the robot is lined up, then it shoots.
- **2026-11-01: The Auto can wait on the HIVE.** Example: wait for our partner on the other side to tip
  our HIVE, then shoot the balls we already hold. **Never shoot before the HIVE is fully down.** The
  HIVE has padding and does not bounce back up, so "stable" can be confirmed quickly (still a short
  stable time, as a setting). During the wait the camera must keep the HIVE's tags in view.
- **2026-11-01: If the HIVE does not tip in time: keep the balls and go PARK.** Tags not in view count
  as "not tipped yet" (never shoot without seeing the HIVE down). AUTO PARK = at least partly in our
  LOADING ZONE at the end of AUTO, 5 points (manual 10.5), so the time limit must leave enough time to
  drive there.
- **2026-11-01: Time limit = a simple setting**, like the existing `MainAuto.SCORING_TIMEOUT_MS` (give up
  and park after it). The Auto's timing is predictable, so no need for anything smarter.
- **2026-11-01: Auto structure = our current list of steps** (`SequentialCommandGroup` +
  `FollowPathCommand`, CLAUDE.md section 8). "Wait until the HIVE is down" is one more step in it.

- **2026-11-01: Our code computes the position from each tag (not the Limelight's MegaTag).** The
  Limelight only reports where each tag is relative to the camera; our code picks the right field
  position for that tag from the HIVE's current state, and uses the same readings to spot tipping.
  (Chosen as "probably": can be revisited once tested on the practice HIVE.)
- **2026-11-01: Both HIVES count for localization, not just ours.** The opponent's HIVE can be in view
  (it may be facing the opposite way from ours). Each HIVE's state (2 positions + tipping) is tracked
  on its own; tags from any HIVE that is stable can correct our position. Aiming still only targets
  our HIVE. The opponent's HIVE may tip at any moment (they shoot into it), so the "stable" check
  matters even more for it. How much it helps is to be seen in testing.

- **2026-11-01: Piece 1 is tested with a new test OpMode "Vision Test"** (group "Test"). It shows every
  tag in view (ID, distance, angle) and moves nothing.

- **2026-11-01: The Limelight is already set up** (plugged in, running an AprilTag pipeline). But the
  pipeline is from last season, so its tag size and family must be checked for BIOBUZZ (36h11, 3.25 in.)
  before trusting any distance. Wrong size = wrong distances.
- **2026-11-01: 45º is the best angle for seeing the tags** (found by the team while testing; to be
  confirmed whether this is the camera's tilt). Use it as the starting point for the mounting.

- **2026-11-01: The Limelight's configuration name is `limelight`.**
- **2026-11-01: "Vision Test" creates only `Vision`, not the whole `Robot`**, so it runs on any robot that
  has a `limelight` in its configuration (tested on last year's robot).
- **2026-11-01: The Limelight pipeline's Marker Size must be 82.55 mm** (3.25 in.). Last year's 165.1 mm
  doubled every distance.

- **2026-11-01: Piece 2 takes the tag positions from the official CAD only, for now.** The team
  (the user) reads the numbers off the CAD; Claude can't open it. The check on the practice HIVE with a
  tape measure is postponed (not dropped): do it once the numbers are in, or in piece 3.
  The CAD has a +/- 1 in. tolerance (manual 9.1), so the real field may differ by about that much.

- **2026-11-01 (finding): Pedro fixes the grid, not the physical corner.** Pedro docs: field 144 x 144 in.,
  (0, 0) at the bottom-left, +x right, +y up, heading 0 = facing right, not tied to an alliance. Which
  real corner that is stays our choice. Reference: Cuttlefish (6165) BIOBUZZ repo, `BioBuzzField.java`,
  uses (0, 0) at the corner of the audience wall and the red wall (red half x < 72, blue half x > 72,
  audience side = small y). The manual (section 9, p. 67 area) says the red ALLIANCE AREA is on the left
  as seen from the primary audience. Their file also lists HIVE dimensions from the official CAD
  (HIVE rails 22.75 / 24.733 in. from the center, pivot 12.75 in. from the center line, CELL centre
  13.37 in. from the pivot in plan, bar tilted 30 deg), useful to cross-check piece 2. Our DECODE repo
  (MantaBots27318/decode) has no team code, so nothing to reuse from it.
- **2026-11-01: Field coordinates (Pedro) = (0, 0) at the corner where the audience wall meets the red
  wall**, seen from the primary audience: **+x toward the blue wall, +y away from the audience, heading
  0 = facing the blue wall**, counter-clockwise positive, heights up from the tiles. Red half x < 72,
  blue half x > 72. Consequence to check on the field: a red robot driving away from its drivers faces
  0 deg, so `Drivetrain.RED_DRIVER_FORWARD_DEG = 0` (its current value) and blue = 180.

- **2026-11-01: First CAD reading (Onshape Measure, user's screenshot).** Face "am-5888-blue2: Blue ..."
  (the sticker face on the blue HIVE's far-side cell, HIVE state to confirm) measured against the corner
  vertex of the field (audience/red corner, where our (0, 0) is). Closest to farthest point of the face,
  in inches from the corner: x 74.859 to 91.684, y 81.278 to 85.504, z 48.439 to 50.879. Rough center:
  (83.3, 83.4, 49.7). Still missing: the tilt (angle to the tiles), the other 3 cases, and whether z = 0
  is the top of the tiles.
- **2026-11-01: Second CAD reading (sticker face vs. a tile's top face).** Angle = 30.000 deg. Lowest
  point of the face is 48.412 in. above the tile top (the corner vertex was 0.03 in. lower, so z = 0 is
  the tile top for our purposes). Highest point 50.896 in. Together with the first reading: the lowest
  edge of the face is the one nearer the field center (y 81.3, z 48.4), the highest the far edge
  (y 85.5, z 50.9), so the face rises toward the far wall at 30 deg. Facing direction (down, toward the
  tiles) = (0, +0.5, -0.866); sticker "up" (bottom edge toward the center) = (0, +0.866, +0.5).
  This is the blue far-side cluster (tags 42-45) with its CELL up.
  Proposal (not yet confirmed): get the other 3 cases by geometry (pivot axis 43.95 in. high at y = 72,
  the other CELL is the mirror image across the pivot, the other stable position is the bar tilted the
  other way by 30 deg), then check them with the "Tag Map Check" on the practice HIVE.
- **2026-11-01: Third CAD reading (pivot part "am-5881" vs the corner tile, from the top view).**
  Pivot axis height = 43.950 in. above the tile top (matches the manual). Position of the pivot part
  from the corner: x about 83.3 to 83.7, y about 70.7 to 70.8 (the Measure tool gives the closest and
  farthest points, so +/- 0.2 in.). So the blue HIVE's pivot is at about x 83.5, y 70.8 (not exactly
  the field center line y = 72). The sticker's x (83.3) agrees with the pivot's x. Use these in place
  of the "y = 72" assumption above.
  **Correction (same day, user): the pivot is supposed to be at y = 72.** The manual says the HIVE
  structure is in the center of the FIELD, and the FIELD is the area inside the perimeter walls. All
  three CAD readings came out 1.2 to 1.5 in. too small in x and y (pivot y 70.8, sticker x 83.3 against
  about 84.75 expected), so the reference vertex we measured from is probably about 1.2 in. inside the
  true corner. Fix: use readings relative to the pivot (the offset cancels out) and put the pivot at
  (84.75, 72, 43.95) for the blue HIVE. Relative to the pivot, the measured sticker center is
  (-0.2, +12.6, +5.7), which gives about (84.5, 84.6, 49.7) on the field. The x = 84.75 comes from the
  Cuttlefish repo (HIVE pivots 12.75 in. each side of the center line), not from the manual: to
  confirm by measuring the pivot from the inside corner of the perimeter walls.
- **2026-11-01: Fourth CAD reading (pivot part vs a vertex of the left side wall, "am-2499-Side").**
  Pivot part is 82.911 to 83.773 in. from the vertex in x (so about 83.3 to 83.8), 3.5 in. from it in y
  (the vertex's own y is unknown), and 43.95 in. high (axis height, matches the manual again).
  This agrees with the first pivot reading in x (83.3 to 83.7) from a different reference, so there is
  NO x offset between our two reference vertices, and the "about 1.2 in. shifted" explanation above is
  withdrawn for x. The blue pivot's x is therefore about 83.4 (11.4 in. from the center line), not the
  84.75 taken from the Cuttlefish repo. y is still unsettled (70.8 from the first reading, 72 from the
  manual): next step is to measure the pivot against the field's center marker.
- **2026-11-01: Fifth CAD reading (pivot part vs the field's center marker). This settles the pivot.**
  Difference in y = 0.000, in x = 12.326 to 13.188 (about 12.76), height 43.950. So the blue pivot is
  12.76 in. to the blue side of the center line and exactly on it in y: **blue pivot = (84.75, 72, 43.95)**,
  matching the Cuttlefish repo. **The "about 1.2 in. shifted" explanation was right after all**; I withdrew
  it too early because the two wall/tile vertices we measured from sit on the same inner plane and
  share the same offset (the readings from them were all 1.2 to 1.5 in. too small). Sticker center
  from the pivot: (-0.2, +12.6, +5.7), so about **(84.55, 84.6, 49.7)** on the field.

- **2026-11-01: Piece 2 includes a "Tag Map Check" test OpMode** (group "Test"). You type where the
  camera is (position, direction, tilt) in Panels and say which CELL of each HIVE is up; for each tag
  the Limelight sees, it shows the measured distance and angles next to the ones the list predicts.
  It does not compute the robot's position (that is piece 4).
- **2026-11-01: The other 3 cases are computed by geometry** from the one measured sticker and the
  pivot (no more CAD measuring needed unless the check shows a mismatch).
- **2026-11-01: A HIVE state is "which of its 2 CELLS is up"** (FAR or AUDIENCE, seen from the audience).

- **2026-11-01: The map works with flat distances (as seen from above).** The Limelight gives the
  straight-line distance (it includes the height difference). The robot drives on the floor.
- **2026-11-01 (user): no camera tilt setting.** The camera tilt (45 deg is only a concept while the
  Limelight is held by hand) is not needed: the tags' own tilt is known (30 deg, from the CAD and the
  HIVE state), so the camera's direction can be worked out from how each tag appears to it. The Limelight
  also reports each tag's angle relative to the camera. So per tag: the camera's position on the field
  (x, y, height) is computed from the tag's known pose on the map and the reading, and the flat
  distance comes out of that. Risk to watch: a single tag's angle reading can be noisy (especially far
  away); the Tag Map Check shows how good it is. Replaces the earlier "tilt setting" idea.

- **2026-11-01: The user is away from HQ for about a week (until around 2026-11-08), so nothing can be
  tested on the robot.** Keep building what can be checked by compiling and by calculation; test and
  tune everything together when back. Tuning of the tag map is for later.
- **2026-11-01 (provisional, to confirm): the short way for aiming.** Aim with the Pinpoint position and the
  known HIVE center, then fine-tune with the Limelight's left-right angle to the tag (no map needed).
  "HIVE is down" = tag seen and low enough (one number to calibrate on the practice HIVE). The tag-based
  position correction (piece 4) is postponed. Raised by the user: "we did not do this in past years".

- **2026-11-02 (user correction): we can only shoot into a HIVE when the CELL we aim at is UP.** The earlier
  wording "never shoot before the HIVE is fully down" was backwards or at least unclear (down = the
  other CELL). The rule is: **shoot only when our target CELL is up and has stopped moving** (stable for a
  short time, a setting). The Auto's wait step waits for that, then shoots; if it never happens before the
  time limit, keep the balls and park. Piece 3 is therefore "HIVE is up detection", not "down".
- **2026-11-02: Piece 3 uses the short way, no map (confirmed by the user).** Each tag's height is worked out
  from the camera reading and the camera's mounting height and tilt (measured once at HQ); a CELL whose tags
  are high (about 50 in. against about 36 in. when down) is up.
- **2026-11-02 (user): the tag's angle counts too.** A CELL is "up and ready" only when the tags are at the right
  height AND at the right 30 deg tilt. Note: the tilt is 30 deg in BOTH resting positions (only the lean
  direction flips), so the height says up or down, and the angle says "stopped moving" (halfway through a
  tip the bar is level and the tags face straight down, so the angle is far from 30 deg). A short hold time
  (a setting) is kept on top.

- **2026-11-08 (HQ test, see docs/NOTES.md): the tag map and angle conventions work** (red tags within
  1 to 4 in.). On the practice rig the blue IDs 38-41 / 42-45 sit on the opposite cells to the manual.
  **Checked against Figure 9-17 (2026-11-08): the figure agrees with the manual's text, so the map is
  right for the real field and the practice rig is the odd one.** The user is rebuilding the rig's blue
  HIVE to match; no switch in the code (one was added and removed the same day).
- **2026-11-08 (user): the camera sees both HIVEs, and both cells of each, from under the structure.**
  Proposal for piece 3: when both cells of a HIVE are in view, the higher one is up (no mount
  measurement needed); the absolute height threshold stays as the fallback when only one is in view.

- **2026-11-08 (user): TeleOp aim (piece 5) uses the Limelight only**, because it is locked in place on the
  robot. The robot turns until the tags' left-right angle is 0. The Pinpoint position is NOT used for aiming
  (a Pinpoint exists on the robot; the earlier "use both" decision of 2026-11-01 is replaced for now: the
  position-based turn is set aside, not deleted).

- **2026-11-08: Piece 5 is tested with a new "Aim Test" OpMode (drive motors, Pinpoint, Limelight only), plus the
  RIGHT_BUMPER binding in "A. TeleOp".** The test robot has all of those in its configuration.
- **2026-11-08: Aim target rule.** The up cell of our HIVE (from `Hives`); if unknown, the last known up cell; if
  that is unknown too, any of our HIVE's tags in view; if no tag is in view the robot does not turn by itself
  (the right stick still turns it). Our HIVE comes from the alliance chosen at INIT. The left-right angle is the
  angle seen from above, corrected for the camera's tilt (same tilt setting as `Hives`).

- **2026-11-08 (user): wait for piece 6 (the Auto) until the final robot exists.** The test setup is not the final
  robot, so the Auto's paths, shooting spots and timing cannot be made yet. Also waiting for the final robot:
  the real camera tilt and height (`Hives` settings), the aim gains, and the "aim when the target is out of
  view" idea.

- **2026-11-08 (user): do piece 4 now** (position correction); it is no longer postponed. The tag map is confirmed
  and the HIVE up detection works, which is what it needs.
- **2026-11-08 (user, "I do not really know, do A"): piece 4 corrects x and y only, not the heading.** The Pinpoint's
  heading is reliable; a tag's angle is noisier. The test shows the heading disagreement for information.
- **2026-11-08: Piece 4 defaults (Claude's, to veto).** Only tags whose HIVE is settled (UP or DOWN) are used; a
  reading is thrown out when the camera height it implies is too far from the real mounting height; the median
  over the tags in view is used. The correction is a delta: measured position minus the Pinpoint position
  from the moment of the picture (camera delay), added to the current position with a gain. The gain is higher
  for close tags, several tags and a slow robot. Several consecutive readings that agree with each other but
  not with the Pinpoint make it jump to the tag position (bump). Until the pose is known (TeleOp without an
  Auto before it), the first good reading sets the position. The camera's place on the robot (forward, left)
  is a setting. A "Localization Test" OpMode has a fake-bump button. Switch `ENABLED` in Panels.
- **2026-11-08 (user): the TeleOp can start from a set position, as an option** (`TELEOP_START_ENABLED`, with x, y
  and heading in blue-side coordinates, turned for red). Order at START: the Auto's pose, else the set spot if
  enabled, else the first good tag reading sets the position. More accurate than waiting for a tag.
- **2026-11-08: the Pinpoint+Limelight pairing idea is covered by the gates** (height, agreement with the
  Pinpoint, snap logic); no separate quadrant check for now.

## Mixing Pinpoint and tags (decided 2026-11-01)

The Pinpoint drifts slowly and can jump after a bump (wheel slip). Tags don't drift. So:
- **Weighted average:** each good tag reading pulls the position part of the way toward what the tag
  says. The pull is stronger when the reading is good (close, several tags, robot not moving fast),
  weaker when it is poor. Strength = settings tuned in Panels.
- **After a bump:** if several tag readings in a row all disagree a lot with the Pinpoint and agree
  with each other, jump straight to the tag position.
- **Ignore** readings while a HIVE is tipping, too far away, or while the robot turns fast.
- **Camera delay:** a tag reading describes where the robot was when the picture was taken (a few
  tens of ms ago). Compare it with the Pinpoint position from that same moment, not the current one.
- Not a PID: a PID controls a motor toward a target. Here we are mixing two measurements.

## Pieces (order confirmed 2026-11-01)

(2026-11-08: piece 2 done: "Tag Map Check" on the rebuilt rig gives every cluster, both HIVE positions, within 1 to 3 in. The tag map is now confirmed for the real field layout.)
(2026-11-08: piece 4 built and committed as a placeholder: checked by simulation only, never run on the robot; gains and camera offsets get tuned at HQ.)
(2026-11-08: piece 5 done: "Aim Test" works on the test robot. See BACKLOG for the idea of aiming when the target tags are out of view.)
(2026-11-08: piece 3 done: tested on the practice rig, "pretty good"; both HIVEs read correctly from the blue side. Camera tilt and height are placeholders until the Limelight is mounted.)

1. **Limelight reads tags:** a `Vision` subsystem that reads the tags and shows them in telemetry.
   Test: point it at the practice HIVE, check the IDs and distances.
   **Done 2026-11-01**, tested on last year's robot: IDs and distances right once the Limelight's
   Marker Size was set to 82.55 mm (3.25 in.).
2. **Tag positions:** both positions of every cluster, from the CAD model, checked on the practice HIVE.
3. **HIVE is up detection (changed 2026-11-02):** for each cell, UP / DOWN / MOVING / NOT SEEN, from the
   tags' height and angle (no map). Test: tip the practice HIVE by hand and watch the telemetry.
4. **Position correction:** mix Pinpoint and tags. Postponed 2026-11-01, **reinstated 2026-11-08** (x and y only).
5. **TeleOp aim:** hold RIGHT_BUMPER to face our HIVE (camera only, decided 2026-11-08).
6. **Auto:** aim step, "wait until HIVE down" step with time limit, then park if it never tips.

## Open questions

- Confirm the "telling when a HIVE has finished tipping" idea above once we see real readings.
- Where to mount the Limelight (it must look up at the tags). Angle: start at 45º (see Decisions).

## Set aside

- **Position correction from tags (piece 4), postponed 2026-11-01, REINSTATED 2026-11-08 (see Decisions).** Why: it needs the full tag map and a
  lot of tuning, and the user is away from the robot for a week. The tag map (piece 2) is built and kept
  (`vision/TagMap.java`, "Tag Map Check") but untested. It may come back if the Pinpoint drifts too much.
- **Position-based aiming (turn toward the HIVE using the Pinpoint position), set aside 2026-11-08.** Why: the
  Limelight is locked in place on the robot, so aiming with it is simpler and needs no start pose. It may
  come back to help when no tag is in view.
