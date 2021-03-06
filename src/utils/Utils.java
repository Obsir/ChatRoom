package utils;

import java.io.Closeable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Obser on 2017/5/2.
 */
public class Utils {
    public static boolean isEmpty(String str){
        if(str == null || str.equals("")){
            return true;
        }
        return false;
    }

    public static void closeQuietly(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch (Throwable var2) {
                ;
            }
        }
    }

    /**
     * MD5加密
     *
     * @param password
     * @return
     */
    public static String encode(String password){
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");//获取MD5算法
            byte[] digest = instance.digest(password.getBytes());//对字符串加密，返回字节数组

            StringBuffer sb = new StringBuffer();

            for (byte b : digest) {
                int i = b & 0xff;//获取字节的低八位有效值
                String hexString = Integer.toHexString(i);//将整数转为16进制


                if(hexString.length() < 2){
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            return sb.toString();


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //没有该算法时，抛出异常，不会走到这里
        }

        return "";
    }

}
