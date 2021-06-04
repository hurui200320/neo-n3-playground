package info.skyblond.demo.neo.utils;

public class CommonUtils {
    public static double getGasWithDecimals(long value) {
        return value / Math.pow(10, 8);
    }

}
