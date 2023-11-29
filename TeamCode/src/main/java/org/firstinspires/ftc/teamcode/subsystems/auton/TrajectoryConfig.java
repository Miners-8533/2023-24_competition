package org.firstinspires.ftc.teamcode.subsystems.auton;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.opmode.Production_Auton.StagePosition;
import org.firstinspires.ftc.teamcode.subsystems.menu.SelectionMenu.FieldParkPosition;
import org.firstinspires.ftc.teamcode.subsystems.vision.SpikeMark;

/**
 * invert is managing the inversion for red vs. blue alliance start positions.
 * stagePosition is managing the mirror for left vs. right start positions.
 * (a LEFT detection on the APRON start position is a mirror of a RIGHT detection
 * on the BACKSTAGE start position, etc)
 */
public class TrajectoryConfig {
    private SampleMecanumDrive rrdrive;
    Trajectory apronTraj;
    Trajectory backstageTraj;
    double starting_X;
    double starting_Y;
    double starting_H;
    double initial_X;
    double initial_Y;
    double initial_H;
    double spikeMark_Y;
    double spikeMark_X;
    double spikeMark_H;
    double common_X;
    double common_Y;
    double common_H;
    double board_X;
    double board_Y;
    double board_H;
    double park_X;
    double park_Y;
    double park_H;
    double apronSafe_X;
    double apronSafe_Y;
    double apronSafe_H;
    double apronTruss_X;
    double apronTruss_Y;
    double apronTruss_H;

    public TrajectoryConfig(SampleMecanumDrive autonDrive){
        this.rrdrive = autonDrive;
    }
//    public Trajectory GenerateTrajectory(Pose2d startPose, Vector2d endPosition){
//        TrajectoryBuilder genTraj = rrdrive.trajectoryBuilder(startPose)
//                        .lineTo(new Vector2d(endPosition.getX(), endPosition.getY()));
//
//
//        return genTraj.build();
//    }

    public Pose2d getStartPose(boolean invert, StagePosition stagePosition){
        starting_Y = invert ? -62 : 62;
        starting_H = invert ? 90: 270;

        if (stagePosition == StagePosition.APRON){
            starting_X = -40;
        } else {
            starting_X = 16;
        }

        return new Pose2d(starting_X, starting_Y, Math.toRadians(starting_H));
    }

    public Pose2d getInitialMovePose(boolean invert, StagePosition stagePosition){

        Pose2d currentPositionStartPose = getStartPose(invert, stagePosition);
        initial_Y = currentPositionStartPose.getY();
        initial_H = currentPositionStartPose.getHeading();

        if (stagePosition == StagePosition.APRON) {
            initial_X = -44;
        } else {
            initial_X = 20;
        }

        return new Pose2d(initial_X, initial_Y, Math.toRadians(initial_H));
    }
    public Pose2d getSpikeMarkPose(SpikeMark detectedSpikeMark, boolean invert, StagePosition stagePosition){

        spikeMark_Y = invert ? -34 : 34;
        spikeMark_H = 180;

        switch(detectedSpikeMark){
            case LEFT:
                if (stagePosition == StagePosition.APRON){
                    spikeMark_X = invert ? -45 : -29;
                    spikeMark_H = invert ? 90 : 305;
                } else {
                    spikeMark_X = invert ? 4 : 28;
                }
                break;
            case RIGHT:
                if (stagePosition == StagePosition.APRON) {
                    spikeMark_X = invert ? -29 : -45;
                    spikeMark_H = invert ? 55 : 270;
                } else {
                    spikeMark_X = invert ? 28 : 4;
                }
                break;
            case CENTER:
            default:
                if (stagePosition == StagePosition.APRON) {
                    spikeMark_X = -40;
                    spikeMark_Y = invert ? -29 : 29;
                    spikeMark_H = invert ? 90 : 270;
                } else {
                    spikeMark_X = 20;
                    spikeMark_Y = invert ? -24 : 26;
                }
                break;
        }
        return new Pose2d(spikeMark_X, spikeMark_Y, Math.toRadians(spikeMark_H));
    }
    public Pose2d getCommonMarkPose(boolean invert){
        common_X = 43;
        common_Y = invert ? -35 : 35;
        common_H = 180;

        return new Pose2d(common_X, common_Y, Math.toRadians(common_H));
    }
    public Pose2d getBoardPose(SpikeMark detectedSpikeMark, boolean invert, StagePosition stagePosition){
        board_X = 53.5;
        board_Y = invert ? -35 : 35;
        board_H = 180;

        if (stagePosition == StagePosition.APRON) {
            board_X = 55.5;
        }

        if (detectedSpikeMark == SpikeMark.RIGHT) {
            board_Y = invert ? -42 : 28;
        } else if (detectedSpikeMark == SpikeMark.LEFT){
            board_Y = invert ? -28: 42;
        }

        return new Pose2d(board_X, board_Y, Math.toRadians(board_H));
    }
    public Pose2d getParkPose(FieldParkPosition parkPosition, boolean invert){
        park_H = 180;
        park_X = 47;

        switch(parkPosition){
            case NEAR_WALL:
                park_Y = invert ? -59 : 59;
                break;
            case NEAR_CENTER:
                park_Y = invert ? -11 : 11;
                break;
            default:
            case ON_BACKDROP:
                park_Y = invert ? -35 : 35;
                break;
        }
        return new Pose2d(park_X, park_Y, Math.toRadians(park_H));
    }

    public Pose2d getApronSafePose(boolean invert){
        apronSafe_X = -52;
        apronSafe_Y = invert ? -60 : 60;
        apronSafe_H = 180;

        return new Pose2d(apronSafe_X, apronSafe_Y, Math.toRadians(apronSafe_H));
    }

    public Pose2d getApronTrussPose(boolean invert){
        apronTruss_X = 14;
        apronTruss_Y = invert ? -60 : 60;
        apronTruss_H = 180;

        return new Pose2d(apronTruss_X, apronTruss_Y, Math.toRadians(apronTruss_H));
    }
//    public TrajectorySequence getSpikeMarkTrajectory(SpikeMark detectedSpikeMark, boolean invert, StagePosition stagePosition){
//
//        Pose2d spikeMarkPose = getSpikeMarkPose(detectedSpikeMark, invert, stagePosition);
//        TrajectorySequence spikeMarkTraj = null;
//
//        if (stagePosition == StagePosition.APRON){
//            spikeMarkTraj = rrdrive.trajectorySequenceBuilder(rrdrive.getPoseEstimate())
//                    .lineToLinearHeading(spikeMarkPose)
//                    .addDisplacementMarker(() -> { //start placing purple pixel
//                        picker.update(Picker.PickerState.OUTAKE);
//                    })
//                    .waitSeconds(2)
//                    .addDisplacementMarker(() -> { //stop placing purple pixel
//                        picker.update(Picker.PickerState.HOLD);
//                    })
//                    .build();
//        } else {
//            spikeMarkTraj = rrdrive.trajectorySequenceBuilder(rrdrive.getPoseEstimate())
//                    .splineToLinearHeading(spikeMarkPose, 180)
//                    .build();
//        }

//        TrajectoryBuilder spikeMarkTraj = rrdrive.trajectoryBuilder(currentPose)
//                .strafeTo(new Vector2d(spikeMarkPose.getX(), spikeMarkPose.getY()));

//        return spikeMarkTraj;
//    }
//    public Trajectory getCommonTrajectory(Pose2d startPose, )
}
