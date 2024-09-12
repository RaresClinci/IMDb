public class UserFactory {
    private static final UserFactory instance = new UserFactory();

    private UserFactory() {}

    public static UserFactory getInstance() {
        return instance;
    }

    public User createUser(AccountType type) {
        switch(type) {
            case ADMIN: return new Admin();
            case REGULAR: return new Regular();
            case CONTRIBUTOR: return new Contributor();
            default: return null;
        }
    }
}
