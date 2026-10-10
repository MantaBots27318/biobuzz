# CLAUDE.md: repo guide for Claude

Claude Code loads this file automatically at the start of every session. It explains everything you need
to change this repo **without breaking the team's rules**, even if you know nothing about FTC.
Read it in full before touching the code. When in doubt, ask the person instead of guessing.

Write everything in the code in English: class, method and variable names, code comments, telemetry
text and error messages.

---

## 1. The context in 2 minutes

- **FTC** (FIRST Tech Challenge): a robotics competition for high school students. Two alliances (red
  and blue) of two robots each play on a square field of 144 × 144 inches (~3.66 m).
- **The team**: MantaBots, team number 27318. GitHub repo: `MantaBots27318/biobuzz`.
- **The season**: **BIOBUZZ** (2026-2027). Game elements: POLLEN (yellow balls) and NECTAR (red and
  blue balls), launched into the HIVES or placed into the FLOWERS. The official manual is the authority:
  https://ftc-resources.firstinspires.org/ftc/game/manual
- **A match**: 30 s of **Autonomous** (the robot runs alone, no drivers), an 8 s transition, then
  2 min of **TeleOp** (two drivers with gamepads).
- **The BIOBUZZ field has 180° rotational symmetry** around its center (it is not mirrored): a red
  position is the blue position rotated by 180° (see `util/Alliance.java`).

### The hardware

| Item | Role |
|---|---|
| **Control Hub** | The robot's Android computer. Our code runs on it. Ports for motors, servos and sensors (I2C, analog, digital) |
| **Expansion Hub** | A second hub connected to the Control Hub, for more ports |
| **Driver Hub** (or Driver Station) | The drivers' tablet. You pick the OpMode, press INIT then START, and see the telemetry. The gamepads plug into it |
| **Hardware configuration** | Stored on the Driver Hub: the name given to each device on each port. The code finds devices by that name |
| **goBILDA Pinpoint** | A small board that tracks the robot's position on the field (odometry) |

### Code vocabulary

| Term | Meaning |
|---|---|
| **OpMode** | A program started from the Driver Hub: `@TeleOp` (driven) or `@Autonomous` |
| **INIT / START / STOP** | The 3 stages of an OpMode, triggered by the Driver Hub buttons |
| **`hardwareMap`** | The object that gives access to devices by their configuration name |
| **Telemetry** | The lines of text shown on the Driver Hub (and in Panels for us) |
| **Loop** | An OpMode runs in a loop (often 50 to 200 times per second). The faster the loop, the more precise the robot |
| **Bulk read** | Reading all of a hub's values in a single request instead of one request per sensor |
| **Pose** | The robot's position: `x`, `y` (inches) and `heading` (radians in Pedro) |

---

## 2. The tech stack

| Tool | Version | What it is for |
|---|---|---|
| FTC SDK | 12.0 | The base provided by FIRST (the `FtcRobotController/` module) |
| Android Gradle Plugin / Gradle | 8.13.2 / 9.1 | Building |
| **Java 8** (language level) | set by the SDK | See the restrictions below |
| **Pedro Pathing** | `revhub` 3.0.1, `tuning` 1.0.1 | Path following and driving the drivetrain |
| **SolversLib** | 0.3.6 | Command and subsystem architecture, gamepads (`GamepadEx`) |
| **Panels** | `com.bylazar.sloth:fullpanels:0.3.2+1.0.13` | Live tuning and telemetry in the browser |
| **Sloth** | 0.3.2 (`Load` plugin 0.3.2) | Hot reload of the code in ~1 s |

The team's libraries are declared **only** in `TeamCode/build.gradle`.

### Version traps: very important

Your training data most likely contains older versions of these libraries. **Their APIs have changed.**

- **Pedro Pathing 3 is nothing like Pedro 1 or 2.** Never use `PathChain`, building `BezierLine` or
  `BezierCurve` directly, `follower.followPath(...)`, `follower.getPose()`,
  `follower.startTeleopDrive()`, `FollowerConstants` or `PathConstraints`: they no longer exist.
  In Pedro 3:
  - poses: `PoseFactory.degrees().of(x, y, headingDeg)` or `new Pose(x, y, headingRad)`; read them
    with `pose.x()`, `pose.y()`, `pose.heading()` (a `Pose` is immutable);
  - paths: `Paths.line(a, b)` or `Paths.curve(a, control, b)`, **always** followed by a heading
    interpolation such as `.linear(a, b)`, otherwise Pedro throws an exception;
  - follower: `follower.update()`, `follower.pose()`, `follower.setPose(p)`, `follower.isBusy()`,
    `follower.manual(DrivePowers)` or `follower.manual(forward, lateral, turn)`.
