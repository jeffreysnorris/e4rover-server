package org.eclipsecon.ebots.internal.core;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipsecon.e4rover.core.IGoal;
import org.eclipsecon.e4rover.internal.core.Player;

public class GameController extends Thread {

	private final GoalHandler goalHandler = new GoalHandler();
	private boolean shutdown;

	public GameController() {
		super("Game Controller Thread");
	}

	@Override
	public void run() {

		GameStatus game = new GameStatus();

		while (!shutdown) {

			// TODO get hash and player from Rest

			List<String> playerAndHash = Persister.getCurrentPlayerAndHashFromQueue();
			if (playerAndHash.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			String hash = (String) playerAndHash.get(0);
			Player player = (Player)Persister.fromXML(playerAndHash.get(1));

			// COUNTDOWN TO GAME START
			game.enterCountdownState(player.getName());
			while (game.tickCountdownClock()) {
				try { sleep(1000); } catch (InterruptedException e) {/*ignore*/}
				Persister.updateToServer(game);
			}

			// START GAME
			CommandRelay commandRelay = new CommandRelay(hash);
			commandRelay.start();
			try { 
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
			} finally {
				// GAME OVER
				commandRelay.stop();
			}
			int lastScore = game.getScore();
			game.enterGameOverState();
			Persister.updateToServer(game);

			// UPDATE SCORES
			player.incrementPlayCount();
			// Update player best score if warranted
			player.updateHighScore(lastScore);

			//update with just the player
			Persister.updateToServer(player, hash);

		}


	}

	public void shutdown() {
		shutdown = true;
		if (goalHandler != null) goalHandler.shutdown();
	}
}
