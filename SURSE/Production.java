import java.util.*;
public abstract class Production implements Comparable {
    String title;
    List<String> directors;
    List<String> actors;
    List<Genre> genres;
    List<Rating> reviews;
    String description;
    Double rating;
    ProductionType type;
    List<User> observers;

    public Production() {
        directors = new ArrayList<>();
        actors = new ArrayList<>();
        genres = new ArrayList<>();
        reviews = new ArrayList<>();
        observers = new ArrayList<>();
    }

    // sending notifications
    public void notification(User reviewer) {
        for(User u : observers) {
            if (u.type == AccountType.ADMIN && Admin.sharedProductions.contains(this)) {
                u.notifications.add(title + ", from the shared list, received a review from " + reviewer.username);
            } else {
                if (u.type != AccountType.REGULAR && ((Staff) u).addedProductions.contains(this)) {
                    u.notifications.add(title + ", which you added, received a review from " + reviewer.username);
                } else {
                    u.notifications.add(title + ", which you reviewed, received a review from " + reviewer.username);
                }
            }
        }
    }

    public void Unsubscribe(User u) {
        observers.remove(u);
    }

    public void Subscribe(User u) {
        observers.add(u);
    }

    // recalculate the rating
    public void calculateRating() {
        long sum = 0;

        for(Rating r: reviews)
            sum += r.rating;

        rating = sum * 1.0/reviews.size();
    }

    public abstract void displayInfo();

    public abstract String infoToString();

    public int compareTo(Object o) {
        Production prod = (Production)o;
        return title.compareTo(prod.title);
    }

    public String toString() {
        return title;
    }
}
