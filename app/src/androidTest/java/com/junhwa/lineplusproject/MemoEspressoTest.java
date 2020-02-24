package com.junhwa.lineplusproject;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.junhwa.lineplusproject.activity.MemoListActivity;
import com.junhwa.lineplusproject.recycler.memo.MemoAdapter;
import com.junhwa.lineplusproject.recycler.memo.MemoItem;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MemoEspressoTest {
    @Rule
    public ActivityTestRule<MemoListActivity> testRule =
            new ActivityTestRule<>(MemoListActivity.class);

    private MemoListActivity memoListActivity;
    private int count;
    private MemoAdapter adapter;

    @Before
    public void setUp() {
        this.memoListActivity = this.testRule.getActivity();
    }

    @Test
    public void A_testNewMemo() {
        for (int i = 0; i < 4; i++)
            importNewTextMemo("title" + i, "contents" + i);
    }

    @Test
    public void B_removeMemo() {
        this.adapter = this.memoListActivity.getAdapter();
        MemoItem item = adapter.getMemo(1);
        String title = item.getTitle();
        String contents = item.getContents();
        onView(withId(R.id.recyclerMemo)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.buttonRemove)).perform(click());
        this.adapter = this.memoListActivity.getAdapter();
        item = adapter.getMemo(0);

        assertEquals(title, item.getTitle());
        assertEquals(contents, item.getContents());
    }

    @Test
    public void C_updateMemo() {
        String title = "update title";
        String contents = "update contents";

        this.adapter = this.memoListActivity.getAdapter();

        onView(withId(R.id.recyclerMemo)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.buttonUpdate)).perform(click());
        onView(withId(R.id.editTitle)).perform(clearText()).perform(typeText(title));
        onView(withId(R.id.editContents)).perform(clearText()).perform(typeText(contents));
        onView(withId(R.id.buttonSave)).perform(click());
        pressBack();

        this.adapter = this.memoListActivity.getAdapter();
        MemoItem item = adapter.getMemo(0);

        //Error if item's title and contents was already like that
        assertEquals(title, item.getTitle());
        assertEquals(contents, item.getContents());
    }

    @Test
    public void D_getImageFromURL() {
        String title = "This is URL update test";
        String contents = "android image";
        String url = "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fk.kakaocdn.net%2Fdn%2Fbusk9F%2FbtqyQTOzyOf%2FKncfaZEcpUZuYKska0hk91%2Fimg.jpg";

        onView(withId(R.id.newMemo)).perform(click());
        onView(withId(R.id.editTitle)).perform(typeText(title));
        onView(withId(R.id.editContents)).perform(typeText(contents));

        onView(withId(R.id.buttonUrl)).perform(click());
        onView(withId(R.id.editUrl)).perform(typeText(url));
        onView(withId(R.id.buttonSearchUrl)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onView(withId(R.id.buttonConfirm)).perform(click());

        onView(withId(R.id.buttonSave)).perform(click());
        onView(withId(R.id.recyclerMemo)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.recyclerThumbnail)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    @Test
    public void E_checkMemoShow() {
        this.adapter = this.memoListActivity.getAdapter();
        this.count = adapter.getItemCount();
        for (int i = 0; i < count; i++) {
            MemoItem item = adapter.getMemo(i);
            String title = item.getTitle();
            String contents = item.getContents();
            onView(withId(R.id.recyclerMemo)).perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));

            onView(withId(R.id.textViewTitle)).check(matches(withText(title)));
            onView(withId(R.id.textViewContents)).check(matches(withText(contents)));
            pressBack();
        }
    }

    public void importNewTextMemo(String title, String contents) {
        onView(withId(R.id.newMemo)).perform(click());
        onView(withId(R.id.editTitle)).perform(typeText(title));
        onView(withId(R.id.editContents)).perform(typeText(contents));
        onView(withId(R.id.buttonSave)).perform(click());

        this.adapter = this.memoListActivity.getAdapter();
        MemoItem item = adapter.getMemo(0);
        assertEquals(title, item.getTitle());
        assertEquals(contents, item.getContents());
        //pressBack();
        //onView(withId(R.id.recyclerMemo)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }
}
