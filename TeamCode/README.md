# MantaBots 27318 code: BIOBUZZ 2026-2027

All of the team's code lives in `TeamCode/`. We never edit `FtcRobotController/`, the Gradle files at the
root, or FIRST's `README.md`: they are replaced with every SDK update.

Libraries: [Pedro Pathing 3](https://pedropathing.com/docs/pathing) (paths) and
[SolversLib](https://docs.seattlesolvers.com) (commands and subsystems), declared in `TeamCode/build.gradle`.

What we learned from the best teams, and the priority list: [docs/top-teams-research.md](docs/top-teams-research.md).

## Structure

```
teamcode/
├── opmodes/
│   ├── teleop/MainTeleOp     "A. TeleOp", the match TeleOp
│   ├── auto/MainAuto         "A. Auto", a single Auto, alliance chosen during init (X = blue, B = red)
│   └── test/                 SystemCheck (pre-match check), SlothTest…
├── subsystems/               1 class = 1 mechanism (Drivetrain, Intake…)
├── robot/
│   ├── Robot                 creates every subsystem, enables bulk reads
│   ├── BatteryVoltage        battery voltage (re-read every 500 ms) and compensation
│   └── HardwareNames         every Driver Hub configuration name
├── pedro/
│   ├── Constants             Pedro settings (AutoTune output)
│   ├── Tuning                registered AutoTune procedures
│   └── procedures/           copied from the Pedro Quickstart, do not edit
└── util/                     Alliance, MatchState, TelemetryUtil, LoopTimer…
```

The script `scripts/update-pedro-procedures.sh` replaces `pedro/procedures/` with the latest version of the
Pedro Quickstart and records the revision in `pedro/procedures/QUICKSTART_REV`. Run it when Pedro releases an
update, then make a separate commit.

## During a match

1. **Auto** ("A. Auto"): choose the alliance during init (X = blue, B = red). On every loop, the Auto saves
   its pose and alliance (`MatchState`). Each path has a time limit (`PATH_TIMEOUT_MS`), and past
   `SCORING_TIMEOUT_MS` the robot gives up and goes to park.
2. **TeleOp** ("A. TeleOp", preselected at the end of the Auto): it reuses the Auto's pose and alliance if
   the Auto ran less than 3 minutes ago. Otherwise, place the robot facing away from the driver and choose
   the alliance during init. BACK (gamepad 1) resets the heading when the robot faces away from the driver.
3. After a Sloth reload or an app restart, `MatchState` is empty: the TeleOp falls back to the
   "robot facing away from the driver" case.

To set once on the field: `Drivetrain.RED_DRIVER_FORWARD_DEG`, the Pedro heading of a robot moving away from
the red driver (editable live in Panels, then copy it into the code).

**In the pits**: run "System Check" (Test group) to spin every motor and servo one by one and check that it
is plugged in correctly.

## Gamepad controls

Keep this up to date with every control change in `MainTeleOp`: it is the drivers' reference.

| Gamepad | Input | Action |
|---|---|---|
| 1 (driver) | Left stick | Field-centric movement (forward = away from the driver) |
| 1 (driver) | Right stick (X axis) | Rotation |
| 1 (driver) | BACK | Reset the heading (robot facing away from the driver) |
| 1 (driver) | X / B during init | Choose the blue / red alliance |
| 2 (operator) | Hold A | Intake: collect |
| 2 (operator) | Hold B | Intake: eject |

## Rules

- **An OpMode never touches the `hardwareMap`.** It creates a `Robot` and calls the subsystems.
- **Every subsystem has a high-level API** (`intake.collect()`, not `motor.set(1.0)` in an OpMode).
  Settings (`public static`) are at the top of the class, with the unit in the name when useful.
- **Nothing blocks the loop**: no `sleep()`, no waiting `while`. Use commands
  (`WaitCommand`, `SequentialCommandGroup`, `StartEndCommand`…).
- **`follower.update()` is called in exactly one place**: `Drivetrain.periodic()`.
- **Watch the loop rate** ("Loop" telemetry). If it drops after a change, that change is the cause.
- **Java naming**: `PascalCase` for classes, `camelCase` for methods, `UPPER_SNAKE` for constants.
- **Code comments and telemetry are in English.**
- Old test OpModes get `@Disabled` or are deleted. Git keeps the history, so no commented-out code.

## Wiring

Keep this up to date with every configuration change on the Driver Hub.

| Name (config) | Type              | Hub              | Port |
|---------------|-------------------|------------------|------|
| `frontLeft`   | Motor             | Control Hub      | ?    |
| `frontRight`  | Motor             | Control Hub      | ?    |
| `backLeft`    | Motor             | Control Hub      | ?    |
| `backRight`   | Motor             | Control Hub      | ?    |
| `pinpoint`    | goBILDA Pinpoint  | Control Hub I2C  | ?    |
| `intake`      | Motor             | Expansion Hub    | ?    |

## Owners

Every mechanism has an owner: they know its code, review the PRs that touch it and can fix it at a
competition. Everyone can change any code, but with the owner's review.

| Part                           | Owner | Backup |
|--------------------------------|-------|--------|
| Drivetrain + Pedro tuning      |       |        |
| Intake                         |       |        |
| Auto (paths)                   |       |        |
| CI / Git / SDK updates         |       |        |

## Pedro tuning (once the drivetrain drives)

1. Create the configuration on the Driver Hub with the names from `HardwareNames`.
2. Run AutoTune in this order: **Mecanum Tuner, then Pinpoint Tuner, then Foresight Tuner, then Tests**
   (see the [Pedro docs](https://pedropathing.com/docs/pathing/tuning)).
3. Paste the code generated by each tuner into `pedro/Constants.java` (where it says `TODO`).

Until the Foresight Tuner has been run, the TeleOp and the Auto refuse to start with an explicit message.

## Panels and Sloth

- **Panels** (live tuning and telemetry): connect to the robot's Wi-Fi and open
  `http://192.168.43.1:8001` in the browser. Panels only shows the telemetry it is sent: every OpMode
  starts its init with `telemetry = TelemetryUtil.withPanels(telemetry);`, which sends each line to both the
  Driver Hub and Panels. Values changed in Panels are lost on the next deploy: copy the ones worth keeping
  into the code.
- **Sloth** (hot reload): after a full install, the `deploySloth` task (VS Code:
  **FTC: Hot Reload (Sloth)**) sends only the `teamcode` code in a second or two. Wait for the load to
  finish before pressing INIT.
- **A full install is required** after changing a library or a `build.gradle`, `FtcRobotController/`, the
  manifest or `res/` (including the hardware configuration), and before every competition.
- If old code keeps coming back: `adb shell rm -rf /storage/emulated/0/FIRST/dairy/sloth/*`.
- The Sloth version, the Load plugin version and the Panels prefix (`0.3.2+…`) must stay identical.

## Git workflow

1. Create a branch: `git switch -c feat/thing-name`
2. Make small commits with clear messages (`Add lift PID`, not `fix`)
3. Push, then open a pull request to `master`
4. Merge only when the "Build" check is green and someone else has reviewed it
5. Before every competition: `git tag competition-name`, then `git push --tags`

At a competition, only deploy committed code, so you always know what runs on the robot.

### Updating the FIRST SDK

```bash
git fetch upstream
git merge upstream/master
```

### Updating Pedro or SolversLib

Change the versions in `TeamCode/build.gradle`. For Pedro, also run `scripts/update-pedro-procedures.sh` to
refresh `pedro/procedures/` from the [Quickstart](https://github.com/Pedro-Pathing/Quickstart).
