# BACKLOG

## To do at the robot (nothing here has run on the robot yet unless it says so)

- **Measure the TeleOp start spot** and set `TELEOP_START_ENABLED`, `TELEOP_START_X_IN`, `TELEOP_START_Y_IN` and
  `TELEOP_START_HEADING_DEG` in `TagLocalizer`'s settings (they are placeholders, option off). Added 2026-11-08.
- **On the final robot:** measure the Limelight's camera offsets from the robot's reference point (now 6 in. forward,
  0 left in `TagLocalizer`) and its real tilt and height and set `Hives.CAMERA_TILT_DEG` and
  `Hives.CAMERA_HEIGHT_IN` (now 28 deg and 14 in. on the test setup); tune the aim in `HiveAim` (turn strength,
  maximum and minimum power, tolerance, launcher offset). Added 2026-11-08.
- **Fill in the Limelight's USB port** in the "Wiring" table of TeamCode/README.md (still `?`). Added 2026-11-01.
- **On the final robot: retune the far-start Auto** (start pose, tracked point, pod offsets, speed limits, park
  spot, aim gains) and decide whether the tag position fix can be switched back on. Added 2026-11-22.

## Done

- 2026-11-22: piece 7, loop time with the camera on: the real cause of the slow loop and of the 15 to 20 deg park
  was the tag fix writing to the Pinpoint; fixed with a software shift. Loop Check tool, ask rate 50, park 50 in/s
  with the camera on, "works very well" (user). The 2026-11-22 entries at the end are its history.

- 2026-11-22: piece 6, far-start Auto, closed on the test robot: 8 in. ahead, wait for the HIVE, shoot
  (placeholder), park parallel in the LOADING ZONE; the Limelight stops before the park. The 2026-11-14 to 11-22
  entries below are its test history.

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

## From the coach, 2026-11-14 (planning, nothing decided yet)

