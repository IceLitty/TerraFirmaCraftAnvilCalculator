package moe.icyr.tfc.anvil.calc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * {@link net.minecraft.world.level.levelgen.XoroshiroRandomSource}
 */
public class XoroshiroRandomUtil {

    private long seedLo;
    private long seedHi;

    public int calcTarget(long seed, String recipeId) {
        upgradeSeedTo128bit(seed);
        if ((seedLo | seedHi) == 0L) {
            seedLo = -7046029254386353131L;
            seedHi = 7640891576956012809L;
        }
        long _seedLo = nextLong();
        long _seedHi = nextLong();
        seedLo = _seedLo;
        seedHi = _seedHi;
        fromHashOf(recipeId);
        int i = nextInt(154 - 2 * 40);
        i = i + 40;
        return i;
    }

    private void upgradeSeedTo128bit(long p_189332_) {
        long i = p_189332_ ^ 7640891576956012809L;
        long j = i + -7046029254386353131L;
        seedLo = mixStafford13(i);
        seedHi = mixStafford13(j);
    }

    private long mixStafford13(long p_189330_) {
        p_189330_ = (p_189330_ ^ p_189330_ >>> 30) * -4658895280553007687L;
        p_189330_ = (p_189330_ ^ p_189330_ >>> 27) * -7723592293110705685L;
        return p_189330_ ^ p_189330_ >>> 31;
    }

    private long nextLong() {
        long i = seedLo;
        long j = seedHi;
        long k = Long.rotateLeft(i + j, 17) + i;
        j ^= i;
        seedLo = Long.rotateLeft(i, 49) ^ j ^ j << 21;
        seedHi = Long.rotateLeft(j, 28);
        return k;
    }

    private void fromHashOf(String p_190134_) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md5.update(p_190134_.getBytes(StandardCharsets.UTF_8));
        byte[] abyte = md5.digest();
        long i = fromBytes(abyte[0], abyte[1], abyte[2], abyte[3], abyte[4], abyte[5], abyte[6], abyte[7]);
        long j = fromBytes(abyte[8], abyte[9], abyte[10], abyte[11], abyte[12], abyte[13], abyte[14], abyte[15]);
        seedLo = i ^ seedLo;
        seedHi = j ^ seedHi;
    }

    private long fromBytes(
            byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        return (b1 & 0xFFL) << 56
                | (b2 & 0xFFL) << 48
                | (b3 & 0xFFL) << 40
                | (b4 & 0xFFL) << 32
                | (b5 & 0xFFL) << 24
                | (b6 & 0xFFL) << 16
                | (b7 & 0xFFL) << 8
                | (b8 & 0xFFL);
    }

    private int nextInt(int p_190118_) {
        if (p_190118_ <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        } else {
            long i = Integer.toUnsignedLong((int) nextLong());
            long j = i * (long) p_190118_;
            long k = j & 4294967295L;
            if (k < (long) p_190118_) {
                for (int l = Integer.remainderUnsigned(~p_190118_ + 1, p_190118_); k < (long) l; k = j & 4294967295L) {
                    i = Integer.toUnsignedLong((int) nextLong());
                    j = i * (long) p_190118_;
                }
            }
            long i1 = j >> 32;
            return (int) i1;
        }
    }

}