- **SolversLib replaces FTCLib.** Imports are `com.seattlesolvers.solverslib...`, never
  `com.arcrobotics.ftclib...`. The two cannot be installed together.
- **Panels**: use the Sloth build (`com.bylazar.sloth:fullpanels`), never `com.bylazar:fullpanels` or
  the old name `ftcontrol`. The `0.3.2+` prefix must stay equal to the Sloth and Load plugin versions.
- If you are not sure a method exists, compile (`./gradlew :TeamCode:assembleDebug`) instead of
  assuming. A compile error is better than an invented API.

### Java 8: not allowed

No `var`, no `record`, no `switch` expressions (`->` returning a value), no `"""` text blocks, no
`List.of()` / `Map.of()`. Lambdas, method references (`robot.intake::collect`) and
`java.util.function` are fine.

---

## 3. Where things are

All of our code is in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`:

```
teamcode/
├── opmodes/
│   ├── teleop/MainTeleOp.java   "A. TeleOp": the match TeleOp
│   ├── auto/MainAuto.java       "A. Auto": a single Auto for both alliances
│   └── test/                    SystemCheck (pre-match check), SlothTest
├── subsystems/                  1 class = 1 mechanism
│   ├── Drivetrain.java          drivetrain (holds the Pedro Follower)
│   └── Intake.java              example mechanism, the template to copy
├── robot/
│   ├── Robot.java               creates and registers every subsystem, enables bulk reads
│   ├── HardwareNames.java       ALL hardware configuration names
│   └── BatteryVoltage.java      battery voltage (re-read every 500 ms) and compensation
├── pedro/
│   ├── Constants.java           Pedro settings (TODO values to fill in after AutoTune)
│   ├── Tuning.java              registers the AutoTune procedures
│   └── procedures/              copied from the Pedro Quickstart: DO NOT EDIT
└── util/
    ├── Alliance.java            BLUE / RED, turns blue poses into red ones
    ├── MatchState.java          alliance and pose handed from the Auto to the TeleOp
    ├── TelemetryUtil.java       withPanels(): telemetry to the Driver Hub + Panels
    └── LoopTimer.java           measures the loop rate
```

### Files to read for each task

| Task | Read first |
|---|---|
| Anything | `TeamCode/README.md` (rules, wiring, gamepad controls, Git workflow) |
| TeleOp / gamepads | `opmodes/teleop/MainTeleOp.java`, the "Gamepad controls" section of the README, section 6 below |
| New mechanism | `subsystems/Intake.java` (template), `robot/Robot.java`, `robot/HardwareNames.java` |
| Autonomous | `opmodes/auto/MainAuto.java`, `util/Alliance.java`, `util/MatchState.java` |
| Drivetrain / Pedro | `subsystems/Drivetrain.java`, `pedro/Constants.java`, the Pedro section of `TeamCode/docs/top-teams-research.md` |
| Roadmap, best practices | `TeamCode/docs/top-teams-research.md` (priority checklist, Pedro 3.0.1 pitfalls, Sloth) |
| Setup, deploying, VS Code | `VSCODE_SETUP.md` |
| Official FTC examples | `FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/` (copy into `teamcode`, never edit in place) |
| Pull requests | `.github/PULL_REQUEST_TEMPLATE.md` |
| CI | `.github/workflows/build.yml` |

---

## 4. The team's rules (follow them on every change)

### Architecture

1. **An OpMode never touches the `hardwareMap`.** It creates a `Robot` (`new Robot(hardwareMap)`) and
   calls the subsystems' methods. Only subsystems (and `Robot`, `BatteryVoltage`) call
   `hardwareMap.get(...)`.
2. **One mechanism = one subsystem** in `subsystems/`, extending `SubsystemBase` (SolversLib).
   It exposes a **high-level API**: `intake.collect()`, `lift.goTo(LiftPosition.HIGH)`, never
   `motor.setPower(0.8)` from an OpMode.
3. **Hardware configuration names live in `HardwareNames.java`** and nowhere else. A name that is
   added or changed must also be updated in the "Wiring" table of `TeamCode/README.md`.
4. **Settings** (powers, servo positions, gains, timeouts) are `public static` (not `final`) fields
   **at the top of the class**, with `@Configurable` on the class so they can be changed live in
   Panels. Put the unit in the name when useful (`..._MS`, `..._DEG`).
5. **Nothing blocks the loop**: never `sleep()`, `Thread.sleep()`, a waiting `while`, or
   `waitForStart()` in our OpModes. Waiting is done with commands (`WaitCommand`, `WaitUntilCommand`,
   `SequentialCommandGroup`) or state machines (`enum` + `ElapsedTime`).
6. **`follower.update()` is called in exactly one place**: `Drivetrain.periodic()`.
7. **Every new OpMode starts its init with** `telemetry = TelemetryUtil.withPanels(telemetry);`,
   otherwise its telemetry does not show up in Panels.
8. **One single Auto for both alliances**: poses are written for the **blue** side and transformed by
   `alliance.poses()`. Do not create copy-pasted `AutoRed` / `AutoBlue`.
9. **OpMode names**: `@TeleOp(name = "A. ...", group = "Match")` for match OpModes (the "A." puts them at
   the top of the list), `group = "Test"` for tests. An old, useless test is deleted (git keeps the
   history), not commented out.

### Files you must never edit

- `FtcRobotController/` (FIRST's SDK)
- `build.gradle`, `build.common.gradle`, `build.dependencies.gradle`, `settings.gradle` at the root
- `README.md` at the root (FIRST's, rewritten with every SDK release)
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/procedures/`: updated only by
  `scripts/update-pedro-procedures.sh`

