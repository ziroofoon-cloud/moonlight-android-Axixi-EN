package com.limelight.nvstream;

public interface NvConnectionListener {
    void stageStarting(String stage);
    void stageComplete(String stage);
    void stageFailed(String stage, int portFlags, int errorCode);
    
    void connectionStarted();
    void connectionTerminated(int errorCode);
    void connectionStatusUpdate(int connectionStatus);
    
    void displayMessage(String message);
    void displayTransientMessage(String message);

    void rumble(short controllerNumber, short lowFreqMotor, short highFreqMotor);
    void rumbleTriggers(short controllerNumber, short leftTrigger, short rightTrigger);

    void setHdrMode(boolean enabled, byte[] hdrMetadata);

    void setMotionEventState(short controllerNumber, byte motionType, short reportRateHz);

    void setControllerLED(short controllerNumber, byte r, byte g, byte b);

    void setAdaptiveTriggers(short controllerNumber, byte eventFlags,
                             byte typeLeft, byte typeRight,
                             byte[] left, byte[] right);

    void setPlayerIndicator(short controllerNumber, byte playerIndicator);

    void controllerPcm(short controllerNumber, short sequence, int sampleRate,
                       byte channels, byte bitsPerSample, byte[] pcm);
}
