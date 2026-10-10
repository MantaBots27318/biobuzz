# What the best FTC teams do

Research done in October 2026 on 36 public repos from 28 teams: world champions from 2023 to 2026, the
best OPRs worldwide (FTCScout) and teams known for their code. The goal: know what is worth copying into
our code, and what is not.

## What we already had last season

- **Panels** for live tuning and telemetry (a library by Lazar / FTControl, distributed through Dairy's
  Maven repository). Brought back this season.
- **The hardware configuration under version control** in `TeamCode/src/main/res/xml/`. To redo for BIOBUZZ.
- No **Sloth** (hot reload), see its own section below.

## Teams studied

| Team | Record | Takeaway |
|---|---|---|
| 11212 The Clueless | 2024 world champions | 100% home-made framework, prioritized motor writes with a time budget per loop, a CSV log of every match |
| 21229 Quality Control | 2023 world champions | Fake hardware when a device is missing, end-of-Auto pose and time saved to a file, an `AutoBase` class |
| 19066 AiCitizens | 2024 world champions | Home-made command engine and path follower (Kotlin), IMU read in a thread |
| 11228 OverClucked Bots | 2026 world champions | Simple code, but Autos with a fallback ("failover") and a manual backup mode |
| 14481 Don't Blink | 2023 world champions | Already on BIOBUZZ with Pedro 3.0.1 and tuning 1.0.1, the same versions as us |
| 16379 KookyBotz | Top OPR 2024, widely followed code | `read()` / `periodic()` / `write()` cycle for every subsystem, commands sorted by use |
| 12808 RevAmped | 4th OPR worldwide in 2026 | Selection menus during init, automatic robot self-test, Pedro simulator |
| 19043 CyLiis | Top 5 OPR | Configuration names by port (`ch0`, `eh1`…), servos that estimate when they have arrived |
| 11329 ICE | Division winners at the 2026 Worlds | Modular Autos (each module starts where the previous one ended), README with a photo |
| 23511 Seattle Solvers | Authors of SolversLib | JUnit tests on the math run by CI, loop profiler to CSV, tuning guides in .md |
| 19411 Tech Tigers | Strong software culture | 124 PRs, CI with build + tests + lint, a diagnostic OpMode for the pits |
| 6165 MSET Cuttlefish | Already far along on BIOBUZZ | Reusable code (`architecture/`) kept apart from season code, a "faults" system, a conventions guide |

## What almost all of the best teams do

1. **Live tuning** with FTC Dashboard or Panels (`@Config` / `@Configurable`): present in every robot
   repo studied.
2. **An optimized loop**:
   - bulk reads and motor write caching (we already have them);
   - voltage compensation (`power × 12 V / measured voltage`), with the voltage re-read only every
     0.2 to 5 s because reading it is expensive;
   - slow I2C sensors (color, distance, IMU) read less often or in a thread.
3. **The end of the Auto handed over to the TeleOp**: pose and alliance, sometimes saved to a file to
   survive an app restart.
4. **One test OpMode per mechanism**, plus a pre-match check stepped through with the gamepad.
5. **`enum` state machines** in every subsystem.
6. **Motor current monitoring** (25 repos): detecting a picked-up game piece, a stall, a brownout risk.
7. **Hardware configuration under version control** in `res/xml/` (Seattle Solvers, RevAmped, MSET,
   Tech Tigers, Escape Velocity, Clueless).

## Findings

- **Winning Worlds does not require clean code.** OverClucked (2026 champions) and Clueless (2024
  champions) work on a single branch, with commented-out code and duplicated red/blue OpModes. What
  scores points is the robot and driver practice. Code quality drives reliability and how fast you can
  iterate.
- **PRs and CI are rare.** Only teams with a strong software culture use them (Tech Tigers, Escape
  Velocity, KookyBotz, Seattle Solvers). Our setup puts us in that group. We just need to stay flexible:
  no mandatory review in the middle of a competition.
- **Unit tests:** a single team (Seattle Solvers), only for the math.
- **Red/blue:** most teams duplicate OpModes. The best ones parameterize with a pose transformation,
  like we do.
- **The BIOBUZZ field has 180° rotational symmetry**, it is not mirrored (manual, section 9: GARDENS in
  opposite corners, the red cell's AprilTags on the far side and the blue cell's on the audience side).
  Fixed in `util/Alliance.java`.
- **Sloth was already in our APK**: Pedro tuning 1.0.1 ships it (version 0.3.2). Only the Gradle plugin
  was missing.

## Priorities for our code

### Priority 1: before the first robot

