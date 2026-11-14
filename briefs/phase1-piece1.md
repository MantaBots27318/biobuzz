# Piece 1: the Limelight reads the tags

## Goal

Add a `Vision` subsystem that reads the AprilTags seen by the Limelight 3A, and a test OpMode
"Vision Test" that shows them on the Driver Hub and in Panels. Nothing moves. This piece only proves
that the robot can see the tags and that the numbers are believable.

## In scope

- `Vision` subsystem (`subsystems/Vision.java`): starts the Limelight, reads the latest result, gives
  the list of tags in view (ID, distance, angle, camera-relative position) and the age of the reading.
- `HardwareNames.LIMELIGHT = "limelight"`, and a row in the "Wiring" table of `TeamCode/README.md`.
- `Vision` created and registered in `Robot.java`.
- Test OpMode `opmodes/test/VisionTest.java`: `@TeleOp(name = "Vision Test", group = "Test")`.
- Settings editable in Panels (`@Configurable`): pipeline number.

## Out of scope

- Tag positions on the field (piece 2), HIVE state (piece 3), correcting the position (piece 4),
  aiming (pieces 5 and 6).
- Using MegaTag / the Limelight's own field position (decided: our code computes it).
- Any change to A. TeleOp or A. Auto.

## Decisions (see PLAN.md)

- 2026-11-01: camera = Limelight 3A, config name `limelight`.
- 2026-11-01: our code uses each tag's position relative to the camera, not MegaTag.
- 2026-11-01: test with a new "Vision Test" OpMode.
- 2026-11-01: 45º is the starting angle for the camera (tilt, to confirm).

## Facts the builder needs

- Class: `com.qualcomm.hardware.limelightvision.Limelight3A` (FTC SDK 12.0, no new dependency).
  Result: `LLResult` -> fiducial results (`LLResultTypes.FiducialResult`). Compile to check exact
  method names; do not guess (CLAUDE.md section 2).
- Call `limelight.start()` when the OpMode starts and `stop()` when it ends; pipeline chosen with
  `pipelineSwitch(n)`.
- Rule 1: only `Vision` calls `hardwareMap.get(...)`. Rule 3: the name lives in `HardwareNames`.
  Rule 7: the OpMode starts with `TelemetryUtil.withPanels(telemetry)`. Nothing blocks the loop.
- Tags: 36h11 family, 3.25 in., IDs red 30-37, blue 38-45.
- The pipeline on the Limelight is from last season: its tag size and family must be checked in the
  Limelight web page before trusting distances.
- The Pedro tuning is done, so `new Robot(hardwareMap)` starts.

## How the user tests it

1. In the Limelight web page, check that the AprilTag pipeline uses family 36h11 and tag size 3.25 in.
   (or tell me what you see; I will adapt). Note the pipeline number.
2. Check the Driver Hub configuration has a device called `limelight`.
3. Deploy (full install the first time, since the manifest/config is involved).
4. Select "Vision Test" (Test group), press INIT then START.
5. Point the Limelight at the practice HIVE (start around 45º tilt, 1 to 2 m away).
6. You should see: how many tags, each tag's ID (30-45), its distance and angle, and the age of the
   reading. With no tag in view: "No tags in view".
7. Measure the real distance to a tag with a tape measure and compare. Tell me the difference.
8. Cover the camera: the screen goes back to "No tags in view" within a moment.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no warnings.
- Wiring table, `HardwareNames`, `Robot` registration all updated.
- No `sleep`, no `hardwareMap` in the OpMode, no Java 9+ features.
- What I checked is stated plainly: "it compiles" is not "it works on the robot".
- Nothing from pieces 2 to 6 added.
