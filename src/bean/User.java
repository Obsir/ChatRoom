package bean;

import java.io.Serializable;

/**
 * Created by Obser on 2017/5/2.
 */
public class User implements Serializable{
    private String name;
    private String ip;
    private String password;

    public User(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(){

    }

    @Override
    public boolean equals(Object obj) {
        User user = null;
        if(obj instanceof User){
            user = (User) obj;
        }
        return user.getIp().equals(this.ip) && user.getName().equals(this.name);
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

    public User(String name, String password, String ip) {
        this.password = password;
        this.name = name;
        this.ip = ip;
    }
}
