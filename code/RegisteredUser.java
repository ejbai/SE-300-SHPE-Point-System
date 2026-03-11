public class RegisteredUser {
    private int studentID;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String points;

    public RegisteredUser() {}
    public RegisteredUser(int ID, String firstName, String lastName) {
        studentID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
