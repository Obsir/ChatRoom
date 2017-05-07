package protocol;

import com.google.gson.Gson;
import utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Obser on 2017/5/2.
 */
public class Protocol {
    public static final String CLOSE = Utils.encode("CLOSE");
    public static final String MSG = Utils.encode("MSG");
    public static final String MSG_ALL = Utils.encode("MSG_ALL");
    public static final String INIT = Utils.encode("INIT");
    public static final String FAILED = Utils.encode("FAILED");
    public static final String LIST = Utils.encode("LIST");
    public static final String MSG_PRIVATE_TO = Utils.encode("MSG_PRIVATE_TO");
    public static final String MSG_PRIVATE_FROM = Utils.encode("MSG_PRIVATE_FROM");
    public static final String CONFIRM = Utils.encode("CONFIRM");
    private static Gson gson = new Gson();

    /**
     * 封装消息
     * @param message
     * @return
     */
    public static String packMessage(Message message){
        gson = new Gson();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        String time = dateFormat.format(new Date());
        message.setTime(time);
        return gson.toJson(message);
    }

    /**
     * 解析消息
     * @param json
     * @return
     */
    public static Message unPackMessage(String json){
        Message message = gson.fromJson(json, Message.class);
        return message;
    }

    /**
     * 协议消息包对象
     */
    public static class Message{
        private String name;
        private String ip;
        private String message;
        private String id;
        private String time;
        private String toUser;
        private boolean confirmed;

        public boolean getConfirmed() {
            return confirmed;
        }

        public void setConfirmed(boolean confirmed) {
            this.confirmed = confirmed;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        private String password;

        public String getToUser() {
            return toUser;
        }

        public void setToUser(String toUser) {
            this.toUser = toUser;
        }

        private List<String> nameList;

        public Message() {
        }

        public Message(String name, String ip, String toUser, String message, String id) {
            this.name = name;
            this.ip = ip;
            this.message = message;
            this.id = id;
            this.toUser = toUser;
        }



        public List<String> getNameList() {
            return nameList;
        }

        public void setNameList(List<String> nameList) {
            this.nameList = nameList;
        }

        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Message(String id) {
            this.id = id;
        }

        public Message(String name, String ip, String id){
            this.name = name;
            this.ip = ip;
            this.id = id;
        }

        public Message(String name, String ip, String message, String id) {
            this.name = name;
            this.ip = ip;
            this.message = message;
            this.id = id;
        }



    }
}
