public class Member extends RegisteredUser {
    public Member(int ID, String firstName, String lastName, String email, String phoneNumber, int points) {
        studentID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.points = points;
        setRank("Member");
    }
}
