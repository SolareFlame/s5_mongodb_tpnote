package objects;

import org.bson.codecs.pojo.annotations.BsonProperty;

public record Rating(
        @BsonProperty("user_id") Integer user_id,
        @BsonProperty("name") String name,
        @BsonProperty("age") Integer age,
        @BsonProperty("gender") String gender,
        @BsonProperty("occupation") String occupation,
        @BsonProperty("rating") Integer rating,
        @BsonProperty("timestamp") Long timestamp
) {}
