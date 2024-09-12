import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Request {
    private RequestTypes type;
    private LocalDateTime date;
    String name;
    String description;
    String requester;
    String fixer;

    public void setRequestType(RequestTypes type) {
        this.type = type;
    }

    public RequestTypes getRequestType() {
        return type;
    }

    public void setDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        this.date = LocalDateTime.parse(date, formatter);
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String giveDate() {
        return date.toString();
    }

    public String toString() {
        return name;
    }

    public String infoToString() {
        String request = "";
        request += "Name:\n\t" + name + "\n";
        request += "Type:\n\t" + type + "\n";
        request += "Time:\n\t" + date + "\n";
        request += "Description:\n\t" + description + "\n";
        request += "Requested by:\n\t" + requester + "\n";
        request += "Responsible:\n\t" + fixer + "\n";
        return request;
    }

    // notify requester when request is solved/dismissed
    public void notifySolved(boolean solved) {
        String text = "Your request";
        switch(type){
            case MOVIE_ISSUE:
                text += " on " + name;
                break;
            case ACTOR_ISSUE:
                text += " on " + name;
                break;
            case DELETE_ACCOUNT:
                text += " on account deletion";
                break;
            case OTHERS:
                text += ": " + description;
                break;
        }

        if(solved)
            text += " has been solved";
        else
            text += " has been dismissed";

        // sending the notification
        IMDB imdb = IMDB.getInstance();
        for(User u: imdb.users) {
            if(u.username.equals(requester))
                u.notifications.add(text);
        }
    }

    // notify fixer when request is added
    public void notifyReceived() {
        String text = "You received ";
        switch(type){
            case MOVIE_ISSUE:
                text += "a movie";
                break;
            case ACTOR_ISSUE:
                text += "an actor";
                break;
            case DELETE_ACCOUNT:
                text += "an account deletion";
                break;
            case OTHERS:
                text += "a miscellaneous";
                break;
        }
        text += " request from " + requester;

        // sending the notification
        IMDB imdb = IMDB.getInstance();
        if(!fixer.equals("ADMIN")) {
            for (User u : imdb.users) {
                if (u.username.equals(fixer))
                    u.notifications.add(text);
            }
        } else {
            for (User u : imdb.users) {
                if (u.type == AccountType.ADMIN)
                    u.notifications.add(text);
            }
        }
    }

    // notify fixer when requestwer removes their request
    public void notifyRemove() {
        // sending the notification
        IMDB imdb = IMDB.getInstance();
        for(User u: imdb.users) {
            if(u.username.equals(fixer))
                u.notifications.add(requester + " retracted their request");
        }
    }
}
