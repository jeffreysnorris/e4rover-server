package org.eclipsecon.ebots.internal.core;

import java.io.IOException;

import lejos.nxt.remote.NXTCommRequest;
import lejos.nxt.remote.NXTCommand;
import lejos.nxt.remote.OutputState;

public class SynchronizedNXTCommand extends NXTCommand {

	
	@Override
	public synchronized byte setOutputState(int port, byte power, int mode,
			int regulationMode, int turnRatio, int runState, int tachoLimit)
			throws IOException {
		return super.setOutputState(port, power, mode, regulationMode, turnRatio,
				runState, tachoLimit);
	}
	
	
	@Override
	public synchronized OutputState getOutputState(int port) throws IOException {
		return super.getOutputState(port);
	}
	
	@Override
	public synchronized boolean isOpen() {
		return super.isOpen();
	}
	
	@Override
	public synchronized byte playTone(int frequency, int duration) throws IOException {
		return super.playTone(frequency, duration);
	}
	
	@Override
	public synchronized void close() throws IOException {
		super.close();
	}
	
	@Override
	public synchronized int getBatteryLevel() throws IOException {
		return super.getBatteryLevel();
	}
	
	@Override
	public synchronized int getTachoCount(int arg0) throws IOException {
		return super.getTachoCount(arg0);
	}
	
	@Override
	public synchronized void setNXTComm(NXTCommRequest nxtComm) {
		super.setNXTComm(nxtComm);
	}
}
