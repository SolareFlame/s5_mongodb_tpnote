package objects;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import java.util.List;

public record Film(
        @BsonProperty("_id") int id,
        String title,
        String genres,
        List<Rating> ratings
) {}