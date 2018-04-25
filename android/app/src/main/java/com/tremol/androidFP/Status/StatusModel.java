package com.tremol.androidFP.Status;

/**
 * Created by User on 3/11/2015.
 */
public class StatusModel
{
    String name;
    int value;

    public StatusModel(String name, int value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return this.name;
    }

    public int getValue()
    {
        return this.value;
    }

}
