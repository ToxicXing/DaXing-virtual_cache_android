package com.example.daxing.qualitytest;

import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ResourceId;

import java.util.ArrayList;

public class ChannelList {
    public String kind;
    public String etag;
    public PageInfo pageInfo;
    public ArrayList<Item> items;

    public class PageInfo {
        public int totalResults;
        public int resultsPerPage;
    }

    public class Item {
        public String kind;
        public String etag;
        public String id;
        public ContentDetails contentDetails;
    }

    public class ContentDetails {
        public ChannelContentDetails.RelatedPlaylists relatedPlaylists;
    }
}


