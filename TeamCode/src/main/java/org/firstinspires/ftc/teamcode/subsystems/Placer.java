package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

public class Placer {
    public final int PLACE_LEVEL_MAX = 10; //TODO
    private final int ELEVATOR_STOWED = 0;//TODO
    private final int PIXEL_HEIGHT = 0;//TODO
    private final int BACKDROP_HEIGHT = 0;//TODO
    private final int CLIMB_HEIGHT = 0;//TODO
    private final int HANGING = 0;//TODO
    private Gripper gripper;
    private Elevator elevator;
    private Gripper.GripperState gripper_state = Gripper.GripperState.READY;
    public int place_level = 0;
    public enum PlacerState {
        READY_TO_INTAKE,
        STOW,
        DEPLOY,
        PLACE_FIRST,
        PLACE_SECOND,
        READY_TO_CLIMB,
        HANG
    }
    Placer(HardwareMap hardwareMap) {
        gripper = new Gripper(hardwareMap);
        elevator = new Elevator(hardwareMap);
    }
    public void update(PlacerState desired_state) {
        Gripper.GripperState new_gripper_state;
        int new_elevator_position;
        switch (desired_state) {
            default:
                //error state set safe state by falling into READY_TO_INTAKE
            case READY_TO_INTAKE:
                new_gripper_state = Gripper.GripperState.READY;
                new_elevator_position = ELEVATOR_STOWED;
                break;
            case STOW:
                new_gripper_state = Gripper.GripperState.STOWED;
                new_elevator_position = ELEVATOR_STOWED;
                break;
            case DEPLOY:
                new_gripper_state = Gripper.GripperState.TRANSFER;
                new_elevator_position = place_level*PIXEL_HEIGHT + BACKDROP_HEIGHT;
                break;
            case PLACE_FIRST:
                new_gripper_state = Gripper.GripperState.SCORE_FIRST;
                new_elevator_position = place_level*PIXEL_HEIGHT + BACKDROP_HEIGHT;
                break;
            case PLACE_SECOND:
                new_gripper_state = Gripper.GripperState.SCORE_SECOND;
                new_elevator_position = place_level*PIXEL_HEIGHT + BACKDROP_HEIGHT;
                break;
            case READY_TO_CLIMB:
                new_gripper_state = Gripper.GripperState.READY;
                new_elevator_position = CLIMB_HEIGHT;
                break;
            case HANG:
                new_gripper_state = Gripper.GripperState.READY;
                new_elevator_position = HANGING;
                break;
        }
        gripper.update(new_gripper_state);
        elevator.update(new_elevator_position);
    }

}
