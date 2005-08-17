package com.cinnamonbob.model;

public enum ResultState
{
    INITIAL
    {
        public String getPrettyString()
        {
            return "initial";
        }
    },
    IN_PROGRESS
    {
        public String getPrettyString()
        {
            return "in progress";
        }
    },
    SUCCESS
    {
        public String getPrettyString()
        {
            return "success";
        }
    },
    FAILURE
    {
        public String getPrettyString()
        {
            return "failure";
        }
    },
    ERROR
    {
        public String getPrettyString()
        {
            return "error";
        }
    };
    
    public abstract String getPrettyString();
    
}
