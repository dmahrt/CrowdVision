package net.dividedattention.crowdvision.models;

/**
 * Created by drewmahrt on 8/3/16.
 */
public class Photo {
    String photoUrl;
    int likes;
    float height;
    float width;

    public Photo(String photoUrl, int likes) {
        this.photoUrl = photoUrl;
        this.likes = likes;
    }

    public Photo(String photoUrl, float height, float width){
        this.photoUrl = photoUrl;
        this.likes = 0;
        this.height = height;
        this.width = width;
    }

    public Photo(){
        this.photoUrl = null;
        this.likes = 0;
        this.width = 0;
        this.height = 0;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
