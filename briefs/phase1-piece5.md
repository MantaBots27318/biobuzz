# Piece 5: TeleOp aim

## Goal

While the driver holds RIGHT_BUMPER (gamepad 1), the robot turns by itself until it faces the cell of our HIVE
that is up, and the left stick keeps moving it. When the bumper is released, the right stick turns it again.
It uses the Limelight only (it is locked in place on the robot): the robot turns until the left-right angle to
our target cell's tags is 0.

## In scope

- `vision/AimLogic.java` (no hardware, so it can be checked on its own): the angle seen from above to a tag,
  corrected for the camera's tilt; the median over a cluster's tags; the turn power from an angle error.
- `subsystems/HiveAim.java` (a subsystem, registered in `Robot` after `Hives`): picks the target tags, works
  out the angle error and the turn power each loop. It does not move the robot: the OpMode passes
  `turnPower()` to `drivetrain.driveFieldCentric(...)`.
- Target rule: the up cell of our HIVE (`Hives.getSideUp`); if unknown, the last known up cell
  (`getLastKnownSideUp`); if unknown too, any tag of our HIVE in view. No tag in view = no target = no
  automatic turn. A reading older than a limit (a setting) counts as no tag in view.
- Our HIVE = the alliance chosen at INIT (`HiveAim.setAlliance`, like `Drivetrain`).
- `MainTeleOp`: start the Limelight in `preRun`, stop it in `end`, set the alliance on `HiveAim`, and in
  `run()`: turn = `robot.aim.turnPower()` while RIGHT_BUMPER is held AND there is a target, else the right
  stick as today. Show the aim status in the telemetry. Add the control to the "Gamepad controls" table.
- New `opmodes/test/AimTest.java`, `@TeleOp(name = "Aim Test", group = "Test")`: Drivetrain + Vision + Hives +
  HiveAim only (no intake needed). X / B at INIT chooses the alliance. Left stick drives (field-centric),
  right stick turns, RIGHT_BUMPER aims. Telemetry: target cell, angle error, turn power, aimed yes/no.
- Settings in Panels (`@Configurable`, placeholders): turn strength per degree, maximum turn power, a minimum
  turn power while outside the tolerance, tolerance in degrees, an offset in degrees (if the launcher does
  not point exactly where the camera does), the maximum age of a reading.

## Out of scope

- Using the Pinpoint position to aim (set aside), shooting, gamepad rumble, the Auto aim step (piece 6),
  the tag map, position correction (postponed). Nothing to do with the intake.

## Decisions (see PLAN.md)

- 2026-11-08 (user): camera only, because the Limelight is locked in place.
- 2026-11-08: test with "Aim Test" plus the binding in "A. TeleOp". Target rule as above.
- 2026-11-08: the angle is the one seen from above, corrected for the camera's tilt.
- 2026-11-01: the Limelight points the same way as the launcher; the robot turns, no turret.
- 2026-11-01: the aim is hold RIGHT_BUMPER on gamepad 1.

## Facts the builder needs

- `Vision.Tag`: x right, y down, z forward (inches). Angle seen from above: atan2(x, z*cos(t) + y*sin(t)),
  t = camera tilt up from level (use `Hives.CAMERA_TILT_DEG`). Positive = the tag is to the right of the
  camera's line of sight.
- Pedro's rotation is counter-clockwise positive, and `driveFieldCentric(forward, lateral, turn)` takes it that
  way (the TeleOp passes `-gamepad1.right_stick_x`). A target on the right needs a clockwise turn, so
  `turn = -clip(kP * error)`.
- Clusters: blue far 42-45, blue audience 38-41, red far 30-33, red audience 34-37 (`Hives.Cluster`).
  Use the median angle over the target cluster's tags in view (single tags are sometimes wrong).
- `Vision.getAgeMs()` is the age of the reading. `Vision.start()` must be called (at START) or no tag is read.
- Rules: no `hardwareMap` outside subsystems, `follower.update()` only in `Drivetrain.periodic()`, settings
  `public static` + `@Configurable`, nothing blocks, Java 8, new OpMode starts with
  `TelemetryUtil.withPanels(telemetry)`, button hold is a continuous input read in `run()`.

## How the user tests it (at HQ)

1. Safety first: wheels off the ground, or lots of room. The first turns are small and slow (low maximum power).
2. Check the `Hives` settings (camera tilt 28, height 14) and that the Limelight sees the HIVE's tags.
3. Select "Aim Test", choose the alliance with X / B, START. Face the robot roughly at your HIVE so the tags
   are in view.
4. Hold RIGHT_BUMPER: the robot should turn and stop facing the up cell's tags (angle error close to 0).
   Release: the right stick turns it again.
5. Tip the HIVE the other way: it should aim at the other cell's tags (the one that is now up).
6. Cover the camera: no automatic turn, the right stick still works.
7. Tell me what it does: wrong direction, too slow, overshoots, jitters near the target. Then tune the turn
   strength, maximum and minimum power in Panels, or I do from your description.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no warnings from our code.
- A scratch calculation checks: the tilt-corrected angle (for example tilt 28, a tag at x 5, y -35, z 50 is
  about 10.2 deg, not 5.7), the sign (target on the right gives a negative turn), the tolerance (zero turn
  inside it), the clip at the maximum and the minimum power, and the target choice (up cell, last known up
  cell, any tag of our HIVE, none; blue and red).
- No `sleep`, no `hardwareMap` in OpModes, `follower.update()` untouched, Java 8 only.
- Say plainly: compiled and calculated, NOT tested on the robot.
- Nothing from piece 6 added.
