public class Rating implements Comparable{
    String user;
    long rating;
    String comment;

    public String toString() {
        return user + " (" + rating + "/10): " + comment;
    }

    @Override
    public int compareTo(Object o) {
        Rating r = (Rating) o;
        IMDB imdb = IMDB.getInstance();

        // finding the original user
        User user1 = new Regular();
        for(User u : imdb.users) {
            if(u.username.equals(user))
                user1 = u;
        }

        // finding the other user
        User user2 = new Regular();
        for(User u : imdb.users) {
            if(u.username.equals(r.user))
                user2 = u;
        }

        return user2.experience - user1.experience;
    }
}
