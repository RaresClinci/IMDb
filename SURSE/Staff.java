import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.text.JTextComponent;

public abstract class Staff extends User implements StaffInterface{
    List<Request> personalReq;
    SortedSet<Production> addedProductions;
    SortedSet<Actor> addedActor;

    public Staff() {
        super();
        addedProductions = new TreeSet<>();
        addedActor = new TreeSet<>();
        personalReq = new ArrayList<>();
    }

    @Override
    public void addProductionSystem(Production p) {
        IMDB imdb = IMDB.getInstance();
        addedProductions.add(p);
        imdb.productions.add(p);
        p.Subscribe(this);

        // adding experience
        if(type == AccountType.CONTRIBUTOR) {
            ContributionExperience strategy = new ContributionExperience();
            addExperience(strategy.calculateExperience());
        }
    }

    @Override
    public void addActorSystem(Actor a) {
        IMDB imdb = IMDB.getInstance();
        addedActor.add(a);
        imdb.actors.add(a);

        // adding experience
        if(type == AccountType.CONTRIBUTOR) {
            ContributionExperience strategy = new ContributionExperience();
            addExperience(strategy.calculateExperience());
        }
    }

    @Override
    public void removeProductionSystem(String name) {
        IMDB imdb = IMDB.getInstance();
        Production p = null;
        for(Production prod: addedProductions) {
            if(prod.title.equals(name)) {
                p = prod;
                imdb.productions.remove(prod);
                break;
            }
        }

        // removing the production from the user that added it
        for(User u: imdb.users) {
            if(u.type != AccountType.REGULAR) {
                Staff s = (Staff) u;
                s.addedProductions.remove(p);
            }
        }

        Admin.sharedProductions.remove(p);

        // removing the production from favourites
        for(User u : imdb.users) {
            u.favouriteProduction.remove(p);
        }
    }

    @Override
    public void removeActorSystem(String name) {
        IMDB imdb = IMDB.getInstance();
        Actor a = null;
        for(Actor act: addedActor) {
            if(act.name.equals(name)) {
                a = act;
                imdb.actors.remove(act);
                break;
            }
        }

        // removing the production from the user that added it
        for(User u: imdb.users) {
            if(u.type != AccountType.REGULAR) {
                Staff s = (Staff) u;
                s.addedActor.remove(a);
            }
        }

        Admin.sharedActor.remove(a);

        // removing the production from favourites
        for(User u : imdb.users) {
            u.favouriteActor.remove(a);
        }
    }

    @Override
    public void updateProduction(Production p) {
        IMDB imdb = IMDB.getInstance();
        Production p2 = null;

        // finding the original production
        for(Production prod: imdb.productions) {
            if(prod.title.equals(p.title)) {
                p2 = prod;
                break;
            }
        }
        imdb.productions.remove(p2);
        imdb.productions.add(p);

        // removing the production from the user that added it
        for(User u: imdb.users) {
            if(u.type != AccountType.REGULAR) {
                Staff s = (Staff) u;
                s.addedProductions.remove(p2);
                s.addedProductions.add(p);
            }
        }

        if(Admin.sharedProductions.contains(p2)) {
            Admin.sharedProductions.remove(p2);
            Admin.sharedProductions.add(p);
        }

        // removing the production from favourites
        for(User u : imdb.users) {
            u.favouriteProduction.remove(p2);
            u.favouriteProduction.add(p);
        }
    }

    @Override
    public void updateActor(Actor a) {
        IMDB imdb = IMDB.getInstance();
        Actor a2 = null;

        // finding the original actor
        for(Actor act: imdb.actors) {
            if(act.name.equals(a.name)) {
                a2 = act;
                break;
            }
        }
        imdb.actors.remove(a2);
        imdb.actors.add(a);

        // removing the production from the user that added it
        for(User u: imdb.users) {
            if(u.type != AccountType.REGULAR) {
                Staff s = (Staff) u;
                s.addedActor.remove(a2);
                s.addedActor.add(a);
            }
        }

        if(Admin.sharedActor.contains(a2)) {
            Admin.sharedActor.add(a2);
            Admin.sharedActor.add(a);
        }

        // removing the production from favourites
        for(User u : imdb.users) {
            u.favouriteActor.remove(a2);
            u.favouriteActor.add(a);
        }
    }

    public void resolveRequest(Request r, boolean solved) {
        // eliminating the request from the fixer's end
        IMDB imdb = IMDB.getInstance();
        if (r.fixer.equals("ADMIN")) {
            IMDB.RequestHolder.removeRequest(r);
        } else {
            for(User u : imdb.users) {
                if(u.username.equals(r.fixer)) {
                    ((Staff) u).personalReq.remove(r);
                }
            }
        }

        // eliminating the request from the requester's end
        for(User u : imdb.users) {
            if(u.username.equals(r.requester)) {
                u.createdRequests.remove(r);

                // giving the user experience
                if(solved) {
                    RequestExperience strategy = new RequestExperience();
                    addExperience(strategy.calculateExperience());
                }
            }
        }

        r.notifySolved(solved);
    }

