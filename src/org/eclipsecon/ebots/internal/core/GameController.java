package org.eclipsecon.ebots.internal.core;

import java.util.concurrent.TimeUnit;

import org.eclipsecon.ebots.core.ContestPlatform;
import org.eclipsecon.ebots.core.IShot;

public class GameController extends Thread {

	public static void main(String[] args) throws InterruptedException {
		(new GameController()).start();
		Thread.sleep(Long.MAX_VALUE);
	}

	public GameController() {
		super("Game Controller Thread");
	}
	
	public void run() {

			Game game = new Game();
			while (true) {

				// TODO Check queue for next player
				game.playerName = "Jeff";

				// COUNTDOWN PHASE
				game.countdownSeconds = Game.COUNTDOWN_SECONDS;
				long startTime = System.currentTimeMillis() + (game.countdownSeconds * 1000);
				while (System.currentTimeMillis()< startTime) {
					try { sleep(1000); } catch (InterruptedException e) {/*ignore*/}
					game.countdownSeconds = (int) ((startTime - System.currentTimeMillis())/1000);
					Persister.updateToServer(game);
				}

				// START GAME
				game.countdownSeconds = 0;
				game.remainingSeconds = Game.GAME_LENGTH_SECONDS;
				game.lastShot = null;
				game.nextShot = new Shot();
				game.nextReward = Game.FIRST_REWARD;
				game.score = 0;
				long endTime = System.currentTimeMillis() + (game.remainingSeconds * 1000);
				
				//TODO Get the real bucket for this player
				String bucketName = "";
				CommandRelay commandRelay = new CommandRelay(bucketName);
				commandRelay.start();
				while (System.currentTimeMillis() < endTime) {
					// Check for shots
					IShot shot = null;
					try {
						//TODO there isn't yet anything that put a shot into this Queue.  Need to remember to hook that up.
						shot = ShotHandler.instance.getShotQueue().poll(1, TimeUnit.SECONDS);
					} catch (InterruptedException e) {/*ignore*/}
					if (shot != null) {
						// A shot has been made
						if (shot.equals(game.nextShot)) {
							// It's good!  Dispense rewards.
							game.score += game.nextReward;
							game.nextReward++;
						} else {
							// They get nothing.  They lose.  Good day sir.
							game.nextReward = Game.FIRST_REWARD;
						}
						// time for next shot
						game.lastShot = game.nextShot;
						game.nextShot = new Shot();
					}										

					// count down time
					game.remainingSeconds = (int) ((endTime - System.currentTimeMillis())/1000);
					Persister.updateToServer(game);
				}
				commandRelay.stop();
				
				// GAME OVER
				game.remainingSeconds = 0;
				game.lastShot = null;
				game.nextShot = null;
				game.nextReward = 0;
				
				Player player = ContestPlatform.getPlayers().getPlayerMap().get(game.playerName);
				// TODO: Null check OR make the get blocking 
				player.incrementPlayCount();
				// Update player best score if warranted
				if (player.getHighScore() < game.score) {
					player.setHighScore(game.score);
				}
				
				Persister.updateToServer(game);
				Persister.updateToServer(ContestPlatform.getPlayers());
				
			}


		}
}
