package com.cldellow.manu.serve;

public class HttpResponse {
    public final int status;
    public String body;

    public HttpResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }
}
