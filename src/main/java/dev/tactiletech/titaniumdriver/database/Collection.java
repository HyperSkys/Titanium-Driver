package dev.tactiletech.titaniumdriver.database;

import org.json.JSONObject;

@SuppressWarnings("all")
public interface Collection {
    JSONObject[] getDocuments();
    JSONObject getDocument(String key);
    void addDocument(String key, JSONObject document);
    void deleteDocument(String key);
    void updateDocument(String key, JSONObject document);
    boolean documentExists(String key);
    void delete();
    String getName();
    void replaceDocument(String key, JSONObject document);
    JSONObject searchDocument(String key, Object value);
    JSONObject searchDocument(String key);
}
