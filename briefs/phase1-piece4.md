# Piece 4: position correction (x and y)

## Goal

The Pinpoint drifts slowly and can jump after a bump; the AprilTags do not drift. The robot nudges the
Pinpoint's x and y toward what the tags say, so it always knows where it is. The heading is NOT corrected (the
Pinpoint's heading is reliable). It is built and checked by calculation first, then tuned on the robot.

## In scope

- `Vision` gives two more things: a frame number (so one picture is used only once) and the delay of the
  reading in ms (capture + targeting latency + staleness from the Limelight result).
- `vision/LocalizerLogic.java` (no hardware, so it can be checked on its own): the camera's position from a
  tag (uses `TagMap.cameraFromTag`), the robot's position from the camera's (camera offset on the robot,
  rotated by the robot's heading), the median over tags, the gain rules, the snap rules, the plain-language
  reason when a reading is ignored.
- `subsystems/TagLocalizer.java` (a subsystem, registered in `Robot` after `Hives`): keeps a short history of
  the Pinpoint's poses (with times), and for each new picture applies the correction (below). Uses
  `drivetrain.setPose` (never calls `follower.update()`).
- The correction for one new picture, in this order:
  1. Skip it if the correction is off (`ENABLED`), the reading is too old (a setting), or no tag is usable.
  2. Usable tags: the tag's HIVE is settled (`Hives` says which cell is up; MOVING or UNKNOWN = skip that
     HIVE), the tag is not too far (a setting), and the camera height computed from it is within a
     tolerance of the real mounting height (`Hives.CAMERA_HEIGHT_IN`; a setting for the tolerance).
  3. Skip it if the robot moves or turns too fast (settings, speeds from the pose history).
  4. Measured robot position = median over the usable tags (camera position from each tag, then the camera
     offset removed using the Pinpoint's heading at the time of the picture).
  5. Error = measured position minus the Pinpoint's position at the time of the picture (so the delay does
     not matter). New position = current position + gain * error. Apply the same shift to the stored history.
  6. Gain = base gain, times a factor for the number of tags, one for the distance, one for the speed
     (all settings).
  7. Snap: if the position is not known yet (no Auto before this TeleOp), the first usable reading sets the
     position. If several readings in a row disagree with the Pinpoint by more than a limit and agree with
     each other, set the position to the measured one (a bump).
- Settings in Panels (`@Configurable` on `TagLocalizer`, placeholders): `ENABLED`, camera forward and left
  offsets from the robot's reference point, base gain, tags for full gain, near and maximum distance, slow
  and maximum speed, maximum turn rate, height tolerance, snap distance, snap count, snap consistency,
  maximum reading age, extra latency.
- `MainTeleOp`: the start position, in this order: the Auto's pose if one just ran; else, if the option is on,
  a fixed TeleOp start spot (a setting, in blue-side coordinates and turned for red like every pose); else
  the old way (heading reset, position unknown until the first good tag reading). Tell the localizer whether
  the pose is known. Show a one-line status in the telemetry. The option is `TELEOP_START_ENABLED` (off by
  default until the spot is measured) with its x, y and heading in `TagLocalizer`'s settings.
- New `opmodes/test/LocalizationTest.java`, `@TeleOp(name = "Localization Test", group = "Test")`: Drivetrain,
  Vision, Hives, TagLocalizer only. Settings: start x, y, heading, and "pose known". Sticks drive the robot;
  gamepad 1 A = a fake bump (moves the pose by +10 in. in x and y); B = set the pose back to the start
  values. Telemetry: the pose, the last tag measurement, the error, the gain, the tags used, and the reason
  when a reading is ignored, the snap counter, and the heading disagreement (information only).

## Added by the user, 2026-11-08

- The TeleOp usually starts from a set position, which is more accurate than waiting for the first tag reading,
  so that is an option (above).

## Out of scope

