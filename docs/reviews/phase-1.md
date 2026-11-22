# Review: phase 1 (AprilTags, HIVE watching, aim, position fix, far-start Auto, loop time)

Date: 2026-11-22. Branch reviewed: `feat/loop-time` (pieces 1 to 7; pieces 1 to 5 are on `master` through PR #8).
Reviewer: Claude (Opus), fresh session.

## What was checked

- Read: `CLAUDE.md`, `PLAN.md`, `PROGRESS.md`, `BACKLOG.md`, `docs/NOTES.md`, `docs/AUTO-HANDOVER.md`, the 7 briefs.
- Code read in full: `FarStartAuto`, `AimAndWaitForCell`, `ShiftedLocalizer`, `Constants`, `Drivetrain`, `Robot`,
  `Vision`, `Hives`, `HiveAim`, `TagLocalizer`, `MainTeleOp`, `CsvLog`, `MatchState`, `ShootingTest`; the maths
  classes and the test OpModes skimmed for crashes and rule breaks.
- Build: `./gradlew :TeamCode:assembleDebug` and a forced recompile of TeamCode: **builds**. The only warnings are
  "source/target value 8 is obsolete", which come from the SDK's Java 8 setting (the FIRST module gets the same ones),
  not from our code.
- **Not run on the robot** (no robot here). Everything below about behaviour comes from reading the code and the
  test notes.

## Overall

The phase does what the plan decided. Every piece was tested on the test robot by the user, and the notes explain
each decision well. The big finding of piece 7 (the tag fix writing to the Pinpoint) is fixed in a clean way:
`ShiftedLocalizer` keeps the shift in software, the heading always comes from the Pinpoint, and the AutoTune tools
are not affected (they build their own localizer). The team rules are kept: no `sleep`, `follower.update()` only in
`Drivetrain`, Java 8, Pedro 3 API, settings at the top with `@Configurable`, the test OpModes no longer run
`periodic()` twice.

One thing must be settled before the phase is closed, and a few should be fixed before the next Autos.

## Must fix

1. **The camera tilt in the code does not match the notes.** `Hives.CAMERA_TILT_DEG` is **35**
   ([Hives.java:20](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/subsystems/Hives.java#L20)). It was
   changed from 28 to 35 in the piece 6 commit, but nothing says why. `BACKLOG.md`, `docs/NOTES.md` and
   `docs/AUTO-HANDOVER.md` all still say "28 deg, measured". This number is used by the HIVE up check and by the
   aim, so the wrong one changes when the robot shoots and where it points.
   *What to do:* the user says which is right. If 35 was a fix found while testing (for example for the
   "MOVING for 8 s" problem), write it in `NOTES.md` and `BACKLOG.md`. If not, put 28 back.

## Should fix

2. **Nothing makes sure the far-start Auto parks before 30 s.** The brief says "the whole Auto also has an
   overall limit, so PARK always happens before the 30 s end". Today the scoring part may last up to
   `SCORING_TIMEOUT_MS` = 20 s, and the park up to 3 tries x `PARK_TIMEOUT_MS` 4 s = 12 s: 32 s in the worst case.
   With today's other settings it is really about 15.5 + 12 = 27.5 s, so it is fine now, but raising one wait
   would break it without warning.
   ([FarStartAuto.java:54](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/auto/FarStartAuto.java#L54),
   [:192-205](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/auto/FarStartAuto.java#L192-L205))
   *What to do:* one "park by" time (for example start the park at the latest at 18 s, and cut the park at 29 s),
   checked against the match clock, not added-up limits. Autos 2 and 3 will need the same.

3. **The camera position on the robot is probably out of date.** `TagLocalizer.CAMERA_FORWARD_IN = 6`,
   `CAMERA_LEFT_IN = 0` were measured on 2026-11-14 from the Pinpoint's tracked point. That point moved when the
   pod offsets were changed (2026-11-15 and 11-21: about 1.5 in. forward and 1.4 in. sideways, and later found 3.4 in.
   behind the robot's middle). So the 6 / 0 now point to the wrong place, which is part of why the tags were off.
   ([TagLocalizer.java:27-28](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/subsystems/TagLocalizer.java#L27-L28))
   *What to do:* re-measure from the tracked point (12 in. behind the front edge, middle sideways) when the camera
   is mounted; add the reason to the backlog item that already exists.

4. **The two match Autos and the alliance default.**
   - "A. Auto" (`MainAuto`) is still in the Match list with old placeholder poses and no camera. At an event
     someone could pick it by mistake.
     *What to do:* move it to the Test group (or delete it) until it is a real Auto.
   - `FarStartAuto` starts on RED if nobody presses a button; `MainTeleOp` and `AimTest` start on BLUE. A forgotten
     button press sends the robot to the wrong side with no warning.
     *What to do:* no default (refuse to start until a colour is chosen), or at least the same default everywhere
     and a clear "NOT CHOSEN" line.

5. **Pieces 6 and 7 are not on `master` yet.** They are pushed on `feat/loop-time`, but there is no pull request.
   The co-coder and the next branches do not have the fix for the turned park.
   *What to do:* open a PR to `MantaBots27318/biobuzz` when the user is ready (and do the PlayStation-names piece
   after, as planned).

## Could improve

6. **The tag fix runs in the TeleOp, but is off in the Auto.** In the Auto it was turned off because the taped
   camera moved the position 5 in. wrong. In the TeleOp `TagLocalizer.ENABLED` is still on, with the same camera.
   Nothing in the TeleOp uses the position yet, so nothing breaks today, but the pose handed to later code would
   be off. One switch for both, off until the camera is mounted, is simpler.
7. **The far-start Auto builds its own subsystems** instead of using `Robot` (because the test robot has no intake),
   and calls `setBulkReading(hardwareMap, ...)` itself; `LoopCheck` calls `hardwareMap.getAll(...)`. These bend rule 1
   a little. Fine on the test robot; on the final robot the Auto should go back to `new Robot(hardwareMap)`.
8. **On time-out, the wait step shoots even if not aimed** when the cell is up
   ([AimAndWaitForCell.java:107](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/auto/AimAndWaitForCell.java#L107)).
   It is written down in the class comment, but it is not a dated decision in `PLAN.md`. Ask the user once.
9. **The speed limit is shared.** The Auto sets the Pedro speed limit on the shared Foresight settings and gives it back
   in `end()`. If the app crashes in the Auto, the next OpMode stays slow until the app restarts. Rare; good to know.
10. **Keep the maths checks.** `HiveLogic`, `AimLogic`, `LocalizerLogic` and `TagMap` have no hardware, and each was
    checked with a scratch calculation that was then thrown away. Turning those into small automatic tests would
    catch a later change that breaks them, without the robot.
11. **Tidy the docs.** `PROGRESS.md` is still titled "branch `feat/apriltags`". `docs/AUTO-HANDOVER.md` says
    `TagLocalizer` "nudges the Pinpoint" (it now shifts in software) and tilt 28. The `NOTES.md` section
    "2026-11-21/22" still says "suspect the camera first when driving gets worse"; the section below it shows that
    was wrong. The `BACKLOG.md` line about deleting `IMG_4942.MOV` can go (the file is no longer there).

## Outside this phase (seen in passing)

- `ShootingTest` (the co-coder's, not part of phase 1): `POWER = 1.5` is above the maximum of 1 (the SDK cuts it to
  1), and it calls `hardwareMap.get` in the OpMode. Fine for a bench test; the real shooter should be a subsystem.
- The README wiring table still has `?` for the Limelight USB port and the shooter ports (already in the backlog for
  the Limelight).

## Safety and privacy

No secrets in the code. The only data written is the CSV log on the Control Hub (`/sdcard/FIRST/biobuzz/`), and the
pulled folder is git-ignored. Nothing leaves the robot.
