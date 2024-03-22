package dev.tactiletech.titaniumdriver.web;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
    @Override
    public String getMethod() {
        return "GET";
    }
}
