package com.example.fosha.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties

public class Post implements Parcelable {
    public String uid;
    public int price;
    public String description;
    public String address;
    public String placeName;
    public String author;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();
    public ArrayList<String> download_urls=new ArrayList<>();

    public Post() {
    }

    public Post(String uid, String author, String title, String body) {
        this.uid = uid;
        this.author = author;
        this.placeName = title;
        this.description = body;
    }

    public Post(String uid, int price, String description, String address, String placeName, String author, ArrayList<String> photosDownloadUrl) {
        this.uid = uid;
        this.price = price;
        this.description = description;
        this.address = address;
        this.placeName = placeName;
        this.author = author;
        this.download_urls = photosDownloadUrl;
    }

    protected Post(Parcel in) {
        uid = in.readString();
        price = in.readInt();
        description = in.readString();
        address = in.readString();
        placeName = in.readString();
        author = in.readString();
        starCount = in.readInt();
        download_urls = in.createStringArrayList();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("placeName", placeName);
        result.put("description", description);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("price",price);
        result.put("address",address);
        result.put("download_urls",download_urls);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeInt(price);
        dest.writeString(description);
        dest.writeString(address);
        dest.writeString(placeName);
        dest.writeString(author);
        dest.writeInt(starCount);
        dest.writeStringList(download_urls);
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }
}
