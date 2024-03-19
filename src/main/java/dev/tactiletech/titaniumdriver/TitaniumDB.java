package dev.tactiletech.titaniumdriver;

import dev.tactiletech.titaniumdriver.database.Collection;
import dev.tactiletech.titaniumdriver.database.Database;
import dev.tactiletech.titaniumdriver.database.exceptions.ConnectionFailedException;
import dev.tactiletech.titaniumdriver.database.exceptions.collection.FailedToCreateCollectionException;
import dev.tactiletech.titaniumdriver.database.exceptions.collection.FailedToDeleteCollectionException;
import dev.tactiletech.titaniumdriver.database.exceptions.collection.FailedToGetAllCollectionsException;
import dev.tactiletech.titaniumdriver.database.exceptions.collection.FailedToGetCollectionException;
import dev.tactiletech.titaniumdriver.database.exceptions.database.*;
import dev.tactiletech.titaniumdriver.database.exceptions.document.*;
import dev.tactiletech.titaniumdriver.database.utils.EncryptorUtils;
import dev.tactiletech.titaniumdriver.utils.HTTPPostUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class TitaniumDB {

    private final String username;
    private final String password;
    private final String host;
    private final String port;

    public TitaniumDB(String connectionURI) {
        try {
            this.username = connectionURI.split("tdb://")[1].split(":")[0];
            this.password = connectionURI.split("@")[0].split("://")[1].split(":")[1];
            this.host = connectionURI.split("@")[1].split(":")[0];
            this.port = connectionURI.split("@")[1].split(":")[1];
            connect();
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new ConnectionFailedException("Your connection string is malformed.");
        }
    }

    public TitaniumDB(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = String.valueOf(port);
        connect();
    }

    public static String buildConnectionURI(String username, String password, String host, int port) {
        return "tdb://" + username + ":" + password + "@" + host + ":" + port;
    }

    private void connect() {
        try {
            URL url = new URL("http://" + host + ":" + port + "/storage/auth");
            HashMap<String, String> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);

            JSONTokener tokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
            JSONObject object = new JSONObject(tokener);
            if (!object.getJSONObject("data").getBoolean("success")) throw new ConnectionFailedException(object.getJSONObject("data").getString("reason"));
        } catch (Exception exception) {
            throw new ConnectionFailedException(exception.getMessage());
        }
    }

    public boolean databaseExists(String dbName) {
        try {
            URL url = new URL("http://" + host + ":" + port + "/storage/databaseExists");
            HashMap<String, String> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);
            map.put("databaseName", dbName);

            JSONTokener tokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
            JSONObject object = new JSONObject(tokener);
            return object.getJSONObject("data").getBoolean("exists");
        } catch (Exception exception) {
            throw new FailedToGetDatabaseException(exception.getMessage());
        }
    }

    public Database[] getDatabases() {
        try {
            URL url = new URL("http://"+host+":"+port+"/storage/getAllDatabases");
            HashMap<String, String> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);

            JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
            JSONObject object = new JSONObject(jsonTokener);
            if (!object.getJSONObject("data").getBoolean("success")) throw new FailedToGetAllDatabasesException(object.getJSONObject("data").getString("reason"));
            JSONArray collections = object.getJSONObject("data").getJSONArray("data");
            ArrayList<Database> arrayList = new ArrayList<>();
            for (Object name : collections) {
                arrayList.add(createDatabase((String) name));
            }
            return arrayList.toArray(new Database[0]);
        } catch (Exception exception) {
            throw new FailedToGetAllDatabasesException(exception.getMessage());
        }
    }

    public Database grabNewDatabase(String dbName) {
        return new Database() {
            @Override
            public Collection[] getCollections() {
                try {
                    URL url = new URL("http://" + host + ":" + port + "/storage/getAllCollections");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("username", username);
                    map.put("password", password);
                    map.put("databaseName", dbName);

                    JSONTokener tokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                    JSONObject object = new JSONObject(tokener);
                    if(!object.getJSONObject("data").getBoolean("success")) throw new FailedToGetAllCollectionsException(object.getJSONObject("data").getString("reason"));
                    JSONArray collections = object.getJSONObject("data").getJSONArray("data");
                    ArrayList<Collection> collectionList = new ArrayList<>();
                    for (Object collectionName : collections) {
                        collectionList.add(createCollection((String) collectionName));
                    }
                    return collectionList.toArray(new Collection[0]);
                } catch (Exception exception) {
                    throw new FailedToGetAllCollectionsException(exception.getMessage());
                }
            }

            @Override
            public Collection getCollection(String name) {
                try {
                    URL url = new URL("http://" + host + ":" + port + "/storage/getCollection");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("username", username);
                    map.put("password", password);
                    map.put("databaseName", dbName);
                    map.put("collectionName", name);

                    JSONTokener tokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                    JSONObject object = new JSONObject(tokener);
                    if (!object.getJSONObject("data").getBoolean("success")) throw new FailedToGetCollectionException(object.getJSONObject("data").getString("reason"));

                    return new Collection() {
                        @Override
                        public JSONObject[] getDocuments() {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/getAllDocuments");
                                HashMap<String, String> map = new HashMap<>();
                                map.put("username", username);
                                map.put("password", password);
                                map.put("databaseName", dbName);
                                map.put("collectionName", name);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToGetAllDocumentsException(jsonObject.getJSONObject("data").getString("reason"));
                                JSONObject documents = (JSONObject) jsonObject.getJSONObject("data").get("data");
                                return documents.keySet().stream().map(documents::getJSONObject).toArray(JSONObject[]::new);
                            } catch (Exception exception) {
                                throw new FailedToGetAllDocumentsException(exception.getMessage());
                            }
                        }

                        @Override
                        public JSONObject getDocument(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/getDocument");
                                HashMap<String, String> map = new HashMap<>();
                                map.put("username", username);
                                map.put("password", password);
                                map.put("databaseName", dbName);
                                map.put("collectionName", name);
                                map.put("documentName", key);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToGetDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                                return jsonObject.getJSONObject("data").getJSONObject("data");
                            } catch (Exception exception) {
                                throw new FailedToGetDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public void addDocument(String key, JSONObject document) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/createDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                contents.put("data", EncryptorUtils.getBytes(document.toString()));
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToCreateDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToCreateDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public void deleteDocument(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/deleteDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToDeleteDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToDeleteDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public void updateDocument(String key, JSONObject document) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/replaceDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                contents.put("data", EncryptorUtils.getBytes(document.toString()));
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToReplaceDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToReplaceDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public boolean documentExists(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/documentExists");
                                HashMap<String, String> map = new HashMap<>();
                                map.put("username", username);
                                map.put("password", password);
                                map.put("databaseName", dbName);
                                map.put("collectionName", name);
                                map.put("documentName", key);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                return jsonObject.getJSONObject("data").getBoolean("exists");
                            } catch (Exception exception) {
                                throw new FailedToCheckIfDocumentExists(exception.getMessage());
                            }
                        }

                        @Override
                        public void delete() {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/deleteCollection");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToDeleteCollectionException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToDeleteCollectionException(exception.getMessage());
                            }
                        }

                        @Override
                        public String getName() {
                            return name;
                        }

                        @Override
                        public void replaceDocument(String key, JSONObject document) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/replaceDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                contents.put("data", EncryptorUtils.getBytes(document.toString()));
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToReplaceDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToReplaceDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public JSONObject searchDocument(String key, Object value) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/search");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("key", key);
                                contents.put("value", value.toString());

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) return null;
                                return jsonObject.getJSONObject("data").getJSONObject("data");
                            } catch (Exception exception) {
                                throw new FailedToSearchDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public JSONObject searchDocument(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/searchOpt");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("key", key);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) return null;
                                return jsonObject.getJSONObject("data").getJSONObject("data");
                            } catch (Exception exception) {
                                throw new FailedToSearchDocumentException(exception.getMessage());
                            }
                        }
                    };
                } catch (Exception exception) {
                    throw new FailedToGetCollectionException(exception.getMessage());
                }
            }

            @Override
            public Collection createCollection(String name) {
                try {
                    URL url = new URL("http://" + host + ":" + port + "/storage/createCollection");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("username", username);
                    map.put("password", password);
                    map.put("databaseName", dbName);
                    map.put("collectionName", name);
                    JSONObject object = new JSONObject(new JSONTokener(new InputStreamReader(HTTPPostUtils.sendPostRequest(url.toString(), map))));
                    if (!object.getJSONObject("data").getBoolean("success")) throw new FailedToCreateCollectionException(object.getJSONObject("data").getString("reason"));

                    return new Collection() {
                        @Override
                        public JSONObject[] getDocuments() {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/getAllDocuments");
                                HashMap<String, String> map = new HashMap<>();
                                map.put("username", username);
                                map.put("password", password);
                                map.put("databaseName", dbName);
                                map.put("collectionName", name);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToGetAllDocumentsException(jsonObject.getJSONObject("data").getString("reason"));
                                JSONObject documents = (JSONObject) jsonObject.getJSONObject("data").get("data");
                                return documents.keySet().stream().map(documents::getJSONObject).toArray(JSONObject[]::new);
                            } catch (Exception exception) {
                                throw new FailedToGetAllDocumentsException(exception.getMessage());
                            }
                        }

                        @Override
                        public JSONObject getDocument(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/getDocument");
                                HashMap<String, String> map = new HashMap<>();
                                map.put("username", username);
                                map.put("password", password);
                                map.put("databaseName", dbName);
                                map.put("collectionName", name);
                                map.put("documentName", key);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToGetDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                                return jsonObject.getJSONObject("data").getJSONObject("data");
                            } catch (Exception exception) {
                                throw new FailedToGetDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public void addDocument(String key, JSONObject document) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/createDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                contents.put("data", EncryptorUtils.getBytes(document.toString()));
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToCreateDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToCreateDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public void deleteDocument(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/deleteDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToDeleteDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToDeleteDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public void updateDocument(String key, JSONObject document) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/replaceDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                contents.put("data", EncryptorUtils.getBytes(document.toString()));
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToReplaceDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToReplaceDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public boolean documentExists(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/documentExists");
                                HashMap<String, String> map = new HashMap<>();
                                map.put("username", username);
                                map.put("password", password);
                                map.put("databaseName", dbName);
                                map.put("collectionName", name);
                                map.put("documentName", key);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                return jsonObject.getJSONObject("data").getBoolean("exists");
                            } catch (Exception exception) {
                                throw new FailedToCheckIfDocumentExists(exception.getMessage());
                            }
                        }

                        @Override
                        public void delete() {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/deleteCollection");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToDeleteCollectionException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToDeleteCollectionException(exception.getMessage());
                            }
                        }

                        @Override
                        public String getName() {
                            return name;
                        }

                        @Override
                        public void replaceDocument(String key, JSONObject document) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/replaceDocument");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("documentName", key);
                                contents.put("data", EncryptorUtils.getBytes(document.toString()));
                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToReplaceDocumentException(jsonObject.getJSONObject("data").getString("reason"));
                            } catch (Exception exception) {
                                throw new FailedToReplaceDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public JSONObject searchDocument(String key, Object value) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/search");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("key", key);
                                contents.put("value", value.toString());

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) return null;
                                return jsonObject.getJSONObject("data").getJSONObject("data");
                            } catch (Exception exception) {
                                throw new FailedToSearchDocumentException(exception.getMessage());
                            }
                        }

                        @Override
                        public JSONObject searchDocument(String key) {
                            try {
                                URL url = new URL("http://" + host + ":" + port + "/storage/searchOpt");
                                HashMap<String, String> contents = new HashMap<>();
                                contents.put("username", username);
                                contents.put("password", password);
                                contents.put("databaseName", dbName);
                                contents.put("collectionName", name);
                                contents.put("key", key);

                                JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), contents));
                                JSONObject jsonObject = new JSONObject(jsonTokener);
                                if (!jsonObject.getJSONObject("data").getBoolean("success")) return null;
                                return jsonObject.getJSONObject("data").getJSONObject("data");
                            } catch (Exception exception) {
                                throw new FailedToSearchDocumentException(exception.getMessage());
                            }
                        }
                    };
                } catch (Exception exception) {
                    throw new FailedToCreateCollectionException(exception.getMessage());
                }
            }

            @Override
            public boolean collectionExists(String name) {
                try {
                    URL url = new URL("http://" + host + ":" + port + "/storage/collectionExists");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("username", username);
                    map.put("password", password);
                    map.put("databaseName", dbName);
                    map.put("collectionName", name);

                    JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendGetWithBody(url.toString(), map));
                    JSONObject jsonObject = new JSONObject(jsonTokener);
                    return jsonObject.getJSONObject("data").getBoolean("exists");
                } catch (Exception exception) {
                    throw new FailedToCheckIfCollectionExists(exception.getMessage());
                }
            }

            @Override
            public void deleteCollection(String name) {
                try {
                    URL url = new URL("http://" + host + ":" + port + "/storage/deleteCollection");
                    HashMap<String, String> contents = new HashMap<>();
                    contents.put("username", username);
                    contents.put("password", password);
                    contents.put("databaseName", dbName);
                    contents.put("collectionName", name);
                    JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                    JSONObject jsonObject = new JSONObject(jsonTokener);
                    if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToDeleteCollectionException(jsonObject.getJSONObject("data").getString("reason"));
                } catch (Exception exception) {
                    throw new FailedToDeleteCollectionException(exception.getMessage());
                }
            }

            @Override
            public void delete() {
                try {
                    URL url = new URL("http://" + host + ":" + port + "/storage/deleteDatabase");
                    HashMap<String, String> contents = new HashMap<>();
                    contents.put("username", username);
                    contents.put("password", password);
                    contents.put("databaseName", dbName);
                    JSONTokener jsonTokener = new JSONTokener(HTTPPostUtils.sendPostRequest(url.toString(), contents));
                    JSONObject jsonObject = new JSONObject(jsonTokener);
                    if (!jsonObject.getJSONObject("data").getBoolean("success")) throw new FailedToDeleteDatabaseException(jsonObject.getJSONObject("data").getString("reason"));
                } catch (Exception exception) {
                    throw new FailedToDeleteDatabaseException(exception.getMessage());
                }
            }

            @Override
            public String getName() {
                return dbName;
            }
        };
    }

    public Database getDatabase(String dbName) {
        try {
            URL url = new URL("http://" + host + ":" + port + "/storage/getDatabase");
            HashMap<String, String> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);
            map.put("databaseName", dbName);
            JSONObject object = new JSONObject(new JSONTokener(new InputStreamReader(HTTPPostUtils.sendGetWithBody(url.toString(), map))));
            if (!object.getJSONObject("data").getBoolean("success")) throw new FailedToGetDatabaseException(object.getJSONObject("data").getString("reason"));
            return grabNewDatabase(dbName);
        } catch (Exception exception) {
            throw new FailedToGetDatabaseException(exception.getMessage());
        }
    }

    public Database createDatabase(String dbName) {
        try {
            URL url = new URL("http://" + host + ":" + port + "/storage/createDatabase");
            HashMap<String, String> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);
            map.put("databaseName", dbName);
            JSONObject object = new JSONObject(new JSONTokener(new InputStreamReader(HTTPPostUtils.sendPostRequest(url.toString(), map))));
            if (!object.getJSONObject("data").getBoolean("success")) throw new FailedToCreateDatabaseException(object.getJSONObject("data").getString("reason"));
            return grabNewDatabase(dbName);
        } catch (Exception exception) {
            throw new FailedToCreateDatabaseException(exception.getMessage());
        }
    }
}
