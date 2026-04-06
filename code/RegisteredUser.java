public class RegisteredUser {
    protected int studentID;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String phoneNumber;
    protected int points;
    protected String rank;

    public RegisteredUser() {}
    public RegisteredUser(int ID, String firstName, String lastName, String email, String phoneNumber, int points, String rank) {
        studentID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.points = points;
    }

    public void setFirstName(String name) {
        firstName = name;
    }
    public void setLastName(String name) {
        lastName = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setPoints(int points) {
        this.points = points;
    }
    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getStudentID() {
        return studentID;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public int getPoints() {
        return points;
    }
    public String getRank() {
        return rank;
    }
}
