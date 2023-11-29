package org.firstinspires.ftc.teamcode.opmode.testing;

import android.util.Size;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.subsystems.Picker;
import org.firstinspires.ftc.teamcode.subsystems.Placer;
import org.firstinspires.ftc.teamcode.subsystems.auton.TrajectoryConfig;
import org.firstinspires.ftc.teamcode.subsystems.menu.SelectionMenu;
import org.firstinspires.ftc.teamcode.subsystems.menu.SelectionMenu.AllianceColor;
import org.firstinspires.ftc.teamcode.subsystems.menu.SelectionMenu.FieldParkPosition;
import org.firstinspires.ftc.teamcode.subsystems.menu.SelectionMenu.FieldStartPosition;
import org.firstinspires.ftc.teamcode.subsystems.menu.SelectionMenu.MenuState;
import org.firstinspires.ftc.teamcode.subsystems.vision.SpikeMark;
import org.firstinspires.ftc.teamcode.subsystems.vision.TFObjectPropDetect;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import org.firstinspires.ftc.teamcode.subsystems.auton.TrajectoryConfig;

import java.util.List;

@Autonomous(name = "Auton Test - jjenkins", group = "TestAuton")
public class Testing_Auton_jjenkins extends LinearOpMode {

    public ElapsedTime runtime = new ElapsedTime();
    public enum StagePosition {
        APRON,
        BACKSTAGE
    }

    // Camera Property Variables
    public int CAMERA_WIDTH = 640;
    int CAMERA_HEIGHT = 480;
    float highestConf = 0;
    float highestXDistance = 0;
    String highestXDistanceLabel = " ";

    // TFOD Variables
    TFObjectPropDetect tfObjectPropDetect;
    private static final String TFOD_MODEL_ASSET = "ssd_mobilenet_v2_320x320_coco17_tpu_8.tflite";
    private static final String[] LABELS = {
            "person",
            "bicycle",
            "car",
            "motorcycle",
            "airplane",
            "bus",
            "train",
            "truck",
            "boat",
            "traffic light",
            "fire hydrant",
            "???",
            "stop sign",
            "parking meter",
            "bench",
            "bird",
            "cat",
            "dog",
            "horse",
            "sheep",
            "cow",
            "elephant",
            "bear",
            "zebra",
            "giraffe",
            "???",
            "backpack",
            "umbrella",
            "???",
            "???",
            "handbag",
            "tie",
            "suitcase",
            "frisbee",
            "skis",
            "snowboard",
            "sports ball",
            "kite",
            "baseball bat",
            "baseball glove",
            "skateboard",
            "surfboard",
            "tennis racket",
            "bottle",
            "???",
            "wine glass",
            "cup",
            "fork",
            "knife",
            "spoon",
            "bowl",
            "banana",
            "apple",
            "sandwich",
            "orange",
            "broccoli",
            "carrot",
            "hot dog",
            "pizza",
            "donut",
            "cake",
            "chair",
            "couch",
            "potted plant",
            "bed",
            "???",
            "dining table",
            "???",
            "???",
            "toilet",
            "???",
            "tv",
            "laptop",
            "mouse",
            "remote",
            "keyboard",
            "cell phone",
            "microwave",
            "oven",
            "toaster",
            "sink",
            "refrigerator",
            "???",
            "book",
            "clock",
            "vase",
            "scissors",
            "teddy bear",
            "hair drier",
            "toothbrush",
    };

    public String labelToDetect = "cup";
    private TfodProcessor tfod;

    public VisionPortal visionPortal;
    public boolean propDetected = false;
    SelectionMenu selectionMenu = new SelectionMenu(this,telemetry);
    boolean invertedDetection = false; // invert detections based on starting position
    boolean invertedPosition = false; // separate bool to invert
    StagePosition stagePosition = StagePosition.BACKSTAGE;
    Picker picker;
    Placer placer;

