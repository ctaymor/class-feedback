
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
 * {@link MySQLiteHelper}.  This reuses code from 
 * <a href="http://www.vogella.com/tutorials/AndroidSQLite/article.html">
 * Android SQLite database and content provider - Tutorial</a> by Lars Vogella.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentsDataSource {
    private static final String TAG = "CommentsDataSource";
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { 
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_RECIPIENT,
            MySQLiteHelper.COLUMN_CONTENT};

    public CommentsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    /**
     * Open a connection to the database, creating it if necessary.
     * This should be called before any of the other methods.
     * When the connection is no longer needed, {@link #close()} should be called.
     * 
     * @throws SQLException if the database could not be opened.
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Close the connection to the database, opened with {@link #open()}.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Create a comment with the specified content for the specified recipient.
     * This both adds the comment to the database and constructs a {@link Comment}
     * instance.
     * 
     * @param recipient the index within {@link Person#everyone} of the recipient
     * @param content the content of the comment
     * @return a new {@link Comment} instance
     */
    Comment createComment(int recipient, String content) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_RECIPIENT, recipient);
        values.put(MySQLiteHelper.COLUMN_CONTENT, content);
        long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
                values);
        Log.d(TAG, "Inserted comment " + insertId + " into database.");
        return new Comment(insertId, recipient, content);
    }

    /**
     * Retrieve all comments from the database.
     * 
     * @return all comments in the database
     */
    List<Comment> getAllComments() {
        List<Comment> comments = new ArrayList<Comment>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                allColumns, null, null, null, null, null);

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
                cursor.getLong(MySQLiteHelper.COLUMN_ID_POS), 
                cursor.getInt(MySQLiteHelper.COLUMN_RECIPIENT_POS), 
                cursor.getString(MySQLiteHelper.COLUMN_CONTENT_POS));
        return comment;
    }
}