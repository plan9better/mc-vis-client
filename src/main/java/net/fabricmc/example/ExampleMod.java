package net.fabricmc.example;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ModInitializer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExampleMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("modid");
	private final IBaritone pb = BaritoneAPI.getProvider().getPrimaryBaritone();
	private final Queue<Task> q = new LinkedList<>();
	private final boolean cancel[] = {false};
	boolean isBusy = false;


	@Override
	public void onInitialize() {
		MinecraftClient client = MinecraftClient.getInstance();
		BaritoneAPI.getSettings().allowSprint.value = true;
		BaritoneAPI.getSettings().primaryTimeoutMS.value = 2000L;


		startServer();
		AtomicBoolean isScanned = new AtomicBoolean(false);

		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			if(!isScanned.get()){
				SurfaceScanner ss = new SurfaceScanner();
				isScanned.set(ss.scanSurfaceAroundPlayer());
			}
			handleCancelTask();
			handleTasks(client);
		});
	}

	private void startServer() {
		new Thread(() -> {
			try {
				ServerSocket ss = new ServerSocket(1337);
				while (true) {
					Socket socket = ss.accept();
					System.out.println("CLIENT CONNECTED");
					new ServerThread(socket, this.q, cancel).start();
				}
			} catch (Exception e) {
				System.out.println("Could not start server Exception: " + e.toString());
			}
		}).start();
	}

	private void handleCancelTask(){
		if(cancel[0]){
			pb.getCustomGoalProcess().setGoalAndPath(null);
			isBusy = false;
			cancel[0] = false;
		}
	}

	private void handleTasks(MinecraftClient client){
		if(client.player != null && client.world != null){
			if(!q.isEmpty() && !isBusy){
				Task t = q.remove();
				t.Do(pb);
				isBusy = true;
			} else {
				if(pb.getCustomGoalProcess().getGoal() == null){
					isBusy = false;
				}
			}
		}
	}

}
