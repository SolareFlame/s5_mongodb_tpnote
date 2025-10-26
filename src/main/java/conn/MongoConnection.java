package conn;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.*;

public class MongoConnection {
    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "TP_NOTE";

    public static MongoDatabase getDatabase() {
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(URI))
                .codecRegistry(codecRegistry)
                .build();

        MongoClient client = MongoClients.create(settings);
        return client.getDatabase(DB_NAME);
    }
}