- Correcting the heading, using tags when the HIVE is moving, the Auto itself (piece 6), aiming (done), the
  "aim when the target is out of view" idea, any change to how the aim works.

## Decisions (see PLAN.md)

- 2026-11-08 (user): do piece 4 now; x and y only.
- 2026-11-08: the defaults listed in PLAN.md (settled HIVEs only, height gate, median, delta with a gain, quality
  factors, snap for a bump and for the first reading, camera offset as a setting, fake-bump button).
- 2026-11-01: the correction is a weighted pull, not a PID; the camera delay is handled by comparing with the
  Pinpoint's pose from the moment of the picture.

## Facts the builder needs

- `TagMap.cameraFromTag(tagPose, x, y, z, roll, pitch, yaw)` returns the camera's position on the field (and its
  heading and tilt; the heading is not used). Tag poses: `TagMap.pose(id, blueFarCellUp, redFarCellUp)`.
  `Hives.getSideUp(hive)` gives FAR / AUDIENCE / UNKNOWN; FAR means the far cell is up.
- Tag Map Check on the rebuilt rig (docs/NOTES.md): the camera position from single tags is good to about 1 to
  3 in., the height to about 1 in., and a tag at the edge of the picture can be worse. Single tags sometimes
  give wrong readings: hence the height gate and the median.
- Field frame = Pedro frame, heading 0 = toward the blue wall, counter-clockwise positive. A camera offset
  of (forward f, left l) from the robot's reference point is, in the field,
  (f*cos(h) - l*sin(h), f*sin(h) + l*cos(h)) for the robot's heading h.
- `Limelight LLResult`: `getCaptureLatency()`, `getTargetingLatency()` and `getStaleness()` are in ms; whether
  the sum is the true delay is not verified: keep a setting for extra latency.
- Rules: no `hardwareMap` outside subsystems, `follower.update()` only in `Drivetrain.periodic()`
  (`setPose` is fine), settings `public static` + `@Configurable`, nothing blocks, Java 8, a new OpMode
  starts with `TelemetryUtil.withPanels(telemetry)`.
- `Robot` registers the subsystems in the order drivetrain, intake, vision, hives, aim, localizer.

## How the user tests it (at HQ, with the Pinpoint, the Limelight and the drive motors)

1. Measure where the Limelight sits relative to the robot's reference point (the Pinpoint's tracking point):
   forward and left, in inches. Type them in Panels. Check the camera height in the `Hives` settings.
2. Select "Localization Test". In Panels set the start x, y and heading to where you put the robot (from the
   audience/red corner; heading 0 = facing the blue wall) and "pose known" true. START.
3. With the HIVE in view and not moving: the pose should stay near the start values, and the telemetry shows
   the tag measurement agreeing to about 2 to 3 in.
4. Press A (fake bump): the pose jumps by 10 in. and should glide back over about a second. Press it a few
   times; try a bigger bump with the snap settings.
5. Drive around with the tags in view and check the pose against a tape measure at 2 or 3 spots.
6. Set "pose known" false and restart: the first good reading should place the robot.
7. Tip the HIVE: while it moves the telemetry says the correction is paused. Cover the camera: the pose keeps
   working from the Pinpoint alone, without jumps.
8. Tell me what you see; I tune the gains and limits.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no warnings from our code.
- A scratch simulation (truth pose moving, a Pinpoint with a drift or a bump, pretend camera readings from
  `TagMap` with a delay) shows: the corrected pose converges to the truth; the delay is handled (a moving
  robot is not pulled to a stale position); the camera offset conversion; ignored readings (HIVE moving or
  unknown, too far, too fast, height off, reading too old); the same picture is never used twice; the first
  reading sets the position when it is unknown; a bump is snapped after the set number of readings; a lone
  wild tag does not move the pose.
- No `sleep`, no `hardwareMap` in OpModes, `follower.update()` untouched, Java 8 only.
- Say plainly: compiled and calculated, NOT tested on the robot.
- Nothing from piece 6 added, and the heading is never changed.
