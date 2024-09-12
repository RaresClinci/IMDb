import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

public class Contributor extends Staff implements RequestsManager {
    public Contributor() {
        super();
        type = AccountType.CONTRIBUTOR;
        experience = 0;
    }

    // check if the name was added by the user
    public boolean ownContribution(String name) {
        // checking if the user added the actor
        for (Actor a : addedActor)
            if(a.name.equals(name))
                return true;

        // checking if the user added the production
        for (Production p : addedProductions)
            if(p.title.equals(name))
                return true;
        return false;
    }

    // was the request added successfully?
    @Override
    public boolean handleReq(Request r) {
        if(ownContribution(r.name))
            return false;
        else {
            createRequest(r);
            r.notifyReceived();
            return true;
        }
    }
    @Override
    public void createRequest(Request r) {
        if(r.fixer.equals("ADMIN")) {
            createdRequests.add(r);
            IMDB.RequestHolder.addRequest(r);
        } else {
            if(ownContribution(r.name))
                return;

            // searching for the one who added
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

                    // searching if th user added that actor
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
                    ((Staff) u).personalReq.remove(r);
                }
            }
        }
        createdRequests.remove(r);
    }

    // GUI displaying requests created by the user
    public void ownRequestGUI(Request r) {
        JFrame requestFrame = new JFrame("Request");
        requestFrame.setDefaultCloseOperation(requestFrame.EXIT_ON_CLOSE);
        requestFrame.setMinimumSize(new Dimension(400, 400));
        requestFrame.setLayout(new BoxLayout(requestFrame.getContentPane(), BoxLayout.PAGE_AXIS));

        // request info and buttons
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

    // CLI displaying requests created by the user
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
            System.out.println("\t8. Solve requets");
            System.out.println("\t9. Add/remove actor/movie/series");
            System.out.println("\t10. Log Out");
            System.out.println("\t11. Exit");

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
                    solveRequestCLI();
                    break;
                case 9:
                    manageContributions();
                    break;
                case 10:
                    IMDB imdb = IMDB.getInstance();
                    imdb.CLILogIn();
                case 11:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