These files are replaced when the SDK or Pedro is updated: editing them causes merge conflicts.

### Style

- Standard Java: `PascalCase` for classes, `camelCase` for methods and variables, `UPPER_SNAKE` for
  constants and `public static` settings.
- Comments in English, few of them: explain **why**, not what the line does.
- Telemetry text (what the drivers read on the Driver Hub) and error messages are in English too.
- No commented-out code left in files.
- 4-space indentation (see `.editorconfig`).
- Match the style of the neighbouring files.

---

## 5. How a loop works (SolversLib's CommandOpMode)

Our OpModes extend `CommandOpMode`:

| Method | When | What goes in it |
|---|---|---|
| `initialize()` | once, at INIT | `withPanels`, `reset()`, `new Robot(...)`, button bindings |
| `initialize_loop()` | in a loop during INIT | alliance selection, display. Nothing should move |
| `preRun()` | once, at START | set the starting pose, schedule the Auto's sequence |
| `run()` | in a loop after START | continuous inputs (sticks), then `super.run()`, then telemetry |

`super.run()` runs the `CommandScheduler`, which, in this order:
1. calls `periodic()` on every registered subsystem (including `follower.update()`);
2. reads the gamepad buttons and starts or stops the bound commands;
3. runs the active commands;
4. clears the bulk read cache.

**Always** call `reset()` at the start of `initialize()` (the scheduler is a singleton shared by all
OpModes) and **always** call `super.run()` in `run()`.

---

## 6. Writing TeleOp code and gamepad controls

### Principles

- **Gamepad 1 = driver** (moving the robot), **gamepad 2 = operator** (mechanisms). Do not mix them
  without the team's agreement.
- **Continuous inputs** (sticks, proportional triggers): read in `run()`, before `super.run()`.
- **Actions triggered by a button**: bound **once** in `initialize()` with `GamepadEx`. Do not test
  `gamepad2.a` in `run()` to start an action: it would restart it on every loop while the button is held.
- **Stick axes**: pushing up gives a **negative** value. Hence `-gamepad1.left_stick_y` to drive forward.
- **Pedro's frame**: +x forward, +y left, positive rotation is counter-clockwise.
  Hence `driveFieldCentric(-left_stick_y, -left_stick_x, -right_stick_x)`.
- Every new control goes into the **"Gamepad controls" table of `TeamCode/README.md`**. Before using a button,
  check in that table that it is free.

### Binding a button (SolversLib)

```java
GamepadEx operator = new GamepadEx(gamepad2);

// Once, on press
operator.getGamepadButton(GamepadKeys.Button.Y).whenPressed(robot.lift::goHigh);

// While held: one action at the start, another on release
operator.getGamepadButton(GamepadKeys.Button.A)
        .whenHeld(new StartEndCommand(robot.intake::collect, robot.intake::stop, robot.intake));

// A longer command (sequence)
operator.getGamepadButton(GamepadKeys.Button.X).whenPressed(new SequentialCommandGroup(
        new InstantCommand(robot.claw::open, robot.claw),
        new WaitCommand(200),
        new InstantCommand(robot.lift::goDown, robot.lift)
));
```

