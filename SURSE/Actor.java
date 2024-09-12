import java.util.*;
public class Actor implements Comparable<Actor> {

    static class Pair {
        String name;
        ProductionType type;

        public String toString() {
            return name + ": " + type;
        }
    }

    String name;
    ArrayList<Pair> starred;
    String biography;
    List<Rating> reviews;
    List<User> observers;

    public Actor() {
        starred = new ArrayList<>();
        reviews = new ArrayList<>();
        observers = new ArrayList<>();
    }
    // send notifcations
    public void notification(User reviewer) {
        for(User u : observers) {
            if(u.type == AccountType.ADMIN && Admin.sharedActor.contains(this)) {
                // for actors in shared list
                u.notifications.add(name + ", from the shared list, received a review from " + reviewer.username);
            } else {
                if (u.type != AccountType.REGULAR && ((Staff) u).addedActor.contains(this)) {
                    // for actors they created
                    u.notifications.add(name + ", who you added, received a review from " + reviewer.username);
                } else {
                    // for actors they reviewed
                    u.notifications.add(name + ", who you reviewed, received a review from " + reviewer.username);
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

    @Override
    public int compareTo(Actor actor) {
        return name.compareTo(actor.name);
    }

    public String toString() {
        return name;
    }

    public String infoToString() {
        String actor = "";
        if(!name.isEmpty())
            actor += "Name:\n\t" + name + "\n";
        if(!starred.isEmpty())
            actor += "Starred in:\n\t" + starred + "\n";
        if(!biography.isEmpty())
            actor += "Biography:\n\t" + biography +"\n";

        if(!reviews.isEmpty()) {
            actor += "Reviews:\n";
            for(Rating r : reviews)
                actor += "\t" + r + "\n";
        }

        return actor;
    }

    public void displayInfo() {
        System.out.print(infoToString());
    }
}
