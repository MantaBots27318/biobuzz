# NOTES: learned the hard way

- **Limelight Marker Size:** the pipeline keeps last season's value. For BIOBUZZ it must be 82.55 mm
  (3.25 in.). With 165.1 mm every distance came out twice too big (2026-11-01).
- **Robot needs every device:** `new Robot(hardwareMap)` fails if any device (intake, drive motors,
  pinpoint, limelight) is missing from the active configuration. Test OpModes for one mechanism should
  create only that subsystem.
