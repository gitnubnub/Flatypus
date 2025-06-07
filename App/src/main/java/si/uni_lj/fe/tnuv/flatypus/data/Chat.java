package si.uni_lj.fe.tnuv.flatypus.data;

public class Chat {
    private String message;
    private User sender;

    private Chat(String message, User sender) {
        this.message = message;
        this.sender = sender;

    }
}
