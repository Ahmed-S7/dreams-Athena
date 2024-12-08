package com.example.athena;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;

import org.junit.Test;

import com.example.athena.EntrantAndOrganizerFragments.ProfileDetails.profileScreenEditFragment;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.*;

public class ProfileEditNotifTest {
    @Test
    public void testFragmentUIElements() {
        FragmentScenario.launchInContainer(profileScreenEditFragment.class);

        // Check if the notification switches are displayed
        Espresso.onView(withId(R.id.chosen_notif))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("Notify when chosen")));

        Espresso.onView(withId(R.id.not_chosen_notif))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("Notify when not chosen")));

        Espresso.onView(withId(R.id.geolocation_warn))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("Warn event requires geolocation")));

        Espresso.onView(withId(R.id.notifs_from_others))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("Recieve notification from others")));
    }

    @Test
    public void testSwitchClick() {
        FragmentScenario.launchInContainer(profileScreenEditFragment.class);

        // Toggle the chosen notification switch
        Espresso.onView(withId(R.id.chosen_notif))
                .perform(click())
                .check(ViewAssertions.matches(isChecked()));
    }
}
