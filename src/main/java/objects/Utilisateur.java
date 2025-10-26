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
                r.name() != null ? r.name() : "Inconnu",
                r.age() != null ? r.age() : 0,
                r.gender() != null ? r.gender() : "N/A",
                r.occupation() != null ? r.occupation() : "N/A"
        );
    }
}
