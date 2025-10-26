package app;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import objects.Film;
import objects.Rating;
import objects.Utilisateur;
import org.bson.Document;

import java.util.*;
import java.util.stream.Stream;

public class FilmApp {

    private final MongoCollection<Film> filmsCollection;

    public FilmApp(MongoDatabase db) {
        this.filmsCollection = db.getCollection("films_fusion", Film.class);
    }

    /**
     * a. Afficher tous les films (titre, l’année de parution et nombre de notations) ayant un genre donnée. On classera les films par ordre décroissant du nombre de notations.
     * <ul>
     *     <li>Filtre par genre</li>
     *     <li>Tri par note décroissante</li>
     * </ul>
     * @param genre Le genre du film voulu
     */
    public void displayFilmsFiltered_g(String genre) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Film> filtered = films.stream()
                // Filtrer les films par genre
                .filter(f -> f.genres() != null && f.genres().contains(genre))
                // Trier par nombre de notations décroissant
                .sorted(Comparator.comparingInt(f -> -((f.ratings() != null) ? f.ratings().size() : 0)))
                .toList();

        filtered.forEach(
                f -> System.out.printf("Titre: %s | Nombre de notes: %d%n",
                        f.title(),
                        (f.ratings() != null) ? f.ratings().size() : 0)
        );
    }

    /**
     * b. Afficher tous les utilisateurs (nom, âge) ayant une profession et un genre donnés.
     * <ul>
     *     <li>Cherche les utilisateurs du film</li>
     *     <li>Filtre par profession et genre</li>
     * </ul>
     *
     * @param profession Profession de l'utilisateur
     * @param genre Genre (M/F) de l'utilisateur
     */
    public void displayUserFiltered_p_g(String profession, String genre) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Utilisateur> users = films.stream()
                // Extraire tous les utilisateurs ayant noté les films
                .flatMap(f -> f.ratings() != null ? f.ratings().stream() : Stream.empty())
                // Creer les utilisateurs à partir des notations
                .map(Utilisateur::fromRating)
                .distinct()
                // Filtrer par profession et genre
                .filter(u -> u.occupation().equals(profession) && u.gender().equals(genre))
                .toList();

        users.forEach(u ->
                System.out.printf("Nom: %s | Age: %d%n", u.name(), u.age())
        );
    }

    /**
     * c. Afficher les films (titre, année et genre) les plus vus/notés par une profession donnée
     * <ul>
     *     <li>Cherche les films</li>
     *     <li>Compte les notations par profession</li>
     *     <li>Trie par nombre de notations décroissant</li>
     * </ul>
     *
     * @param profession Profession des utilisateurs
     */
    public void displayFilms_p(String profession) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Film> sorted = films.stream()
                // Filtrer les films qui ont au moins une note correspondant à la profession
                .filter(f -> f.ratings() != null &&
                        f.ratings().stream().anyMatch(r -> r.occupation().equals(profession)))
                // Trier par nombre de notations correspondant à la profession, décroissant
                .sorted((f1, f2) -> Long.compare(
                        f2.ratings().stream().filter(r -> r.occupation().equals(profession)).count(),
                        f1.ratings().stream().filter(r -> r.occupation().equals(profession)).count()))
                .toList();

        sorted.forEach(f -> System.out.printf("Titre: %s | Genres: %s%n",
                f.title(), f.genres()));
    }

    /**
     * d. Afficher les films (titre, date et genre) les plus vus/notés par une catégorie d'âges donnée.
     * <ul>
     *     <li>Cherche les films</li>
     *     <li>Compte les notations par catégorie d'âge</li>
     *     <li>Trie par nombre de notations décroissant</li>
     * </ul>
     *
     * @param minAge Borne d'âge minimale
     * @param maxAge Borne d'âge maximale
     */
    public void displayFilms_a(int minAge, int maxAge) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Film> sorted = films.stream()
                // Filtrer les films qui ont au moins une note dans la tranche d'âge
                .filter(f -> f.ratings() != null &&
                        f.ratings().stream().anyMatch(r -> r.age() >= minAge && r.age() <= maxAge))
                // Trier par nombre de notations dans la tranche d'âge, décroissant
                .sorted((f1, f2) -> Long.compare(
                        f2.ratings().stream().filter(r -> r.age() >= minAge && r.age() <= maxAge).count(),
                        f1.ratings().stream().filter(r -> r.age() >= minAge && r.age() <= maxAge).count()))
                .toList();

        sorted.forEach(f -> System.out.printf("Titre: %s | Genres: %s%n",
                f.title(), f.genres()));
    }

    /**
     * e. Afficher pour le Top10 des films notés, leur titre, note et le nombre des
     * utilisateurs qui les ont notés.
     * <ul>
     *     <li>Cherche les films</li>
     *     <li>Calcule la note moyenne</li>
     *     <li>Trie par note moyenne décroissante</li>
     * </ul>
     */
    public void displayTop10Films() {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Film> top10 = films.stream()
                // Filtrer les films n'ayant pas de notations
                .filter(f -> f.ratings() != null && !f.ratings().isEmpty())
                // Trier par note moyenne décroissante
                .sorted(Comparator.comparingDouble(f -> -f.ratings().stream().mapToInt(Rating::rating).average().orElse(0)))
                // Max 10
                .limit(10)
                .toList();

        top10.forEach(f -> {
            double avgRating = f.ratings().stream().mapToInt(Rating::rating).average().orElse(0);
            System.out.printf("Titre: %s | Note moyenne: %.2f | Nombre de notations: %d%n",
                    f.title(), avgRating, f.ratings().size());
        });
    }

    /**
     * f. Insérer une notation unique pour un film donné et un utilisateur donné
     * <ul>
     *     <li>Vérifie l'existence du film</li>
     *     <li>Vérifie l'existence de l'utilisateur</li>
     *     <li>Ajoute ou met à jour la notation</li>
     * </ul>
     *
     * @param filmId ID du film
     * @param userId ID de l'utilisateur
     * @param ratingValue Notation (1-5)
     */
    public void addRating(int filmId, int userId, int ratingValue) {
        // Trouver le film
        Film film = filmsCollection.find(Filters.eq("_id", filmId)).first();
        if (film == null) {
            System.out.println("Film non trouvé !");
            return;
        }
        System.out.println("Film trouvé: " + film.title());

        // Trouver l'utilisateur (parmis tous les films)
        Utilisateur user = filmsCollection.find()
                .into(new ArrayList<>())
                .stream()
                // Aplatissement des notations de tous les films
                .flatMap(f -> f.ratings().stream())
                // Création des utilisateurs à partir des notations
                .map(Utilisateur::fromRating)
                // Trouver l'utilisateur par ID
                .filter(u -> u.user_id() == userId)
                .findFirst()
                .orElse(null);
        if (user == null) {
            System.out.println("Utilisateur non trouvé !");
            return;
        }
        System.out.println("Utilisateur trouvé: " + user.name());

        // Créer la nouvelle notation
        Rating newRating = new Rating(
                user.user_id(),
                user.name(),
                user.age(),
                user.gender(),
                user.occupation(),
                ratingValue,
                System.currentTimeMillis() / 1000L
        );

        // Mettre à jour ou ajouter la notation
        List<Rating> updatedRatings = new ArrayList<>(film.ratings() != null ? film.ratings() : Collections.emptyList());

        // Vérifier si l'utilisateur a déjà noté le film trouvé
        boolean updated = false;
        for (int i = 0; i < updatedRatings.size(); i++) {
            if (updatedRatings.get(i).user_id() == userId) {
                // Mettre à jour la notation existante
                updatedRatings.set(i, newRating);
                updated = true;
                break;
            }
        }
        // Si pas mis à jour, ajouter la nouvelle notation
        if (!updated) {
            updatedRatings.add(newRating);
        }

        // Mettre à jour la collection dans la base de données
        filmsCollection.updateOne(
                Filters.eq("_id", filmId),
                new Document("$set", new Document("ratings", updatedRatings))
        );

        System.out.println(updated ? "Note mise à jour avec succès !" : "Note ajoutée avec succès !");
    }
}
