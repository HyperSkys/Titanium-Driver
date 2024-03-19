package dev.tactiletech.titaniumdriver.database;

@SuppressWarnings("all")
public interface Database {
    Collection[] getCollections();
    Collection getCollection(String name);
    Collection createCollection(String name);
    boolean collectionExists(String name);
    void deleteCollection(String name);
    void delete();
    String getName();
}