- **Limelight on a servo.** Lets us change the camera angle: look for balls on the floor, or turn toward AprilTags
  that are not otherwise in view. Affects: `Hives.CAMERA_TILT_DEG` (becomes the servo's angle, not a constant),
  `TagLocalizer` camera offsets, the aim, `HardwareNames` + README wiring (a servo), a new subsystem (name to
  decide), and the Limelight pipeline for balls.
- **Plan several Autos, and when to get balls**: from the FLOWERS, or from the human player. Look first at the
  branch the co-coder started the week before (they are away): it may show the auto paths they planned.
- (Looked at on 2026-11-14, see docs/AUTO-PLANNING-NOTES.md: no match Auto paths found; two overlapping shooter efforts,
  `Outtake` and `ShootingTest`, to reconcile.)
- **Bring the co-coder's branch up to date with `master`** (to get what PR #8 adds) once it is merged; how to
  do it (rebase or merge) to be decided with the user and the co-coder, since it is their branch.

## 2026-11-14: from testing the far-start Auto on the test robot
- The robot shakes while waiting at the shoot spot (aim chasing flickering tags is the main suspect; turning is now
  only allowed once the target cell is up). To confirm with the log (`/sdcard/FIRST/biobuzz/farstart-*.csv`).
- It took 4 to 5 s to notice the HIVE was up before shooting (suspect: "aimed" needs 1.5 deg for 200 ms in a row; check the
  log). Tags may also be out of the camera's view when too close to the HIVE (shoot spot moved back to 12 in. from the wall).
- The FLOWER pickup and the second shot were removed from the far-start Auto for now (routes kept in
  docs/AUTO-PLANNING-NOTES.md: FLOWER (47, 129) red, curve controls (43.9, 126.7) and (43.9, 126.5)). Put them back after the first part works.

## 2026-11-15: far-start Auto, second and third tests (user measured the real positions)
- Real start (60.5, 135) vs believed (60, 133); real end about (8, 124), stopped by the wall, vs believed (14.6, 118.3).
  The tags' y at the shoot spot (126.3) matched the real y; their x was about 3 in. off. The park leg went about
  7 degrees clockwise of what the believed heading says: the same size as the 6 deg aim error. So the robot was
  probably placed about 7 deg crooked, and the Pinpoint heading (set to 270) did not know.
- Idea: after the camera aim succeeds, set the Pinpoint heading from the position and the HIVE direction, so a
  crooked start is fixed (the localizer already measures a heading disagreement, now in the log).
- The Limelight is held with duct tape and may be tilted a bit (roll/yaw): it biases the aim and the tag position.
  Re-measure on the final robot; `HiveAim.AIM_OFFSET_DEG` can absorb a constant yaw bias.
- The user does not want to be asked to change Panels values: put defaults in the code.

## 2026-11-15: far-start Auto, fourth test
- Target cell stayed MOVING for 8 s with its tags in view: the angle estimate flips between about 30 and 41-42 deg
  (limit was 30 +/- 12) while the height is steady at 37.6 in. Tolerance widened to 20 in `Hives`. Cause is probably
  tag pose ambiguity (camera almost face-on to the tags from this spot) and the taped camera. Watch the TeleOp Hive State Test.
- The robot believed it parked at (14.3, 118) in two runs, but physically hit the red wall (centre at most 8 in. from the
  wall): the Pinpoint seems to under-report x travel by about 10 to 15% on this test robot (last year's robot; the
  Pedro/Pinpoint constants may not be for it). To check: push it 48 in. along a tape measure in Pedro's Localization Test.
- The tag correction also applies while the robot drives and turns at the start of parking: consider freezing it
  during path following if jumps appear in the log.

## 2026-11-15: Odometry Check result
- Pushed by hand 48 in. forward and 48 in. left: the Pinpoint reads 100% both ways. So the straight-line scale is fine, and
  the park miss (believed x 14.4, physically at the wall, at most 8 in.) comes from something else. Suspects: the tag
  correction pushed x by about 3.5 in. away from the wall (the taped camera biases tag x); the pod offsets
  (`xPodOffset`, `yPodOffset` in `pedro/Constants.java`) if wrong would show when the robot turns 90 deg while driving
  (the park path does). To check: the spin test in Odometry Check (one full turn in place, position should not move).

## 2026-11-15: Pinpoint tracked point moved to the turning centre (placeholder until checked)
- Spin test (half turn in place, 3 takes, both directions): the Pinpoint's tracked point sat about 2 in. behind and 2.6 in.
  left of the robot's turning centre. Pod offsets changed in `pedro/Constants.java`: xPodOffset 0.765 -> 3.37,
  yPodOffset -4.43 -> -6.33 (old values kept here in case the signs are wrong). Verify with Odometry Check: a half turn
  in place should now read about 0 forward and left.
- Still unexplained: about 4 in. of the x miss at the red wall.

## 2026-11-15: park measured at the wall
- With the new pod offsets the Auto ran end to end (shot at 2.9 s, parked, done at 9.5 s) but ground against the wall for the whole
  5 s path timeout: believed pose (16.9, 118.3, 188 deg) with the front touching the wall (centre about 9 in. from the wall).
  So the Pinpoint's point is about 17 in. from the front edge when driving, but the robot's centre is about 9 in. from it:
  unexplained (hand pushes read 100%). Park target is now calibrated from this: edge gap 3 + 17 = 20 in. (red x = 20,
  blue x = 124); `docs/paths/far-start-*.pp.json` updated. Odometry Check after the offset change not yet reported.

## 2026-11-21: park after the new pod offsets
- Run with `ROBOT_HALF_LENGTH_IN = 17` and the new pod offsets: the robot believed its front edge was 3 in. from the wall but really stopped about 12 in. away (front-left corner inside the zone). The 17 was measured with the OLD offsets; the tracked point is now close to the centre (about 9 in. from the front). Set to 9 (red park x = 12, blue x = 132). Not tested yet.
- The half-turn spin test with the new offsets is still not reported (a 360 turn cannot show the offset; only a 180 can).

## 2026-11-21 (later): park with 9 in. ground on the wall
- With `ROBOT_HALF_LENGTH_IN = 9` (red x = 12) the robot stopped at believed x 15.4 against the wall and pushed until the timeout. Back to 17 (x = 20). Real park position changes by about 6 in. between runs (hand placement of the start, tag corrections): an idea is a more repeatable start spot, or ending the park when the robot stops moving.
- Half-turn spin test: old offsets (0.77 / -4.43): forward about 4, left about -5.2; offsets 3.37 / -6.33: forward -1.6, left 4.1 (179 deg turn). Interpolated to xPod 2.3, yPod -5.8. To check: a 180 turn should give about 0 and 0.

## 2026-11-21: measured start spot
- Robot is 16.5 in. wide x 17 in. long. Start: back against the far wall, left edge 2 in. from the centre line. Red centre (61.75, 135.5, 270), blue (82.25, 8.5, 90): set in `FarStartAuto.START` (was 60, 133). Assumes the Pinpoint's point is at the centre.
- After the 8 in. drive the Pinpoint says y = 125 but the tags pull it to 117 to 121: the tags say the robot is 5 to 7 in. closer to the HIVE. Suspect the taped Limelight (tilt, offsets). Not solved.

## 2026-11-21: tags moved the position wrong, switched off in the Auto
- Spin test with offsets 2.3 / -5.8 (179 deg turn): forward 0.1, left 1.3. Offsets are good.
- The last two Auto runs: believed end (18.6, 117.9, 180) = the target, but the robot really ended in the far corner past the park zone and not parallel to the wall. In both logs the tags put the robot at y 121 to 123 at the shoot spot where odometry says about 127.5 (5 in. closer to the HIVE), and the tag heading differs from the Pinpoint's by 3 to 4.5 deg. Tags are wrong (taped camera: tilt, yaw or offsets). Park uses fixed field coordinates, so a y error goes straight to the park.
- `FarStartAuto.USE_TAG_POSITION_FIX = false` (the camera still does up-detection and aim). Turn on again once the camera is mounted and measured. Not tested yet.

## 2026-11-21: Auto position is off while driving, pods suspected
- Two runs without tag correction: Pinpoint believed the end at x 17.6 / 18.2 (= target), real front edge 1 to 2 in. from the wall, so the centre about 10 in. from the wall: about 8 in. more than believed. Robot also ends not parallel and too far toward the far side. Shoot spot: believed y 122.6 to 123.5 where 127.5 was commanded (5 in. overshoot).
- User saw: the robot "flies" to the start spot and then reverses and jumps a bit. Suspect the odometry pods lose contact (or slip) under hard acceleration and braking; hand pushes read 100% and the spin test is good, so the Pinpoint itself is fine.
- Pedro 3 Foresight has `maxVelocityConstraint`, `maxAccelerationConstraint`, `maxDecelerationConstraint`, `maxPathSpeed` (not set in `pedro/Constants.java`, units not checked yet): a way to slow the Auto down.
- The target cell read MOVING for 7 s in one run (both red cells), UP in the next. The log now has hive_rule, far_info, aud_info to see why if it comes back.
- Test to do: Odometry Check, drive 48 in. by tape at full speed forward and left, compare with half speed.

## 2026-11-21: where the Pinpoint's tracked point is on the robot
- Pedro and the Pinpoint are accurate to 0.2 in. on straight 48 in. lines (Path Check A and B), and the heading is right under power (quarter turns 89.8 and 88.8 deg). The first Auto leg really moves a bit over a foot (8 in. commanded), as the Pinpoint says.
- Path Check Y (44 in. to the right while turning -88): screen forward 0, left -44.1, turned -88; the middle of the robot moved 47 to 48 in. and ended about 3 in. behind the mark. So the tracked point is about 3.4 in. BEHIND the middle: 12 in. from the front edge, 5.1 in. from the back edge. Before, the code assumed the middle.
- `FarStartAuto.START` y now 5.1 (blue; red 138.9) and the park target believed x = 17 red / 127 blue (gap 5 + 12). Not tested.
- Still unexplained: the real end of the park leg was far from the believed one (about 17 in. toward the far wall, heavy angle). Wait for the next run with the corrected start.

## 2026-11-21: video of the old-code run (IMG_4942.MOV at the repo root, delete when the branch is done)
- The run was the OLD code (START y 135.5, park x 20): the hot reload failed with "adb device offline". Logs farstart-1795293526030 and -1795293583192 in `biobuzz/`.
- Video: leg 1 moves the robot forward about a foot (matches the Pinpoint). In the park leg the robot goes toward the red wall AND back toward the far wall, and ends pressed against both walls near the far corner, turned a little, with the red tape of the LOADING ZONE in front of it, not under it. The Pinpoint believes y 118 (26 in. from the far wall) while it moves toward the far wall.
- Moved the project folder to /Users/raphaeldavy1/Documents/StudioProjects/biobuzz (2026-11-21).

## 2026-11-21: the real LOADING ZONE and the park target
- Measured by tape: the horizontal tape is 22.5 in. from the far wall, the vertical tape is 14.75 in. from the red wall and 24 in. long. So the zone is 14.75 in. deep (not 11) and covers red y = 97.5 to 121.5 (blue y = 22.5 to 46.5), centre 109.5. A 17 in. long robot with its front at the wall sticks out 2.25 in.
- Last two runs ended in the far corner, outside the zone (video IMG_4943.MOV): real y about 16 in. more than the Pinpoint's 118. Park y now = zone centre + 16 (settings `ZONE_CENTRE_Y_IN`, `PARK_Y_ERROR_IN`): red believed 93.5, blue 50.5. Not tested. The 16 in. is a guess from the video: measure the real end (far wall to the robot's side edge, red wall to the front edge) and correct. The cause of the y error is still unknown.