    public void runOpMode() throws InterruptedException{

        telemetry.setAutoClear(false);
        while(!isStarted()) {
            selectionMenu.displayMenu();

            // Check for user input
            if (gamepad1.dpad_up) {
                selectionMenu.navigateUp();
            } else if (gamepad1.dpad_down) {
                selectionMenu.navigateDown();
            } else if (gamepad1.a) {
                selectionMenu.selectOption();
            } else if (gamepad1.b) {
                selectionMenu.navigateBack();
            }

            idle();
        }
        selectionMenu.setMenuState(MenuState.READY);
        selectionMenu.displayMenu();

        initTfod();
        tfObjectPropDetect = new TFObjectPropDetect(tfod, CAMERA_WIDTH, labelToDetect);

        picker = new Picker(hardwareMap);
        placer = new Placer(hardwareMap);

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        TrajectoryConfig trajectoryConfig = new TrajectoryConfig(drive);

        AllianceColor allianceColor = selectionMenu.getAllianceColor();
        FieldStartPosition fieldStartPosition = selectionMenu.getFieldStartPosition();
        FieldParkPosition fieldParkPosition = selectionMenu.getFieldParkPosition();

        double waitTime1 = 1.5;
        ElapsedTime waitTimer1 = new ElapsedTime();


        switch(allianceColor){
            case RED:
                invertedPosition = true;
                if(fieldStartPosition == FieldStartPosition.RIGHT) {
                    invertedDetection = true; // F4 RIGHT detect == A4 LEFT detect
                } else {
                    stagePosition = StagePosition.APRON;
                }
                break;
            case BLUE:
            default:
                if(fieldStartPosition == FieldStartPosition.RIGHT) {
                    stagePosition = StagePosition.APRON;
                    invertedDetection = true; // A2 has inverted detections compared to A4
                }
                break;
        }

        Pose2d startPose = trajectoryConfig.getStartPose(invertedPosition, stagePosition);
        Pose2d initialMovePos = trajectoryConfig.getInitialMovePose(invertedPosition, stagePosition);

        TrajectorySequence initialMove = drive.trajectorySequenceBuilder(startPose)
                .strafeTo(new Vector2d(initialMovePos.getX(), initialMovePos.getY()))
                .build();

        drive.setPoseEstimate(startPose);

        SpikeMark location = null;

        waitForStart();
        if (opModeIsActive()){

            drive.followTrajectorySequence(initialMove);

            runtime.reset();

            while (opModeIsActive() & !propDetected & runtime.seconds() < 3) {
                telemetryTfod();

                location = tfObjectPropDetect.getSpikeMark(invertedDetection);
                telemetry.addData("Spike Mark Location", location.toString());
                telemetry.update();

                if (location != SpikeMark.NONE && location != null){
                    propDetected = true;
                }
            }

            Pose2d spikeMarkPos = trajectoryConfig.getSpikeMarkPose(location, invertedPosition, stagePosition);
            Pose2d commonPos = trajectoryConfig.getCommonMarkPose(invertedPosition);
            Pose2d boardPos = trajectoryConfig.getBoardPose(location, invertedPosition);
            Pose2d parkPos = trajectoryConfig.getParkPose(fieldParkPosition, invertedPosition);

            TrajectorySequence spikeMarkTraj;

            if (stagePosition == StagePosition.APRON){

                spikeMarkTraj = drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(spikeMarkPos) // line to spike mark
                        .addDisplacementMarker(() -> { // start placing purple pixel
                            picker.update(Picker.PickerState.OUTAKE);
                        })
                        .waitSeconds(2) //does this actually enable to motor to spin for 2 seconds or does it just blip it
                        .addDisplacementMarker(() -> { // stop placing purple pixel
                            picker.update(Picker.PickerState.HOLD);
                        })
                        .lineToLinearHeading(new Pose2d(-52, 60, Math.toRadians(180))) // get in position to go under truss
                        .lineToConstantHeading(new Vector2d(14, 60)) // go under truss
                        .splineToConstantHeading(new Vector2d(49, 35), Math.toRadians(0)) // get to common point next to board
                        // drive to actual board position
                        .build();


            } else {

                spikeMarkTraj = drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                        .splineToLinearHeading(spikeMarkPos, Math.toRadians(180))
                        .addDisplacementMarker(() -> { // start placing purple pixel
                            picker.update(Picker.PickerState.OUTAKE);
                        })
                        .waitSeconds(2) //does this actually enable to motor to spin for 2 seconds or does it just blip it
                        .addDisplacementMarker(() -> { // stop placing purple pixel
                            picker.update(Picker.PickerState.HOLD);
                        })
                        .splineToLinearHeading(new Pose2d(49,-35, Math.toRadians(180)), Math.toRadians(0))
                        // score yellow
                        // park
                        .build();
            }

            drive.followTrajectorySequence(spikeMarkTraj);
        }
        visionPortal.close();

        // Print out detected spike mark location for debugging
        telemetry.addData("Spike Mark Location", location.toString());
        telemetry.update();
    }

    private void initTfod() {
        tfod = new TfodProcessor.Builder()
                .setModelAssetName(TFOD_MODEL_ASSET)
                .setModelLabels(LABELS)
                .setIsModelTensorFlow2(true)
                .setIsModelQuantized(true)
                .setModelInputSize(320)
                .setModelAspectRatio(16.0 / 9.0)
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();

        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));

        builder.setCameraResolution(new Size(CAMERA_WIDTH, CAMERA_HEIGHT));

        builder.enableLiveView(true);

        builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        builder.setAutoStopLiveView(false);

        builder.addProcessor(tfod);

        visionPortal = builder.build();

        tfod.setMinResultConfidence(0.45f);
    }

    private void telemetryTfod() {
        double x = 0;
        double y = 0;

        List<Recognition> currentRecognitions = tfod.getRecognitions();

        telemetry.clearAll();
        telemetry.addData("# Objects Detected", currentRecognitions.size());

        for (Recognition recognition : currentRecognitions) {
            if (recognition.getConfidence() > highestConf){
                highestConf = recognition.getConfidence();
                highestXDistance = recognition.getLeft();
                highestXDistanceLabel = recognition.getLabel();

                x = (recognition.getLeft() + recognition.getRight()) / 2 ;
                y = (recognition.getTop()  + recognition.getBottom()) / 2 ;
            }

            telemetry.addData(""," ");
            telemetry.addData("Image", "%s (%.0f %% Conf.)", highestXDistanceLabel, highestConf * 100);
            telemetry.addData("- Position", "%.0f / %.0f", x, y);
            telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
        }   // end for() loop
    }

    /**
     * TODO: Function to abstract setting initial move for TFOD
     * @param startingGrid
     * @return
     */
    public TrajectorySequence setInitialMove(String startingGrid) {
        return null;
    }

    /**
     * Print the menu selection
     */
    private void printMenuSelections() {
        telemetry.clear();
        telemetry.addLine("Alliance Color: " + selectionMenu.getAllianceColor());
        telemetry.addLine("Start Position: " + selectionMenu.getFieldStartPosition());
        telemetry.addLine("Park Position: " + selectionMenu.getFieldParkPosition());
        telemetry.update();
    }
}