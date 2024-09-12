import java.awt.event.*;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.io.*;
import java.util.Scanner;

public class IMDB {
    public static class RequestHolder {
        static ArrayList<Request> requests = new ArrayList<>();
        public static void removeRequest(Request req) {
             requests.remove(req);
        }

        public static void addRequest(Request req) {
            if(req.fixer.equals("ADMIN"))
                requests.add(req);
        }
    }

    ArrayList<User> users;
    ArrayList<Request> requests;
    ArrayList<Actor> actors;
    ArrayList<Production> productions;
    private static final IMDB instance = new IMDB();

    private IMDB() {
        users = new ArrayList<>();
        actors = new ArrayList<>();
        productions = new ArrayList<>();
        requests = new ArrayList<>();
    }

    public static IMDB getInstance() {
        return instance;
    }

    // return the user with that email and password
    public User checkCredentials(String email, String password) {
        for(User u : users) {
            if(u.info.getCredentials().getEmail().compareTo(email) == 0) {
                if(u.info.getCredentials().getPassword().compareTo(password) == 0)
                    return u;
                else
                    return null;
            }
        }
        return null;
    }
    public void parseActors() {
        try {
            // parsing the json file
            Object obj = new JSONParser().parse(new FileReader("src/POO-TEMA-2023-input/actors.json"));
            JSONArray jsonActors = (JSONArray) obj;

            // iterating through the actors
            for(Object act : jsonActors) {
                JSONObject jObj = (JSONObject) act;

                // name and biography
                Actor a = new Actor();
                a.name = (String) jObj.get("name");
                a.biography = (String) jObj.get("biography");

                // adding performances
                JSONArray performances = (JSONArray) jObj.get("performances");
                for (Object perf : performances) {
                    JSONObject perfObj = (JSONObject) perf;
                    Actor.Pair pair = new Actor.Pair();
                    pair.name = (String) perfObj.get("title");
                    String type = (String) perfObj.get("type");
                    if(type.equals("Movie"))
                        pair.type = ProductionType.Movie;
                    else
                        pair.type = ProductionType.Series;
                    a.starred.add(pair);
                }

                // adding actor to list
                actors.add(a);
            }
        } catch (FileNotFoundException e1) {
            System.err.println("File not found");
        } catch (ParseException e2) {
            System.err.println("File can't be parsed");
        } catch (IOException e3) {
            System.err.println("IO error");
        }
    }

    public void parseProductions() {
        try {
            // parsing the json file
            Object obj = new JSONParser().parse(new FileReader("src/POO-TEMA-2023-input/production.json"));
            JSONArray jsonProductions = (JSONArray) obj;

            // iterating through the actors
            for(Object prod : jsonProductions) {
                JSONObject jObj = (JSONObject) prod;
                ProductionType type;
                Production production;

                // type of production
                if(((String) jObj.get("type")).equals("Movie"))
                    production = new Movie();
                else
                    production = new Series();

                // general information
                production.title = (String) jObj.get("title");

                JSONArray jDirectors = (JSONArray) jObj.get("directors");
                for (Object director : jDirectors) {
                    production.directors.add((String) director);
                }

                JSONArray jActors = (JSONArray) jObj.get("actors");
                for (Object actor : jActors) {
                    production.actors.add((String) actor);
                }

                JSONArray jGenre = (JSONArray) jObj.get("genres");
                for(Object genre : jGenre) {
                    production.genres.add(Genre.valueOf((String) genre));
                }

                // ratings
                JSONArray jRating = (JSONArray) jObj.get("ratings");
                for(Object r : jRating) {
                    JSONObject rate = (JSONObject) r;
                    Rating rating = new Rating();

                    rating.user = (String) rate.get("username");
                    rating.rating = (Long) rate.get("rating");
                    rating.comment = (String) rate.get("comment");
                    production.reviews.add(rating);
                }

                production.rating = (Double) jObj.get("averageRating");
                production.description = (String) jObj.get("plot");

                if(production.type == ProductionType.Movie) {
                    // adding movie specific data
                    Movie movie = (Movie) production;
                    movie.duration = (String) jObj.get("duration");
                    if(jObj.get("releaseYear") != null)
                        movie.year = (Long) jObj.get("releaseYear");

                    // adding to the vector with all productions
                    productions.add(movie);
                } else {
                    // adding series specific data
                    Series series = (Series) production;
                    series.year = (Long) jObj.get("releaseYear");
                    series.numSeasons = (Long) jObj.get("numSeasons");

                    // processing seasons
                    JSONObject jMap = (JSONObject) jObj.get("seasons");

                    for(Object seasonKey : jMap.keySet()) {
                        String seasonName = (String) seasonKey;
                        JSONArray jEpisodes = (JSONArray) jMap.get(seasonName);

                        // creating episode list
                        List<Episode> episodesList = new ArrayList<>();
                        for(Object ep : jEpisodes) {
                            JSONObject jEp = (JSONObject) ep;

                            Episode episode = new Episode();

                            episode.name = (String) jEp.get("episodeName");
                            episode.duration = (String) jEp.get("duration");

                            episodesList.add(episode);
                        }
                        series.addSeason(seasonName, episodesList);
                    }

                    productions.add(series);
                }
            }
        } catch (FileNotFoundException e1) {
            System.err.println("File not found");
        } catch (ParseException e2) {
            System.err.println("File can't be parsed");
        } catch (IOException e3) {
            System.err.println("IO error");
        }
    }

