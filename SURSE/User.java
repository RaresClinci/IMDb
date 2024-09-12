import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public abstract class User{
    static class Information {
        private Credentials credentials;
        String name;
        String country;
        long age;
        char gender;
        private LocalDate birthday;

        public Credentials getCredentials() {
            return credentials;
        }

        public void setCredentials(String email, String password) {
            credentials.setEmail(email);
            credentials.setPassword(password);
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(String formattedBirthday) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            birthday = LocalDate.parse(formattedBirthday, formatter);
        }

        private Information(InformationBuilder builder) {
            this.credentials = builder.credentials;
            this.name = builder.name;
            this.country = builder.country;
            this.age = builder.age;
            this.gender = builder.gender;
            this.birthday = builder.birthday;
        }

        public static class InformationBuilder {
            private Credentials credentials;
            private String name;
            private String country;
            private long age;
            private char gender;
            private LocalDate birthday;

            public InformationBuilder() {
                this.credentials = new Credentials();
            }

            public InformationBuilder credentials(String email, String password) {
                this.credentials.setEmail(email);
                this.credentials.setPassword(password);
                return this;
            }

            public InformationBuilder name(String name) {
                this.name = name;
                return this;
            }

            public InformationBuilder country(String country) {
                this.country = country;
                return this;
            }

            public InformationBuilder age(long age) {
                this.age = age;
                return this;
            }

            public InformationBuilder gender(char gender) {
                this.gender = gender;
                return this;
            }

            public InformationBuilder birthday(String formattedBirthday) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                this.birthday = LocalDate.parse(formattedBirthday, formatter);
                return this;
            }

            public Information build() {
                return new Information(this);
            }
        }
    }
    Information info;

    AccountType type;
    String username;
    int experience;
    List<String> notifications;
    SortedSet<Production> favouriteProduction;
    SortedSet<Actor> favouriteActor;
    List<Request> createdRequests;
    JFrame frontPage;
    IMDB imdb;
    JFrame actorFrame;
    JFrame prodFrame;
    JPanel workingSpace;
    JPanel leftMenu;

    public User() {
        notifications = new ArrayList<>();
        favouriteProduction = new TreeSet<>();
        favouriteActor = new TreeSet<>();
        imdb = IMDB.getInstance();
        createdRequests = new ArrayList<>();
    }

    public String toString() {
        return username;
    }

    public void addBirthday(String birthday) {
        info.setBirthday(birthday);
    }
    public void addFavouriteProduction(Production fav) {
        favouriteProduction.add(fav);
    }

    public void removeFavouriteProduction(Production fav) {
        favouriteProduction.remove(fav);
    }

    public void addFavouriteActor(Actor fav) {
        favouriteActor.add(fav);
    }

    public void removeFavouriteActor(Actor fav) {
        favouriteActor.remove(fav);
    }

    public void addExperience(int exp) {
        experience += exp;
    }

    public void logOut() {
        frontPage.dispose();
        imdb.GUILogIn();
    }

    // was the request created successfully?
    public abstract boolean handleReq(Request r);

    // GUI for creating requests
    public void createRequestGUI(User u) {
        JFrame createReq = new JFrame("Create Request");
        createReq.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createReq.setLayout(new BoxLayout(createReq.getContentPane(), BoxLayout.PAGE_AXIS));
        Request r = new Request();

        JPanel info = new JPanel(new GridLayout(4, 2));
        info.add(new JLabel("Type"));

        // choose the request type
        RequestTypes[] choices = {RequestTypes.DELETE_ACCOUNT, RequestTypes.ACTOR_ISSUE, RequestTypes.MOVIE_ISSUE, RequestTypes.OTHERS};
        JComboBox<RequestTypes> choose = new JComboBox<>(choices);
        info.add(choose);

        // insert the name of the production/actor
        JLabel name = new JLabel("Name:");
        JComboBox<Object> insertName = new JComboBox<>();
        name.setVisible(false);
        info.add(name);
        insertName.setVisible(false);
        info.add(insertName);

        // insert the description
        JLabel desc = new JLabel("Description:");
        desc.setVisible(false);
        info.add(desc);
        JTextField insertDesc = new JTextField();
        insertDesc.setVisible(false);
        info.add(insertDesc);

        // buttons
        JButton discard = new JButton("Discard");
        discard.setBackground(Color.RED);
        discard.setForeground(Color.WHITE);
        info.add(discard);
        JButton submit = new JButton("Submit");
        submit.setBackground(Color.GREEN);
        submit.setEnabled(false);
        info.add(submit);

        // message if contributor requests for his own production
        JLabel warning = new JLabel("You added this contribution yourself");
        warning.setForeground(Color.RED);
        warning.setVisible(false);

        createReq.add(info);
        createReq.add(warning);

        // adding action listener for combo box
        choose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RequestTypes type = (RequestTypes) choose.getSelectedItem();
                r.setRequestType(type);

                if(type == RequestTypes.MOVIE_ISSUE || type == RequestTypes.ACTOR_ISSUE) {
                    name.setVisible(true);
                    insertName.setVisible(true);
                } else {
                    name.setVisible(false);
                    insertName.setVisible(false);
                }

                if(type == RequestTypes.MOVIE_ISSUE) {
                    insertName.removeAllItems();
                    for(Production p : imdb.productions) {
                        insertName.addItem(p);
                    }
                }

                if(type == RequestTypes.ACTOR_ISSUE) {
                    insertName.removeAllItems();
                    for(Actor a : imdb.actors) {
                        insertName.addItem(a);
                    }
                }

                desc.setVisible(true);
                insertDesc.setVisible(true);
            }
        });

        // adding action listeners to the text boxes
        ActionListener fill = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!insertDesc.getText().isEmpty()) {
                    submit.setEnabled(true);
                }
            }
        };
        insertDesc.addActionListener(fill);
        insertName.addActionListener(fill);

        // adding action listeners to the buttons
        discard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
                createReq.dispose();
            }
        });

        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(r.getRequestType() == RequestTypes.MOVIE_ISSUE || r.getRequestType() == RequestTypes.ACTOR_ISSUE) {
                    if(r.getRequestType() == RequestTypes.MOVIE_ISSUE) {
                        r.name = ((Production) insertName.getSelectedItem()).title;
                    } else {
                        r.name = ((Actor) insertName.getSelectedItem()).name;
                    }
                    r.fixer = "TBD";
                } else {
                    r.fixer = "ADMIN";
                }
                r.description = insertDesc.getText();
                r.requester = username;
                r.setDate(LocalDateTime.now());

                if(handleReq(r) == false) {
                    insertDesc.setText("");
                    warning.setVisible(true);
                } else {
                    warning.setVisible(false);
                    GUI();
                    createReq.dispose();
                }
            }
        });

        createReq.pack();
        createReq.setLocationRelativeTo(null);
        createReq.setVisible(true);
    }

    // GUI with info for request created by the user
    public abstract void ownRequestGUI(Request r);

    // option for accessing created request GUI
    public void RequestRegularGUI(User u) {
        JButton requestManageButton = new JButton("Manage Requests");
        leftMenu.add(requestManageButton);

        requestManageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRequests(u);
            }
        });
    }

    // GUI with requests the user proposed
    public void showRequests(User u) {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // request page
        workingSpace.add(new JLabel("Requests you proposed"));

        JButton addRequest = new JButton("+Create request");
        workingSpace.add(addRequest);

        JList<Request> reqList = new JList<>(u.createdRequests.toArray(new Request[u.createdRequests.size()]));
        reqList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        workingSpace.add(reqList);

        addRequest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createRequestGUI(u);
                frontPage.dispose();
            }
        });

        // access specific request
        reqList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    Request selectedRequest = (Request) reqList.getSelectedValue();
                    ownRequestGUI(selectedRequest);
                    frontPage.dispose();
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
    }

    // GUI with info about an actor
    public void ActorGUI(Actor actor) {
        actorFrame = new JFrame("Actor");
        actorFrame.setDefaultCloseOperation(actorFrame.EXIT_ON_CLOSE);
        actorFrame.setMinimumSize(new Dimension(400, 400));
        actorFrame.setLayout(new BoxLayout(actorFrame.getContentPane(), BoxLayout.PAGE_AXIS));

        // info and options
        JTextArea info = new JTextArea(actor.infoToString());
        JButton addFav = new JButton("Add to favourites");
        JButton removeFav = new JButton("Remove from favourites");
        JButton returnIMDB = new JButton("Return to IMDB");

        actorFrame.add(new JScrollPane(info));
        actorFrame.add(addFav);
        actorFrame.add(removeFav);
        actorFrame.add(returnIMDB);

        if(favouriteActor.contains(actor)) {
            addFav.setEnabled(false);
        } else {
            removeFav.setEnabled(false);
        }

        addFav.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFavouriteActor(actor);
                addFav.setEnabled(false);
                removeFav.setEnabled(true);
            }
        });

        returnIMDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
                actorFrame.dispose();
            }
        });

        removeFav.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeFavouriteActor(actor);
                removeFav.setEnabled(false);
                addFav.setEnabled(true);
            }
        });

        actorFrame.pack();
        actorFrame.setLocationRelativeTo(null);
        actorFrame.setVisible(true);
    }

    // GUI with info about a production
    public void ProductionGUI(Production prod) {
        prodFrame = new JFrame("Production");
        prodFrame.setDefaultCloseOperation(prodFrame.EXIT_ON_CLOSE);
        prodFrame.setMinimumSize(new Dimension(400, 400));
        prodFrame.setLayout(new BoxLayout(prodFrame.getContentPane(), BoxLayout.PAGE_AXIS));

        // info and options
        JTextArea info = new JTextArea(prod.infoToString());
        JButton addFav = new JButton("Add to favourites");
        JButton removeFav = new JButton("Remove from favourites");
        JButton returnIMDB = new JButton("Return to IMDB");

        prodFrame.add(new JScrollPane(info));
        prodFrame.add(addFav);
        prodFrame.add(removeFav);
        prodFrame.add(returnIMDB);

        if(favouriteProduction.contains(prod)) {
            addFav.setEnabled(false);
        } else {
            removeFav.setEnabled(false);
        }

        addFav.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFavouriteProduction(prod);
                addFav.setEnabled(false);
                removeFav.setEnabled(true);
            }
        });

        returnIMDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
                prodFrame.dispose();
            }
        });

        removeFav.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeFavouriteProduction(prod);
                removeFav.setEnabled(false);
                addFav.setEnabled(true);
            }
        });

        prodFrame.pack();
        prodFrame.setLocationRelativeTo(null);
        prodFrame.setVisible(true);
    }

    public void GUI() {
        frontPage = new JFrame("IMDB");
        frontPage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frontPage.setMinimumSize(new Dimension(800, 600));
        frontPage.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setLayout(new GridLayout(2, 2));
        JLabel title = new JLabel("IMDB");
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 18));
        JButton logOut = new JButton("Log Out");
        logOut.setMaximumSize(new Dimension(1, 10));
        header.add(title);
        header.add(logOut);

        // adding experience
        header.add(new JLabel("Your experience:"));

        JTextField exp = new JTextField();
        if(type == AccountType.ADMIN)
            exp.setText("-");
        else
            exp.setText(experience + "");
        exp.setEditable(false);
        header.add(exp);

        frontPage.add(header, BorderLayout.NORTH);

        // Left Menu
        leftMenu = new JPanel();
        leftMenu.setLayout(new GridLayout(11, 1));
        JButton productionsButton = new JButton("Productions");
        JButton actorsButton = new JButton("Actors");
        JButton searchButton = new JButton(("Search"));
        JButton notificationsButton = new JButton("Notifications");
        JButton favActorsButton = new JButton("Favourite Actors");
        JButton favProductionsButton = new JButton("Favourite Productions");
        leftMenu.add(productionsButton);
        leftMenu.add(actorsButton);
        leftMenu.add(searchButton);
        leftMenu.add(notificationsButton);
        leftMenu.add(favActorsButton);
        leftMenu.add(favProductionsButton);
        frontPage.add(leftMenu, BorderLayout.WEST);

        // Working Space
        workingSpace = new JPanel();
        workingSpace.setLayout(new BoxLayout(workingSpace, BoxLayout.Y_AXIS));

        frontPage.add(workingSpace, BorderLayout.CENTER);

        // startup
        showProductions();

        productionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Display productions in the working space
                showProductions();
            }
        });

        actorsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Display actors in the working space
                showActors();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearch();
            }
        });

        notificationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Display notifications in the working space
                showNotifications();
            }
        });

        favActorsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Display favourite actors in the working space
                showFavouriteActors();
            }
        });

        favProductionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Display favourite productions in the working space
                showFavouriteProductions();
            }
        });

        // Action listener for log out
        logOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logOut();
            }
        });

        frontPage.pack();
        frontPage.setLocationRelativeTo(null);
        frontPage.setVisible(true);
    }

    // GUI list with filters of all productions
    public void showProductions() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // adding list of productions
        DefaultListModel<Production> allProdModel = new DefaultListModel<>();
        for (Production production : imdb.productions) {
            allProdModel.addElement(production);
        }
        JList<Production> allProd = new JList<>(allProdModel);
        allProd.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        // adding filters
        JPanel filters = new JPanel(new GridLayout(8, 2));

        // genre filter
        Genre[] genreList = {null, Genre.Action, Genre.Adventure, Genre.Comedy, Genre.Drama,
                Genre.Horror, Genre.SF, Genre.Fantasy, Genre.Romance, Genre.Mystery, Genre.Thriller, Genre.Crime,
                Genre.Biography, Genre.War, Genre.Cooking};
        JComboBox<Genre> filterGenre = new JComboBox<>(genreList);
        filters.add(new JLabel("Genre:"));
        filters.add(filterGenre);

        filterGenre.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("All");
                } else if (value instanceof Genre) {
                    setText(((Genre) value).toString());
                }
                return this;
            }
        });

        // filter by number of ratings
        JTextField ratingFilter = new JTextField("0");
        filters.add(new JLabel("Number of reviews:"));
        filters.add(ratingFilter);

        // filter by minimum rating
        JTextField minimumFilter = new JTextField("0");
        filters.add(new JLabel("Minimum rating:"));
        filters.add(minimumFilter);

        // filter by actor playing
        ArrayList<Actor> actorList = new ArrayList<>();
        actorList.add(null);
        actorList.addAll(imdb.actors);

        JComboBox<Actor> filterActor = new JComboBox<>(actorList.toArray(new Actor[actorList.size()]));
        filters.add(new JLabel("Actor:"));
        filters.add(filterActor);

        filterActor.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Any");
                } else if (value instanceof Genre) {
                    setText(value.toString());
                }
                return this;
            }
        });

        // filter by production type
        ProductionType[] prodTypeList = {null, ProductionType.Movie, ProductionType.Series};
        JComboBox<ProductionType> filterType = new JComboBox<>(prodTypeList);

        filterType.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Both");
                } else if (value instanceof Genre) {
                    setText(value.toString());
                }
                return this;
            }
        });
        filters.add(new JLabel("Type:"));
        filters.add(filterType);

        // movies: minimum duration
        JTextField durationFilter = new JTextField("0");
        filters.add(new JLabel("Duration:"));
        filters.add(durationFilter);
        durationFilter.setToolTipText("Only available for Movies");
        durationFilter.setEnabled(false);

        // series: minimum no of seasons
        JTextField seasonFilter = new JTextField("0");
        filters.add(new JLabel("Number of seasons:"));
        filters.add(seasonFilter);
        seasonFilter.setToolTipText("Only available for series");
        seasonFilter.setEnabled(false);

        JButton filterBy = new JButton("Filter");
        filters.add(filterBy);

        JButton removeFilters = new JButton("Remove filters");
        filters.add(removeFilters);

        workingSpace.add(new JLabel("Productions:"));
        workingSpace.add(filters);
        workingSpace.add(new JScrollPane(allProd));

        // action listeners for filters
        filterType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProductionType selection = (ProductionType) filterType.getSelectedItem();
                if(selection == null) {
                    durationFilter.setEnabled(false);
                    seasonFilter.setEnabled(false);
                } else if (selection == ProductionType.Movie) {
                    durationFilter.setEnabled(true);
                    seasonFilter.setEnabled(false);
                } else {
                    durationFilter.setEnabled(false);
                    seasonFilter.setEnabled(true);
                }
            }
        });

        durationFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer.parseInt(durationFilter.getText());
                    filterBy.setEnabled(true);
                } catch (NumberFormatException er) {
                    filterBy.setEnabled(false);
                }
            }
        });

        seasonFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer.parseInt(seasonFilter.getText());
                    filterBy.setEnabled(true);
                } catch (NumberFormatException er) {
                    filterBy.setEnabled(false);
                }
            }
        });

        ratingFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer.parseInt(ratingFilter.getText());
                    filterBy.setEnabled(true);
                } catch (NumberFormatException er) {
                    filterBy.setEnabled(false);
                }
            }
        });

        ratingFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Float.parseFloat(minimumFilter.getText());
                    filterBy.setEnabled(true);
                } catch (NumberFormatException er) {
                    filterBy.setEnabled(false);
                }
            }
        });

        filterBy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int numRating = Integer.parseInt(ratingFilter.getText());
                float minRating = Float.parseFloat(minimumFilter.getText());
                Genre genreFilter = (Genre) filterGenre.getSelectedItem();
                Actor actorFilter = (Actor) filterActor.getSelectedItem();
                ProductionType prodFilter = (ProductionType) filterType.getSelectedItem();

                DefaultListModel<Production> filtered = new DefaultListModel<>();

                ArrayList<Production> filterProd = new ArrayList<>();
                filterProd.addAll(imdb.productions);

                // filtering by genre
                if(genreFilter != null) {
                    filterProd.removeIf(p -> !p.genres.contains(genreFilter));
                }

                // filtering by number of ratings
                filterProd.removeIf(p -> p.reviews.size() < numRating);

                // filter by minimum rating
                filterProd.removeIf(p -> p.rating < minRating);

                // filtering by actor
                if(actorFilter != null) {
                    filterProd.removeIf(p -> !p.actors.contains(actorFilter.name));
                }

                // filtering by type
                if(prodFilter != null) {
                    filterProd.removeIf(p -> p.type != prodFilter);
                }

                // filtering by duration
                if(prodFilter == ProductionType.Movie) {
                    int duration = (Integer) Integer.parseInt(durationFilter.getText());
                    String time = duration + " minutes";

                    filterProd.removeIf(p -> ((Movie) p).duration.compareTo(time) < 0);
                }

                // filtering by seasons
                if(prodFilter == ProductionType.Series) {
                    int seasons = (Integer) Integer.parseInt(seasonFilter.getText());

                    filterProd.removeIf(p -> ((Series) p).numSeasons < seasons);
                }

                // adding remaining to filtered list
                for(Production p : filterProd)
                    filtered.addElement(p);

                allProd.setModel(filtered);
            }
        });

        removeFilters.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allProd.setModel(allProdModel);
            }
        });

        allProd.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    Production selectedProd = (Production) allProd.getSelectedValue();
                    ProductionGUI(selectedProd);
                    frontPage.dispose();
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
    }

    // GUI with search bar
    public void showSearch() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        JPanel searchBar = new JPanel();
        searchBar.setLayout(new GridLayout(1, 3));

        // search bar
        searchBar.add(new JLabel("Search"));

        JTextField searchBox = new JTextField();
        JButton search = new JButton("Search");
        search.setEnabled(false);

        searchBar.add(searchBox);
        searchBar.add(search);

        workingSpace.add(searchBar);

        // warning message
        JLabel notFound = new JLabel("Search not found");
        notFound.setForeground(Color.RED);
        notFound.setVisible(false);
        workingSpace.add(notFound);

        searchBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!searchBox.getText().isEmpty()) {
                    search.setEnabled(true);
                }
            }
        });

        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = searchBox.getText();

                // searching through actors
                for(Actor a : imdb.actors) {
                    if(a.name.equals(name)){
                        ActorGUI(a);
                        frontPage.dispose();
                    }
                }

                // searching through actors
                for(Production p : imdb.productions) {
                    if(p.title.equals(name)){
                        ProductionGUI(p);
                        frontPage.dispose();
                    }
                }

                notFound.setVisible(true);
            }
        });
    }

    public abstract void CLI();

    // GUI with sortable list of all actors
    public void showActors() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // displaying actors
        JList<Actor> allActors = new JList<>(imdb.actors.toArray(new Actor[imdb.actors.size()]));
        allActors.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        List<Actor> sortedActors = new ArrayList<>();
        sortedActors.addAll(imdb.actors);
        Collections.sort(sortedActors);
        DefaultListModel<Actor> sortedActorModel = new DefaultListModel<>();
        for (Actor actor : sortedActors) {
            sortedActorModel.addElement(actor);
        }

        JButton sortActors = new JButton("Sort actors");

        workingSpace.add(new JLabel("Actors:"));
        workingSpace.add(sortActors);
        workingSpace.add(new JScrollPane(allActors));

        sortActors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allActors.setModel(sortedActorModel);
            }
        });

        allActors.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    Actor selectedActor = (Actor) allActors.getSelectedValue();
                    ActorGUI(selectedActor);
                    frontPage.dispose();
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
    }

    // GUI list of all notifications
    public void showNotifications() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // notifcation panel
        DefaultListModel<String> notifModel = new DefaultListModel<>();
        for(String n : notifications) {
            notifModel.addElement(n);
        }
        JList<String> notif = new JList<>(notifModel);
        notif.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        notif.setToolTipText("Double click to remove notification");

        workingSpace.add(new JLabel("Notifications:"));
        workingSpace.add(new JScrollPane(notif));

        notif.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    String selectedNotification = notif.getSelectedValue();
                    notifModel.removeElement(selectedNotification);
                    notifications.remove(selectedNotification);
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
    }

    // GUI favourite actor list
    public void showFavouriteActors() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // displaying favourite actors
        JList<Actor> favActors = new JList<>(favouriteActor.toArray(new Actor[favouriteActor.size()]));
        favActors.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        workingSpace.add(new JLabel(" Favourite Actors:"));
        workingSpace.add(new JScrollPane(favActors));

        favActors.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    Actor selectedActor = (Actor) favActors.getSelectedValue();
                    ActorGUI(selectedActor);
                    frontPage.dispose();
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
    }

    // GUI favourite production list
    public void showFavouriteProductions() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // displaying favourite productions
        JList<Production> favProd = new JList<>(favouriteProduction.toArray(new Production[favouriteProduction.size()]));
        favProd.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        workingSpace.add(new JLabel("Favourite Productions:"));
        workingSpace.add(new JScrollPane(favProd));

        favProd.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    Production selectedProd = (Production) favProd.getSelectedValue();
                    ProductionGUI(selectedProd);
                    frontPage.dispose();
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
    }

    // CLI with info and options for a production
    public abstract void ProductionCLI(Production selectedProd);

    // CLI for selecting production
    public void selectProductionCLI(List<Production> prodList) {
        Scanner scanner = new Scanner(System.in);
        int choice;
        boolean ok = false;
        Production selectedProd = null;

        do {
            System.out.print("Enter the number of the production you want to select (0 to cancel): ");
            choice = scanner.nextInt();

            if (choice >= 1 && choice <= prodList.size()) {
                selectedProd = prodList.get(choice - 1);
                ok = true;
            } else if (choice == 0) {
                System.out.println("Selection canceled.");
                return;
            } else {
                System.out.println("Invalid production number. Please try again.");
            }
        } while(!ok);

        ProductionCLI(selectedProd);
    }

    // CLI for filtering productions
    public List<Production> filterProductionsCLI() {
        Scanner scanner = new Scanner(System.in);

        // including the lists
        ArrayList<Actor> actorList = new ArrayList<>();
        actorList.add(null);
        actorList.addAll(imdb.actors);

        ProductionType[] prodTypeList = {null, ProductionType.Movie, ProductionType.Series};

        Genre[] genreList = {null, Genre.Action, Genre.Adventure, Genre.Comedy, Genre.Drama,
                Genre.Horror, Genre.SF, Genre.Fantasy, Genre.Romance, Genre.Mystery, Genre.Thriller, Genre.Crime,
                Genre.Biography, Genre.War, Genre.Cooking};

        // filter by genre
        System.out.println("Select Genre:");
        for (int i = 0; i < genreList.length; i++) {
            System.out.println((i + 1) + ") " + (genreList[i] == null ? "All" : genreList[i].toString()));
        }
        System.out.print("Enter the number of the genre you want to filter by (0 for All): ");
        int genreChoice = scanner.nextInt();
        Genre genreFilter = (genreChoice > 0 && genreChoice <= genreList.length) ? genreList[genreChoice - 1] : null;

        // filter by number of ratings
        System.out.print("Enter the number of reviews to filter by: ");
        int numRating = scanner.nextInt();

        // filter by minimum rating
        System.out.print("Enter the minimum rating to filter by: ");
        float minRating = scanner.nextFloat();

        // filter by actor
        System.out.println("Select Actor:");
        for (int i = 0; i < actorList.size(); i++) {
            System.out.println((i + 1) + ") " + (actorList.get(i) == null ? "Any" : actorList.get(i).toString()));
        }
        System.out.print("Enter the number of the actor you want to filter by (0 for Any): ");
        int actorChoice = scanner.nextInt();
        Actor actorFilter = (actorChoice > 0 && actorChoice <= actorList.size()) ? actorList.get(actorChoice - 1) : null;

        // filter by production type
        System.out.println("Select Production Type:");
        for (int i = 0; i < prodTypeList.length; i++) {
            System.out.println((i + 1) + ") " + (prodTypeList[i] == null ? "Both" : prodTypeList[i].toString()));
        }
        System.out.print("Enter the number of the production type you want to filter by (0 for Both): ");
        int typeChoice = scanner.nextInt();
        ProductionType prodFilter = (typeChoice > 0 && typeChoice <= prodTypeList.length) ? prodTypeList[typeChoice - 1] : null;

        // filter by duration (only for movies)
        int durationFilter = 0;
        if (prodFilter == ProductionType.Movie) {
            System.out.print("Enter the minimum duration to filter by (in minutes): ");
            durationFilter = scanner.nextInt();
        }

        // filter by seasons (only for series)
        int seasonsFilter;
        if (prodFilter == ProductionType.Series) {
            System.out.print("Enter the minimum number of seasons to filter by: ");
            seasonsFilter = scanner.nextInt();
        } else {
            seasonsFilter = 0;
        }

        ArrayList<Production> filterProd = new ArrayList<>();
        filterProd.addAll(imdb.productions);

        // filtering by genre
        if(genreFilter != null) {
            filterProd.removeIf(p -> !p.genres.contains(genreFilter));
        }

        // filtering by number of ratings
        filterProd.removeIf(p -> p.reviews.size() < numRating);

        // filter by minimum rating
        filterProd.removeIf(p -> p.rating < minRating);

        // filtering by actor
        if(actorFilter != null) {
            filterProd.removeIf(p -> !p.actors.contains(actorFilter.name));
        }

        // filtering by type
        if(prodFilter != null) {
            filterProd.removeIf(p -> p.type != prodFilter);
        }

        // filtering by duration
        if(prodFilter == ProductionType.Movie) {
            String time = durationFilter + " minutes";

            filterProd.removeIf(p -> ((Movie) p).duration.compareTo(time) < 0);
        }

        // filtering by seasons
        if(prodFilter == ProductionType.Series) {
            filterProd.removeIf(p -> ((Series) p).numSeasons < seasonsFilter);
        }

        return filterProd;
    }

    // CLI with list of all productions
    public void showProductionsCLI() {
        System.out.println("Displaying Productions...");
        IMDB imdb = IMDB.getInstance();
        List<Production> workingList = imdb.productions;

        // display productions
        int idx = 1;
        for (Production p : workingList) {
            System.out.println("\t" + idx + ") " + p);
            idx++;
        }

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Filter productions");
            System.out.println("2. Remove filters");
            System.out.println("3. Select a production");
            System.out.println("4. Go back to main menu");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    workingList = filterProductionsCLI();
                    for (Production p : workingList) {
                        System.out.println("\t" + idx + ") " + p);
                        idx++;
                    }
                    break;
                case 2:
                    workingList = imdb.productions;
                    for (Production p : workingList) {
                        System.out.println("\t" + idx + ") " + p);
                        idx++;
                    }
                    break;
                case 3:
                    selectProductionCLI(workingList);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // CLI for sorting actors
    public List<Actor> showSortedActorsCLI() {
        System.out.println("Displaying Actors...");
        IMDB imdb = IMDB.getInstance();
        List<Actor> sorted = new ArrayList<>();
        sorted.addAll(imdb.actors);
        Collections.sort(sorted);

        // display actors
        int idx = 1;
        for(Actor a: sorted) {
            System.out.println("\t" + idx + ") " + a);
            idx++;
        }

        return sorted;
    }

    // CLI with info and options for an actor
    public abstract void ActorCLI(Actor selectedActor);

    // CLI for selecting an actor
    public void selectActorCLI(List<Actor> actorList) {
        Scanner scanner = new Scanner(System.in);
        int choice;
        boolean ok = false;
        Actor selectedActor = new Actor();

        do {
            System.out.print("Enter the number of the actor you want to select (0 to cancel): ");
            choice = scanner.nextInt();

            if (choice >= 1 && choice <= actorList.size()) {
                selectedActor = actorList.get(choice - 1);
                ok = true;
            } else if (choice == 0) {
                System.out.println("Selection canceled.");
                return;
            } else {
                System.out.println("Invalid actor number. Please try again.");
            }
        } while(!ok);

        ActorCLI(selectedActor);
    }

    // CLI list of all actors
    public void showActorsCLI() {
        System.out.println("Displaying Actors...");
        IMDB imdb = IMDB.getInstance();
        List<Actor> workingList = imdb.actors;

        // display actors
        int idx = 1;
        for(Actor a: workingList) {
            System.out.println("\t" + idx + ") " + a);
            idx++;
        }

        while(true) {
            System.out.println("\nOptions:");
            System.out.println("1. Sort actors");
            System.out.println("2. Select an actor");
            System.out.println("3. Go back to main menu");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    workingList = showSortedActorsCLI();
                    break;
                case 2:
                    selectActorCLI(workingList);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // CLI search bar
    public void showSearchCLI() {
        IMDB imdb = IMDB.getInstance();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Search:");

            System.out.print("Enter the name or title to search (enter 'exit' to leave): ");
            String searchQuery = scanner.nextLine();

            if (searchQuery.equalsIgnoreCase("exit")) {
                System.out.println("Exiting search.");
                return;
            }

            boolean found = false;

            // Search through actors
            for (Actor actor : imdb.actors) {
                if (actor.name.equalsIgnoreCase(searchQuery)) {
                    found = true;
                    ActorCLI(actor);
                    break;
                }
            }

            // Search through productions
            for (Production production : imdb.productions) {
                if (production.title.equalsIgnoreCase(searchQuery)) {
                    found = true;
                    ProductionCLI(production);
                    break;
                }
            }

            if (!found) {
                System.out.println("Search not found.");
            }
        }
    }

    // CLI for removing notification
    public void removeNotificationCLI(List<String> notifications) {
        Scanner scanner = new Scanner(System.in);
        boolean ok = false;

        do {
            System.out.print("Enter the number of the notification you want to remove (0 to cancel): ");
            int notificationChoice = scanner.nextInt();

            if (notificationChoice >= 1 && notificationChoice <= notifications.size()) {
                String removedNotification = notifications.remove(notificationChoice - 1);
                System.out.println("Notification removed: " + removedNotification);
                ok = true;
            } else if (notificationChoice == 0) {
                System.out.println("Removal canceled.");
                return;
            } else {
                System.out.println("Invalid notification number. Please try again.");
            }
        }while(!ok);
    }

    // CLI with list of all notifications
    public void showNotificationsCLI() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Notifications:");

        for (int i = 0; i < notifications.size(); i++) {
            System.out.println((i + 1) + ") " + notifications.get(i));
        }

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Remove a notification");
            System.out.println("2. Go back to main menu");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    removeNotificationCLI(notifications);
                    break;
                case 2:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // CLI with list of favourite actors
    public void showFavouriteActorsCLI() {
        System.out.println("Displaying Actors...");
        IMDB imdb = IMDB.getInstance();
        List<Actor> workingList = new ArrayList<>();

        while(true) {
            // display actors
            int idx = 1;
            for(Actor a: favouriteActor) {
                System.out.println("\t" + idx + ") " + a);
                workingList.add(a);
                idx++;
            }

            System.out.println("\nOptions:");
            System.out.println("1. Select an actor");
            System.out.println("2. Go back to main menu");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    selectActorCLI(workingList);
                    break;
                case 2:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // CLI with list of favourite productions
    public void showFavouriteProductionsCLI() {
        System.out.println("Displaying Productions...");
        List<Production> workingList = new ArrayList<>();

        while (true) {
            // display productions
            int idx = 1;
            for (Production p : favouriteProduction) {
                System.out.println("\t" + idx + ") " + p);
                workingList.add(p);
                idx++;
            }

            System.out.println("\nOptions:");
            System.out.println("1. Select a production");
            System.out.println("2. Go back to main menu");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    selectProductionCLI(workingList);
                    break;
                case 2:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // CLI with info on request created by user
    public abstract void ownRequestCLI(Request r);

    // CLI for creating request
    public void createRequestCLI(User u) {
        Scanner scanner = new Scanner(System.in);
        Request r = new Request();

        System.out.println("Create Request:");

        // request type
        RequestTypes type = null;
        while (type == null) {
            System.out.println("Choose the request type:");
            for (int i = 0; i < RequestTypes.values().length; i++) {
                System.out.println((i + 1) + ") " + RequestTypes.values()[i]);
            }
            System.out.print("Enter the number of the request type: ");
            int typeChoice = scanner.nextInt();

            if (typeChoice > 0 && typeChoice <= RequestTypes.values().length) {
                type = RequestTypes.values()[typeChoice - 1];
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
        r.setRequestType(type);

        // name(optional)
        String name = null;
        if (type == RequestTypes.MOVIE_ISSUE || type == RequestTypes.ACTOR_ISSUE) {
            while (name == null) {
                if (type == RequestTypes.MOVIE_ISSUE) {
                    int idx = 1;
                    for(Production p : imdb.productions) {
                        System.out.println(idx + ") " + p.title);
                        idx++;
                    }
                    System.out.print("Enter the number of the production: ");
                    int prodChoice = scanner.nextInt();

                    if (prodChoice > 0 && prodChoice <= imdb.productions.size()) {
                        name = imdb.productions.get(prodChoice - 1).title;
                        break;
                    } else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                } else {
                    int idx = 1;
                    for(Actor a : imdb.actors) {
                        System.out.println(idx + ") " + a.name);
                        idx++;
                    }
                    System.out.print("Enter the number of the actor: ");
                    int actorChoice = scanner.nextInt();

                    if (actorChoice > 0 && actorChoice <= imdb.actors.size()) {
                        name = imdb.actors.get(actorChoice - 1).name;
                        break;
                    } else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                }
            }
        }
        r.name = name;

        // description
        System.out.print("Enter the description: ");
        scanner.nextLine(); // Consume the newline character
        String description = scanner.nextLine();
        r.description = description;

        // description
        System.out.println("Summary:");
        System.out.println("Type: " + r.getRequestType());
        System.out.println("Name: " + r.name);
        System.out.println("Description: " + r.description);

        // confirm submission
        System.out.print("Submit this request? (yes/no): ");
        String submitChoice = scanner.nextLine().toLowerCase();
        if ("yes".equals(submitChoice)) {
            // create request and submit
            r.fixer = "TBD";
            r.requester = username;
            r.setDate(LocalDateTime.now());

            if (handleReq(r)) {
                System.out.println("Request submitted successfully.");
            } else {
                System.out.println("Failed to submit request. Please try again.");
            }
        } else {
            System.out.println("Request discarded.");
        }
    }

    // CLI for managing requests created
    public void showRequestsCLI(User u) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Requests you proposed");

            List<Request> userRequests = u.createdRequests;
            for (int i = 0; i < userRequests.size(); i++) {
                System.out.println((i + 1) + ") " + userRequests.get(i));
            }

            System.out.println("Options:");
            System.out.println("1) Create Request");
            System.out.println("2) Access Request");
            System.out.println("3) Exit");

            System.out.print("Enter the number of your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // newline character

            switch (choice) {
                case 1:
                    createRequestCLI(u);
                    break;
                case 2:
                    boolean ok = false;

                    do {
                        if (!userRequests.isEmpty()) {
                            int idx = 1;
                            for(Request r :createdRequests) {
                                System.out.println(idx + ") " + r);
                                idx++;
                            }
                            System.out.print("Enter the number of the request you want to access(or 0 for exit): ");
                            int accessChoice = scanner.nextInt();
                            scanner.nextLine(); // newline character

                            if (accessChoice > 0 && accessChoice <= userRequests.size()) {
                                Request selectedRequest = userRequests.get(accessChoice - 1);
                                ownRequestCLI(selectedRequest);
                                ok = true;
                            } else if (accessChoice == 0){
                                System.out.println("Retunrning...");
                                break;
                            } else {
                                System.out.println("Invalid request number. Please try again.");
                            }
                        } else {
                            System.out.println("No requests available to access.");
                            break;
                        }
                    }while(!ok);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
