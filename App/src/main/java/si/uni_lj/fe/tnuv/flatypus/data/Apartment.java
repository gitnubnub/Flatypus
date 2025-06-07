package si.uni_lj.fe.tnuv.flatypus.data;

import java.util.List;

public class Apartment {
    private String name;
    private String code;
    private List<String> shoppingList;
    private List<Expense> expenses;
    private List<Task> tasks;
    private List<Chat> chats;

    public Apartment(String name, String code, List<String> shoppingList, List<Expense> expenses, List<Task> tasks, List<Chat> chats) {
        this.name = name;
        this.code = code;
        this.shoppingList = shoppingList;
        this.expenses = expenses;
        this.tasks = tasks;
        this.chats = chats;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}