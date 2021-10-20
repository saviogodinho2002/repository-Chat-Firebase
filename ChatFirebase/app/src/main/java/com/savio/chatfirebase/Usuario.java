package com.savio.chatfirebase;

import android.os.Parcel;
import android.os.Parcelable;

public class Usuario implements Parcelable {
    private String user_id;
    private  String user_name;
    private String user_url_profilepicture;
    private String token;
    private boolean online;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }



    protected Usuario(Parcel in) {
        user_id = in.readString();
        user_name = in.readString();
        user_url_profilepicture = in.readString();
        token = in.readString();
        online= in.readInt() == 1;
    }

    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_url_profilepicture() {
        return user_url_profilepicture;
    }

    public Usuario(){

    }

    public Usuario(String user_id, String user_name, String user_url_profilepicture) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_url_profilepicture = user_url_profilepicture;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(user_name);
        dest.writeString(user_url_profilepicture);
        dest.writeString(token);
        dest.writeInt(online ?1:0);
    }
}
