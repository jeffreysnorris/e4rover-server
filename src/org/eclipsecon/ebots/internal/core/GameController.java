package org.eclipsecon.ebots.internal.core;

import java.util.concurrent.TimeUnit;

import org.eclipsecon.ebots.core.ContestPlatform;
import org.eclipsecon.ebots.core.IShot;

public class GameController extends Thread {

	private ShotHandler shotHandler = new ShotHandler();
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
			//commandRelay.start();
			game.enterPlayingState();

			// PLAY GAME
			while (game.tickPlayClock()) {
				// Check for shots
				IShot shot = null;
				try {
					shot = shotHandler.getShotQueue().poll(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {/*ignore*/}
				if (shot != null) {
					game.handleShot(shot);
				}
				Persister.updateToServer(game);
			}

			// GAME OVER
			//commandRelay.stop();
			game.enterGameOverState();
			Persister.updateToServer(game);

			// UPDATE SCORES
			Player player = ContestPlatform.getPlayers().getPlayerMap().get(game.playerName);
			// TODO: Null check OR make the get blocking 
			player.incrementPlayCount();
			// Update player best score if warranted
			if (player.getHighScore() < game.getScore()) {
				player.setHighScore(game.getScore());
			}

			Persister.updateToServer(ContestPlatform.getPlayers());

		}


	}

	public void shutdown() {
		shutdown = true;
		shotHandler.shutdown();
	}
}
