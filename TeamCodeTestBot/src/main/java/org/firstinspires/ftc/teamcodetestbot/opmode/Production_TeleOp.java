package org.firstinspires.ftc.teamcodetestbot.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcodetestbot.subsystems.Robot;

@TeleOp(name= "Competition Teleop", group = "TeleOp")
public class Production_TeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {

        Robot robot = new Robot(hardwareMap);

        waitForStart();

        while (!isStopRequested()) {

            robot.updateTeleOp(gamepad1,gamepad2,telemetry);
        }
    }
}
