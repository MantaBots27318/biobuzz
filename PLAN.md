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

1. **Limelight reads tags:** a `Vision` subsystem that reads the tags and shows them in telemetry.
   Test: point it at the practice HIVE, check the IDs and distances.
2. **Tag positions:** both positions of every cluster, from the CAD model, checked on the practice HIVE.
3. **HIVE state:** each HIVE tracked as position A, position B or tipping. Test: tip the practice HIVE
   by hand and watch the telemetry.
4. **Position correction:** mix Pinpoint and tags. Test: push the robot off course, watch it correct.
5. **TeleOp aim:** hold RIGHT_BUMPER to face our HIVE.
6. **Auto:** aim step, "wait until HIVE down" step with time limit, then park if it never tips.

## Open questions

- Confirm the "telling when a HIVE has finished tipping" idea above once we see real readings.
- Where and at what angle to mount the Limelight (it must look up at the tags).

## Set aside

(nothing yet)