Other options on `getGamepadButton(...)`: `whenReleased`, `whileHeld`, `toggleWhenPressed`.
Buttons: `GamepadKeys.Button.A`, `B`, `X`, `Y`, `LEFT_BUMPER`, `RIGHT_BUMPER`, `DPAD_UP`,
`DPAD_DOWN`, `DPAD_LEFT`, `DPAD_RIGHT`, `BACK`, `START`, `LEFT_STICK_BUTTON`, `RIGHT_STICK_BUTTON`
(the PlayStation names `CROSS`, `CIRCLE`, `SQUARE`, `TRIANGLE` also exist).

The **last argument** of the commands (`robot.intake`) is the subsystem they use: two commands using
the same subsystem never run at the same time, the new one interrupts the old one. Always pass it.

### Adding a gamepad control: the recipe

1. Check that the mechanism has a high-level method in its subsystem. If not, add it there (not in
   the TeleOp).
2. Check in the "Gamepad controls" table that the button is free.
3. Add the binding in `MainTeleOp.initialize()`, with a short comment in English.
4. Update the "Gamepad controls" table in the README.
5. Compile: `./gradlew :TeamCode:assembleDebug`.

---

## 7. Adding a mechanism (subsystem): the recipe

1. Add the configuration name to `robot/HardwareNames.java` and to the "Wiring" table of the README.
2. Create `subsystems/MyMechanism.java` based on `Intake.java`:

```java
@Configurable
public class Lift extends SubsystemBase {
    public static double HIGH_POWER = 0.8;   // editable live in Panels

    private final MotorEx motor;             // MotorEx only writes when the value changes
    private final BatteryVoltage battery;

    public Lift(HardwareMap hardwareMap, BatteryVoltage battery) {
        motor = new MotorEx(hardwareMap, HardwareNames.LIFT);
        this.battery = battery;
    }

    public void goUp() { motor.set(battery.compensate(HIGH_POWER)); }
    public void stop() { motor.set(0); }
}
```

3. In `robot/Robot.java`: add the field `public final Lift lift;`, create it in the constructor and
   add it to `registerSubsystem(...)`. **A subsystem that is not registered never gets its
   `periodic()` called.**
4. Use it in the OpModes (buttons in `MainTeleOp`, steps in `MainAuto`).
5. It shows up automatically in "System Check" (which tests every motor and servo in the configuration).

For a mechanism with several states (an arm, a lift with set positions), use an `enum` of positions
and a `goTo(Position p)` method. Any computation that must run on every loop (PID, state machine) goes
in `periodic()`.

---

## 8. Autonomous and the handover to TeleOp

- `MainAuto`: the alliance is chosen during INIT (X = blue, B = red). In `preRun()`, poses are created
  with `alliance.poses().of(x, y, degrees)` (blue-side coordinates) and the sequence is scheduled with
  `schedule(...)`.
- Paths: `new FollowPathCommand(follower, Paths.line(a, b).linear(a, b))`, always with a time limit
  (`.withTimeout(...)`, see `PATH_TIMEOUT_MS`).
- A pose computed at run time (for example "from where I am now") goes through
  `new DeferredCommand(() -> ..., null)`.
- The Auto saves its pose and alliance to `MatchState` on every loop; the TeleOp reuses them if the
  Auto ran less than 3 minutes ago. `MatchState` (like every `static` field) is cleared when the app
  restarts and on every Sloth reload.
- The poses currently in `MainAuto` are placeholders (`TODO`) to replace.

---

## 9. Pedro Pathing: current state

- `pedro/Constants.java` contains `TODO`s: until AutoTune has been run (Mecanum Tuner, then Pinpoint
  Tuner, then Foresight Tuner, then Tests), `createFollower()` throws a clear exception.
  **A. TeleOp and A. Auto therefore do not start before that tuning.** To test anything else, use
  System Check or Sloth Test.
- A `@Tuner` method in `Tuning.java` must be `static`, take no arguments and return exactly
  `Procedure`, otherwise the app crashes at startup.
- `Drivetrain.RED_DRIVER_FORWARD_DEG` (the Pedro heading of a robot moving away from the red driver)
  still has to be measured on the field.
- Known Pedro 3.0.1 pitfalls: see `TeamCode/docs/top-teams-research.md`.

---

## 10. Building, deploying, checking

