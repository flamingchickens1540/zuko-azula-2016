package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class Portcullis {

    private static final TalonExtendedMotor leftGrabMotor = FRC.talonCAN(12);
    private static final TalonExtendedMotor rightGrabMotor = FRC.talonCAN(13);
    
    private static final FloatIO leftEncoder = leftGrabMotor.modEncoder().getEncoderPosition();
    private static final FloatIO rightEncoder = rightGrabMotor.modEncoder().getEncoderPosition();
    
    private static final FloatInput control = ZukoAzula.controlBinding.addFloat("Portcullis Grabber Axis").deadzone(0.2f);
    private static final BooleanInput levelButton = ZukoAzula.controlBinding.addBoolean("Level Portcullis Arms");

    private static final ArbitratedFloat leftInput = ZukoAzula.behaviors.addFloat();
    private static final ArbitratedFloat rightInput = ZukoAzula.behaviors.addFloat();  
   
    private static BooleanCell isLevel = new BooleanCell();
    
    public static void setup() throws ExtendedMotorFailureException{
    Cluck.publish("Zero Portcullis Encoders", leftEncoder.eventSet(0).combine(rightEncoder.eventSet(0)));
    Cluck.publish("Portcullis Left Angle", leftEncoder);
    Cluck.publish("Portcullis Right Angle", rightEncoder);
    Cluck.publish("Portcullis Arms are Level", isLevel);
    
    isLevel = (BooleanCell) leftEncoder.minus(rightEncoder).inRange(-10, 10); //TODO set correct range

    leftInput.attach(ZukoAzula.teleop, control);
    rightInput.attach(ZukoAzula.teleop, control);
    leftInput.attach(ZukoAzula.pit, control);
    rightInput.attach(ZukoAzula.pit, control);
    
    leftInput.send(leftGrabMotor.simpleControl().addRamping(0.1f, FRC.constantPeriodic));
    rightInput.send(rightGrabMotor.simpleControl().addRamping(0.1f, FRC.constantPeriodic));
    
    EventOutput levelArms = new EventOutput(){ //TODO actually make this work
        public void event(){
           if(leftEncoder.get()<rightEncoder.get()){
                while(!leftEncoder.minus(rightEncoder).inRange(-10, 10).get()){
                    leftInput.send(ZukoAzula.mainTuning.getFloat("Portcullis Arm Level Speed", 0.4f));
                }
            }
        }};
    levelButton.onPress().send(levelArms);

    }
   

    
}
