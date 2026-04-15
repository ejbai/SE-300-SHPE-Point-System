public class ClubEvent {
    private String name;
    private String location;
    private String dateTime;
    private String pointsEarned;
    private String pointsNeeded;

    public ClubEvent(String name, String location, String dateTime, String pointsEarned, String pointsNeeded) {
        this.name = name;
        this.location = location;
        this.dateTime = dateTime;
        this.pointsEarned = pointsEarned;
        this.pointsNeeded = pointsNeeded;
    }

    public ClubEvent(String name) {
        this.name = name;
        this.location = "";
        this.dateTime = "";
        this.pointsEarned = "";
        this.pointsNeeded = "";
    }

    public String getName() {
        return name;
    }
    public String getLocation() {
        return location;
    }
    public String getDateTime() {
        return dateTime;
    }
    public String getPointsEarned() {
        return pointsEarned;
    }
    public String getPointsNeeded() {
        return pointsNeeded;
    }
}
