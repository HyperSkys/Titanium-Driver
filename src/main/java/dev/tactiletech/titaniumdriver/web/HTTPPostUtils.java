package dev.tactiletech.titaniumdriver.web;

import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all") // I'm not going to bother with the warnings.
public class HTTPPostUtils {

    @SneakyThrows
    public static InputStream sendPostRequest(String url, HashMap<String, String> contents) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);

        List<NameValuePair> pairs = new ArrayList<>();
        for (String key : contents.keySet()) {
            pairs.add(new BasicNameValuePair(key, contents.get(key)));
        }
        request.setEntity(new UrlEncodedFormEntity(pairs ));

        return client.execute(request).getEntity().getContent();
    }

    @SneakyThrows
    public static InputStream sendGetWithBody(String url, HashMap<String, String> contents) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGetWithEntity entity = new HttpGetWithEntity();
        entity.setURI(new java.net.URI(url));

        List<NameValuePair> pairs = new ArrayList<>();
        for (String key : contents.keySet()) {
            pairs.add(new BasicNameValuePair(key, contents.get(key)));
        }
        entity.setEntity(new UrlEncodedFormEntity(pairs));

        return httpClient.execute(entity).getEntity().getContent();
    }

}