    public void parseRequests() {
        try {
            // parsing the json file
            Object obj = new JSONParser().parse(new FileReader("src/POO-TEMA-2023-input/requests.json"));
            JSONArray jsonRequests = (JSONArray) obj;

            // iterating through the requests
            for(Object req : jsonRequests) {
                JSONObject jReq = (JSONObject) req;
                Request request = new Request();

                request.setRequestType(RequestTypes.valueOf((String) jReq.get("type")));
                request.setDate((String) jReq.get("createdDate"));

                // setting cause of issue(name)
                if(request.getRequestType() == RequestTypes.MOVIE_ISSUE)
                    request.name = (String) jReq.get("movieTitle");
                else
                    if(request.getRequestType() == RequestTypes.ACTOR_ISSUE)
                        request.name = (String) jReq.get("actorName");

                request.description = (String) jReq.get("description");
                request.requester = (String) jReq.get("username");
                request.fixer = (String) jReq.get("to");

                // adding request to list
                IMDB.RequestHolder.addRequest(request);
                requests.add(request);
            }

        } catch (FileNotFoundException e1) {
            System.err.println("File not found");
        } catch (ParseException e2) {
            System.err.println("File can't be parsed");
        } catch (IOException e3) {
            System.err.println("IO error");
        }
    }

    // searching for the actor with that name
    public Actor searchActor(String name) {
        for(Actor a : actors) {
            if(a.name.equals(name))
                return a;
        }
        return null;
    }

    // searching for the production with that name
    public Production searchProduction(String name) {
        for(Production p : productions) {
            if(p.title.equals(name))
                return p;
        }
        return null;
    }

    public void parseUsers() {
        try {
            // parsing the json file
            Object obj = new JSONParser().parse(new FileReader("src/POO-TEMA-2023-input/accounts.json"));
            JSONArray jsonUsers = (JSONArray) obj;

            UserFactory factory = UserFactory.getInstance();

            // iterating through the actors
            for(Object u : jsonUsers) {
                JSONObject jObj = (JSONObject) u;

                // constructing the user
                String accountType = ((String) jObj.get("userType")).toUpperCase();
                User user;

                user = (User) factory.createUser(AccountType.valueOf(accountType));

                // common information
                user.username = (String) jObj.get("username");
                String exp = (String) jObj.get("experience");
                if(exp != null) {
                    user.experience = Integer.parseInt(exp);
                } else {
                    if(user.type != AccountType.ADMIN)
                        user.experience = 0;
                }

                JSONObject jInfo = (JSONObject) jObj.get("information");
                JSONObject jCred = (JSONObject) jInfo.get("credentials");

                // credentials
                User.Information.InformationBuilder builder = new User.Information.InformationBuilder();
                builder.credentials((String) jCred.get("email"), (String) jCred.get("password"));

                builder.name((String) jInfo.get("name"));
                builder.country((String) jInfo.get("country"));
                builder.age((Long) jInfo.get("age"));
                builder.gender(((String) jInfo.get("gender")).charAt(0));
                builder.birthday((String) ((JSONObject) jObj.get("information")).get("birthDate"));
                user.info = builder.build();

                // adding favourite actors
                if(jObj.get("favoriteActors") != null) {
                    JSONArray favAct = (JSONArray) jObj.get("favoriteActors");
                    for (Object a : favAct) {
                        Actor act = searchActor((String) a);
                        if(act != null)
                            user.addFavouriteActor(act);
                    }
                }

                // adding favourite productions
                if(jObj.get("favoriteProductions") != null) {
                    JSONArray favProd = (JSONArray) jObj.get("favoriteProductions");
                    for (Object p : favProd) {
                        Production prod = searchProduction((String) p);
                        if(prod != null)
                            user.addFavouriteProduction(prod);
                    }
                }

                // adding actor contributions
                if(jObj.get("actorsContribution") != null) {
                    JSONArray contAct = (JSONArray) jObj.get("actorsContribution");
                    for (Object a : contAct) {
                        Actor act = searchActor((String) a);
                        if(act != null) {
                            ((Staff) user).addedActor.add(act);
                            act.Subscribe(user);
                        }
                    }
                }

                // adding production contributions
                if(jObj.get("productionsContribution") != null) {
                    JSONArray contProd = (JSONArray) jObj.get("productionsContribution");
                    for (Object p : contProd) {
                        Production prod = searchProduction((String) p);
                        if(prod != null) {
                            ((Staff) user).addedProductions.add(prod);
                            prod.Subscribe(user);
                        }
                    }
                }

                // adding notifications
                if(jObj.get("notifications") != null) {
                    JSONArray jsonNotList = (JSONArray) jObj.get("notifications");
                    for(Object notif : jsonNotList) {
                        String notification = (String) notif;
                        user.notifications.add(notification);
                    }
                }

                // adding user to list
                users.add(user);
            }
        } catch (FileNotFoundException e1) {
            System.err.println("File not found");
        } catch (ParseException e2) {
            System.err.println("File can't be parsed");
        } catch (IOException e3) {
            System.err.println("IO error");
        }
    }

