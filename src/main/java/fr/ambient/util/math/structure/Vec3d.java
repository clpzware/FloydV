package fr.ambient.util.math.structure;

import com.google.common.base.Objects;
import lombok.Getter;

@Getter
public class Vec3d {
    public final double x;
    public final double y;
    public final double z;

    public Vec3d(double xIn, double yIn, double zIn) {
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }

    public Vec3d crossProduct(Vec3d vec) {
        return new Vec3d(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(), this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    public double distanceSq(double toX, double toY, double toZ) {
        double d0 = this.getX() - toX;
        double d1 = this.getY() - toY;
        double d2 = this.getZ() - toZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distanceSqToCenter(double xIn, double yIn, double zIn) {
        double d0 = this.getX() + 0.5D - xIn;
        double d1 = this.getY() + 0.5D - yIn;
        double d2 = this.getZ() + 0.5D - zIn;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distanceSq(Vec3d to) {
        return this.distanceSq(to.getX(), to.getY(), to.getZ());
    }

    public String toString() {
        return Objects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    public Vec3d subtract(double viewerPosX, double viewerPosY, double viewerPosZ) {
        return new Vec3d(this.getX() - viewerPosX, this.getY() - viewerPosY, this.getZ() - viewerPosZ);
    }
}
