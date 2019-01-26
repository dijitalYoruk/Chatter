package com.example.chatter.Main;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.chatter.Main.FragmentChats.FragmentChats;
import com.example.chatter.Main.FragmentGroups.FragmentGroups;
import com.example.chatter.Main.FragmentRequests.FragmentRequests;

public class TabBarAdapter extends FragmentPagerAdapter {

    // constants
    private final int FRAGMENT_COUNT = 3;

    /**
     * Constructor of Tab Bar Adapter
     * @param fm is the fragment manager.
     */
    public TabBarAdapter(FragmentManager fm) {
        super(fm);
    }

    // methods

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return new FragmentChats();

        else if (position == 1)
            return new FragmentGroups();

        else
            return new FragmentRequests();
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0)
            return "Chats";

        else if (position == 1)
            return "Groups";

        else return "Requests";
    }
}