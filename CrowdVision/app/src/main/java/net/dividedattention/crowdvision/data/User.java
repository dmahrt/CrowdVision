package net.dividedattention.crowdvision.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drewmahrt on 8/3/16.
 */
public class User {
    private List<String> likesList;

    public User(List<String> likesList) {
        this.likesList = likesList;
    }

    public User(){
        likesList = new ArrayList<>();
    }

    public List<String> getLikesList() {
        return likesList;
    }

    public void setLikesList(List<String> likesList) {
        this.likesList = likesList;
    }
}
