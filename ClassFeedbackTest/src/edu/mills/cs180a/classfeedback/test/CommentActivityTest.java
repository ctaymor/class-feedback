/**
 * 
 */
package edu.mills.cs180a.classfeedback.test;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import edu.mills.cs180a.classfeedback.CommentActivity;
import edu.mills.cs180a.classfeedback.MySQLiteOpenHelper;
import edu.mills.cs180a.classfeedback.Person;
import edu.mills.cs180a.classfeedback.R;

public class CommentActivityTest extends ActivityInstrumentationTestCase2<CommentActivity> {
    private static final int RECIPIENT_INDEX = 0;  // Use person 0 in Person.everyone.
    private static final Person RECIPIENT = Person.everyone[RECIPIENT_INDEX];
    private static final String COMMENT_TEXT = "lorem ipsum";
    private CommentActivity mActivity;
    private ImageView mImageView;
    private EditText mCommentField;
    private Button mSaveButton;
    private Button mCancelButton;
    private MockCommentsDataSource mCds;
    private static final String TAG = "CommentActivityTest";

    public CommentActivityTest() {
        super(CommentActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Intent i = new Intent();
        setActivityInitialTouchMode(true);
        i.putExtra(CommentActivity.RECIPIENT, RECIPIENT_INDEX);
        i.putExtra(CommentActivity.CDS_FACTORY, new MockCommentsDataSourceFactory());
        setActivityIntent(i);
        // This must occur after setting the touch mode and intent.
        mActivity = getActivity();
        
        // Initialize references to views.
        mImageView = (ImageView) mActivity.findViewById(R.id.commentImageView);
        mCommentField = (EditText) mActivity.findViewById(R.id.commentEditText);
        mSaveButton = (Button) mActivity.findViewById(R.id.saveCommentButton);
        mCancelButton = (Button) mActivity.findViewById(R.id.cancelCommentButton);
        
        // Get data source connection, in case it is needed.
        mCds = MockCommentsDataSource.create(null);  // context argument ignored
    }
    
    protected void tearDown() throws Exception {
        // Note that our tear down code must go before the call to super.tearDown(),
        // which apparently nulls out our instance variables.
        mCds.reset();
        super.tearDown();
    }
    
    // Make sure the imageView contains the picture of the right person.
    public void testImageView() {
        Drawable expectedDrawable = 
                mActivity.getResources().getDrawable(RECIPIENT.getImageId());
        // Drawables cannot be compared directly.   Instead, compare their 
        // constant state, which will be the same for any instances
        // created from the same resource.
        assertEquals(expectedDrawable.getConstantState(), 
                mImageView.getDrawable().getConstantState());
    }
    
    // Make sure that the comment field is initially empty.
    public void testCommentFieldEmpty() {
        assertEquals(0, mCommentField.getText().length());
    }

    private int getNumCommentsForRecipient(Person recipient) {
        Cursor cursor = mCds.getCursorForCommentsForRecipient(
                recipient.getEmail(), null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private void testCommentEntryInternal() {
        String[] desiredColumns = { MySQLiteOpenHelper.COLUMN_CONTENT };
        assertEquals("Database is not empty at beginning of test.",
                0, getNumCommentsForRecipient(RECIPIENT));
        
        // Simulate entering a comment.
        mCommentField.setText(COMMENT_TEXT);
        mSaveButton.performClick();
        
        Cursor cursor = mCds.getCursorForCommentsForRecipient(
                RECIPIENT.getEmail(), desiredColumns);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(COMMENT_TEXT, cursor.getString(0));
        assertFalse(cursor.moveToNext());
        cursor.close();
    }
    
    // Test comment entry twice, to make sure that the database has no comments
    // at the beginning of each test.  Note that the order in which tests run
    // is undefined within JUnit, so we cannot assume that testCommentEntry1()
    // runs before testCommentEntry2().
    @UiThreadTest
    public void testCommentEntry1() {
       testCommentEntryInternal();
    }
    
    @UiThreadTest
    public void testCommentEntry2() {
       testCommentEntryInternal();
    }
}
