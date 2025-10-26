package objects;

public record Utilisateur(
        int user_id,
        String name,
        int age,
        String gender,
        String occupation
) {
    public static Utilisateur fromRating(Rating r) {
        return new Utilisateur(
                r.user_id(),
                r.name(),
                r.age(),
                r.gender(),
                r.occupation()
        );
    }
}
