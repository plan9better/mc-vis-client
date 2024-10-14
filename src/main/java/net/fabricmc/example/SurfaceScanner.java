package net.fabricmc.example;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SurfaceScanner {
    List<String> blocks = new ArrayList<>();
    public boolean scanSurfaceAroundPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return false;
        }

        // Get the player's chunk position
        ChunkPos playerChunkPos = new ChunkPos(client.player.getBlockPos());

        // Define the range around the player to scan
        int radius = 1; // This will scan a 5x5 chunk area around the player

        // Loop through chunks within the radius
        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + xOffset, playerChunkPos.z + zOffset);

                // Iterate over blocks in the chunk
                for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
                    for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                        // Find the highest block (surface block) at this x, z coordinate
                        Optional<BlockPos> surfaceBlockPos = findSurfaceBlock(client, x, z);
                        surfaceBlockPos.ifPresent(blockPos -> {
                            BlockState blockState = client.world.getBlockState(blockPos);

                            blocks.add(blockState.getBlock().getName().getString() + " " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ() + '\n');

                        });
                    }
                }
            }
        }
        sendBlocks("127.0.0.1", 8337);
        return true;
    }

    // Helper method to find the surface block
    private Optional<BlockPos> findSurfaceBlock(MinecraftClient client, int x, int z) {
        // Iterate downwards from the world height limit to find the first non-air block (surface block)
        for (int y = client.world.getTopY() - 1; y >= client.world.getBottomY(); y--) {
            BlockPos blockPos = new BlockPos(x, y, z);
            BlockState blockState = client.world.getBlockState(blockPos);
            BlockState blockAbove = client.world.getBlockState(blockPos.up());

            // Ensure that this block is exposed to the sky (i.e., no block above it)
            if (!blockState.isAir() && blockAbove.isAir()) {
                return Optional.of(blockPos);
            }
        }
        return Optional.empty();
    }
    public void sendBlocks(String ip, int port){
        Socket s = null;
        try{
            s = new Socket(ip, port);
        }catch (Exception exception){
            System.out.println("Exception while sending blocks to server: " + exception);
            return;
        }
        PrintWriter writer = null;
        try{
            writer = new PrintWriter(s.getOutputStream(), true);
        } catch (Exception e){
            System.out.println("Exception getting output stream from socket while trying to send blocks: " + e);
            return;
        }
        for(String block : blocks){
            writer.print(block);
        }
    }
}
