import javax.security.auth.callback.TextInputCallback;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.*;
import java.util.Scanner;

public class Admin extends Staff {
    static List<Production> sharedProductions;
    static List<Actor> sharedActor;
    public Admin() {
        super();
        type = AccountType.ADMIN;
        sharedProductions = new ArrayList<>();
        sharedActor = new ArrayList<>();
        experience = 0;
    }
    @Override
    public boolean handleReq(Request r) {
        // admins can't create requests
        return true;
    }

    // display requests created by the user(GUI)
    public void ownRequestGUI(Request r) {
        // admins can't handle their requests
    }

    public void removeProductionSystem(String name) {
        IMDB imdb = IMDB.getInstance();
        Production p = null;

        // removing production from personal list
        for(Production prod: addedProductions) {
            if (prod.title.equals(name)) {
                addedProductions.remove(prod);
                imdb.productions.remove(prod);
                p = prod;
                break;
            }
        }

        // removing production from shared list
        for(Production prod: sharedProductions) {
            if (prod.title.equals(name)) {
                sharedProductions.remove(prod);
                imdb.productions.remove(prod);
                p = prod;
                break;
            }
        }

        // removing production from favourite lists
        for(User u : imdb.users) {
            u.favouriteProduction.remove(p);
        }

    }

    public void removeActorSystem(String name) {
        IMDB imdb = IMDB.getInstance();
        Actor a = null;

        // removing actor from personal list;
        for(Actor act: addedActor) {
            if(act.name.equals(name)) {
                addedActor.remove(act);
                imdb.actors.remove(act);
                a = act;
                break;
            }
        }

        // removing actor from shared list
        for(Actor act: sharedActor) {
            if(act.name.equals(name)) {
                sharedActor.remove(act);
                imdb.actors.remove(act);
                a = act;
                break;
            }
        }

        // removing from favourite lists
        for(User u : imdb.users) {
            u.favouriteActor.remove(a);
        }
    }

    public String generateUsername(String fullName) {
        IMDB imdb = IMDB.getInstance();
        boolean taken;
        // split the full name into first and last names
        String[] names = fullName.split("\\s+");
        if (names.length < 2) {
            throw new IllegalArgumentException("Full name must have at least a first and last name.");
        }

        String firstName = names[0];
        String lastName = names[names.length - 1];
        String username = null;

        do {
            // combine the first letter of the first name with the last name
            username = firstName.toLowerCase() + "_" + lastName.toLowerCase() + "_";

            // add a random number to make it unique
            int randomSuffix = (int) (Math.random() * 1000);
            username += randomSuffix;

            // seeing if the username isn't taken
            taken = false;
            for(User u : imdb.users) {
                if(u.username.equals(username))
                    taken = true;
            }
        }while(taken);

        return username;
    }

