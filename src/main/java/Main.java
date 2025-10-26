import app.FilmApp;
import com.mongodb.client.MongoDatabase;
import conn.MongoConnection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        MongoDatabase db = MongoConnection.getDatabase();
        FilmApp app = new FilmApp(db);
        Scanner sc = new Scanner(System.in);

        System.out.println("Choisissez une question (a à f) :");
        String choix = sc.nextLine().trim().toLowerCase();

        switch (choix) {
            case "a" -> {
                System.out.println("Entrez le genre :");
                String genre = sc.nextLine();

                output("a");
                app.displayFilmsFiltered_g(genre);
            }
            case "b" -> {
                System.out.println("Entrez la profession :");
                String profession = sc.nextLine();
                System.out.println("Entrez le genre (M/F) :");
                String genre = sc.nextLine();

                output("b");
                app.displayUserFiltered_p_g(profession, genre);
            }
            case "c" -> {
                System.out.println("Entrez la profession :");
                String profession = sc.nextLine();

                output("c");
                app.displayFilms_p(profession);
            }
            case "d" -> {
                System.out.println("Entrez la tranche basse d'âge (ex: 18) :");
                int minRange = sc.nextInt();
                System.out.println("Entrez la tranche haute d'âge (ex: 25) :");
                int maxRange = sc.nextInt();

                output("d");
                app.displayFilms_a(minRange, maxRange);
            }
            case "e" -> {
                output("e");
                app.displayTop10Films();
            }
            case "f" -> {
                System.out.println("Entrez l'ID du film :");
                int filmId = sc.nextInt();
                System.out.println("Entrez l'ID de l'utilisateur :");
                int userId = sc.nextInt();
                System.out.println("Entrez la note :");
                int note = sc.nextInt();

                output("f");
                app.addRating(filmId, userId, note);
            }
            default -> System.out.println("Choix invalide !");
        }

        sc.close();
        System.out.println("Résultats écrits dans output.txt");
    }


    public static void output(String question_letter) throws FileNotFoundException {
        PrintStream fichier = new PrintStream(new FileOutputStream("question" + question_letter + ".txt"));
        System.setOut(fichier);
    }
}

