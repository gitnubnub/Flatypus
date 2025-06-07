package si.uni_lj.fe.tnuv.flatypus.data;

public class Expense {

    private User whoOwes;
    private User whoIsOwed;
    private double money;

    private Expense(User whoOwes, User whoIsOwed, double money) {
        this.whoOwes = whoOwes;
        this.whoIsOwed = whoIsOwed;
        this.money = money;

    }

}
