package app;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import objects.Film;
import objects.Rating;
import objects.Utilisateur;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilmApp {

    private final MongoCollection<Film> filmsCollection;

    public FilmApp(MongoDatabase db) {
        this.filmsCollection = db.getCollection("films_fusion", Film.class);
    }

    public void displayFilmsFiltered_g(String genre) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());
        List<Film> filtered = films.stream()
                .filter(f -> f.genres() != null && f.genres().contains(genre))
                .sorted(Comparator.comparingInt(f -> -((f.ratings() != null) ? f.ratings().size() : 0)))
                .toList();

        filtered.forEach(
                f -> System.out.printf("Titre: %s | Nombre de notes: %d%n",
                        f.title(),
                        (f.ratings() != null) ? f.ratings().size() : 0)
        );
    }

    public void displayUserFiltered_p_g(String profession, String genre) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Utilisateur> users = films.stream()
                .flatMap(f -> f.ratings() != null ? f.ratings().stream() : Stream.empty())
                .map(Utilisateur::fromRating)
                .distinct()
                .filter(u -> u.occupation().equals(profession) && u.gender().equals(genre))
                .toList();

        users.forEach(u ->
                System.out.printf("Nom: %s | Age: %d%n", u.name(), u.age())
        );
    }

    public void displayFilms_p(String profession) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Film> sorted = films.stream()
                .map(f -> new AbstractMap.SimpleEntry<>(f,
                        f.ratings() != null
                                ? f.ratings().stream().filter(r -> r.occupation().equals(profession)).count()
                                : 0))
                .filter(e -> e.getValue() > 0)
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        sorted.forEach(f -> System.out.printf("Titre: %s | Genres: %s%n",
                f.title(), f.genres()));
    }

    public void displayFilms_a(int minAge, int maxAge) {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Film> sorted = films.stream()
                .map(f -> new AbstractMap.SimpleEntry<>(f,
                        f.ratings() != null
                                ? f.ratings().stream().filter(r -> r.age() >= minAge && r.age() <= maxAge).count()
                                : 0))
                .filter(e -> e.getValue() > 0)
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        sorted.forEach(f -> System.out.printf("Titre: %s | Genres: %s%n",
                f.title(), f.genres()));
    }

    public void displayTop10Films() {
        List<Film> films = filmsCollection.find().into(new ArrayList<>());

        List<Film> top10 = films.stream()
                .filter(f -> f.ratings() != null && !f.ratings().isEmpty())
                .sorted(Comparator.comparingDouble(f -> -f.ratings().stream().mapToInt(Rating::rating).average().orElse(0)))
                .limit(10)
                .toList();

        top10.forEach(f -> {
            double avgRating = f.ratings().stream().mapToInt(Rating::rating).average().orElse(0);
            System.out.printf("Titre: %s | Note moyenne: %.2f | Nombre de notations: %d%n",
                    f.title(), avgRating, f.ratings().size());
        });
    }

    public void addRating(int filmId, int userId, int ratingValue) {
        Film film = filmsCollection.find(Filters.eq("_id", filmId)).first();
        if (film == null) {
            System.out.println("Film non trouvé !");
            return;
        }

        Rating newRating = new Rating(
                userId,
                null,
                null,
                null,
                null,
                ratingValue,
                System.currentTimeMillis() / 1000L
        );

        List<Rating> currentRatings = film.ratings() != null ? new ArrayList<>(film.ratings()) : new ArrayList<>();

        boolean updated = false;
        for (int i = 0; i < currentRatings.size(); i++) {
            if (currentRatings.get(i).user_id() == userId) {
                currentRatings.set(i, newRating);
                updated = true;
                break;
            }
        }

        if (!updated) {
            currentRatings.add(newRating);
        }

        film.ratings().clear();
        film.ratings().addAll(currentRatings);

        filmsCollection.replaceOne(Filters.eq("_id", filmId), film);

        System.out.println(updated ? "Notation mise à jour !" : "Nouvelle notation ajoutée !");
    }


}
