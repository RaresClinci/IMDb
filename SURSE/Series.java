import java.util.*;
public class Series extends Production {
    long year;
    long numSeasons;
    private Map<String, List<Episode>> episodes;
    public Series() {
        super();
        type = ProductionType.Series;
        episodes = new HashMap<>();
    }

    public void addSeason(String name, List<Episode> epList) {
        episodes.put(name, epList);
    }

    public Map<String, List<Episode>> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Map<String, List<Episode>> episodes) {
        this.episodes = episodes;
    }
    public String infoToString() {
        String series = "";
        if(!title.isEmpty())
            series += "Title:\n\t" + title + "\n";
        if(year != 0)
            series += "Release year:\n\t" + year + "\n";
        if(numSeasons != 0)
            series += "Seasons:\n\t" + numSeasons + "\n";
        if(rating != 0)
            series += "Score:\n\t" + rating + "\n";

        if(!actors.isEmpty()) {
            series += "Actors:\n";
            for (String a : actors) {
                series += "\t" + a + "\n";
            }
        }

        if(!directors.isEmpty()) {
            series += "Directors:\n";
            for (String d : directors) {
                series += "\t" + d + "\n";
            }
        }

        if(!genres.isEmpty()) {
            series += "Genres:\n";
            for (Genre g : genres) {
                series += "\t" + g + "\n";
            }
        }

        if(!description.isEmpty())
            series += "Description:\n\t" + description + "\n";

        if(!reviews.isEmpty()) {
            series += "Reviews:\n";
            for (Rating r : reviews) {
                series += "\t" + r + "\n";
            }
        }

        if(!episodes.isEmpty()) {
            series += "Episodes:\n";
            for (HashMap.Entry<String, List<Episode>> e : episodes.entrySet()) {
                series += "\t" + e + "\n";
            }
        }

        return series;
    }

    public void displayInfo() {
        System.out.print(infoToString());
    }
}
