package org.firstinspires.ftc.teamcode.opmodes.auto;

import static com.pedropathing.api.Paths.line;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

/**
 * Un seul Auto pour les deux alliances : on choisit l'alliance pendant l'init, et les positions
 * (écrites côté bleu) sont transformées par Alliance.poses().
 */
@Autonomous(name = "Auto", group = "Competition", preselectTeleOp = "TeleOp")
public class MainAuto extends CommandOpMode {
    private Robot robot;
    private Alliance alliance = Alliance.BLUE;
    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        reset();
        robot = new Robot(hardwareMap);
    }

    @Override
    public void initialize_loop() {
        if (gamepad1.x) alliance = Alliance.BLUE;
        if (gamepad1.b) alliance = Alliance.RED;
        telemetry.addData("Alliance", "%s   (X = bleu, B = rouge)", alliance);
        telemetry.update();
    }

    @Override
    public void preRun() {
        PoseFactory p = alliance.poses();
        // TODO positions BIOBUZZ, côté bleu, en pouces et en degrés
        Pose start = p.of(9, 111, -90);
        Pose score = p.of(16, 128, -45);
        Pose park = p.of(68, 96, -90);

        robot.drivetrain.setPose(start);
        Follower follower = robot.drivetrain.follower;

        schedule(new SequentialCommandGroup(
                new FollowPathCommand(follower, line(start, score).linear(start, score)),
                new InstantCommand(robot.intake::eject, robot.intake),
                new WaitCommand(500),
                new InstantCommand(robot.intake::stop, robot.intake),
                new FollowPathCommand(follower, line(score, park).linear(score, park), false)
        ));
    }

    @Override
    public void run() {
        super.run();

        loopTimer.tick();
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Boucle", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Pose", robot.drivetrain.pose());
        telemetry.update();
    }
}
