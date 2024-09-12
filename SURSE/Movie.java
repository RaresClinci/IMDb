public class Movie extends Production {
    String duration;
    long year;

    public Movie() {
        super();
        type = ProductionType.Movie;
    }

    public String infoToString() {
        String movie = "";
        if(!title.isEmpty())
            movie += "Title:\n\t" + title + "\n";
        if(year != 0)
            movie += "Release year:\n\t" + year + "\n";
        if(!duration.isEmpty())
            movie += "Duration:\n\t" + duration + "\n";
        if(rating != 0)
            movie += "Score:\n\t" + rating + "\n";

        if(!actors.isEmpty()) {
            movie += "Actors:\n";
            for (String a : actors) {
                movie += "\t" + a + "\n";
            }
        }

        if(!directors.isEmpty()) {
            movie += "Directors:\n";
            for (String d : directors) {
                movie += "\t" + d + "\n";
            }
        }

        if(!genres.isEmpty()) {
            movie += "Genres:\n";
            for (Genre g : genres) {
                movie += "\t" + g + "\n";
            }
        }

        if(!description.isEmpty())
            movie += "Description:\n\t" + description + "\n";

        if(!reviews.isEmpty()) {
            movie += "Reviews:\n";
            for (Rating r : reviews) {
                movie += "\t" + r + "\n";
            }
        }

        return movie;
    }

    public void displayInfo() {
        System.out.print(infoToString());
    }
}