- [x] Fix the red/blue symmetry in `Alliance.java` (180° rotation)
- [x] Bring back **Panels** (Sloth-compatible build)
- [x] `MatchState` class: end-of-Auto pose and alliance, read back when the TeleOp starts
- [x] Periodic voltage reading and power compensation
- [x] Global timeout on the Auto (and a time limit per path)
- [x] **SystemCheck** OpMode that spins every motor and servo one by one
- [ ] BIOBUZZ hardware configuration in `res/xml/` (waiting for the robot's wiring)
- [x] Prefixed OpMode names ("A. TeleOp") so they appear at the top of the Driver Hub list

### Priority 2: to save time

- [x] **Sloth** to reload the code in under a second (see below)
- [x] Script that updates `pedro/procedures/` from the Quickstart, with the revision pinned in a file
      (like MSET)
- [ ] Driver Hub telemetry built only when it is actually sent (every 250 ms)
- [ ] CSV log of every match

### Priority 3: once the robot exists

- [ ] Selection menus during init: alliance, delay, Auto variant
- [ ] Modular Autos (ICE model)
- [ ] JUnit tests for the math, run by CI
- [ ] "Faults" shown in large text on the Driver Hub when a sensor fails

### Not worth copying

- Singletons and `static` hardware everywhere.
- Commented-out code and `old/` folders: git keeps the history.
- PhotonCore: the gain is real but it is experimental. Only if the loop becomes a problem.

## Sloth in detail

**What it is.** A Dairy Foundation library that sends only our code (`org.firstinspires.ftc.teamcode`)
to the robot, instead of reinstalling the whole app. Under 2 s instead of 40 s or more. Changes survive
robot restarts, and the new code is only loaded when the current OpMode ends, so you can deploy while an
OpMode is running.

**Why it is useful for us.** Tuning sessions (PID, servo positions, paths) mean changing a value,
deploying, testing, and starting again. Dividing the wait by 20 changes how many tests fit in a session.

**What it takes** in `TeamCode/build.gradle` (already done; the Sloth 0.3.2 library came with Pedro tuning):

```groovy
buildscript {
    repositories {
        mavenCentral()
        google()
        maven { url "https://repo.dairy.foundation/releases" }
    }
    dependencies {
        classpath "dev.frozenmilk:Load:0.3.2"
    }
}

apply plugin: 'dev.frozenmilk.sinister.sloth.load'
```

For Panels, use the compatible build: `com.bylazar.sloth:fullpanels:0.3.2+1.0.13`.
For FTC Dashboard: `com.acmerobotics.slothboard:dashboard:0.3.2+0.6.0`. The Sloth version and the
prefix must stay identical.

**Usage.**
- `./gradlew :TeamCode:deploySloth` (or a `deploySloth` Gradle configuration in Android Studio) for
  ordinary code changes.
- A normal full install (with the `removeSlothRemote` task first) for: the first time, a change to a
  library or `build.gradle`, a change in `FtcRobotController/`, the manifest or `res/`.

**Known pitfalls.**
- Code that compiles can fail after a reload if a library or a file outside `teamcode` was changed:
  in that case, do a full install.
- If old Sloth code keeps replacing the new code: delete `/storage/emulated/0/FIRST/dairy/sloth/` on
  the robot (`adb shell rm -rf /storage/emulated/0/FIRST/dairy/sloth/*`). A common problem this season
  for teams moving to Pedro 3.
- Wait for the load to finish (Control Hub screen or Driver Hub log) before deploying again or pressing
  INIT. MSET reports that an INIT during a load can restart the app.
- Values changed in Panels / Dashboard are lost on reload: copy the ones worth keeping into the code.
- **At a competition**: do a full install before the matches and stop using Sloth, so you know exactly
  which code is running.

Who uses it: MSET Cuttlefish, ICE, Don't Blink (with slothboard), the BrainSTEM starter kit.
RoboKings and Escape Velocity had installed it, then disabled it.

## Pedro 3.0.1 pitfalls reported by MSET

- AutoTune runs at `http://192.168.43.1:10158` (connected to the robot's Wi-Fi).
  Order: Mecanum, Pinpoint, Foresight, then Tests (leave a clear 48 × 48 inch area).
- Keep the robot still for the first second of init: the Pinpoint calibrates its IMU.
- A `@Tuner` method must be `static`, take no arguments and return exactly `Procedure`, otherwise the
  app crashes at startup.
- A path must have a heading interpolation (for example `.linear(a, b)`), otherwise Pedro throws an
  exception.
- A curve's `length()` can underestimate the length of S-shaped curves: do not use it for timeouts.
- `Interpolator.piecewise()` has bugs in 3.0.1: prefer a simple interpolation per segment.

## Sources

- Worlds results: [2023](https://ftc-events.firstinspires.org/2022/FTCCMP1/awards),
  [2024](https://ftc-events.firstinspires.org/2023/FTCCMP1/awards),
  [2025](https://ftc-events.firstinspires.org/2024/FTCCMP1/awards),
  [2026](https://ftc-events.firstinspires.org/2025/FTCCMP1/awards)
- [BIOBUZZ manual, section 9 (Arena)](https://ftc-resources.firstinspires.org/ftc/archive/2027/game/manual-09)
- [Sloth](https://github.com/Dairy-Foundation/Sloth)
- Repos: [Clueless](https://github.com/FTCclueless/Decode),
  [Quality Control](https://github.com/21229QualityControl/CenterstageV2),
  [AiCitizens](https://github.com/petrustoica/CenterStage),
  [OverClucked](https://github.com/cmcarpen85/OCB_Decode2025_26),
  [Don't Blink](https://github.com/DontBlink14481/Biobuzz14481),
  [KookyBotz](https://github.com/KookyBotz/CenterStage),
  [RevAmped](https://github.com/junkjunk123/RevAmped-Decode-V2),
  [CyLiis](https://github.com/Cyliis/CyLiis_Into_The_Deep_OfficialCode),
  [ICE](https://github.com/FTC11329/11329-2026-repo),
  [Seattle Solvers](https://github.com/FTC-23511/Decode-2026),
  [Tech Tigers](https://github.com/techtigers-ftc/decode),
  [MSET Cuttlefish](https://github.com/6165-MSET-Cuttlefish/BioBuzz)
