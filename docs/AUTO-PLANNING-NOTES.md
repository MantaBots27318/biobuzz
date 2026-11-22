# Auto planning notes (2026-11-14, nothing decided yet)

From a talk with the coach, then a read-only look at the other branches and the manual (TU07). Read with
`docs/AUTO-HANDOVER.md`.

## From the coach

- The Limelight could sit on a **servo**: change the camera angle to look for balls on the floor, or to turn toward
  AprilTags that are not otherwise in view.
- Plan **several Autos**, and decide when to get balls: from the **FLOWERS** or from the **human player**.
- Check the co-coder's branch for the auto paths they planned. Bring that branch up to date with `master`.

## The other branches (read-only look, nothing changed)

PR #8 (`feat/apriltags`) is merged into `master` (`c1eab3a`). Unmerged branches on GitHub:

| Branch | Author, date | What | Merge into the new master |
|---|---|---|---|
| `shooting-test` | antoinedutang, 2026-11-08, 1 commit | "Shooting Test" OpMode: two counter-rotating shooter motors, `HardwareNames.SHOOTER_LEFT/RIGHT` (`shooterLeft`, `shooterRight`), two README wiring rows. `POWER = 1.5` (above the 1.0 maximum: placeholder), run time 0.2 s | one conflict: the README wiring table (both sides added rows) |
| `feat/outtake-test` | EricJohnson123, 2026-10-22, 1 commit | `Outtake` subsystem: two goBILDA 1150 RPM motors held at `TARGET_RPM` by a velocity loop, `spinUp()` / `stop()` / `toggle()`, "Outtake Test" with the tuning procedure, `HardwareNames.OUTTAKE_LEFT/RIGHT`, wired into `Robot` | conflicts: README and `Robot.java` |
| `feat/auto-test` | EricJohnson123, 2026-10-24, 1 commit | "Path Test Auto": a Pedro path test (line, two curves, line), blue only, start (60, 133, 270), drivetrain only | clean |
| `movement-testing` | antoinedutang and others, Oct 2026, 5 commits | early chassis, intake and servo tests | 4 conflicts (MainTeleOp, HardwareNames, Robot, Intake): old |

- **No planned match Auto paths were found** in any branch. `MainAuto` on every branch still has the old
  placeholder poses. The only paths are the Path Test Auto's tuning route. The real plans may be on someone's
  computer, in the Pedro visualizer, or not written yet: ask the co-coder.
- **Two shooter efforts overlap:** `Outtake` (velocity-controlled subsystem, names `outtake*`) and `ShootingTest`
  (raw power, names `shooter*`). They would add two pairs of hardware names for the same two motors. One has to be
  chosen before anything is merged.

## What the manual says that matters for the Autos (TU07; the points table text is jumbled, check the PDF)

- AUTO lasts 30 s, then an 8 s transition, then 2 min of TELEOP. HIVE TIPS finished before TELEOP count in AUTO.
- Points (Table 10-2, section 10.5): HIVE TIP 20 (in AUTO and in TELEOP); LEAVE 3 (AUTO: stop touching the
  perimeter wall); PARK 5 (at least partly in our LOADING ZONE; in AUTO and at the end of TELEOP); POLLEN or
  NECTAR left in an up-facing CELL at the end 2 each; FLOWER: 2 per element in an owned FLOWER, 5 bottom NECTAR
  bonus, and FLOWER scoring cannot begin until one minute remains; GARDEN 1 each. Ranking points: 1 for enough
  LEAVE + PARK, and for 4 and 7 TIPS (at most events).
- A HIVE TIP = the HIVE moves from one stable state to the other, so only launching into the **up-facing** CELL
  tips it. Launching at the down-facing CELL while it tips can stop the tip.
- Staging: each ROBOT starts with 4 POLLEN preloaded (16 in all); 4 POLLEN in each of the 4 FLOWERS (16) and 4 in each
  GARDEN (8); 3 NECTAR in each up-facing CELL of its colour and 5 NECTAR in each ALLIANCE AREA (10).
