package se.vendler;

import se.vendler.floorheat.FloorheatController;

import java.util.logging.Logger;

public class FloorheatExerciseWorker extends Thread {
	private int floorheatNbr;
	private FloorheatController controller;
	private long exercisePeriod;
	private Logger logger = Logger.getLogger(this.getName()+ "-" + this.getThreadGroup().getName());
	private RoomInformation roomInformation;
	public FloorheatExerciseWorker(int floorheatNbr, long exercisePeriod, FloorheatController controller, RoomInformation roomInformation) {
		this.floorheatNbr = floorheatNbr;
		this.controller = controller;
		this.exercisePeriod = exercisePeriod;
		this.roomInformation = roomInformation;
	}
	
	@Override
	public void run() {
		logger.info("Exercising start for room " + roomInformation.getName());
		try {
			controller.heatOn(floorheatNbr, true);
			roomInformation.setExercising(true);
			Thread.sleep(exercisePeriod);
			controller.heatOff(floorheatNbr, true);
			roomInformation.setExercising(false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Exercising stop for room " + roomInformation.getName());
	}
}
