import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class Regular extends User implements RequestsManager{
    Map<Production, Rating> yourReviewsProduction;
    Map<Actor, Rating> yourReviewsActor;
    List<Production> pastReviewedProductions;
    List<Actor> pastReviewedActors;
    public Regular() {
        super();
        type = AccountType.REGULAR;
        yourReviewsProduction = new HashMap<>();
        experience = 0;
        pastReviewedProductions = new ArrayList<>();
        yourReviewsActor = new HashMap<>();
        pastReviewedActors = new ArrayList<>();
    }

    // the request is always submitted for regular
    @Override
    public boolean handleReq(Request r) {
        createRequest(r);
        r.notifyReceived();
        return true;
    }

    @Override
    public void createRequest(Request r) {
        if(r.fixer.equals("ADMIN")) {
            IMDB.RequestHolder.addRequest(r);
            createdRequests.add(r);
        } else {
            for(User u : imdb.users) {
                if(u.type != AccountType.REGULAR) {
                    // searching if the user added that production
                    if(r.getRequestType() == RequestTypes.MOVIE_ISSUE) {
                        for(Production p : ((Staff) u).addedProductions) {
                            if(p.title.equals(r.name)) {
                                r.fixer = u.username;
                                ((Staff) u).personalReq.add(r);
                                createdRequests.add(r);
                            }
                        }
                    }

                    // searching if the user added that actor
                    if(r.getRequestType() == RequestTypes.ACTOR_ISSUE) {
                        for(Actor a : ((Staff) u).addedActor) {
                            if(a.name.equals(r.name)) {
                                r.fixer = u.username;
                                ((Staff) u).personalReq.add(r);
                                createdRequests.add(r);
                            }
                        }
                    }
                }
            }

            // searching if it is in the admin's shared lists
            for(Production prod: Admin.sharedProductions) {
                if(prod.title.equals(r.name)) {
                    r.fixer = "ADMIN";
                    IMDB.RequestHolder.addRequest(r);
                    createdRequests.add(r);
                }
            }

            for(Actor act: Admin.sharedActor) {
                if(act.name.equals(r.name)) {
                    r.fixer = "ADMIN";
                    IMDB.RequestHolder.addRequest(r);
                    createdRequests.add(r);
                }
            }
        }
    }

    @Override
    public void removeRequest(Request r) {
        if(r.fixer.equals("ADMIN")) {
            IMDB.RequestHolder.removeRequest(r);
        } else {
            IMDB imdb = IMDB.getInstance();
            for(User u : imdb.users) {
                if(u.username.equals(r.fixer)) {
                    ((Contributor) u).personalReq.remove(r);
                }
            }
        }
        createdRequests.remove(r);
    }

    public void addReviewActor(Rating review, Actor actor) {
        // giving user experience
        if(!pastReviewedActors.contains(actor)) {
            // adding experience to the user
            ReviewExperience strategy = new ReviewExperience();
            addExperience(strategy.calculateExperience());
        }

        // adding review
        actor.reviews.add(review);
        yourReviewsActor.put(actor, review);

        // sorting list
        Collections.sort(actor.reviews);

        // subscribing user
        actor.notification(this);
        actor.Subscribe(this);
        pastReviewedActors.remove(actor);
    }

    public void removeReviewActor(Rating review, Actor actor) {
        // removing review
        yourReviewsActor.remove(actor);
        actor.reviews.remove(review);

        // unsubscribing
        actor.Unsubscribe(this);
        pastReviewedActors.add(actor);
    }
    public void addReviewProduction(Rating review, Production production) {
        // giving user experience
        if(!pastReviewedProductions.contains(production)) {
            // adding experience to the user
            ReviewExperience strategy = new ReviewExperience();
            addExperience(strategy.calculateExperience());
        }

        // adding review
        production.reviews.add(review);
        yourReviewsProduction.put(production, review);

        // sorting list
        Collections.sort(production.reviews);

        // modifing the rating
        production.calculateRating();

        // subscribing user
        production.notification(this);
        production.Subscribe(this);
        pastReviewedProductions.remove(production);
    }

    public void removeReviewProduction(Rating review, Production production) {
        // removing review
        yourReviewsProduction.remove(production);
        production.reviews.remove(review);

        // modifying the rating
        production.calculateRating();

        // unsubscribing
        production.Unsubscribe(this);
        pastReviewedProductions.add(production);
    }

    // GUI for production info and actions
    public void ProductionGUI(Production prod) {
        super.ProductionGUI(prod);

        JPanel rPanel = new JPanel(new GridLayout(3, 2));

        // add score
        rPanel.add(new JLabel("Score:"));

        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        JComboBox<Integer> scoreBox = new JComboBox<>(numbers);
        rPanel.add(scoreBox);

        // add comment
        rPanel.add(new JLabel("Comment:"));

        JTextField commentBox = new JTextField();
        rPanel.add(commentBox);

        // remove button
        JButton remove = new JButton("Remove your review");
        remove.setForeground(Color.WHITE);
        remove.setBackground(Color.RED);
        rPanel.add(remove);

        // save button
        JButton save = new JButton("Save your review");
        save.setBackground(Color.GREEN);
        rPanel.add(save);

        prodFrame.add(rPanel);

        save.setEnabled(false);
        // completing the fields if there is a review
        if(yourReviewsProduction.containsKey(prod)) {
            Rating currentReview = yourReviewsProduction.get(prod);
            scoreBox.setSelectedItem(currentReview.rating);
            commentBox.setText(currentReview.comment);
        } else {
            remove.setEnabled(false);
        }

        // action listeners
        ActionListener enableSave = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save.setEnabled(scoreBox.getSelectedItem() != null && !commentBox.getText().isEmpty());
            }
        };

        scoreBox.addActionListener(enableSave);
        commentBox.addActionListener(enableSave);

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rating newReview = new Rating();
                newReview.rating = (Integer) scoreBox.getSelectedItem();
                newReview.comment = commentBox.getText();
                newReview.user = username;

                // removing the old rating
                if(yourReviewsProduction.containsKey(prod)) {
                    Rating currentReview = yourReviewsProduction.get(prod);
                    removeReviewProduction(currentReview, prod);
                }

                addReviewProduction(newReview, prod);

                // refreshing the page contents
                prodFrame.dispose();
                ProductionGUI(prod);
            }
        });

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rating currentReview = yourReviewsProduction.get(prod);
                yourReviewsProduction.remove(prod);
                prod.reviews.remove(currentReview);

                // refreshing the page contents
                prodFrame.dispose();
                ProductionGUI(prod);
            }
        });
    }

    // GUI for actor info and actions
    public void ActorGUI(Actor actor) {
        super.ActorGUI(actor);
        JPanel rPanel = new JPanel(new GridLayout(3, 2));

        // add score
        rPanel.add(new JLabel("Score:"));

        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        JComboBox<Integer> scoreBox = new JComboBox<>(numbers);
        rPanel.add(scoreBox);

        // add comment
        rPanel.add(new JLabel("Comment:"));

        JTextField commentBox = new JTextField();
        rPanel.add(commentBox);

        // remove button
        JButton remove = new JButton("Remove your review");
        remove.setForeground(Color.WHITE);
        remove.setBackground(Color.RED);
        rPanel.add(remove);

        // save button
        JButton save = new JButton("Save your review");
        save.setBackground(Color.GREEN);
        rPanel.add(save);

        actorFrame.add(rPanel);

        save.setEnabled(false);
        // completing the fields if there is a review
        if(yourReviewsActor.containsKey(actor)) {
            Rating currentReview = yourReviewsActor.get(actor);
            scoreBox.setSelectedItem(currentReview.rating);
            commentBox.setText(currentReview.comment);
        } else {
            remove.setEnabled(false);
        }

        // action listeners
        ActionListener enableSave = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save.setEnabled(scoreBox.getSelectedItem() != null && !commentBox.getText().isEmpty());
            }
        };

        scoreBox.addActionListener(enableSave);
        commentBox.addActionListener(enableSave);

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rating newReview = new Rating();
                newReview.rating = (Integer) scoreBox.getSelectedItem();
                newReview.comment = commentBox.getText();
                newReview.user = username;

                // removing the old rating
                if(yourReviewsActor.containsKey(actor)) {
                    Rating currentReview = yourReviewsActor.get(actor);
                    removeReviewActor(currentReview, actor);
                }

                addReviewActor(newReview, actor);

                // refreshing the page contents
                actorFrame.dispose();
                ActorGUI(actor);
            }
        });

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rating currentReview = yourReviewsActor.get(actor);
                yourReviewsActor.remove(actor);
                actor.reviews.remove(currentReview);

                // refreshing the page contents
                actorFrame.dispose();
                ActorGUI(actor);
            }
        });
    }

    // GUI for managing a request the user created
    public void ownRequestGUI(Request r) {
        JFrame requestFrame = new JFrame("Request");
        requestFrame.setDefaultCloseOperation(requestFrame.EXIT_ON_CLOSE);
        requestFrame.setMinimumSize(new Dimension(400, 400));
        requestFrame.setLayout(new BoxLayout(requestFrame.getContentPane(), BoxLayout.PAGE_AXIS));

        // request info an option buttons
        JTextArea info = new JTextArea(r.infoToString());
        JButton remove = new JButton("Remove request");
        JButton returnIMDB = new JButton("Return to IMDB");

        requestFrame.add(new JScrollPane(info));
        requestFrame.add(remove);
        requestFrame.add(returnIMDB);

        // action listeners
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeRequest(r);
                r.notifyRemove();
                GUI();
                requestFrame.dispose();
            }
        });

        returnIMDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
                requestFrame.dispose();
            }
        });

        requestFrame.pack();
        requestFrame.setLocationRelativeTo(null);
        requestFrame.setVisible(true);
    }

    public void GUI() {
        super.GUI();
        super.RequestRegularGUI(this);
    }

    // CLI for managing a request the user created
    public void ownRequestCLI(Request r) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Request Information:\n" + r.infoToString());

            System.out.println("\nOptions:");
            System.out.println("1) Remove Request");
            System.out.println("2) Return to IMDB");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    removeRequest(r);
                    r.notifyRemove();
                    System.out.println("Request removed.");
                    return;
                case 2:
                    System.out.println("Returning to IMDB...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // CLI for managing a review
    public void reviewMenuCLI(Production prod) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Review Menu for " + prod.title + ":");

        if (yourReviewsProduction.containsKey(prod)) {
            System.out.println("1. View Your Review");
            System.out.println("2. Modify Your Review");
            System.out.println("3. Remove Your Review");
        } else {
            System.out.println("1. Add a Review");
        }

        System.out.println("4. Back to Production Menu");

        System.out.print("Enter your choice: ");
        int reviewChoice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        switch (reviewChoice) {
            case 1:
                if (yourReviewsProduction.containsKey(prod)) {
                    System.out.println("Your Review:");
                    System.out.println(yourReviewsProduction.get(prod));
                } else {
                    System.out.print("Enter your review score (1-10): ");
                    int score = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character

                    System.out.print("Enter your review comment: ");
                    String comment = scanner.nextLine();

                    Rating newReview = new Rating();
                    newReview.rating = score;
                    newReview.comment = comment;
                    newReview.user = username;

                    addReviewProduction(newReview, prod);
                    System.out.println("Review added successfully.");
                }
                break;
            case 2:
                if (yourReviewsProduction.containsKey(prod)) {
                    System.out.print("Enter your modified review score (1-10): ");
                    int score = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character

                    System.out.print("Enter your modified review comment: ");
                    String comment = scanner.nextLine();

                    removeReviewProduction(yourReviewsProduction.get(prod), prod);

                    Rating modifiedReview = new Rating();
                    modifiedReview.rating = score;
                    modifiedReview.comment = comment;

                    addReviewProduction(modifiedReview, prod);

                    System.out.println("Review modified successfully.");
                } else {
                    System.out.println("You haven't reviewed this production yet.");
                }
                break;
            case 3:
                if (yourReviewsProduction.containsKey(prod)) {
                    Rating currentReview = yourReviewsProduction.get(prod);
                    removeReviewProduction(currentReview, prod);
                    System.out.println("Your review has been deleted successfully.");
                } else {
                    System.out.println("You haven't reviewed this production yet.");
                }
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    public void reviewMenuCLI(Actor actor) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Review Menu for " + actor.name + ":");

        if (yourReviewsActor.containsKey(actor)) {
            System.out.println("1. View Your Review");
            System.out.println("2. Modify Your Review");
            System.out.println("3. Remove Your Review");
        } else {
            System.out.println("1. Add a Review");
        }

        System.out.println("4. Back to Actor Menu");

        System.out.print("Enter your choice: ");
        int reviewChoice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        switch (reviewChoice) {
            case 1:
                if (yourReviewsActor.containsKey(actor)) {
                    System.out.println("Your Review:");
                    System.out.println(yourReviewsActor.get(actor));
                } else {
                    System.out.print("Enter your review score (1-10): ");
                    int score = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character

                    System.out.print("Enter your review comment: ");
                    String comment = scanner.nextLine();

                    Rating newReview = new Rating();
                    newReview.rating = score;
                    newReview.comment = comment;
                    newReview.user = username;

                    addReviewActor(newReview, actor);
                    System.out.println("Review added successfully.");
                }
                break;
            case 2:
                if (yourReviewsActor.containsKey(actor)) {
                    System.out.print("Enter your modified review score (1-10): ");
                    int score = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character

                    System.out.print("Enter your modified review comment: ");
                    String comment = scanner.nextLine();

                    removeReviewActor(yourReviewsActor.get(actor), actor);

                    Rating modifiedReview = new Rating();
                    modifiedReview.rating = score;
                    modifiedReview.comment = comment;

                    addReviewActor(modifiedReview, actor);

                    System.out.println("Review modified successfully.");
                } else {
                    System.out.println("You haven't reviewed this actor yet.");
                }
                break;
            case 3:
                if (yourReviewsActor.containsKey(actor)) {
                    Rating currentReview = yourReviewsActor.get(actor);
                    removeReviewActor(currentReview, actor);
                    System.out.println("Your review has been deleted successfully.");
                } else {
                    System.out.println("You haven't reviewed this actor yet.");
                }
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // CLI for displaying production info and options
    public void ProductionCLI(Production selectedProd) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("Production Information:");
            selectedProd.displayInfo();

            System.out.println("\nOptions:");
            System.out.println("1. Add to favourites");
            System.out.println("2. Remove from favourites");
            System.out.println("3. View/Modify Reviews");
            System.out.println("4. Back to productions menu");

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
                    reviewMenuCLI(selectedProd);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // CLI for displaying actor info and options
    public void ActorCLI(Actor selectedActor) {
        Scanner scanner = new Scanner(System.in);
        int choice;
        System.out.println("Actor Information:");
        selectedActor.displayInfo();

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Add to favourites");
            System.out.println("2. Remove from favourites");
            System.out.println("3. View/Modify Reviews");
            System.out.println("4. Back to productions menu");

            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    addFavouriteActor(selectedActor);
                    break;
                case 2:
                    removeFavouriteActor(selectedActor);
                    break;
                case 3:
                    reviewMenuCLI(selectedActor);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public void CLI() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Welcome back user " + username);
            System.out.println("Experience: " + experience);
            System.out.println("Choose action:");
            System.out.println("\t1. View productions details");
            System.out.println("\t2. View actors details");
            System.out.println("\t3. Search for actor/movie/series");
            System.out.println("\t4. View notifications");
            System.out.println("\t5. Favourite Actors");
            System.out.println("\t6. Favourite Productions");
            System.out.println("\t7. Manage your requests");
            System.out.println("\t8. Log Out");
            System.out.println("\t9. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    showProductionsCLI();
                    break;
                case 2:
                    showActorsCLI();
                    break;
                case 3:
                    showSearchCLI();
                    break;
                case 4:
                    showNotificationsCLI();
                    break;
                case 5:
                    showFavouriteActorsCLI();
                    break;
                case 6:
                    showFavouriteProductionsCLI();
                    break;
                case 7:
                    showRequestsCLI(this);
                    break;
                case 8:
                    return;
                case 9:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