    enum ChangeType {
        REPLACE, ADD;
    }

    // GUI for modifying series
    public void modifySeries(Series s, ChangeType type) {
        JFrame newSeries = new JFrame("Series Editor");
        newSeries.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newSeries.setLayout(new GridLayout(10, 2, 5, 5));

        // create JLabels for each field
        JLabel[] labels = {
                new JLabel("Title:"),
                new JLabel("Directors:"),
                new JLabel("Actors:"),
                new JLabel("Genres:"),
                new JLabel("Description:"),
                new JLabel("Year:"),
                new JLabel("Number of Seasons:"),
                new JLabel("Episodes:")
        };

        // create JTextFields or JTextAreas
        JComponent[] inputFields = new JComponent[labels.length];
        for (int i = 0; i < inputFields.length; i++) {
            if (i == 1 || i == 2 || i == 3 || i == 4 || i == 7) {
                // directors, actors, genres, description, episodes
                JTextArea textArea = new JTextArea();
                textArea.setRows(4);
                textArea.setColumns(20);
                inputFields[i] = textArea;
            } else {
                // title, year, numSeasons
                JTextField textField = new JTextField();
                inputFields[i] = textField;
            }
        }

        // initial values
        ((JTextComponent) inputFields[0]).setText(s.title);
        ((JTextComponent) inputFields[1]).setText(String.join("\n", s.directors));
        ((JTextComponent) inputFields[2]).setText(String.join("\n", s.actors));
        ((JTextComponent) inputFields[3]).setText(String.join("\n", s.genres.stream().map(Enum::name).toArray(String[]::new)));
        ((JTextComponent) inputFields[4]).setText(s.description);
        ((JTextComponent) inputFields[5]).setText(String.valueOf(s.year));
        ((JTextComponent) inputFields[6]).setText(String.valueOf(s.numSeasons));

        // handle episodes
        StringBuilder episodesText = new StringBuilder();
        for (Map.Entry<String, List<Episode>> entry : s.getEpisodes().entrySet()) {
            episodesText.append(entry.getKey()).append(": ");
            for (Episode episode : entry.getValue()) {
                episodesText.append(episode).append(", ");
            }
            episodesText.delete(episodesText.length() - 2, episodesText.length()); // Remove the last comma and space
            episodesText.append("\n");
        }
        ((JTextComponent) inputFields[7]).setText(episodesText.toString());

        // add to jFrame
        for (int i = 0; i < labels.length; i++) {
            if (i == 1 || i == 2 || i == 3 || i == 4 || i == 7) {
                // directors, actors, genres, description, episodes
                newSeries.add(labels[i]);
                newSeries.add(new JScrollPane(inputFields[i]));
            } else {
                // title, year, numSeasons
                newSeries.add(labels[i]);
                newSeries.add(inputFields[i]);
            }
        }

        // title can't be changed when modifying
        if (type == ChangeType.REPLACE)
            inputFields[0].setEnabled(false);

        // adding a warning
        JLabel warning = new JLabel();
        warning.setForeground(Color.RED);
        warning.setVisible(false);

        // save and discard buttons
        JButton saveButton = new JButton("Save");
        JButton discardButton = new JButton("Discard");

        // action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Series series = new Series();
                    series.title = ((JTextComponent) inputFields[0]).getText();
                    series.directors = Arrays.asList(((JTextComponent) inputFields[1]).getText().split("\\s*,\\s*"));
                    series.actors = Arrays.asList(((JTextComponent) inputFields[2]).getText().split("\\s*,\\s*"));

                    // genres parsing
                    List<String> genreStrings = Arrays.asList(((JTextComponent) inputFields[3]).getText().split("\\n"));
                    List<Genre> genres = new ArrayList<>();
                    for (String genreString : genreStrings) {
                        genres.add(Genre.valueOf(genreString.trim()));
                    }
                    series.genres = genres;

                    series.description = ((JTextComponent) inputFields[4]).getText();
                    series.year = Long.parseLong(((JTextComponent) inputFields[5]).getText());
                    series.numSeasons = Long.parseLong(((JTextComponent) inputFields[6]).getText());

                    // episodes parsing
                    Map<String, List<Episode>> episodesMap = new HashMap<>();
                    String[] seasonEpisodes = ((JTextComponent) inputFields[7]).getText().split("\\n");
                    for (String seasonEpisode : seasonEpisodes) {
                        String[] parts = seasonEpisode.split(":");
                        if (parts.length == 2) {
                            String seasonName = parts[0].trim();
                            List<Episode> episodeList = new ArrayList<>();
                            String[] episodeStrings = parts[1].split(", ");
                            for (String episodeString : episodeStrings) {
                                String[] episodeParts = episodeString.split("- ");
                                if (episodeParts.length == 2) {
                                    Episode episode = new Episode();
                                    episode.name = episodeParts[0];
                                    episode.duration = episodeParts[1];
                                    episodeList.add(episode);
                                }
                            }
                            episodesMap.put(seasonName, episodeList);
                        }
                    }
                    series.setEpisodes(episodesMap);

                    if (type == ChangeType.REPLACE)
                        updateProduction((Production) series);
                    else
                        addProductionSystem((Production) series);

                    GUI();
                    newSeries.dispose();
                } catch (NumberFormatException err) {
                    warning.setText("Year or number of seasons invalid");
                    warning.setVisible(true);
                } catch (IllegalArgumentException err) {
                    warning.setText("Genre invalid");
                    warning.setVisible(true);
                }
            }
        });

        discardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
                newSeries.dispose();
            }
        });

        newSeries.add(saveButton);
        newSeries.add(discardButton);
        newSeries.add(warning);

        newSeries.pack();
        newSeries.setLocationRelativeTo(null);
        newSeries.setVisible(true);
    }

    // GUI for modifying actor
    public void modifyActor(Actor a, ChangeType type) {
        JFrame newActor = new JFrame("Actor Editor");
        newActor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newActor.setLayout(new GridLayout(8, 2, 5, 5));

        // JLabels for each field
        JLabel[] labels = {
                new JLabel("Name:"),
                new JLabel("Starred:"),
                new JLabel("Biography:")
        };

        // JTextFields or JTextAreas for each field
        JComponent[] inputFields = new JComponent[labels.length];
        for (int i = 0; i < inputFields.length; i++) {
            if (i == 1 || i == 2) {
                // starred, biography
                JTextArea textArea = new JTextArea();
                textArea.setRows(8);
                textArea.setColumns(30);
                inputFields[i] = textArea;
            } else {
                // name
                JTextField textField = new JTextField();
                inputFields[i] = textField;
            }
        }

        // initial values
        ((JTextComponent) inputFields[0]).setText(a.name);

        if (type == ChangeType.REPLACE)
            inputFields[0].setEnabled(false);

        StringBuilder starredText = new StringBuilder();
        for (Actor.Pair pair : a.starred) {
            starredText.append(pair).append("\n");
        }
        ((JTextComponent) inputFields[1]).setText(starredText.toString());

        ((JTextComponent) inputFields[2]).setText(a.biography);

        // add to JFrame
        for (int i = 0; i < labels.length; i++) {
            if (i == 1 || i == 2) {
                // starred, biography
                newActor.add(labels[i]);
                newActor.add(new JScrollPane(inputFields[i]));
            } else {
                // name
                newActor.add(labels[i]);
                newActor.add(inputFields[i]);
            }
        }

        // adding a warning
        JLabel warning = new JLabel();
        warning.setForeground(Color.RED);
        warning.setVisible(false);

        // save and discard buttons
        JButton saveButton = new JButton("Save");
        JButton discardButton = new JButton("Discard");

        // action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Actor actor = new Actor();
                    actor.name = ((JTextComponent) inputFields[0]).getText();

                    // Handle starred parsing
                    ArrayList<Actor.Pair> starredList = new ArrayList<>();
                    String[] starredLines = ((JTextComponent) inputFields[1]).getText().split("\\n");
                    for (String line : starredLines) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            Actor.Pair pair = new Actor.Pair();
                            pair.name = parts[0].trim();
                            pair.type = ProductionType.valueOf(parts[1].trim());
                            starredList.add(pair);
                        }
                    }
                    actor.starred = starredList;

                    actor.biography = ((JTextComponent) inputFields[2]).getText();

                    if (type == ChangeType.REPLACE)
                        updateActor(actor);
                    else
                        addActorSystem(actor);

                    GUI();
                    newActor.dispose();
                } catch (NumberFormatException err) {
                    warning.setText("Year invalid");
                    warning.setVisible(true);
                } catch (IllegalArgumentException err) {
                    warning.setText("Production type invalid");
                    warning.setVisible(true);
                }
            }
        });

        discardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
                newActor.dispose();
            }
        });

        newActor.add(saveButton);
        newActor.add(discardButton);
        newActor.add(warning);

        newActor.setSize(600, 500);
        newActor.setLocationRelativeTo(null);
        newActor.setVisible(true);
    }

    // GUI for modifying movie
    public void modifyMovie(Movie m, ChangeType type) {
        JFrame newMovie = new JFrame("Movie Editor");
        newMovie.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newMovie.setLayout(new GridLayout(10, 2, 5, 5));

        // JLabels for each field
        JLabel[] labels = {
                new JLabel("Title:"),
                new JLabel("Directors:"),
                new JLabel("Actors:"),
                new JLabel("Genres:"),
                new JLabel("Description:"),
                new JLabel("Year:"),
                new JLabel("Duration:")
        };

        // JTextFields or JTextAreas for each field
        JComponent[] inputFields = new JComponent[labels.length];
        for (int i = 0; i < inputFields.length; i++) {
            if (i == 1 || i == 2 || i == 3 || i == 4) {
                // directors, actors, genres, description
                JTextArea textArea = new JTextArea();
                textArea.setColumns(40);
                if (i == 4) {
                    // description
                    textArea.setRows(12);
                } else {
                    textArea.setRows(6);
                }
                inputFields[i] = textArea;
            } else {
                // title, year, duration
                JTextField textField = new JTextField();
                inputFields[i] = textField;
            }
        }

        // initial values
        ((JTextComponent) inputFields[0]).setText(m.title);
        ((JTextComponent) inputFields[1]).setText(String.join("\n", m.directors));
        ((JTextComponent) inputFields[2]).setText(String.join("\n", m.actors));
        ((JTextComponent) inputFields[3]).setText(String.join("\n", m.genres.stream().map(Enum::name).toArray(String[]::new)));
        ((JTextComponent) inputFields[4]).setText(m.description);
        ((JTextComponent) inputFields[5]).setText(String.valueOf(m.year));
        ((JTextComponent) inputFields[6]).setText(m.duration);

        // add JLabels and JTextFields to the JFrame
        for (int i = 0; i < labels.length; i++) {
            if (i == 1 || i == 2 || i == 3 || i == 4) {
                // directors, actors, genres, description
                newMovie.add(labels[i]);
                JScrollPane scrollPane = new JScrollPane(inputFields[i]);
                newMovie.add(scrollPane);
            } else {
                // title, year, duration
                newMovie.add(labels[i]);
                newMovie.add(inputFields[i]);
            }
        }

        if (type == ChangeType.REPLACE)
            inputFields[0].setEnabled(false);

        // adding a warning
        JLabel warning = new JLabel();
        warning.setForeground(Color.RED);
        warning.setVisible(false);

        // save and discard buttons
        JButton saveButton = new JButton("Save");
        JButton discardButton = new JButton("Discard");

        // action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Movie movie = new Movie();
                    movie.title = ((JTextComponent) inputFields[0]).getText();
                    movie.directors = Arrays.asList(((JTextComponent) inputFields[1]).getText().split("\\s*,\\s*"));
                    movie.actors = Arrays.asList(((JTextComponent) inputFields[2]).getText().split("\\s*,\\s*"));

                    // genres parsing
                    List<String> genreStrings = Arrays.asList(((JTextComponent) inputFields[3]).getText().split("\\n"));
                    List<Genre> genres = new ArrayList<>();
                    for (String genreString : genreStrings) {
                        genres.add(Genre.valueOf(genreString.trim()));
                    }
                    movie.genres = genres;

                    movie.description = ((JTextComponent) inputFields[4]).getText();

                    movie.year = Long.parseLong(((JTextComponent) inputFields[5]).getText());

                    movie.duration = ((JTextComponent) inputFields[6]).getText();

                    if (type == ChangeType.REPLACE)
                        updateProduction((Production) movie);
                    else
                        addProductionSystem((Production) movie);

                    GUI();
                    newMovie.dispose();
                } catch (NumberFormatException err) {
                    warning.setText("Year invalid");
                    warning.setVisible(true);
                } catch (IllegalArgumentException err) {
                    warning.setText("Genre invalid");
                    warning.setVisible(true);
                }
            }
        });

        discardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
                newMovie.dispose();
            }
        });

        newMovie.add(saveButton);
        newMovie.add(discardButton);
        newMovie.add(warning);

        newMovie.setSize(600, 500);
        newMovie.setLocationRelativeTo(null);
        newMovie.setVisible(true);
    }

    // GUI for requests addressed to the user
    public void RequestGUI(Request req) {
        JFrame requestFrame = new JFrame("Request");
        requestFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        requestFrame.setMinimumSize(new Dimension(400, 400));
        requestFrame.setLayout(new BoxLayout(requestFrame.getContentPane(), BoxLayout.PAGE_AXIS));

        // request info and solved button
        JTextArea info = new JTextArea(req.infoToString());
        JButton solve = new JButton("Solved");
        solve.setBackground(Color.GREEN);

        // dismissed button
        JButton dismiss = new JButton("Dismiss");
        dismiss.setBackground(Color.RED);
        dismiss.setForeground(Color.WHITE);

        // return
        JButton returnIMDB = new JButton("Return to IMDB");

        requestFrame.add(new JScrollPane(info));
        requestFrame.add(solve);
        requestFrame.add(dismiss);
        requestFrame.add(returnIMDB);

        // action listeners
        solve.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resolveRequest(req, true);
                if(frontPage.isDisplayable()) {
                    frontPage.dispose();
                    GUI();
                }
                requestFrame.dispose();
            }
        });

        dismiss.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resolveRequest(req, false);
                if(frontPage.isDisplayable()) {
                    frontPage.dispose();
                    GUI();
                }
                requestFrame.dispose();
            }
        });

        requestFrame.pack();
        requestFrame.setLocationRelativeTo(null);
        requestFrame.setVisible(true);
    }

    // GUI for adding productions and actors
    public void addItems() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // adding new items
        workingSpace.add(new JLabel("Add new item"));
        JPanel addNew = new JPanel(new GridLayout(1, 3));

        JButton addMovie = new JButton("Add movie");
        addNew.add(addMovie);

        JButton addSeries = new JButton("Add series");
        addNew.add(addSeries);

        JButton addActor = new JButton("Add actor");
        addNew.add(addActor);

        workingSpace.add(addNew);

        // button listeners
        addMovie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyMovie(new Movie(), ChangeType.ADD);
                frontPage.dispose();
            }
        });

        addActor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyActor(new Actor(), ChangeType.ADD);
                frontPage.dispose();
            }
        });

        addSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifySeries(new Series(), ChangeType.ADD);
                frontPage.dispose();
            }
        });
    }

    // GUI with all requests addressed to the user
    public void solveRequests() {
        workingSpace.removeAll();;
        workingSpace.revalidate();
        workingSpace.repaint();

        // view your requests
        ArrayList<Request> requestTrueList = new ArrayList<>(personalReq);

        if (type == AccountType.ADMIN) {
            // appending the admin requests
            requestTrueList.addAll(IMDB.RequestHolder.requests);
        }

        JList<Request> requestList = new JList<>(requestTrueList.toArray(new Request[requestTrueList.size()]));
        requestList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        workingSpace.add(new JLabel("Requests:"));
        workingSpace.add(new JScrollPane(requestList));

        // open specific request
        requestList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    Request selectedRequest = (Request) requestList.getSelectedValue();
                    RequestGUI(selectedRequest);
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
    }
    public void GUI() {
        super.GUI();

        // buttons for adding new movies
        JButton addItemButton = new JButton("Add item");
        leftMenu.add(addItemButton);

        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addItems();
            }
        });


        // view user requests
        JButton requestButton = new JButton("Your requests");
        leftMenu.add(requestButton);

        requestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveRequests();
            }
        });

    }

    // GUI with actor information
    public void ActorGUI(Actor actor){
        super.ActorGUI(actor);

        // modify actor
        JButton modify = new JButton("Modify");
        actorFrame.add(modify);

        // remove an actor
        JButton remove = new JButton("Remove");
        remove.setForeground(Color.WHITE);
        remove.setBackground(Color.RED);

        actorFrame.add(remove);

        // users can only remove their contributions
        if(!addedActor.contains(actor)) {
            modify.setEnabled(false);
            remove.setEnabled(false);
        }

        // admins can also remove shared contributions
        if(type == AccountType.ADMIN) {
            if(Admin.sharedActor.contains(actor)) {
                modify.setEnabled(true);
                remove.setEnabled(true);
            }
        }

        modify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyActor(actor, ChangeType.REPLACE);
                actorFrame.dispose();
            }
        });

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeActorSystem(actor.name);
                GUI();
                actorFrame.dispose();
            }
        });
    }

    // GUI with production information
    public void ProductionGUI(Production prod) {
        super.ProductionGUI(prod);

        // modify production
        JButton modify = new JButton("Modify");

        prodFrame.add(modify);

        // remove a production
        JButton remove = new JButton("Remove");
        remove.setForeground(Color.WHITE);
        remove.setBackground(Color.RED);

        prodFrame.add(remove);

        // users can only remove their contributions
        if(!addedProductions.contains(prod)) {
            modify.setEnabled(false);
            remove.setEnabled(false);
        }

        // admins can also remove shared contributions
        if(type == AccountType.ADMIN) {
            if(Admin.sharedProductions.contains(prod)) {
                modify.setEnabled(true);
                remove.setEnabled(true);
            }
        }

        modify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(prod.type == ProductionType.Movie)
                    modifyMovie((Movie) prod, ChangeType.REPLACE);
                else
                    modifySeries((Series) prod, ChangeType.REPLACE);
                prodFrame.dispose();
            }
        });

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeProductionSystem(prod.title);
                GUI();
                prodFrame.dispose();
            }
        });
    }

    // CLI with actor information
    public void ActorCLI(Actor selectedActor) {
        Scanner scanner = new Scanner(System.in);
        int choice;
        System.out.println("Actor Information:");
        selectedActor.displayInfo();

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Add to favourites");
            System.out.println("2. Remove from favourites");
            System.out.println("3. Remove from system");
            System.out.println("4. Modify");
            System.out.println("5. Back to actors menu");

            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            switch (choice) {
                case 1:
                    addFavouriteActor(selectedActor);
                    break;
                case 2:
                    removeFavouriteActor(selectedActor);
                    break;
                case 3:
                    if(addedActor.contains(selectedActor) || (type == AccountType.ADMIN &&
                            Admin.sharedActor.contains(selectedActor))) {
                        System.out.print("Are you sure you want to remove " + selectedActor.name + "? (yes/no): ");
                        String submitChoice = scanner.nextLine().toLowerCase();
                        if (submitChoice.equals("yes")) {
                            removeActorSystem(selectedActor.name);
                            return;
                        }
                    } else {
                        System.out.println("You can't remove this actor");
                    }
                    break;
                case 4:
                    if(addedActor.contains(selectedActor) || (type == AccountType.ADMIN &&
                            Admin.sharedActor.contains(selectedActor))) {
                        modifyActorCLI(selectedActor, ChangeType.REPLACE);
                        System.out.println("Actor Information:");
                        selectedActor.displayInfo();
                    } else {
                        System.out.println("You can't modify this actor");
                    }
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // CLI with production information
    public void ProductionCLI(Production selectedProd) {
        Scanner scanner = new Scanner(System.in);
        int choice;
        System.out.println("Production Information:");
        selectedProd.displayInfo();

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Add to favourites");
            System.out.println("2. Remove from favourites");
            System.out.println("3. Remove from system");
            System.out.println("4. Modify");
            System.out.println("5. Back to productions menu");

            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    addFavouriteProduction(selectedProd);
                    break;
                case 2:
                    removeFavouriteProduction(selectedProd);
                    break;
                case 3:
                    if(addedProductions.contains(selectedProd) || (type == AccountType.ADMIN &&
                            Admin.sharedProductions.contains(selectedProd))) {
                        System.out.print("Are you sure you want to remove " + selectedProd.title + "? (yes/no): ");
                        String submitChoice = scanner.nextLine().toLowerCase();
                        if (submitChoice.equals("yes")) {
                            removeProductionSystem(selectedProd.title);
                            return;
                        }
                    } else {
                        System.out.println("You can't remove this production");
                    }
                    break;
                case 4:
                    if(addedProductions.contains(selectedProd) || (type == AccountType.ADMIN &&
                            Admin.sharedProductions.contains(selectedProd))) {
                        if (selectedProd.type == ProductionType.Movie) {
                            modifyMovieCLI((Movie) selectedProd, ChangeType.REPLACE);
                        } else {
                            modifySeriesCLI((Series) selectedProd, ChangeType.REPLACE);
                        }
                        System.out.println("Production Information:");
                        selectedProd.displayInfo();
                    } else {
                        System.out.println("You can't modify this production");
                    }
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // CLI for modifying series
    public void modifySeriesCLI(Series s, ChangeType type) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Series Editor");

        // display series information for REPLACE type
        if (type == ChangeType.REPLACE) {
            System.out.println("Current Series Information:");
            System.out.println("Title: " + s.title);
            System.out.println("Directors: " + s.directors);
            System.out.println("Actors: " + s.actors);
            System.out.println("Genres: " + s.genres);
            System.out.println("Description: " + s.description);
            System.out.println("Year: " + s.year);
            System.out.println("Number of Seasons: " + s.numSeasons);
            System.out.println("Episodes: " + s.getEpisodes());
        }

        // title
        String title = s.title;
        if(type == ChangeType.ADD) {
            System.out.print("Title: ");
            title = scanner.nextLine();
        }

        // directors
        System.out.print("Directors (comma-separated)" + (type == ChangeType.REPLACE ? " (press Enter to keep curren " +
                " value): " : ": "));
        List<String> directors = Arrays.asList(scanner.nextLine().split("\\s*,\\s*"));
        if (directors.isEmpty()) {
            directors = s.directors;
        }

        // actors
        System.out.print("Actors (comma-separated)" + (type == ChangeType.REPLACE ? " (press Enter to keep current " +
                "value): " : ": "));
        List<String> actors = Arrays.asList(scanner.nextLine().split("\\s*,\\s*"));
        if (actors.isEmpty()) {
            actors = s.actors;
        }

        // genres
        List<Genre> genres = new ArrayList<>();
        boolean validGenres = false;
        while (!validGenres) {
            try {
                System.out.print("Genres (comma-separated): ");
                String genresInput = scanner.nextLine();
                if (genresInput.isEmpty()) {
                    genres = s.genres;
                    validGenres = true;
                } else {
                    List<String> genresParsed = Arrays.asList(genresInput.split("\\s*,\\s*"));
                    genresParsed.replaceAll(String::trim);
                    genres = genresParsed.stream()
                            .map(genre -> Genre.valueOf(genre))
                            .collect(Collectors.toList());
                    validGenres = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid genre. Please enter valid genres.");
            }
        }

        // description
        System.out.print("Description" + (type == ChangeType.REPLACE ? " (press Enter to keep current value): " : ": "));
        String description = scanner.nextLine();
        if (description.isEmpty()) {
            description = s.description;
        }

        // year
        System.out.print("Year" + (type == ChangeType.REPLACE ? " (press Enter to keep current value): " : ": "));
        String yearInput = scanner.nextLine();
        long year = yearInput.isEmpty() ? s.year : Long.parseLong(yearInput);

        // number of seasons
        System.out.print("Number of Seasons" + (type == ChangeType.REPLACE ? " (press Enter to keep current value): " +
                "" : ": "));
        String numSeasonsInput = scanner.nextLine();
        long numSeasons = numSeasonsInput.isEmpty() ? s.numSeasons : Long.parseLong(numSeasonsInput);

        // episodes
        Map<String, List<Episode>> episodesMap = new HashMap<>();
        boolean validEpisodes;
        for (int i = 0; i < numSeasons; i++) {
            validEpisodes = false;
            while (!validEpisodes) {
                try {
                    System.out.print("Episodes for Season " + (i + 1) + " (episodeName1-episodeDuration1, " +
                            "episodeName2-episodeDuration2, ...): ");
                    String episodesInput = scanner.nextLine();
                    if (episodesInput.isEmpty()) {
                        episodesMap = s.getEpisodes();
                        validEpisodes = true;
                    } else {
                        String[] episodeStrings = episodesInput.split(",\\s*");
                        List<Episode> episodeList = new ArrayList<>();

                        for (String episodeString : episodeStrings) {
                            String[] episodeParts = episodeString.split("-\\s*");
                            if (episodeParts.length == 2) {
                                Episode episode = new Episode();
                                episode.name = episodeParts[0].trim();
                                episode.duration = episodeParts[1].trim();
                                episodeList.add(episode);
                            } else {
                                System.out.println("Error: Invalid format for episodes. " +
                                        "Please enter episodes in the format 'episodeName1-episodeDuration1, " +
                                        "episodeName2-episodeDuration2, ...'.");
                                break;
                            }
                        }

                        episodesMap.put("Season " + (i + 1), episodeList);
                        validEpisodes = true;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Error: Invalid input. Please enter episodes in the correct format.");
                }
            }
        }

        s.title = title;
        s.directors = directors;
        s.actors = actors;
        s.genres = genres;
        s.description = description;
        s.year = year;
        s.numSeasons = numSeasons;
        s.setEpisodes(episodesMap);

        if (type == ChangeType.REPLACE)
            updateProduction((Production) s);
        else
            addProductionSystem((Production) s);
    }

    // CLI for modifying actor
    public void modifyActorCLI(Actor a, ChangeType type) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Actor Editor");

        // display actor information for REPLACE type
        if (type == ChangeType.REPLACE) {
            System.out.println("Current Actor Information:");
            System.out.println("Name: " + a.name);
            System.out.println("Starred: " + a.starred);
            System.out.println("Biography: " + a.biography);
        }

        // name
        String name = a.name;
        if (type == ChangeType.ADD) {
            System.out.print("Name:");
            name = scanner.nextLine();
        }

        // starred
        ArrayList<Actor.Pair> starredList = new ArrayList<>();
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Starred (name:Type, name:Type, ...): ");
            String starredInput = scanner.nextLine();
            if (starredInput.isEmpty()) {
                starredList = a.starred;
                validInput = true;
            } else {
                String[] starredPairs = starredInput.split(",");
                starredList.clear();

                for (String pair : starredPairs) {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        Actor.Pair starredPair = new Actor.Pair();
                        starredPair.name = parts[0].trim();
                        try {
                            starredPair.type = ProductionType.valueOf(parts[1].trim());
                            starredList.add(starredPair);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: Invalid production type. Please enter a valid production type.");
                            break;
                        }
                    } else {
                        System.out.println("Error: Invalid format. Please enter pairs in the format 'name:Type'.");
                        break;
                    }
                }

                if (starredList.size() == starredPairs.length) {
                    validInput = true;
                }
            }
        }

        // biography
        System.out.print("Biography" + (type == ChangeType.REPLACE ? " (press Enter to keep current value): " : ": "));
        String biography = scanner.nextLine();
        if (biography.isEmpty()) {
            biography = a.biography;
        }

        a.name = name;
        a.starred = starredList;
        a.biography = biography;

        if (type == ChangeType.REPLACE)
            updateActor(a);
        else
            addActorSystem(a);
    }

    // CLI for modifying movie
    public void modifyMovieCLI(Movie m, ChangeType type) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Movie Editor");

        // display existing values in REPLACE mode
        if (type == ChangeType.REPLACE) {
            System.out.println("Existing values:");
            m.displayInfo();
            System.out.println();
        }

        // title
        String titleInput = m.title;
        if (type == ChangeType.ADD) {
            System.out.print("Title: ");
            titleInput = scanner.nextLine();
        }

        // directors
        System.out.print("Directors (comma-separated): ");
        List<String> directorsInput = Arrays.asList(scanner.nextLine().split("\\s*,\\s*"));
        List<String> directors = directorsInput.isEmpty() ? m.directors : directorsInput;

        // actors
        System.out.print("Actors (comma-separated): ");
        List<String> actorsInput = Arrays.asList(scanner.nextLine().split("\\s*,\\s*"));
        List<String> actors = actorsInput.isEmpty() ? m.actors : actorsInput;

        // valid genres
        List<Genre> genres = new ArrayList<>();
        boolean validGenres = false;
        while (!validGenres) {
            try {
                System.out.print("Genres (comma-separated): ");
                String genresInput = scanner.nextLine();
                if (genresInput.isEmpty()) {
                    genres = m.genres;
                    validGenres = true;
                } else {
                    List<String> genresParsed = Arrays.asList(genresInput.split("\\s*,\\s*"));
                    genresParsed.replaceAll(String::trim);
                    genres = genresParsed.stream()
                            .map(genre -> Genre.valueOf(genre))
                            .collect(Collectors.toList());
                    validGenres = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid genre. Please enter valid genres.");
            }
        }

        // description
        System.out.print("Description: ");
        String descriptionInput = scanner.nextLine();
        String description = descriptionInput.isEmpty() ? m.description : descriptionInput;

        // valid year
        long year = 0;
        boolean validYear = false;
        while (!validYear) {
            try {
                System.out.print("Year: ");
                String yearInput = scanner.nextLine();
                if (yearInput.isEmpty()) {
                    year = m.year;
                    validYear = true;
                } else {
                    year = Long.parseLong(yearInput);
                    validYear = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid year. Please enter a valid number.");
            }
        }

        // duration
        System.out.print("Duration: ");
        String durationInput = scanner.nextLine();
        String duration = durationInput.isEmpty() ? m.duration : durationInput;

        m.title = titleInput;
        m.directors = directors;
        m.actors = actors;
        m.genres = genres;
        m.description = description;
        m.year = year;
        m.duration = duration;

        if (type == ChangeType.REPLACE)
            updateProduction((Production) m);
        else
            addProductionSystem((Production) m);

        System.out.println("Changes saved successfully.");
    }

    // CLI for adding actors and productions
    public void manageContributions() {
        Scanner scanner = new Scanner(System.in);

        int choice;
        do {
            System.out.println("Add new item:");
            System.out.println("1) Add movie");
            System.out.println("2) Add series");
            System.out.println("3) Add actor");
            System.out.println("0) Go back");

            System.out.print("Enter the number of your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Consume the invalid input
            }

            choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    modifyMovieCLI(new Movie(), ChangeType.ADD);
                    break;
                case 2:
                    modifySeriesCLI(new Series(), ChangeType.ADD);
                    break;
                case 3:
                    modifyActorCLI(new Actor(), ChangeType.ADD);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (true);
    }

    // CLI fr solving requests
    public void requestCLI(Request req) {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (true) {
            System.out.println("Request Info:\n");
            System.out.println(req.infoToString());

            System.out.println("\nOptions:");
            System.out.println("1) Mark as Solved");
            System.out.println("2) Dismiss");
            System.out.println("3) Return to IMDB");
            System.out.print("Enter your choice (1-3): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                if (choice >= 1 && choice <= 3) {
                    break; // Valid choice, exit the loop
                } else {
                    System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume the invalid input
            }
        }

        switch (choice) {
            case 1:
                resolveRequest(req, true);
                System.out.println("Request marked as solved.");
                break;
            case 2:
                resolveRequest(req, false);
                System.out.println("Request dismissed.");
                break;
            case 3:
                return;
        }
    }

    // CLI for displaying requests addressed to the user
    public void solveRequestCLI() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Requests:");

        ArrayList<Request> requestTrueList = new ArrayList<>(personalReq);

        if (type == AccountType.ADMIN) {
            // Appending the admin requests
            requestTrueList.addAll(IMDB.RequestHolder.requests);
        }

        while (true) {
            for (int i = 0; i < requestTrueList.size(); i++) {
                System.out.println((i + 1) + ") " + requestTrueList.get(i));
            }

            System.out.print("Enter the number of the request you want to solve (0 to go back): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (choice > 0 && choice <= requestTrueList.size()) {
                Request selectedRequest = requestTrueList.get(choice - 1);
                requestCLI(selectedRequest);
                break; // Exit the loop if the user makes a valid choice
            } else if (choice == 0) {
                // Go back
                break; // Exit the loop if the user chooses to go back
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
