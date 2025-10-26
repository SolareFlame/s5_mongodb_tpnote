package objects;

public record Rating(
        Integer user_id,
        String name,
        Integer  age,
        String gender,
        String occupation,
        Integer  rating,
        Long timestamp
) {}
