package ro.pub.cs.systems.eim.ColocviuFinal.model;

public class DataModel {
    String country;
    String code;
    String continent;
    Double latitude;
    Double longitude;

    public DataModel(String country, String code, String continent, Double latitude, Double longitude) {
        /* TODO */

        this.country = country;
        this.code = code;
        this.continent = continent;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public String getImageURL() {
        return "https://www.countryflags.io/" + code + "/flat/64.png";
    }

    @Override
    public String toString() {
        /* TODO */

        return "Country: " + country + "\n" +
                "Code: " + code + "\n" +
                "Continent: " + continent + "\n" +
                "Latitude: " + latitude.toString() + "\n" +
                "Longitude: " + longitude.toString() + "\n";
    }
}
