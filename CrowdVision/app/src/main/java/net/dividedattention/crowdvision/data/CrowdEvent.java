package net.dividedattention.crowdvision.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/**
 * Created by drewmahrt on 5/11/16.
 */
public class CrowdEvent implements Parcelable{
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

    protected CrowdEvent(Parcel in) {
        title = in.readString();
        location = in.readString();
        city = in.readString();
        state = in.readString();
        endDate = in.readString();
        coverImageUrl = in.readString();
        key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(location);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(endDate);
        dest.writeString(coverImageUrl);
        dest.writeString(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CrowdEvent> CREATOR = new Creator<CrowdEvent>() {
        @Override
        public CrowdEvent createFromParcel(Parcel in) {
            return new CrowdEvent(in);
        }

        @Override
        public CrowdEvent[] newArray(int size) {
            return new CrowdEvent[size];
        }
    };

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

    @Override
    public boolean equals(Object obj) {
        CrowdEvent otherEvent = (CrowdEvent)obj;
        return otherEvent.getKey().equals(getKey());
    }
}
