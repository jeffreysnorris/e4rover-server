package org.eclipsecon.ebots.internal.core;

import java.util.concurrent.TimeUnit;

import org.eclipsecon.ebots.core.ContestPlatform;
import org.eclipsecon.ebots.core.IGoal;

public class GameController extends Thread {

	private GoalHandler goalHandler = new GoalHandler();
	private boolean shutdown;
	
	public GameController() {
		super("Game Controller Thread");
	}

	@Override
	public void run() {

		Game game = new Game();
		
		while (!shutdown) {

			// TODO Check queue for next player

			// COUNTDOWN TO GAME START
			game.enterCountdownState("Bob");
			while (game.tickCountdownClock()) {
				try { sleep(1000); } catch (InterruptedException e) {/*ignore*/}
				Persister.updateToServer(game);
			}

			// START GAME
			//TODO Get the real bucket for this player
			String bucketName = "";
			CommandRelay commandRelay = new CommandRelay(bucketName);
			// TODO: command relay was malfunctioning (the file wasn't present on the server).  Needs to be more forgiving of this.  Disabling for now.
			//commandRelay.start();
			game.enterPlayingState();

			// PLAY GAME
			while (game.tickPlayClock()) {
				// Check for goals
				IGoal goal = null;
				try {
					goal = goalHandler.getGoalQueue().poll(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {/*ignore*/}
				if (goal != null) {
					game.handleGoal(goal);
				}
				Persister.updateToServer(game);
			}

			// GAME OVER
			//commandRelay.stop();
			String lastPlayerName = game.getPlayerName();
			int lastScore = game.getScore();
			game.enterGameOverState();
			Persister.updateToServer(game);

			// UPDATE SCORES
			Player player = ContestPlatform.getPlayers().getPlayerMap().get(lastPlayerName);
			// TODO: Null check OR make the get blocking 
			player.incrementPlayCount();
			// Update player best score if warranted
			if (player.getHighScore() < lastScore) {
				player.setHighScore(lastScore);
			}

			Persister.updateToServer(ContestPlatform.getPlayers());

		}


	}

	public void shutdown() {
		shutdown = true;
		goalHandler.shutdown();
	}
}
