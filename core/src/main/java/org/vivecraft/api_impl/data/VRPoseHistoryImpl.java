package org.vivecraft.api_impl.data;

import org.bukkit.util.Vector;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.api_impl.VRAPIImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class VRPoseHistoryImpl implements VRPoseHistory {

    // Holds historical VRPose data. The index into here is simply the number of ticks back that data is, with index
    // 0 being 0 ticks back.
    private final LinkedList<VRPose> dataQueue = new LinkedList<>();

    public VRPoseHistoryImpl() {}

    public void addPose(VRPose pose) {
        this.dataQueue.addFirst(pose);
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
        return Collections.unmodifiableList(new ArrayList<>(this.dataQueue));
    }

    @Override
    public VRPose getHistoricalData(int ticksBack) throws IllegalArgumentException {
        checkTicksBack(ticksBack);
        if (this.dataQueue.size() <= ticksBack) {
            return null;
        }
        return this.dataQueue.get(ticksBack);
    }

    @Override
    public Vector netMovement(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return new Vector();
        }
        VRBodyPartData currentData = this.dataQueue.getFirst().getBodyPartData(bodyPart);
        if (currentData == null) {
            return null;
        }
        Vector current = currentData.getPos();
        VRBodyPartData oldData = this.dataQueue.get(maxTicksBack).getBodyPartData(bodyPart);
        if (oldData == null) {
            return null;
        }
        Vector old = oldData.getPos();
        return current.subtract(old);
    }

    @Override
    public Vector averageVelocity(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return new Vector();
        }
        maxTicksBack = getNumTicksBack(maxTicksBack);
        List<Vector> diffs = new ArrayList<>(maxTicksBack);
        for (int i = 0; i <= maxTicksBack; i++) {
            VRBodyPartData newer = this.dataQueue.get(i).getBodyPartData(bodyPart);
            VRBodyPartData older = this.dataQueue.get(i + 1).getBodyPartData(bodyPart);
            if (newer == null || older == null) {
                break;
            }
            diffs.add(newer.getPos().subtract(older.getPos()));
        }
        if (diffs.isEmpty()) {
            // Return no change if the body part is available but no historical data or null if body part isn't
            // available.
            return this.dataQueue.getFirst().getBodyPartData(bodyPart) != null ? new Vector() : null;
        }
        return new Vector(
            diffs.stream().mapToDouble(Vector::getX).average().orElse(0),
            diffs.stream().mapToDouble(Vector::getY).average().orElse(0),
            diffs.stream().mapToDouble(Vector::getZ).average().orElse(0)
        );
    }

    @Override
    public double averageSpeed(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.size() <= 1) {
            return 0;
        }
        maxTicksBack = getNumTicksBack(maxTicksBack);
        List<Double> speeds = new ArrayList<>(maxTicksBack);
        for (int i = 0; i <= maxTicksBack; i++) {
            VRBodyPartData newer = this.dataQueue.get(i).getBodyPartData(bodyPart);
            VRBodyPartData older = this.dataQueue.get(i + 1).getBodyPartData(bodyPart);
            if (newer == null || older == null) {
                break;
            }
            speeds.add(newer.getPos().subtract(older.getPos()).length());
        }
        return speeds.stream().mapToDouble(Double::valueOf).average().orElse(0);
    }

    @Override
    public Vector averagePosition(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException {
        checkPartNonNull(bodyPart);
        checkTicksBack(maxTicksBack);
        if (this.dataQueue.isEmpty()) {
            return null;
        }
        maxTicksBack = getNumTicksBack(maxTicksBack);
        List<Vector> positions = new ArrayList<>(maxTicksBack);
        int i = 0;
        for (VRPose pose : this.dataQueue) {
            VRBodyPartData data = pose.getBodyPartData(bodyPart);
            if (data == null) {
                break;
            }
            positions.add(data.getPos());
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
}
