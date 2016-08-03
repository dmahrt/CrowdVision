package net.dividedattention.crowdvision;

import java.util.Map;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class CrowdEvent {
    private String title;
    private String location;
    private String endDate;
    private String coverImageUrl;
    private Map<String,Photo> photos;
    private String key;

    public CrowdEvent(String title, String location, String endDate, Map<String,Photo> photos, String coverImageUrl) {
        this.title = title;
        this.location = location;
        this.endDate = endDate;
        this.photos = photos;
        this.coverImageUrl = coverImageUrl;
    }

    public CrowdEvent() {

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
