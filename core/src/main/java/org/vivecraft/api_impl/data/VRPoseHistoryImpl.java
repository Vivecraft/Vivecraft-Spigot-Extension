package org.vivecraft.api_impl.data;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.api_impl.VRAPIImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VRPoseHistoryImpl implements VRPoseHistory {

    // Holds historical VRPose data. The index into here is simply the number of ticks back that data is, with index
    // 0 being 0 ticks back.
    private final LinkedList<PoseData> dataQueue = new LinkedList<>();

    public VRPoseHistoryImpl() {}

    public void addPose(VRPose pose, Vector playerPos) {
        this.dataQueue.addFirst(new PoseData(pose, playerPos));
        // + 1 here since index 0 is 0 ticks back.
        if (this.dataQueue.size() > VRAPIImpl.MAX_HISTORY_TICKS + 1) {
            this.dataQueue.removeLast();
        }
    }

    public void clear() {
        this.dataQueue.clear();
    }

    @Override
    public int ticksOfHistory() {
        return this.dataQueue.size() - 1;
    }

    @Override
    public List<VRPose> getAllHistoricalData() {
        return Collections.unmodifiableList(
            this.dataQueue.stream().map(data -> data.pose).collect(Collectors.toList()));
    }

    @Override
    public VRPose getHistoricalData(int ticksBack, boolean playerPositionRelative) throws IllegalArgumentException {
        checkTicksBack(ticksBack);
        if (this.dataQueue.size() <= ticksBack) {
            return null;
        }
        return this.dataQueue.get(ticksBack).getPose(playerPositionRelative);
    }

    @Override
    public Vector netMovement(
        VRBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException
    {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return new Vector();
        }
        Vector current = this.dataQueue.getFirst().getPos(bodyPart, playerPositionRelative);
        if (current == null) {
            return null;
        }
        Vector old = this.dataQueue.get(maxTicksBack).getPos(bodyPart, playerPositionRelative);
        if (old == null) {
            return null;
        }
        return current.subtract(old);
    }

    @Override
    public Vector averageVelocity(
        VRBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException
    {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return new Vector();
        }
        maxTicksBack = getNumTicksBack(maxTicksBack);
        List<Vector> diffs = new ArrayList<>(maxTicksBack);
        for (int i = 0; i < maxTicksBack; i++) {
            Vector newer = this.dataQueue.get(i).getPos(bodyPart, playerPositionRelative);
            Vector older = this.dataQueue.get(i + 1).getPos(bodyPart, playerPositionRelative);
            if (newer == null || older == null) {
                break;
            }
            diffs.add(newer.subtract(older));
        }
        if (diffs.isEmpty()) {
            // Return no change if the body part is available but no historical data or null if body part isn't
            // available.
            return this.dataQueue.getFirst().pose.getBodyPartData(bodyPart) != null ? new Vector() : null;
        }
        return new Vector(
            diffs.stream().mapToDouble(Vector::getX).average().orElse(0),
            diffs.stream().mapToDouble(Vector::getY).average().orElse(0),
            diffs.stream().mapToDouble(Vector::getZ).average().orElse(0)
        );
    }

    @Override
    public double averageSpeed(
        VRBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException
    {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return 0;
        }
        maxTicksBack = getNumTicksBack(maxTicksBack);
        List<Double> speeds = new ArrayList<>(maxTicksBack);
        for (int i = 0; i < maxTicksBack; i++) {
            Vector newer = this.dataQueue.get(i).getPos(bodyPart, playerPositionRelative);
            Vector older = this.dataQueue.get(i + 1).getPos(bodyPart, playerPositionRelative);
            if (newer == null || older == null) {
                break;
            }
            speeds.add(newer.distance(older));
        }
        return speeds.stream().mapToDouble(Double::valueOf).average().orElse(0);
    }

    @Override
    public Vector averagePosition(
        VRBodyPart bodyPart, int maxTicksBack, boolean playerPositionRelative) throws IllegalArgumentException
    {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.isEmpty()) {
            return null;
        }
        maxTicksBack = getNumTicksBack(maxTicksBack);
        List<Vector> positions = new ArrayList<>(maxTicksBack);
        int i = 0;
        for (PoseData poseData : this.dataQueue) {
            Vector pos = poseData.getPos(bodyPart, playerPositionRelative);
            if (pos == null) {
                break;
            }
            positions.add(pos);
            if (++i >= maxTicksBack) break;
        }
        if (positions.isEmpty()) {
            return null;
        }
        return new Vector(
            positions.stream().mapToDouble(Vector::getX).average().orElse(0),
            positions.stream().mapToDouble(Vector::getY).average().orElse(0),
            positions.stream().mapToDouble(Vector::getZ).average().orElse(0)
        );
    }

    private void checkTicksBack(int ticksBack) {
        if (ticksBack < 0 || ticksBack > VRAPIImpl.MAX_HISTORY_TICKS) {
            throw new IllegalArgumentException("Value must be between 0 and " + VRAPIImpl.MAX_HISTORY_TICKS + ".");
        }
    }

    private void checkPartNonNull(VRBodyPart bodyPart) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("Cannot get data for a null body part!");
        }
    }

    private int getNumTicksBack(int maxTicksBack) {
        if (this.dataQueue.size() <= maxTicksBack) {
            return this.dataQueue.size() - 1;
        } else {
            return maxTicksBack;
        }
    }

    private static class PoseData {
        protected final VRPose pose;
        protected final Vector playerPosition;

        private PoseData(VRPose pose, Vector playerPosition) {
            this.pose = pose;
            this.playerPosition = playerPosition;
        }

        @Nullable
        public Vector getPos(VRBodyPart vrBodyPart, boolean playerPositionRelative) {
            VRBodyPartData vrBodyPartData = this.pose.getBodyPartData(vrBodyPart);
            if (vrBodyPartData == null) {
                return null;
            } else if (playerPositionRelative) {
                return vrBodyPartData.getPos().subtract(this.playerPosition);
            }
            return vrBodyPartData.getPos();
        }

        public VRPose getPose(boolean playerPositionRelative) {
            if (playerPositionRelative) {
                return ((VRPoseImpl) this.pose).relativeToPosition(this.playerPosition);
            }
            return this.pose;
        }
    }
}
