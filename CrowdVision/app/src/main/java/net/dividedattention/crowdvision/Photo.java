package net.dividedattention.crowdvision;

/**
 * Created by drewmahrt on 8/3/16.
 */
public class Photo {
    String photoUrl;
    int likes;

    public Photo(String photoUrl, int likes) {
        this.photoUrl = photoUrl;
        this.likes = likes;
    }

    public Photo(String photoUrl){
        this.photoUrl = photoUrl;
        this.likes = 0;
    }

    public Photo(){
        this.photoUrl = null;
        this.likes = 0;
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