| Command | Effect | Robot needed? |
|---|---|---|
| `./gradlew :TeamCode:assembleDebug` | Compiles everything. **Run it after every change** | No |
| `./gradlew :TeamCode:removeSlothRemote installDebug` (VS Code: `Cmd+Shift+B`) | Full install (~40 s) | Yes |
| `./gradlew :TeamCode:deploySloth` (VS Code task "FTC: Hot Reload (Sloth)") | Hot reload (~1 s) | Yes |

On Windows: `.\gradlew.bat` instead of `./gradlew`.

- **Sloth only reloads code in `org.firstinspires.ftc.teamcode`.** After changing a library,
  `build.gradle`, the manifest, `res/` (including the hardware configuration) or `FtcRobotController/`,
  a full install is required. Before a competition: full install, then no more Sloth.
- Values changed in Panels are lost on the next deploy: copy the ones worth keeping into the code.
- Panels: `http://192.168.43.1:8001` (robot Wi-Fi). Pedro AutoTune: `http://192.168.43.1:10158`.
- The computer has no internet while it is connected to the robot's Wi-Fi.

**Never claim a change works on the robot if it has not been tested there.**
"It compiles" and "it works on the robot" are two different things: say which one you checked.

---

## 11. Git and GitHub

- **Never commit directly to `master`.** Create a branch from an up-to-date `master`:
  `feat/...` (feature), `fix/...` (bug fix), `docs/...` (documentation).
- **Only commit, push or open a PR when the person asks for it.**
- Commit messages: a single-line subject in English, in the imperative mood (`Add lift subsystem`),
  like the existing history.
- **The repo is a fork of `FIRST-Tech-Challenge/FtcRobotController`.** A PR must target
  `MantaBots27318/biobuzz`, never FIRST's repo:
  `gh pr create --repo MantaBots27318/biobuzz --base master ...`
- PR descriptions follow `.github/PULL_REQUEST_TEMPLATE.md`.
- The "Build" check (GitHub Actions) must be green, and someone else must review before merging.
- Never commit `.vscode/settings.json`, `local.properties`, or `build/` folders.
- Updating FIRST's SDK: `git fetch upstream`, then `git merge upstream/master`.

---

## 12. Common mistakes to avoid

- Using the Pedro 1/2 API or FTCLib imports (see section 2).
- Calling `hardwareMap.get(...)` in an OpMode.
- Hard-coding a configuration name (`"intake"`) instead of `HardwareNames.INTAKE`.
- Forgetting `registerSubsystem(...)` in `Robot`, `reset()` at the start of `initialize()`, or
  `super.run()` in `run()`.
- Forgetting `TelemetryUtil.withPanels(telemetry)` at the start of a new OpMode's init.
- Calling `follower.update()` anywhere other than `Drivetrain.periodic()`.
- Putting `sleep()` or a waiting `while` loop in an OpMode.
- Testing a button in `run()` to trigger a one-off action instead of using `GamepadEx`.
- Making a setting `final` (Panels can no longer change it) or forgetting `@Configurable`.
- Adding a dependency anywhere other than the `dependencies` block of `TeamCode/build.gradle`
  (the `buildscript` block only accepts plugin `classpath` entries).
- Duplicating an OpMode for the red alliance.
- Editing a file from the "Files you must never edit" list.
- Using a Java 9+ feature.

---

## 13. Resources

In the repo:
- `TeamCode/README.md`: rules, wiring, gamepad controls, how a match runs, Git workflow
- `TeamCode/docs/top-teams-research.md`: what the best teams do, roadmap, Sloth in detail,
  Pedro 3.0.1 pitfalls
- `VSCODE_SETUP.md`: setup, deploying, VS Code shortcuts
- `FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/`:
  official examples for every sensor and concept

Online:
- Official FTC documentation: https://ftc-docs.firstinspires.org
- SDK Javadoc: https://javadoc.io/doc/org.firstinspires.ftc
- Game Manual 0, a community guide (mechanical and programming): https://gm0.org
- BIOBUZZ manual: https://ftc-resources.firstinspires.org/ftc/game/manual
- Pedro Pathing 3: https://pedropathing.com/docs/pathing and the Quickstart
  https://github.com/Pedro-Pathing/Quickstart
- SolversLib: https://docs.seattlesolvers.com and examples in https://github.com/FTC-23511/SolversLib
  (`examples/` folder)
- Sloth: https://github.com/Dairy-Foundation/Sloth
- Code from top teams (links in `TeamCode/docs/top-teams-research.md`), for example
  https://github.com/6165-MSET-Cuttlefish/BioBuzz (BIOBUZZ, Pedro 3) and
  https://github.com/FTC-23511/Decode-2026 (the SolversLib authors)
