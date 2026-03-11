public class Chairman extends RegisteredUser {
    public Chairman(int ID, String firstName, String lastName, String email, String phoneNumber, int points) {
        studentID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.points = points;
        setRank("Chairman");
    }
}
