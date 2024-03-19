# Titanium-Driver [v1.0.0]

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![GitHub](https://img.shields.io/github/languages/code-size/HyperSkys/Titanium-Driver?color=cyan&label=Size&labelColor=000000&logo=GitHub&style=for-the-badge)
![GitHub](https://img.shields.io/github/license/HyperSkys/Titanium-Driver?color=violet&logo=GitHub&labelColor=000000&style=for-the-badge)
![Twitter URL](https://img.shields.io/twitter/url?color=%2300acee&label=TWITTER&logo=twitter&style=for-the-badge&url=https%3A%2F%2Ftwitter.com%2FDev_HyperSkys)

**Titanium-Driver** is a driver for the TitaniumDB Web Database for the Java programming language.

### Installing

You can install TitaniumDB-Driver into your Java Project by downloading the .jar file from the latest release, after it finishes downloading add it to your artifacts, or you can just add the Maven dependency in your pom.xml.

### Socials

You can contact me on Discord, my discord username is McDonald's#7625 it may change. You can also contact me on Twitter, my twitter username is @Dev_HyperSkys.

### Features

➣ Fast Speeds Gaurteed.

➣ Databases, Collections, and Documents.

➣ Document Searching.

➣ Full control over your database.

➣ And so much more...

### API Example

Here is an example of connecting to the TitaniumDB Server, creating a Database, creating a Collection, and adding a document with a key-value pair to the collection.

```java
TitaniumDB titaniumDB = new TitaniumDB("tdb://root:password@localhost:2048");
Database database = titaniumDB.createDatabase("Testing");
Collection collection = database.createCollection("testing");
JSONObject jsonObject = new JSONObject();
jsonObject.put("key", "value");
collection.addDocument("key", jsonObject);
```

## License
This project is licensed under [Eclipse Public License](https://github.com/HyperSkys/Titanium-Driver/blob/main/LICENSE)