    public String generatePassword() {
        final String lowercase = "abcdefghijklmonpqrstuvwxyz";
        final String uppercase = lowercase.toUpperCase();
        final String digits = "0123456789";
        final String special = "!@#$%&*()_+-=[]{}|,./?><~";

        final String charPool = lowercase + uppercase + digits + special;

        final SecureRandom generator = new SecureRandom();

        // generating the password
        StringBuilder password = new StringBuilder();
        int length = new Random().nextInt(12) + 8;

        for (int i = 0; i < length; i++) {
            int idx = generator.nextInt(charPool.length());
            password.append(charPool.charAt(idx));
        }

        return password.toString();
    }
    public void addAccount(User u) {
        IMDB imdb = IMDB.getInstance();

        // generating username and password
        u.username = generateUsername(u.info.name);
        u.info.setCredentials(u.info.getCredentials().getEmail(), generatePassword());

        imdb.users.add(u);
    }
    public void removeAccount(User u) {
        IMDB imdb = IMDB.getInstance();

        if (u.type != AccountType.REGULAR) {
            Staff staff = (Staff)u;
            // moving all contributions to shared lists
            // productions
            sharedProductions.addAll(staff.addedProductions);
            // subscribing the admins to the production
            for(Production p : staff.addedProductions) {
                for(User user : imdb.users)
                    if(user.type == AccountType.ADMIN) {
                        p.Subscribe(user);
                    }
            }
            // actors
            sharedActor.addAll(staff.addedActor);
            // subscribing the admins to the actor
            for(Actor a : staff.addedActor) {
                for(User user : imdb.users)
                    if(user.type == AccountType.ADMIN) {
                        a.Subscribe(user);
                    }
            }

            // moving his request to ADMIN
            for(Request r : staff.personalReq) {
                r.fixer = "ADMIN";
                IMDB.RequestHolder.addRequest(r);
            }
        }

        imdb.users.remove(u);

        // unsubscribing the account from all productions
        for(Production p : imdb.productions)
            p.Unsubscribe(u);

        // removing all user reviews
        for(Production p : imdb.productions) {
            p.reviews.removeIf(r -> r.user.equals(u.username));
            p.calculateRating();
        }

        // unsubscribing user from all actors
        for(Actor a : imdb.actors)
            a.Unsubscribe(u);

        // removing all user reviews
        for(Actor a : imdb.actors) {
            a.reviews.removeIf(r -> r.user.equals(u.username));
        }
    }

