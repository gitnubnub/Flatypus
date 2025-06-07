package si.uni_lj.fe.tnuv.flatypus.data;

public class Apartment {
    private String name;
    private String code;

    public Apartment(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}