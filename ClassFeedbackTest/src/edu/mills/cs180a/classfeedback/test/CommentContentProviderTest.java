package edu.mills.cs180a.classfeedback.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import edu.mills.cs180a.classfeedback.CommentContentProvider;

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
        Uri uriOfRecipient = Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + EMAIL);
        checkNoCommentsForUser(uriOfRecipient);
        // Insert a comment
        Uri uriReturned = insertAComment(EMAIL, CONTENT);
        // Test comment was inserted
        assertEquals("content://edu.mills.cs180a.classfeedback/comments/"
                +  EMAIL, uriReturned);
        checkCommentContentForRecipient(uriOfRecipient, CONTENT);
    }
    
    public void testInsertCommentForUserWithComment() {
        Uri uriOfRecipient = Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + EMAIL);
        // Insert an initial comment
        Uri uriFirstComment = insertAComment(EMAIL, CONTENT);
        // Try to insert another comment
        Uri uriSecondComment = insertAComment(EMAIL, "Not CONTENT");
        assertNull(uriSecondComment);
        checkCommentContentForRecipient(uriOfRecipient, CONTENT);
    }
    
    public void testDeleteWithCommentsUriNoSelectionCriteria() {
        makeThreeComments();
        mResolver.delete(CommentContentProvider.CONTENT_URI, null, null);
        String[] projection = { "content" };  // desired columns
        Cursor cursor = mResolver.query(CommentContentProvider.CONTENT_URI,
                projection, null, null, null);
        assertFalse(cursor.moveToNext());
        cursor.close();
    }
    
    public void testDeleteWithCommentsUriWithSelectionCriteria() {
        makeThreeComments();
        // Delete comments with selection criteria
        String[] selectionArgs = { CONTENT };
        mResolver.delete(CommentContentProvider.CONTENT_URI,
                "CONTENT = ?", selectionArgs);
        // Test that the comments we expected were deleted
        String[] projection = { "content" };  // desired columns
        Cursor cursor = mResolver.query(CommentContentProvider.CONTENT_URI,
                projection, "CONTENT = ?", selectionArgs, null);
        assertFalse(cursor.moveToNext());
        Uri foobarUri = Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + "foobar@barfoo.com");
        checkCommentContentForRecipient(foobarUri, "not CONTENT");
        checkNoCommentsForUser(Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + "bar@foo.com"));
        checkNoCommentsForUser(Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + EMAIL));        
        cursor.close();
    }
    
    public void testDeleteWithCommentsEmailUriWithoutSelectionCriteria() {
        makeThreeComments();
        mResolver.delete(Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL), null, null);
        // Test that the comments we expected were deleted
        String[] projection = { "content" };  // desired columns
        Cursor cursor = mResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL),
                projection, null, null, null);
        assertFalse(cursor.moveToNext());
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI +
                "/" + "foobar@barfoo.com"), "not CONTENT");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI +
                "/" + "bar@foo.com"), CONTENT);
        cursor.close();
    }
    
    public void testDeleteWithCommentsEmailUriWithSelectionCriteria() {
        makeThreeComments();
        // Delete comments with selection criteria
        String[] selectionArgs = { CONTENT };
        mResolver.delete(Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + EMAIL), "CONTENT = ?", selectionArgs);
        // Test that the comments we expected were deleted
        String[] projection = { "content" };  // desired columns
        Cursor cursor = mResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + EMAIL), projection, "CONTENT = ?", selectionArgs, null);
        assertFalse(cursor.moveToNext());
        Uri foobarUri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "foobar@barfoo.com");
        checkCommentContentForRecipient(foobarUri, "not CONTENT");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI 
                + "/" + "bar@foo.com"), CONTENT);
        checkNoCommentsForUser(Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + EMAIL));
        cursor.close();
    }
    
    public void testUpdateWithCommentsUriWithNoSelectionArgs() {
        makeThreeComments();
        ContentValues values = new ContentValues();
        values.put("CONTENT", "new content");
        mResolver.update(CommentContentProvider.CONTENT_URI, values, null, null);
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "foobar@barfoo.com"), "new content");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "bar@foo.com"), "new content");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + EMAIL), "new content");
    }
    
    public void testUpdateWithCommentsUriWithSelectionArgs() {
        makeThreeComments();
        ContentValues values = new ContentValues();
        values.put("CONTENT", "new content");
        String[] selectionArgs = { CONTENT };
        mResolver.update(CommentContentProvider.CONTENT_URI, values, "CONTENT = ?", selectionArgs);
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "foobar@barfoo.com"), "not CONTENT");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "bar@foo.com"), "new content");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + EMAIL), "new content");
    }
    
    public void testUpdateWithEmailUriWithNoSelectionArgs() {
        makeThreeComments();
        ContentValues values = new ContentValues();
        values.put("CONTENT", "new content");
        mResolver.update(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "foobar@barfoo.com"), values, null, null);
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "foobar@barfoo.com"), "new content");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "bar@foo.com"), CONTENT);
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + EMAIL), CONTENT);
    }
    
    public void testUpdateWithEmailUriWithSelectionArgs() {
        makeThreeComments();
        ContentValues values = new ContentValues();
        values.put("CONTENT", "new content");
        String[] selectionArgs = { "not CONTENT" };
        mResolver.update(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "foobar@barfoo.com"), values, "CONTENT = ?", selectionArgs);
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "foobar@barfoo.com"), "new content");
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + "bar@foo.com"), CONTENT);
        checkCommentContentForRecipient(Uri.parse(CommentContentProvider.CONTENT_URI + "/" 
                + EMAIL), CONTENT);
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
    
    public void checkCommentContentForRecipient(Uri uri, String content) {
        Cursor cursor = getCursorForCommentsForUser(uri);
        assert(cursor.moveToFirst());
        assertEquals(content, cursor.getString(COLUMN_CONTENT_POS));
        cursor.close();
    }
    public void makeThreeComments(){
        insertAComment(EMAIL, CONTENT);
        insertAComment("bar@foo.com", CONTENT);
        insertAComment("foobar@barfoo.com", "not CONTENT");
    }
}
