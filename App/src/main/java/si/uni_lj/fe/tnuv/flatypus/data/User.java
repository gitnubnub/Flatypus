package si.uni_lj.fe.tnuv.flatypus.data;

import java.util.List;

public class User {

    private String email;
    private String password;
    private String username;
    private int profilePicture; //file name of the pfp
    private boolean notifications; //on or off
    private List<Apartment> apartments;
    private Apartment currentApartment;

    public User (String email, String password, String username, int profilePicture, boolean notifications, List<Apartment> apartments, Apartment currentApartment) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.profilePicture = profilePicture;
        this.notifications = notifications;
    }
}
