package broadcastGcp;

public class HelperClass {
     public static String makeMessage(long value1, long value2) {
        StringBuilder builder = new StringBuilder();
        builder.append(value1).append(",").append(value2);
        String address = builder.toString();
        return address;
    }
}