- Supply sources: the **FLOWERS** (attached to the perimeter wall; POLLEN can be removed from the bottom through
  a retrieval opening about 3.55 in. tall and 3.57 in. deep; the top opening is 21.5 in. high), the **GARDENS**
  (POLLEN lines in the corner by the ALLIANCE AREA, not protected: either alliance can take them), and the
  **HUMAN PLAYER** (a SCORING ELEMENT manager; each time a HIVE is tipped the alliance may enter one of the five
  NECTAR from the ALLIANCE AREA, and all the rest with 60 s left). To read in the manual: what the HUMAN PLAYER is
  allowed to do exactly (section 10.3 and the G rules).
- Balls: POLLEN about 2.8 in. yellow, NECTAR about 3.6 in. red or blue.

## The three Autos, as the user described them (2026-11-14, a first idea, nothing decided)

Three Autos, each for both alliances (blue-side poses, one Auto for both colours, as in CLAUDE.md). The user's
idea: write the parts they share as **building blocks** (functions that return a command), so the Autos can be
reordered. Words below: "audience side" and "far side" are the two HIVES on our alliance's half of the field; the
text is written for red and is mirrored for blue. The field has central (180°) symmetry, so what is "far" for red
is "audience" for blue, in terms of which side starts with the NECTAR preloaded (user, 2026-11-14).

Which Auto we run depends on what the partner team can do.

| # | When | Start | Sequence |
|---|---|---|---|
| 1 | The partner shoots better than us | Far side, 4 POLLEN preloaded | Wait. The Limelight watches the partner's HIVE until it **tips** (a timeout protects us: if it never tips, go PARK for the points). When it tips: shoot our 4 POLLEN into the up CELL, tipping our HIVE. Turn around, take the POLLEN from the FLOWER on our side, turn around, shoot again, then PARK. |
| 2 | We are the better Auto, the partner has no Auto | Audience side, 3 NECTAR already in the up CELL, 4 POLLEN preloaded | Shoot our 4 POLLEN, the HIVE tips. Get POLLEN from the human player. Go to the other side, shoot into that HIVE. Then get balls from the FLOWER on that side. With time left: shoot again and PARK, or get more balls and PARK, or PARK before the second shot. Goal: start TELEOP with balls loaded. |
| 3 | The partner does what Auto 1 does | Audience side | Shoot our 4 POLLEN, the HIVE tips. Meanwhile get balls (FLOWER or human player: to decide). Come back and **wait until the partner tips the far HIVE**, then shoot. With time left: PARK, or get another load from the same source as the first and shoot. End with PARK, ideally with balls loaded for TELEOP. |

What this needs from what already exists, and what is new:

- Existing: `robot.hives.isCellUp(cluster)` ("is the HIVE tipped / which cell is up") for the "wait until it tips"
  steps; `robot.aim` to face the up CELL; `robot.localizer` to keep the position right while driving.
- New and not built: a launcher (the shooter choice, decision 2), a pickup from the FLOWER's bottom opening, the
  human player hand-off, a "wait for the HIVE to tip, with a timeout" step, an Auto chooser at INIT, the building
  blocks, and the Limelight being started in `MainAuto`.
- Open questions this raises: how long each step takes (30 s is short: Auto 2 may not fit everything); whether the
  FLOWER's POLLEN can be taken within the Auto and by what mechanism; which HIVE is "ours" for each start spot;
  whether the human player can hand POLLEN to the robot in AUTO (manual 10.3 and the G rules to read); whether the
  partner's tip can be seen from where we wait (tags in view); the camera tilt for the wait spot.
- The Autos must be written with blue coordinates and tested on the real field once the final robot exists.

## The co-coder's drawn paths (pasted by the user 2026-11-14; they were NOT in the repo, only in chat)

