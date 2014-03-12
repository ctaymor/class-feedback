package edu.mills.cs180a.classfeedback.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import edu.mills.cs180a.classfeedback.Comment;
import edu.mills.cs180a.classfeedback.CommentContentProvider;
import edu.mills.cs180a.classfeedback.MySQLiteOpenHelper;

// This creates an IsolatedContext and does not affect the production store.
public class CommentContentProviderTest extends ProviderTestCase2<CommentContentProvider> {
    private MockContentResolver mResolver;
    private static final String KEY_EMAIL = "KEY_EMAIL";
    private static final String CONTENT = "lorem ipsum";
    private static final String EMAIL = "foo@bar.com";
    private static final String KEY_COMMENT = "KEY_COMMENT";
    private static final int COLUMN_CONTENT_POS = 2;
    
    public CommentContentProviderTest() {
        super(CommentContentProvider.class, CommentContentProvider.AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
    }
    
    public void testInsertCommentForUserWithNoComment() {
        // Test no comment yet
        Uri uriOfRecipient = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL);
        checkNoCommentsForUser(uriOfRecipient);
        // Insert a comment
        Uri uriReturned = insertAComment(EMAIL, CONTENT);
        // Test comment was inserted
        assertEquals("content://edu.mills.cs180a.classfeedback/comments/" +  EMAIL, uriReturned);
        Cursor cursor = getCursorForCommentsForUser(uriOfRecipient);
        assert(cursor.moveToFirst());
        assertEquals(CONTENT, cursor.getString(COLUMN_CONTENT_POS));
        cursor.close();
    }
    
    public void testInsertCommentForUserWithComment() {
        Uri uriOfRecipient = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL);
        // Insert an initial comment
        Uri uriFirstComment = insertAComment(EMAIL, CONTENT);
        // Try to insert another comment
        Uri uriSecondComment = insertAComment(EMAIL, "Not CONTENT");
        assertNull(uriSecondComment);
        Cursor cursor = getCursorForCommentsForUser(uriOfRecipient);
        assert(cursor.moveToFirst());
        assertEquals(CONTENT, cursor.getString(COLUMN_CONTENT_POS));
    }
    
    public void testDeleteWithCommentsUriNoSelectionCriteria() {
        
    }
    
    public void testDeleteWithCommentsUriWithSelectionCriteria() {
        // Make some comments
        insertAComment(EMAIL, CONTENT);
        insertAComment("bar@foo.com", CONTENT);
        insertAComment("foobar@barfoo.com", "not CONTENT");
        // Delete comments with selection criteria
        // Test that the comments we expected were deleted
        // (All of them and no others)
    }
    
    public void testDeleteWithCommentsEmailUriWithoutSelectionCriteria() {
        
    }
    
    public void testDeleteWithCommentsEmailUriWithSelectionCriteria() {
        
    }
    
    public void testNoCommentsForEllenAtStart() {
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL);
        checkNoCommentsForUser(uri);
    }
    
    public void checkNoCommentsForUser(Uri uri) {
        Cursor cursor = getCursorForCommentsForUser(uri);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }
    public Cursor getCursorForCommentsForUser(Uri uri) {
        String[] projection = { "content" };  // desired columns
        return mResolver.query(uri, projection, null, null, null);
    }
    
    public Uri insertAComment(String email, String content){
     // Test inserting new comment
        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email);
        values.put(KEY_COMMENT, content);
        return mResolver.insert(CommentContentProvider.CONTENT_URI, values);
    }
}
