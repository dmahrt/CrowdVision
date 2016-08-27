package net.dividedattention.crowdvision.models;

import net.dividedattention.crowdvision.models.Photo;

import java.util.Map;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class CrowdEvent {
    private String title;
    private String location;
    private String city;
    private String state;
    private String endDate;
    private String coverImageUrl;
    private Map<String,Photo> photos;
    private String key;

    public CrowdEvent(String title, String location, String city, String state, String endDate, Map<String,Photo> photos, String coverImageUrl) {
        this.title = title;
        this.location = location;
        this.endDate = endDate;
        this.photos = photos;
        this.coverImageUrl = coverImageUrl;
        this.state = state;
        this.city = city;
    }

    public CrowdEvent() {

    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getEndDate() {
        return endDate;
    }

    public Map getPhotos(){
        return photos;
    }

    public String getCoverImageUrl(){
        return coverImageUrl;
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key = key;
    }
}
