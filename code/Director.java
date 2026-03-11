public class Director extends RegisteredUser {
    public Director(int ID, String firstName, String lastName, String email, String phoneNumber, int points) {
        studentID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.points = points;
        setRank("Director");
    }
}