    // CLI screen for log in
    public void CLILogIn() {
        Scanner scanner = new Scanner(System.in);
        User user;
        System.out.println("Welcome back! Enter your credentials");

        do {
            System.out.print("\tEmail: ");
            String email = scanner.nextLine();

            System.out.print("\tPassword: ");
            String password = scanner.nextLine();

            user = checkCredentials(email, password);
            if(user == null)
                System.out.println("Credentials wrong");
        }while(user == null);
        user.CLI();
    }

    // GUI screen for login
    public void GUILogIn() {
        JFrame logIn = new JFrame("Log in");
        logIn.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logIn.setSize(300, 300);
        logIn.setMinimumSize(new Dimension(200, 200));
        logIn.setLayout(new GridLayout(3, 3));

        // labels
        JLabel eLabel = new JLabel("Email:");
        eLabel.setPreferredSize(new Dimension(10, 1));
        JLabel passLabel = new JLabel("Password:");
        passLabel.setPreferredSize(new Dimension(10, 1));

        // email textbox
        JTextField email = new JTextField(30);
        JPasswordField password = new JPasswordField(30);

        // login button
        JButton log = new JButton("Log In");
        log.setEnabled(false);

        // warning message
        JLabel failedLogIn = new JLabel("Email or password are wrong");
        failedLogIn.setForeground(Color.RED);
        failedLogIn.setVisible(false);

        logIn.add(eLabel);
        logIn.add(email);
        logIn.add(passLabel);
        logIn.add(password);

        logIn.add(failedLogIn);
        logIn.add(log);

        // unlock button if fields aren't empty
        ActionListener keys = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pass = new String(password.getPassword());

                if(!email.getText().isEmpty() &&  !pass.equals("")) {
                    // the fields have been completed
                    log.setEnabled(true);
                }
            }
        };

        email.addActionListener(keys);
        password.addActionListener(keys);

        // log in action listener
        log.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mail = email.getText();
                String pass = new String(password.getPassword());
                User user = checkCredentials(mail, pass);
                if(user == null) {
                    failedLogIn.setVisible(true);
                    log.setEnabled(false);
                    email.setText("");
                    password.setText("");
                } else {
                    user.GUI();
                    logIn.dispose();
                }

            }
        });

        logIn.pack();
        logIn.setLocationRelativeTo(null);
        logIn.setVisible(true);
    }

    // adding requests to user lists
    public void distributeRequests() {
        for(Request req : requests) {
            // distributing to the fixers
            if(!req.fixer.equals("ADMIN")) {
                for(User u : users) {
                    if(u.type != AccountType.REGULAR) {
                        Staff staff = (Staff)u;
                        if (staff.username.equals(req.fixer))
                            staff.personalReq.add(req);
                    }
                }
            }

            // distributing to the requesters
            for(User u : users) {
                if(u.username.equals(req.requester)) {
                    u.createdRequests.add(req);
                }
            }
        }
    }

    // subscribe users and sort reviews
    public void manageReviews() {
        for(Production p : productions) {
            for(Rating r : p.reviews) {
                for(User u : users) {
                    if(u.username.equals(r.user)) {
                        p.Subscribe(u);
                        break;
                    }
                }
            }
            Collections.sort(p.reviews);
        }

        for(Actor a : actors) {
            for(Rating r : a.reviews) {
                for(User u : users) {
                    if(u.username.equals(r.user)) {
                        a.Subscribe(u);
                        break;
                    }
                }
            }
            Collections.sort(a.reviews);
        }
    }

    public void run() {
        // parsing jsons
        parseActors();
        parseProductions();
        parseRequests();
        parseUsers();
        distributeRequests();
        manageReviews();

        // preferred running
        JFrame start = new JFrame("Choose interface");
        start.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        start.setMinimumSize(new Dimension(300, 300));
//        start.setPreferredSize(new Dimension(100, 100));
        start.setLayout(new FlowLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JLabel message = new JLabel("How do you want to use the app?");
        JButton b1 = new JButton("Terminal");
        JButton b2 = new JButton("Graphical Interface");

        panel.add(message, BorderLayout.CENTER);
        panel.add(b1, BorderLayout.CENTER);
        panel.add(b2, BorderLayout.CENTER);

        start.add(panel, BorderLayout.CENTER);

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start.dispose();
                CLILogIn();
            }
        });

        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUILogIn();
                start.dispose();
            }
        });

        start.pack();
        start.setLocationRelativeTo(null);
        start.setVisible(true);
    }

    public static void main(String[] args) {
        IMDB imdb = IMDB.getInstance();
        imdb.run();
    }

}
