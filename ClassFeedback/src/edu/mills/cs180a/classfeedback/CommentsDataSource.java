
package edu.mills.cs180a.classfeedback;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Persistent data storage for {@link Comment} using a database defined in
 * {@link MySQLiteOpenHelper}.  This reuses code from 
 * <a href="http://www.vogella.com/tutorials/AndroidSQLite/article.html">
 * Android SQLite database and content provider - Tutorial</a> by Lars Vogella.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentsDataSource {
    private static final String TAG = "CommentsDataSource";
    private SQLiteDatabase database;
    private MySQLiteOpenHelper dbHelper;

    /**
     * Constructs a {@code CommentsDataSource}.  The {@link #open()} method must be
     * called before retrieving data from this.
     * 
     * @param context required context for the associated {@link SQLiteDatabase}
     */
    public CommentsDataSource(Context context) {
        dbHelper = new MySQLiteOpenHelper(context);
    }

    /**
     * Opens a connection to the database, creating it if necessary.
     * This should be called before any of the other methods.
     * When the connection is no longer needed, {@link #close()} should be called.
     * 
     * @throws SQLException if the database could not be opened
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Closes the connection to the database, opened with {@link #open()}.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Creates a comment with the specified content for the specified recipient.
     * 
     * This both adds the comment to the database and constructs a {@link Comment}
     * instance.
     * 
     * @param recipient the email address of the recipient
     * @param content the content of the comment
     * @return a new {@link Comment} instance
     */
    Comment createComment(String recipient, String content) {
        if (database == null) {
            open();
        }
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, recipient);
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, content);
        long insertId = database.insert(MySQLiteOpenHelper.TABLE_COMMENTS, null,
                values);
        Log.d(TAG, "Inserted comment " + insertId + " into database.");
        return new Comment(insertId, recipient, content);
    }
    
    /**
     * Updates content and recipient of the comment to the database.
     * 
     * This is used when the content or recipient
     * of a comment has been changed. This updates that new information to the 
     * comment in the database. 
     * 
     * Note that the id cannot change.
     * 
     * @param comment the comment whose content and/or recipient has changed
     */
    void updateComment(Comment comment) {
        if (database == null) {
            open();
        }
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, comment.getRecipient());
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, comment.getContent());
        long updateId = database.update(MySQLiteOpenHelper.TABLE_COMMENTS, values,
                MySQLiteOpenHelper.COLUMN_ID + " = ?",
                new String[] {String.valueOf(comment.getId())});
        Log.d(TAG, "Updated comment " + updateId + " in database.");
    }
    
    /**
     * Removes a comment from the database.
     * 
     * @param comment the comment to be deleted
     */
    void deleteComment(Comment comment) {
        if (database == null) {
            open();
        }
        long deleteId = database.delete(MySQLiteOpenHelper.TABLE_COMMENTS, 
                MySQLiteOpenHelper.COLUMN_ID + " = ?", 
                new String[] {String.valueOf(comment.getId())});
        Log.d(TAG, "Deleted comment " + deleteId + "from database.");
    }

    /**
     * Queries the database for all comments for the specified recipient.
     * 
     * @param recipient the email address of the target of the comment
     * @param projection the names of the columns to retrieve
     * @return a {@code Cursor} pointing to all comments for the recipient
     */
    Cursor getCursorForCommentForRecipient(String recipient,
            String[] projection) {
        if (database == null) {
            open();
        }
        return database.query(MySQLiteOpenHelper.TABLE_COMMENTS,
                projection, "recipient = \"" + recipient +
                "\"", null, null, null, null);
    }

    /**
     * Returns a count of how many comments a user has. Used to test 
     * that the database was successfully upgraded to unique comments.
     * 
     * @param recipient the email address of the target of the comments
     * @return the count of the total comments targeted to the recipient
     */
    int getCountOfCommentsForRecipient(String recipient) {
       return getCursorForCommentForRecipient(recipient, null).getCount();
    }
    
    /**
     * Retrieves the unique comment targeted to the recipient from the
     * database.
     * 
     * @param recipient the email address of the target of the comments
     * @return the comment targeted to the user, null if no comments for the user
     */
    Comment getCommentForRecipient(String recipient) {
        Cursor cursor = getCursorForCommentForRecipient(recipient, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            return cursorToComment(cursor);     
        } else {
            return null;
        }
    }
    
    /**
     * Queries database for all comments.
     * 
     * @param projection the names of the columns to retrieve
     * @return a {@code Cursor} referencing all comments in the database
     */
    Cursor getCursorForAllComments(String[] projection) {
        if (database == null) {
            open();
        }
        return database.query(MySQLiteOpenHelper.TABLE_COMMENTS,
                projection, null, null, null, null, null);
    }

    /**
     * Retrieve all comments from the database.
     * 
     * @return all comments in the database
     */
    List<Comment> getAllComments() {
        List<Comment> comments = new ArrayList<Comment>();
        
        Cursor cursor = getCursorForAllComments(null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Comment comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        cursor.close();

        return comments;
    }

    private Comment cursorToComment(Cursor cursor) {
        Comment comment = new Comment(
                cursor.getLong(MySQLiteOpenHelper.COLUMN_ID_POS), 
                cursor.getString(MySQLiteOpenHelper.COLUMN_RECIPIENT_POS), 
                cursor.getString(MySQLiteOpenHelper.COLUMN_CONTENT_POS));
        return comment;
    }
    
    protected void saveComment(Person person, String commentText) {
        Comment comment =
                getCommentForRecipient(person.getEmail());
        if (comment != null) {
            comment.setContent(commentText);
            updateComment(comment);
        } else {
            createComment(person.getEmail(), commentText);
        }
    }
}