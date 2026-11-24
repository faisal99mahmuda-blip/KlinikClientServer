package klinik.common;

import java.io.Serializable;

public class Request implements Serializable {

    public String action;
    public Object data;
    public String username, password;

    public Request(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public Request(String action, String u, String p) {
        this.action = action;
        username = u;
        password = p;
    }
}
