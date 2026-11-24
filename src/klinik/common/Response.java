package klinik.common;

import java.io.Serializable;

public class Response implements Serializable {

    public boolean success;
    public String message;
    public Object data;
    public String role;

    public Response(boolean s, String m, Object d) {
        success = s;
        message = m;
        data = d;
    }

    public Response(boolean s, String m, Object d, String r) {
        success = s;
        message = m;
        data = d;
        role = r;
    }
}
