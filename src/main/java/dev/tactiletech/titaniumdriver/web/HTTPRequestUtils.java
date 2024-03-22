package dev.tactiletech.titaniumdriver.web;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all") // I'm not going to bother with the warnings.
public class HTTPRequestUtils {

    public static InputStream sendPostRequest(String url, HashMap<String, String> contents) {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost entity = new HttpPost(url);

            List<NameValuePair> pairs = new ArrayList<>();
            for (String key : contents.keySet()) {
                pairs.add(new BasicNameValuePair(key, contents.get(key)));
            }
            entity.setEntity(new UrlEncodedFormEntity(pairs));

            return httpClient.execute(entity).getEntity().getContent();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to send POST request to " + url);
        }
    }

    public static InputStream sendGetWithBody(String url, HashMap<String, String> contents) {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGetWithEntity entity = new HttpGetWithEntity();
            entity.setURI(new URI(url));

            List<NameValuePair> pairs = new ArrayList<>();
            for (String key : contents.keySet()) {
                pairs.add(new BasicNameValuePair(key, contents.get(key)));
            }
            entity.setEntity(new UrlEncodedFormEntity(pairs));

            return httpClient.execute(entity).getEntity().getContent();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to send GET request with body to " + url);
        }
    }

}
