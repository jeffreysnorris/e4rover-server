package org.eclipsecon.ebots.internal.core;

import java.util.concurrent.TimeUnit;

import org.eclipsecon.ebots.core.IGoal;
import org.eclipsecon.ebots.core.IPlayers;

public class GameController extends Thread {

	protected IPlayers players = new Players();
	private GoalHandler goalHandler = new GoalHandler();
	private boolean shutdown;
	
	public GameController() {
		super("Game Controller Thread");
	}

	@Override
	public void run() {

		GameStatus game = new GameStatus();
		
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
			CommandRelay commandRelay = new CommandRelay(game.getPlayerName());
			commandRelay.start();
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
			Player player = (Player)players.getPlayerMap().get(lastPlayerName);
			// TODO: Null check OR make the get blocking 
			player.incrementPlayCount();
			// Update player best score if warranted
			if (player.getHighScore() < lastScore) {
				player.setHighScore(lastScore);
			}

			Persister.updateToServer(players);

		}


	}

	public void shutdown() {
		shutdown = true;
		goalHandler.shutdown();
	}
}
