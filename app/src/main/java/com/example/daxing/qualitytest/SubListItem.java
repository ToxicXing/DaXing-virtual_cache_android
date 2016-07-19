package com.example.daxing.qualitytest;

import java.util.ArrayList;

public class SubListItem {
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
        public Snippet snippet;
        public ContentDetails contentDetails;
        public SubscriberSnippet subscriberSnippet;
    }

    public class Snippet {
        public String publishedAt;
        public String channelTitle;
        public String title;
        public String description;
        public ResourceID resourceID;
        public String channelID;
        public Thumbnails thumbnails;
    }

    public class ResourceID {
        public String kind;
        public String channelId;
    }

    public class Thumbnails {
        public Default aDefault;
        public Medium medium;
        public High high;
        public class Default {
            public String url;
            public int width;
            public int height;
        }
        public class Medium {
            public String url;
            public int width;
            public int height;
        }
        public class High {
            public String url;
            public int width;
            public int height;
        }
    }

    public class ContentDetails {
        public int totalItemCount;
        public int newItemCount;
        public String activityType;
    }

    public class SubscriberSnippet {
        public String title;
        public String description;
        public String channelID;
        public Thumbnails thumbnails;
    }
}


