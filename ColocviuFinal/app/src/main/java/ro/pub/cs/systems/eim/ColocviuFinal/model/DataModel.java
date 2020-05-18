package ro.pub.cs.systems.eim.ColocviuFinal.model;

public class DataModel {
    String rate;
    String updated;

    public DataModel(String rate, String updated) {
        this.rate    = rate;
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "Rate: " + rate + "\n" +
                "Updated at: " + updated + "\n";
    }
}
