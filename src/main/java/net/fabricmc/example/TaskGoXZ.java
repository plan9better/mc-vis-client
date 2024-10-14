package net.fabricmc.example;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.util.math.BlockPos;

import java.util.Queue;

public class TaskGoXZ extends Task{
    private final int x;
    private final int z;

    public TaskGoXZ(int x, int z){
        this.x = x;
        this.z = z;
    }
    public TaskGoXZ(BlockPos pos){
        this.x = pos.getX();
        this.z = pos.getZ();
    }

    @Override
    public void Do(IBaritone baritone){
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalXZ(x, z));
    }
    @Override
    public String toString(){
        return String.format("Go to X=%d Y=%d", this.x, this.z);
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }
}
