# PROGRESS: AprilTags (branch `feat/apriltags`)

Legend: [ ] to do, [>] in progress, [~] built but not tested (placeholder values), [x] done

- [x] 1. Limelight reads tags (brief: briefs/phase1-piece1.md)
- [x] 2. Tag positions (brief: briefs/phase1-piece2.md): tested on the rebuilt rig 2026-11-08, all clusters and both positions agree to about 1 to 3 in.
- [x] 3. HIVE is up detection (brief: briefs/phase1-piece3.md): tested on the practice rig 2026-11-08; camera tilt and height still to set when the Limelight is mounted
- [x] 4. Position correction (brief: briefs/phase1-piece4.md): tested on the robot 2026-11-14 ("it all works"); gains and limits are still placeholders to tune on the final robot
- [x] 5. TeleOp aim (brief: briefs/phase1-piece5.md): tested on the robot 2026-11-08 ("it works"); gains are still placeholders to tune
- [x] 6. Auto, step 1: "far start" (the user's Auto 1) with placeholder shoot (brief: briefs/phase1-piece6.md). Closed 2026-11-22 on the test robot: drives 8 in., waits for the HIVE, shoots (placeholder), parks parallel in the LOADING ZONE. To retune on the final robot. Autos 2 and 3 come after
- [x] 7. Loop time with the camera on (brief: briefs/phase1-piece7.md, branch `feat/loop-time`). Done 2026-11-22: "Loop Check" tool; the real cause was the tag fix writing to the Pinpoint (slow loop and 15 to 20 deg park), now a software shift; loop 8 ms standing, 14 ms driving; camera on during the park, park at 50 in/s, ask rate 50
- [x] 8. Fixes from the phase 1 review (brief: briefs/phase1-piece8.md; docs/reviews/phase-1.md, added 2026-11-22): settle the camera tilt (35 in the code, 28 in the notes); a "park by" time so the far-start Auto always parks before 30 s; move "A. Auto" (MainAuto) to the Test group; no silent alliance default (FarStartAuto RED, TeleOp BLUE); note in BACKLOG that the camera offset (6 / 0) is from the old Pinpoint tracked point. Then a PR of pieces 6 to 8 to `master`. Done 2026-11-22: tested on the test robot, normal and late park both "work very well", late park "really good" (user)
- [x] 9. PlayStation button names (brief: briefs/phase1-piece9.md, branch `feat/ps-button-names`, added 2026-11-22): Cross / Circle / Square / Triangle in the code, Driver Hub text and docs, plus a Build check that refuses A/B/X/Y. Done 2026-11-22, approved by the user
- [>] 10. Auto 1, part 2: FLOWER pickup (placeholder), then a second shot or park with the balls by a fixed cut-off time (brief: briefs/phase1-piece10.md)