    // GUI for changing user info
    public void infoChangeGUI(User user) {
        JFrame changeFrame = new JFrame("User Information");
        changeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        changeFrame.setLayout(new GridLayout(11, 2));

        // components
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();
        nameField.setText(user.info.name);

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        usernameField.setText(user.username);

        JLabel countryLabel = new JLabel("Country:");
        JTextField countryField = new JTextField();
        countryField.setText(user.info.country);

        JLabel ageLabel = new JLabel("Age:");
        JTextField ageField = new JTextField();
        ageField.setText(user.info.age + "");

        JLabel genderLabel = new JLabel("Gender:");
        JComboBox<Character> genderComboBox = new JComboBox<>(new Character[]{'M', 'F', 'N'});
        genderComboBox.setSelectedItem(user.info.gender);

        JLabel birthdayLabel = new JLabel("Birthday (yyyy-MM-dd):");
        JTextField birthdayField = new JTextField();
        birthdayField.setText(user.info.getBirthday().toString());

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        emailField.setText(user.info.getCredentials().getEmail());

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        passwordField.setText(user.info.getCredentials().getPassword());

        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");

        JButton saveButton = new JButton("Save");
        JButton back = new JButton("Return to IMDB");

        // adding components to the frame
        changeFrame.add(nameLabel);
        changeFrame.add(nameField);
        changeFrame.add(usernameLabel);
        changeFrame.add(usernameField);
        changeFrame.add(countryLabel);
        changeFrame.add(countryField);
        changeFrame.add(ageLabel);
        changeFrame.add(ageField);
        changeFrame.add(genderLabel);
        changeFrame.add(genderComboBox);
        changeFrame.add(birthdayLabel);
        changeFrame.add(birthdayField);
        changeFrame.add(emailLabel);
        changeFrame.add(emailField);
        changeFrame.add(passwordLabel);
        changeFrame.add(passwordField);
        changeFrame.add(showPasswordCheckBox);
        changeFrame.add(saveButton);
        changeFrame.add(back);

        // error message
        JLabel warning = new JLabel();
        warning.setForeground(Color.red);
        warning.setVisible(false);
        changeFrame.add(warning);

        // return button
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFrame.dispose();
                GUI();
            }
        });

        // action listener for the Show Password checkbox
        showPasswordCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int state = e.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    passwordField.setEchoChar((char) 0); // Show password
                } else {
                    passwordField.setEchoChar('*'); // Hide password
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // verifying if username is unique
                String newName = usernameField.getText();
                IMDB imdb = IMDB.getInstance();

                for(User u : imdb.users)
                    if(u.username.equals(newName) && user != u) {
                        warning.setText("Username taken");
                        warning.setVisible(true);
                        return;
                    }

                try {
                    // build information
                    Information.InformationBuilder builder = new Information.InformationBuilder();
                    builder.name(nameField.getText());
                    builder.country(countryField.getText());
                    builder.age(Long.parseLong(ageField.getText()));
                    builder.gender((Character) genderComboBox.getSelectedItem());
                    builder.birthday(birthdayField.getText());
                    builder.credentials(emailField.getText(), new String(passwordField.getPassword()));

                    user.info = builder.build();
                    user.username = newName;

                    changeFrame.dispose();
                    GUI();
                } catch (DateTimeException err) {
                    warning.setText("Date invalid");
                    warning.setVisible(true);
                } catch (NumberFormatException err) {
                    warning.setText("Age invalid");
                    warning.setVisible(true);
                }
            }
        });

        changeFrame.pack();
        changeFrame.setLocationRelativeTo(null);
        changeFrame.setVisible(true);
    }

    // GUI for adding account
    public void addAccountGUI() {
        JFrame accountFrame = new JFrame("Account Creation");
        accountFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        accountFrame.setLayout(new BoxLayout(accountFrame.getContentPane(), BoxLayout.PAGE_AXIS));

        JPanel panel = new JPanel(new GridLayout(8, 2));

        // account Type
        JLabel accountTypeLabel = new JLabel("Account Type:");
        JComboBox<AccountType> accountTypeComboBox = new JComboBox<>(new AccountType[]{AccountType.ADMIN, AccountType.CONTRIBUTOR, AccountType.REGULAR});

        // name
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameTextField = new JTextField();

        // email
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailTextField = new JTextField();

        // country
        JLabel countryLabel = new JLabel("Country:");
        JTextField countryTextField = new JTextField();

        // age
        JLabel ageLabel = new JLabel("Age:");
        JTextField ageTextField = new JTextField();

        // gender
        JLabel genderLabel = new JLabel("Gender:");
        JComboBox<Character> genderComboBox = new JComboBox<>(new Character[]{'M', 'F', 'N'});

        // birthday
        JLabel birthdayLabel = new JLabel("Birthday (YYYY-MM-DD):");
        JTextField birthdayTextField = new JTextField();

        panel.add(accountTypeLabel);
        panel.add(accountTypeComboBox);
        panel.add(nameLabel);
        panel.add(nameTextField);
        panel.add(emailLabel);
        panel.add(emailTextField);
        panel.add(countryLabel);
        panel.add(countryTextField);
        panel.add(ageLabel);
        panel.add(ageTextField);
        panel.add(genderLabel);
        panel.add(genderComboBox);
        panel.add(birthdayLabel);
        panel.add(birthdayTextField);

        birthdayTextField.setToolTipText("Format: yyyy-mm-dd");

        JLabel warning = new JLabel("Date or age invalid");
        warning.setForeground(Color.RED);
        warning.setVisible(false);
        panel.add(warning);

        JButton createAccountButton = new JButton("Create Account");
        panel.add(createAccountButton);
        accountFrame.add(panel);

        // adding success panel
        JLabel successMsg = new JLabel("Account created(memorise the password, you can only get it now)");
        successMsg.setForeground(Color.green);
        successMsg.setVisible(false);
        accountFrame.add(successMsg);

        JPanel credentials = new JPanel(new GridLayout(2, 2));
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.green);
        credentials.add(userLabel);
        JTextField userName = new JTextField();
        userName.setEditable(false);
        credentials.add(userName);
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.green);
        credentials.add(passLabel);
        JTextField passWord = new JTextField();
        passWord.setEditable(false);
        credentials.add(passWord);
        accountFrame.add(credentials);
        credentials.setVisible(false);

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // generating the user
                try {
                    UserFactory factory = UserFactory.getInstance();
                    Information.InformationBuilder builder = new Information.InformationBuilder();
                    User newUser;

                    AccountType accountType = (AccountType) accountTypeComboBox.getSelectedItem();
                    newUser = factory.createUser(accountType);

                    builder.name(nameTextField.getText());
                    builder.credentials(emailTextField.getText(), null);
                    builder.country(countryTextField.getText());
                    builder.age(Integer.parseInt(ageTextField.getText()));
                    builder.gender((Character) genderComboBox.getSelectedItem());
                    builder.birthday(birthdayTextField.getText());

                    newUser.info = builder.build();
                    addAccount(newUser);

                    userName.setText(newUser.username);
                    passWord.setText(newUser.info.getCredentials().getPassword());

                    successMsg.setVisible(true);
                    credentials.setVisible(true);
                    warning.setVisible(false);
                } catch (DateTimeException err) {
                    successMsg.setVisible(false);
                    credentials.setVisible(false);
                    warning.setVisible(true);
                } catch (NumberFormatException err) {
                    successMsg.setVisible(false);
                    credentials.setVisible(false);
                    warning.setVisible(true);
                }
            }
        });

        accountFrame.setSize(800, 300);
        accountFrame.setLocationRelativeTo(null);
        accountFrame.setVisible(true);
    }

    // GUI for managing accounts
    public void accountManagement() {
        workingSpace.removeAll();
        workingSpace.revalidate();
        workingSpace.repaint();

        // managing accounts
        workingSpace.add(new Label("Manage accounts:"));

        JButton addButton = new JButton("Add Account");
        addButton.setBackground(Color.green);
        workingSpace.add(addButton);

        ArrayList<User> userList = new ArrayList<>();
        userList.add(null);
        userList.addAll(imdb.users);

        JComboBox<User> subjectUser = new JComboBox<>(userList.toArray(new User[userList.size()]));
        workingSpace.add(subjectUser);

        subjectUser.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("None");
                } else if (value instanceof User) {
                    setText(value.toString());
                }
                return this;
            }
        });

        JPanel accountPanel = new JPanel(new GridLayout(1, 2));

        // remove button
        JButton removeButton = new JButton("Remove Account");
        removeButton.setBackground(Color.red);
        removeButton.setForeground(Color.white);
        accountPanel.add(removeButton);

        // modify button
        JButton modifyButton = new JButton("Modify Account");
        accountPanel.add(modifyButton);

        workingSpace.add(accountPanel);

        // action listeners for buttons
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User selectedUser = (User) subjectUser.getSelectedItem();
                if (selectedUser != null) {
                    removeAccount(selectedUser);
                    subjectUser.removeItem(selectedUser);
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addAccountGUI();
                // Optionally, update the user list in the dropdown after addition
                subjectUser.removeAllItems();
                subjectUser.addItem(null);
                for (User user : imdb.users) {
                    subjectUser.addItem(user);
                }
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User selectedUser = (User) subjectUser.getSelectedItem();
                if (selectedUser != null) {
                    infoChangeGUI(selectedUser);
                    frontPage.dispose();
                }
            }
        });
    }
    @Override
    public void GUI() {
        super.GUI();

        // manage accounts section
        JButton accountsButton = new JButton("Manage Accounts");
        leftMenu.add(accountsButton);

        accountsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountManagement();
            }
        });


    }

    // CLI for changing user info
    public void infoChangeCLI(User user) {
        Scanner scanner = new Scanner(System.in);
        Information.InformationBuilder builder = new Information.InformationBuilder();

        System.out.println("User Information");

        // name
        System.out.println("Current Name: " + user.info.name);
        System.out.print("New Name (Enter to keep current): ");
        String newName = scanner.nextLine().trim();
        if (newName.isEmpty()) {
            newName = user.info.name;
        }
        builder.name(newName);

        // username
        System.out.println("Current Username: " + user.username);
        System.out.print("New Username (Enter to keep current): ");
        String newUsername = scanner.nextLine().trim();
        if (newUsername.isEmpty()) {
            newUsername = user.username;
        }

        // country
        System.out.println("Current Country: " + user.info.country);
        System.out.print("New Country (Enter to keep current): ");
        String newCountry = scanner.nextLine().trim();
        if (newCountry.isEmpty()) {
            newCountry = user.info.country;
        }
        builder.country(newCountry);

        // valid age
        long newAge = user.info.age;
        System.out.println("Current Age: " + user.info.age);
        while (true) {
            System.out.print("New Age (Enter to keep current): ");
            String newAgeInput = scanner.nextLine().trim();
            if (newAgeInput.isEmpty()) {
                break;
            }
            try {
                newAge = Long.parseLong(newAgeInput);
                builder.age(newAge);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid age.");
            }
        }

        // valid gender
        System.out.println("Current Gender: " + user.info.gender);
        char newGender = user.info.gender;
        while (true) {
            System.out.print("New Gender (M/F/N, Enter to keep current): ");
            String newGenderInput = scanner.nextLine().trim();

            if (newGenderInput.isEmpty()) {
                break;
            }

            if (newGenderInput.length() == 1) {
                char genderChar = newGenderInput.charAt(0);
                if (genderChar == 'M' || genderChar == 'F' || genderChar == 'N') {
                    // valid gender input
                    newGender = genderChar;
                    builder.gender(newGender);
                    break;
                } else {
                    System.out.println("Invalid input. Please enter M, F, or N.");
                }
            } else {
                System.out.println("Invalid input. Please enter M, F, or N.");
            }
        }

        // valid birthday
        String newBirthday = user.info.getBirthday().toString();
        while (true) {
            System.out.print("New Birthday (yyyy-MM-dd, Enter to keep current): ");
            String newBirthdayInput = scanner.nextLine().trim();
            if (newBirthdayInput.isEmpty()) {
                break;
            }
            try {
                builder.birthday(newBirthday);
                break;
            } catch (DateTimeException e) {
                System.out.println("Invalid input. Please enter a valid birthday.");
            }
        }

        // email
        System.out.println("Current Email: " + user.info.getCredentials().getEmail());
        System.out.print("New Email (Enter to keep current): ");
        String newEmail = scanner.nextLine().trim();
        if (newEmail.isEmpty()) {
            newEmail = user.info.getCredentials().getEmail();
        }

        // password
        System.out.print("New Password (Enter to keep current): ");
        String newPassword = scanner.nextLine().trim();
        if (newPassword.isEmpty()) {
            newPassword = user.info.getCredentials().getPassword();
        }
        builder.credentials(newEmail, newPassword);

        user.info = builder.build();
        user.username = newUsername;

        System.out.println("Changes saved successfully!");
    }

    // CLI for modifying account
    public void modifyAccountCLI() {
        Scanner scanner = new Scanner(System.in);
        IMDB imdb = IMDB.getInstance();

        System.out.println("Modify Account:");

        if (imdb.users.isEmpty()) {
            System.out.println("No accounts to modify.");
            return;
        }

        System.out.println("Select the account to modify:");

        for (int i = 0; i < imdb.users.size(); i++) {
            System.out.println((i + 1) + ") " + imdb.users.get(i).username);
        }

        // account selection
        while(true) {
            System.out.print("Enter the number of the account to modify (0 to cancel): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (choice > 0 && choice <= imdb.users.size()) {
                User selectedUser = imdb.users.get(choice - 1);
                infoChangeCLI(selectedUser);
            } else if (choice == 0) {
                return;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    //CLI for removing account
    public void removeAccountCLI() {
        Scanner scanner = new Scanner(System.in);
        IMDB imdb = IMDB.getInstance();

        System.out.println("Remove Account:");

        if (imdb.users.isEmpty()) {
            System.out.println("No accounts to remove.");
            return;
        }

        System.out.println("Select the account to remove:");

        for (int i = 0; i < imdb.users.size(); i++) {
            System.out.println((i + 1) + ") " + imdb.users.get(i).username);
        }

        // account selection
        while(true) {
            System.out.print("Enter the number of the account to remove (0 to cancel): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (choice > 0 && choice <= imdb.users.size()) {
                User selectedUser = imdb.users.get(choice - 1);
                removeAccount(selectedUser);
                System.out.println("Account removed successfully.");
            } else if (choice == 0) {
                return;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // CLI for adding account
    public void addAccountCLI() {
        Scanner scanner = new Scanner(System.in);
        Information.InformationBuilder builder = new Information.InformationBuilder();

        System.out.println("Account Creation:");

        // account type
        System.out.println("Account Type:");
        System.out.println("1. ADMIN");
        System.out.println("2. CONTRIBUTOR");
        System.out.println("3. REGULAR");
        System.out.print("Select the account type (1-3): ");
        AccountType accountType;

        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        accountType = AccountType.ADMIN;
                        break;
                    case 2:
                        accountType = AccountType.CONTRIBUTOR;
                        break;
                    case 3:
                        accountType = AccountType.REGULAR;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                        continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 3.");
            }
        }

        // name
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        builder.name(name);

        // email
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        builder.credentials(email, null);

        // country
        System.out.print("Enter Country: ");
        String country = scanner.nextLine();
        builder.country(country);

        // valid age
        int age;
        while (true) {
            try {
                System.out.print("Enter Age: ");
                age = Integer.parseInt(scanner.nextLine().trim());
                builder.age(age);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid age.");
            }
        }

        // valid gender
        char gender;
        while (true) {
            System.out.print("Enter Gender (M/F/N): ");
            String genderInput = scanner.nextLine().trim().toUpperCase();

            if (genderInput.length() == 1) {
                gender = genderInput.charAt(0);
                if (gender == 'M' || gender == 'F' || gender == 'N') {
                    builder.gender(gender);
                    break;
                }
            }

            System.out.println("Invalid input. Please enter 'M', 'F', or 'N' for gender.");
        }

        // valid birthday
        String birthday;
        while (true) {
            System.out.print("Enter Birthday (YYYY-MM-DD): ");
            birthday = scanner.nextLine().trim();

            try {
                builder.birthday(birthday); // Validate the date format
                break;
            } catch (DateTimeException e) {
                System.out.println("Invalid date format. Please enter a valid date in YYYY-MM-DD format.");
            }
        }

        // create the user
        UserFactory factory = UserFactory.getInstance();
        User newUser;

        newUser = factory.createUser(accountType);

        newUser.info = builder.build();
        addAccount(newUser);

        System.out.println("Account created successfully(memorise the password, you can only see it now");
        System.out.println("Username: " + newUser.username);
        System.out.println("Password: " + newUser.info.getCredentials().getPassword());
    }

    // CLI for managing accounts
    public void manageAccounts() {
        Scanner scanner = new Scanner(System.in);

        // account menu
        while (true) {
            System.out.println("Manage accounts:");
            System.out.println("1. Remove Account");
            System.out.println("2. Add Account");
            System.out.println("3. Modify account");
            System.out.println("4. Back to main menu");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    removeAccountCLI();
                    break;
                case 2:
                    addAccountCLI();
                    break;
                case 3:
                    modifyAccountCLI();
                    return; // Go back to the main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // display requests created by the user(CLI)
    public void ownRequestCLI(Request r) {
        // admins can't handle requests
    }
    public void CLI() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Welcome back user " + username);
            System.out.println("Experience: -");
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
            System.out.println("\t10. Add/remove accout");
            System.out.println("\t11. Log Out");
            System.out.println("\t12. Exit");

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
                    manageAccounts();
                    break;
                case 11:
                    IMDB imdb = IMDB.getInstance();
                    imdb.CLILogIn();
                case 12:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
