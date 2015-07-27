package com.example.nick.popularmovies;

import java.io.Serializable;

/**
 * Created by NICK on 7/15/2015.
 */
public class Movie implements Serializable{
    public String title;
    public String originalTitle;
    public String releaseDate;
    public String imageLink;
    public String synopsis; //overview
    public Double userRating; //vote_average

}