Four Pedro paths drawn in visualizer.pedropathing.com: `Auto1`, `Auto2`, `Auto3`, `MyAuto` (plain `OpMode`s in a
package `pedropathing`, using their own `Constants.create(hardwareMap)`, not our `Robot` / `CommandOpMode`).
Waypoints as drawn, (x, y, heading deg), "ctrl" = curve control point. **All have x below 72, so in our frame they
are on the red half** (our rule is to write blue and rotate: blue = (144 - x, 144 - y, heading + 180)).

- `Auto1`: start (56, 8, 90) -> (56, 32.4, 90) -> reverse curve (ctrl 7.3, 29.8) -> (7.7, 8.7) -> line (55.9, 32.3)
  -> reverse line (6.7, 46.8, -16) -> line (55.6, 31.8, -17) -> reverse curve (ctrl 21.5, 48.3) -> (12.1, 93.7, -78).
- `Auto2`: start (56.6, 9.9, 90) -> (56.3, 32.4, 90) -> curve (ctrl 35.6, 49.7) -> (6.9, 47.3, -175) -> curve
  (ctrl 22, 114), heading 185 to 270 -> (55.9, 106.1, 270) -> curve (ctrl 45.3, 123.3) -> (46.7, 134, 83) -> reverse
  line (57, 106) -> reverse line (12.7, 105.4, 1).
- `Auto3`: start (60, 133, 270) -> (60, 106, 270) -> curve (ctrl 43.9, 126.7) -> (47.4, 129.4, 270) -> curve
  (ctrl 43.9, 126.5) -> (59.7, 105.4, 270) -> reverse line (11, 118.3, -15).
- `MyAuto`: `Auto1` with changes: start (56, 8, 90) -> (56, 36, 90) -> curve (ctrl 60, 24.4) -> (6.3, 4.3, 269) ->
  reverse curve (ctrl 9.4, 35.3) -> (55.8, 36.3, -178.6) -> turn to 90, wait 1 s -> curve (ctrl 36.7, 51.1) ->
  (5.8, 47.2, -173) -> line (55.4, 36.5, 90) -> curve (ctrl 25.7, 40.7) -> (11.7, 94.4, -75).

Reading of them (mine, from the numbers and the pictures; the user has not confirmed what each point is):

- The shoot spots look like (56, 32) facing 90 (audience side, aiming at the audience CELL of the red HIVE) and
  (56, 106) facing 270 (far side, aiming at the far CELL). The trips go to the red wall at y about 5-9, 47, 94-105,
  118 and to the far wall near (47, 129-134): these are probably the FLOWERS, the GARDEN or the ALLIANCE AREA (to
  confirm with the co-coder and the field drawing).
- `Auto3` is the nearest to the user's Auto 1 and 3 (start on the far side), `Auto2` to the user's Auto 2 (shoot on
  the audience side, FLOWER, far side). `Auto1` / `MyAuto` stay on the audience side and make repeated trips to
  the wall and back to (56, 32).
- None has: a shot, the Limelight, waiting for a HIVE to tip, a PARK, time checks, or the alliance transform.
- Not checked: whether the robot centre can be 4-7 in from the wall (the FLOWERS stick out of the wall), the total
  time of each path (30 s), and collisions with the HIVE structure.

## Open decisions for the planning (ask the user one at a time, recommendation first)

1. How to bring the co-coder's branch up to date: the safe way is a new branch from `master` with their commit
   cherry-picked (their branch untouched); a rebase of their own branch needs a force-push, so only with them.
2. Which shooter: `Outtake` or `ShootingTest`, and one set of hardware names.
3. Which Autos to plan (candidates: preload shot and tip + LEAVE + PARK; plus a pickup from the FLOWER or GARDEN;
   plus the human player's NECTAR), starting spots, partner coordination.
4. The servo-mounted Limelight: what it is for (balls, other tags), and how it changes `Hives.CAMERA_TILT_DEG`,
   the camera offsets, aiming and position correction (all assume a fixed camera today).