## 2026-11-21: the park path ended a third of the way
- Log farstart-1795295343911: the park path (from (60.2, 128.6) to (17, 93.5)) ended after 1.0 s at (49.2, 119.4) while the robot was still at 25 in/s; the robot coasted to (46.6, 116.4) and stopped. `FollowPathCommand` ends when `follower.isBusy()` turns false; Pedro's end conditions (defaults: within 2.5% of the path end for 100 ms, or heading/position/speed all tiny) should not be true there. Cause unknown.
- Workaround: the park is now up to three drives from wherever the robot is, skipped when within `PARK_TOLERANCE_IN` (2 in.). The log has a new `follower_busy` column to see when Pedro stops. Not tested.

## 2026-11-21: park with three tries (photo, no log pulled)
- With `PARK_Y_ERROR_IN = 16` and three tries at 40 in/s the robot ended in the square along y (far side about 9 in. inside the tape, middle about 5 in. toward the audience from the square's middle), front against the red wall, turned about 5 deg, and the user says it "bashes" into the zone. Likely: it reaches the wall before the Pinpoint expects and the retry pushes again.
- Changed: y error 10.5 (red believed y 99, blue 45), park speed 20 in/s, retry only if 6 in. or more short. Not tested.
- A 17 in. robot cannot fit in the 14.75 in. deep square: with the front at the wall, 2.25 in. stays out.

## 2026-11-21: two runs were the older code
- Logs farstart-1795295562801 and -1795295898314 have no `follower_busy` column: the robot still had the version with y error 16, one park drive at 40 in/s. The three tries, the slow park and 10.5 never ran (deploy did not go through).
- y: photo of the last run shows the robot centred along the wall with 16; the run before about 5 in. toward the audience. Now 14.
- x: the front hit the red wall when the Pinpoint read x 18.7 and 21.5, so the target x 17 was never reached and the last run pushed for the full 5 s path limit (the grinding). New `PARK_X_ERROR_IN = 8.5`, gap 3: red target x 23.5, blue 120.5. Not tested.

## 2026-11-21: the real cause looks like a heading error in the Auto
- Run farstart-1795296398465 (new code: three tries, 20 in/s, x error 8.5, y error 14): no wall hit, but the park crawled (2 to 4 in/s for 1.5 s) and was cut by the 5 s limit just as it arrived. Now park speed 30 and its own `PARK_TIMEOUT_MS = 7000`.
- The Pinpoint believed the end at (23.0, 95.9) facing 180.0; the photo shows the robot turned about 22 deg, front-left corner near the red wall, its far end past the zone's far tape. A heading error of about 20 deg explains the x and y errors seen all day.
- The heading was accurate in every test (quarter turns, Path Check Y), all of which start at heading 0; the Auto starts at 270. Pedro's PinpointLocalizer writes the start pose straight into the Pinpoint, so a 270 start should be fine. Not explained yet. Next: an overhead video of a full run to compare the real angle with the log over time.
- The x and y "errors" (8.5, 14) are probably symptoms of the heading error: remove them once the heading is fixed.

## 2026-11-21: overhead video IMG_4949.MOV: the heading is lost during the park turn
- Frames vs log (video about 0.8 s ahead): real heading 270 at the start (square), about 266 at the wait, then during the park turn the real heading runs ahead of the Pinpoint's; at the end real about 163 (front edge tilted 17 deg toward the far wall), Pinpoint 186.4, and it stays 186 at rest. So the Pinpoint misses about 20 deg of the park turn.
- Only the Auto turns toward 180 deg, where the heading wraps (+180 = -180); every test started at 0. Path Check now starts at heading 270 (`START_HEADING_DEG`) so Y repeats the Auto's park turn (270 to 182). Not tested.
- That run's park was cut at exactly 5 s (busy still true): the robot may not have had the 7 s park limit and 30 in/s yet.

## 2026-11-21: same moves without the camera are tracked right
- Path Check D-pad up (the Auto's moves, no Limelight, no aim, start at heading 270): Pinpoint forward 38.5, left -40.2, turned -88.9; the real end matches within 1 to 3 in. and the robot was parallel to the red wall. In the Auto the same moves end about 20 deg off.
- Pedro's PinpointLocalizer reads the heading straight from the Pinpoint every loop (no adding up), so the slower Auto loop (44 ms) cannot lose rotation by itself. Nothing in the Auto sets the pose after START.
- TEST SWITCH `FarStartAuto.USE_CAMERA = false` (no Limelight, no aim, a 3 s wait). MUST go back to true after the test.

## 2026-11-21: camera off = parallel and accurate
- `USE_CAMERA = false` run (log farstart-1795299239226, photo): the robot ended parallel; Pinpoint (22.5, 95.2) vs real about (23, 93.5). The first leg was 8.3 in. (with the camera on it overshoots to 11 or 12). So the 20 deg heading error comes from the camera or the aim step.
- Removed the patch offsets `PARK_X_ERROR_IN` / `PARK_Y_ERROR_IN`. Park target from real positions, as the user wants it ("basically touching the wall, far side of the zone"): front 1.5 in. from the red wall (red x 13.5, blue 130.5), side 1.5 in. inside the far tape (red y 111.75, blue 32.25). Valid with the camera off; with the camera on the heading error comes back until it is found. Not tested.
- Next: camera on but no aim step, to split the Limelight running from the aim.

## 2026-11-21: park works (camera off)
- Log farstart-1795299891151 + photo: park took 3.0 s at up to 32 in/s, ended at the target (13.6, 111.6, 180.6) and coasted 1.1 in. more (12.5); photo shows the robot parallel, front at the red wall, fully between the two tapes, far side about 2 in. inside the far tape. What the user asked for.
- Still `USE_CAMERA = false`. Next: find why the camera/aim step breaks the heading.
- 2026-11-21 test 2: `USE_CAMERA = true`, new `USE_AIM = false` (Limelight runs, the aim step is a plain 3 s wait). Both MUST go back to true after the tests.
- 2026-11-21 test 2 result (log farstart-1795300833894): with the Limelight running but no aim, the first leg overshot while still moving at 10 in/s and backed up 5 in. ("forward and back", user), and the park went wrong again: the robot rammed the wall (user stopped it at 8.4 s, believed x 15). So the Limelight running causes it, not the aim step. Suspect a slower loop; the log now has `loop_ms`. Next: one run camera on, one camera off, compare loop_ms.
- 2026-11-21 run A (log farstart-1795301560829, camera on, aim off, park 30 in/s): loop 13 to 20 ms (not slow: loop theory wrong). Photo: only slightly turned, about 5 to 7 in. toward the far wall, front reached the wall at believed x 15.4 (2 in. early) and pushed until the 7 s limit. The aim step in the 20 deg run was gentle (2 deg over a second, no shaking). Both 20 deg runs had the old 20 in/s crawling park. Back to normal (`USE_CAMERA` and `USE_AIM` true) to see if the 20 deg returns with the 30 in/s park; front gap 3.5 in. (red x 15.5, blue 128.5), park limit 4 s.
- 2026-11-21 normal run (camera and aim on, log farstart-1795302012173, photo): 15 to 18 deg turned again, past the far tape; Pinpoint (16.7, 111.9, 182.7). So the aim step is involved (aim off: a couple of degrees). The aim turns only 1.6 deg (gentle); Pedro's manual mode does not touch the localizer. One difference: aim off = Pedro holds the robot after the first leg; aim on = the aim step ended with zero power in manual mode (nothing holds during the shoot). Changed `AimAndWaitForCell.end()` to `follower.hold(pose)`. Not tested. Cause still not proven.
- 2026-11-22 run with the hold fix (log farstart-240423): still 15 to 20 deg off (user), so the hold was not the cause. New clue from the camera: while the robot sits still the camera and Pinpoint headings agree (about 1 deg); in the first second of the park the camera's heading drifts 14 deg away from the Pinpoint's (column heading_disagreement_deg, -1 to -15) while the Pinpoint says it turned only 3 deg. Same pattern in the earlier camera runs (-10, -14). That matches the 15 deg seen in the photos, so the Pinpoint seems to lose heading early in the park move. Nothing in the Auto writes to the Pinpoint (checked). Loop is 20 to 39 ms while tags are in view, 6 to 9 ms when none are. Camera-off runs ended the first leg cleanly (stopped at 130.8); camera-on runs overshoot to 126 to 128 while still moving.
- 2026-11-22 test: new `STOP_CAMERA_BEFORE_PARK = true` in FarStartAuto (the Limelight stops when the park starts; Vision.stop() now also clears the tags). If the park is right, keep it; if still 15 deg off, the harm happens earlier (the first-leg overshoot and lurch). Not tested.
- 2026-11-22 result (log farstart-594571, photo): with the camera stopped before the park, the robot parked PARALLEL, front at the wall. The camera running during the park was the cause of the 15 deg. Pinpoint (14.3, 111.7, 181.3) for target (15.5, 111.75); loop 6 to 17 ms in the park. Keep `STOP_CAMERA_BEFORE_PARK`. Still open: the first leg overshoots with the camera on (believed y 127.5 for 130.9), and the side position in the zone to confirm with the user.
- 2026-11-22 (user: "def note that"): the slow loop with the camera on (20 to 39 ms with tags in view, 6 to 9 ms without) will come back whenever the robot drives with the Limelight running: TeleOp, Autos 2 and 3. Find why and make it lighter. The Far Start Auto avoids it by stopping the camera before the park. The user is happy with where the robot ends in the zone (2026-11-22).
- 2026-11-22 research on the slow loop (no fix yet). How the SDK's Limelight3A works (read from the SDK 12 code): `start()` launches a background thread that asks the camera for `/results` over the network 100 times a second (default poll rate, settable with `setPollRateHz`, 10 ms) and turns the whole JSON answer into an LLResult on that thread; `getLatestResult()` only returns the last one, so our loop does not wait for the camera. Likely cause: that thread steals CPU from the main loop, and the JSON grows with every tag in view (each tag carries several 3D poses and corners), which fits "slow only with tags in view". Our own per-tag work (Hives, aim, the CSV log line, telemetry) adds to it. Nobody online reports this exact FTC case; FRC teams report Limelight JSON parsing causing loop overruns. Ideas, cheapest first: (1) measure which part of the loop costs what; (2) lower the poll rate to the camera's real frame rate (maybe 30 to 50 Hz); (3) in the Limelight pipeline, only keep the tag IDs we use and turn off outputs we do not read; (4) lighter logging and telemetry. Plan it as its own piece.
- 2026-11-22 Loop Check results (log loopcheck-1795375190319): the camera costs almost nothing (camera result 0.1 ms, HIVE check 0.9 ms, camera on/off changes the loop by about 1 ms). Driving a path is what costs: about 13 ms idle and 26 ms driving, with or without the camera. BUT the tool's numbers are doubled: SolversLib's `SubsystemBase` registers every subsystem with the scheduler in its constructor, so Loop Check ran each periodic() twice (once by the scheduler, once by hand). Real: one Pedro update about 5.5 ms idle, 12 ms following a path.
- 2026-11-22 **likely real cause of the 15 to 20 deg park error** (found from the above): because of that auto-registration, `TagLocalizer` ALWAYS ran in the far-start Auto (`USE_TAG_POSITION_FIX = false` only skipped the explicit register) and `TagLocalizer.ENABLED` is true. Every new camera picture it calls `drivetrain.setPose(x + dx, y + dy, current.heading())`, where the heading was read at the start of the loop: while the robot turns, each write puts back a slightly old heading, so the Pinpoint loses a bit of every turn while tags are in view. That matches: error only with the camera running, it grows in the first second of the park turn while tags are in view, and it stops when the camera is stopped. Also affects the TeleOp (localizer on while turning). Not fixed yet: ask the user.
- 2026-11-22 three fixes (not tested on the robot): (1) the tag fix no longer writes to the Pinpoint: new `pedro/ShiftedLocalizer` wraps it and adds an x/y shift in software (`Drivetrain.shiftPosition`), the heading always comes from the Pinpoint; affects the TeleOp too. (2) the far-start Auto unregisters the localizer when `USE_TAG_POSITION_FIX` is false (and the camera subsystems when `USE_CAMERA` is false). (3) Loop Check unregisters its subsystems so each periodic() runs once. Test: far-start Auto with `STOP_CAMERA_BEFORE_PARK = false` (set in code), should park parallel; Loop Check again for the real numbers.
- 2026-11-22 RESULT (logs farstart-1795376021398, loopcheck-1795376086871): with the three fixes and the camera ON during the park, the robot parked parallel and in the zone (user). The tag fix stayed off ("waiting for a picture"), the first leg stopped cleanly at 130.4 (no more overshoot), loop 7 to 19 ms with 5 tags in view. Real Loop Check numbers: 7.9 ms standing, 13.8 ms driving; Pedro + Pinpoint 5.6 / 11.9 ms, camera 0.1, HIVE check 0.9, screen 1.4; camera about 50 frames a second. So the slow loop and the 15 to 20 deg park both came from the tag fix writing to the Pinpoint every picture, not from the camera. Still to check: the tag fix itself (now a software shift) in "Localization Test".
- 2026-11-22 (user): Localization Test re-run with the software shift: the fake bump and the heading both work. Camera stays ON during the park (`STOP_CAMERA_BEFORE_PARK = false`, user). Park speed raised 30 -> 50 in/s for game speed (user: "very cramped up"). Not tested yet.
- 2026-11-22 (user): Limelight ask rate 100 -> 50 a second by default (`Vision.POLL_RATE_HZ`), matching the camera's ~50 pictures a second. Loop Check's D-pad up now cycles 50 / 100 / 30. Not tested yet.
