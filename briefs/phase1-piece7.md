# Piece 7: loop time with the camera on

## Goal

With the Limelight running and tags in view, the loop takes 20 to 39 ms instead of 6 to 9, and Pedro drives worse
(the far-start park ended 15 to 20 deg turned until the camera was stopped before it). Find which part of the loop
costs the time, then make it lighter, so the TeleOp and the next Autos can drive with the camera on. This piece
first builds a measuring tool and adds bulk reads to the far-start Auto; the fixes come after the numbers, each
measured with the same tool.

## In scope

- New test OpMode **"Loop Check"** (group Test, `opmodes/test/LoopCheck.java`), with only the drivetrain, the
  Limelight, `Hives` and `HiveAim` (no intake, so the test robot's configuration works).
  - **Movement (user's choice 2026-11-22):** press A to start: the robot drives the same short path forward and back
    in front of the HIVE tags, again and again (Pedro, speed limit 40 in/s like the Auto). B stops it.
  - **Measures, every loop:** total loop time, and the time of each part: the Pedro update (with the Pinpoint read),
    the Limelight result, `Hives`, `HiveAim`, the CSV log line, the telemetry. Shows the average and the worst of
    the last second, so the screen is readable.
  - **Camera frame rate:** asked from the camera once every 2 s only (the request is slow), shown on screen.
  - **Switches on the gamepad, not in Panels** (the user forgets Panels settings): camera on/off, poll rate
    (cycles 100 / 50 / 30 times a second), our tag processing (`Hives` and `HiveAim`) on/off, log on/off, bulk reads
    on/off. The screen shows the current state of each.
  - A CSV log of the numbers (same `CsvLog`), so runs can be compared after an `adb pull`.
- **Bulk reads in "A. Auto: far start"**, like `Robot` does for the TeleOp.

## Out of scope

- The fixes themselves beyond bulk reads (lower poll rate for good, Limelight pipeline settings, lighter log or
  telemetry): decided after the numbers, as small follow-ups of this piece.
- Photon (set aside 2026-11-22), changes to the TeleOp, the tag position fix, anything that needs the final robot.

## Decisions (see PLAN.md)

- 2026-11-22: measure first, fix after.
- 2026-11-22: the robot repeats one back-and-forth path while measuring (user).
- 2026-11-22: bulk reads go into the far-start Auto (user: "maybe bulk reads would help").
- 2026-11-22: Photon set aside.

## Facts the builder needs

- SDK 12 `Limelight3A`: `start()` launches a background thread that GETs `/results` from the camera every
  `pollIntervalMs` (default 10 ms = 100 Hz, `setPollRateHz` clamps it) and parses the whole JSON with org.json into an
  `LLResult`; `getLatestResult()` only returns the cached one. `getStatus()` is a blocking HTTP request (`/status`,
  `LLStatus.getFps()`): never call it every loop.
- Bulk reads: `CommandScheduler.getInstance().setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL)`
  (see `robot/Robot.java`); the scheduler clears the cache each `run()`. Check that `reset()` in the Auto does not
  undo it (call it after `reset()`). The Pinpoint is I2C: bulk reads do not cover it.
- The far-start Auto logs `loop_ms` already; baseline: 6 to 9 ms with no tags in view, 12 to 17 ms following a path
  with the camera off, 20 to 39 ms with tags in view (logs of 2026-11-21/22).
- Java 8, no `sleep`, `follower.update()` only through `Drivetrain.periodic()`.

## How the user tests it

1. Full install: `./gradlew :TeamCode:removeSlothRemote installDebug`.
2. Put the robot where it sees the HIVE tags with about 3 ft clear in front. Start "Loop Check", press A.
3. With everything on (camera, processing, log, bulk reads), read the loop time and the part times (about 10 s).
4. Switch one thing at a time and read again: camera off; camera on with processing off; poll rate 50, then 30;
   log off; bulk reads off.
5. Write down the frame rate the screen shows, and pull the log (`adb pull /sdcard/FIRST/biobuzz`).
6. Run "A. Auto: far start" once: it should behave as before (park parallel in the zone), with a lower `loop_ms`
   in the log if bulk reads help.

## Self-check before showing the user

- `./gradlew :TeamCode:assembleDebug` builds with no new warnings.
- The robot stops when B is pressed and at the end of the OpMode; the path stays within about 3 ft.
- `getStatus()` is not called more than once every 2 s; no `sleep`.
- The far-start Auto still builds and nothing else in it changed except bulk reads.
- Nothing from the follow-up fixes added. "It builds" is not "it works on the robot".

## What was found and built (closed 2026-11-22)

Loop Check showed the camera costs almost nothing. The real cause was `TagLocalizer` writing a full pose to the
Pinpoint at every camera picture (it ran in the Auto because SolversLib subsystems register themselves). Fixed with
`pedro/ShiftedLocalizer` (x/y shift in software), real unregistering in the Auto, and Loop Check running each part once.
Also: bulk reads in the Auto, Limelight asked 50 times a second, camera on during the park, park at 50 in/s.
