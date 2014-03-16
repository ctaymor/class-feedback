package edu.mills.cs180a.classfeedback;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * A content provider for comments meant for specified individuals,
 * backed by {@link CommentsDataSource}.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentContentProvider extends ContentProvider {
    private static final String TAG = "CommentContentProvider";
    /**
     * The authority for this content provider.  This must appear in this
     * application's AndroidManifest.xml and in request URLs from clients.
     */
    public static final String AUTHORITY = "edu.mills.cs180a.classfeedback";
    private static final String BASE_PATH = "comments";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);


    // Set up URI matching.
    private static final int COMMENTS = 1;
    private static final int COMMENTS_EMAIL = 2;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // Get all comments.
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, COMMENTS);
        // Get all comments for a specific email address.
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", COMMENTS_EMAIL);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    // TODO need to test. Also does it handle selections?
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "In CommentContentProvider.query()");
        Log.d(TAG, "In CommentContentProvider, getContext().toString(): " + getContext().toString());
        Context context = this.getContext();
        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(context);
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (sURIMatcher.match(uri)) {
        case COMMENTS:
            Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS");
            cursor = readableDatabase.query(MySQLiteOpenHelper.TABLE_COMMENTS,
                    projection, selection, selectionArgs, null, null, null);
            break;
        case COMMENTS_EMAIL:
            Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS_EMAIL");
            if (selectionArgs != null) {
                String[] actualSelectionArgs = new String[selectionArgs.length + 1];
                System.arraycopy(selectionArgs, 0, actualSelectionArgs, 0, selectionArgs.length);
                actualSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();
               Log.d(TAG, "selection statement for query is: " + selection + " AND recipient = ?");
               for (int i = 0; i < actualSelectionArgs.length; i++) {
                   Log.d(TAG, "selection statement: ith element is " + actualSelectionArgs[i]);
               }
               cursor = readableDatabase.query(MySQLiteOpenHelper.TABLE_COMMENTS,
                        projection, selection + "AND recipient = ?", actualSelectionArgs, 
                        null, null, null);
               Log.d(TAG, "cursor length from query with selection statement is: " + cursor.getCount());
            } else {
                cursor = readableDatabase.query(MySQLiteOpenHelper.TABLE_COMMENTS,
                        projection, "recipient = \"" + uri.getLastPathSegment() +
                        "\"", null, null, null, null);
            }
            break;
        default:
            Log.d(TAG, "In CommentContentProvider.query(), uri is not matched: " + uri);
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        // Notify anyone listening on the URI.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    // TODO test if the correct int is returned
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "In CommentContentProvider.delete()");
        Log.d(TAG, "In CommentContentProvider, getContext().toString(): " + getContext().toString());
        Context context = this.getContext();
        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(context);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        int intToReturn = 0;
        switch (sURIMatcher.match(uri)) {
        case COMMENTS:
            Log.d(TAG, "In CommentContentProvider.delete(), uri is COMMENTS");
            intToReturn = writableDatabase.delete(MySQLiteOpenHelper.TABLE_COMMENTS,
                    selection, selectionArgs);
            break;
        case COMMENTS_EMAIL:
            Log.d(TAG, "In CommentContentProvider.delete(), uri is COMMENTS_EMAIL");
            if (selectionArgs != null) {
                String[] actualSelectionArgs = new String[selectionArgs.length + 1];
                System.arraycopy(selectionArgs, 0, actualSelectionArgs, 0, selectionArgs.length);
                actualSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();
                intToReturn = writableDatabase.delete(MySQLiteOpenHelper.TABLE_COMMENTS,
                        selection + " AND recipient = ?", actualSelectionArgs);
            } else {
                intToReturn = writableDatabase.delete(MySQLiteOpenHelper.TABLE_COMMENTS,
                        "recipient = \"" + uri.getLastPathSegment() + "\"", null);
            }

            break;
        default:
            Log.d(TAG, "In CommentContentProvider.delete(), uri is not matched: " + uri);
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        return intToReturn;
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
        case COMMENTS_EMAIL:
        case COMMENTS:
            return ContentResolver.CURSOR_DIR_BASE_TYPE;
        default:
            Log.e(TAG, "Unrecognized uri: " + uri);
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "In CommentContentProvider.insert()");
        Log.d(TAG, "In CommentContentProvider, getContext().toString(): " + getContext().toString());
        switch (sURIMatcher.match(uri)) {
        case COMMENTS_EMAIL:
        case COMMENTS:
            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(this.getContext());
            SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
            long returnFromInsert = writableDatabase.insert(MySQLiteOpenHelper.TABLE_COMMENTS, null, values);
            Log.d(TAG, "insert return" + returnFromInsert);
            Log.d(TAG, "values recipient is " + values.getAsString("recipient"));
            Log.d(TAG, "uri recipient is " + CommentContentProvider.CONTENT_URI + "/" + values.getAsString("recipient"));
            return Uri.parse(CommentContentProvider.CONTENT_URI + "/" + values.getAsString("recipient"));
        default:
            Log.e(TAG, "Unrecognized uri: " + uri);
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "In CommentContentProvider.update()");
        Log.d(TAG, "In CommentContentProvider, getContext().toString(): " + getContext().toString());
        Context context = this.getContext();
        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(context);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        int intToReturn = 0;
        switch (sURIMatcher.match(uri)) {
        case COMMENTS:
            Log.d(TAG, "In CommentContentProvider.update(), uri is COMMENTS");
            intToReturn = writableDatabase.update(MySQLiteOpenHelper.TABLE_COMMENTS,
                    values, selection, selectionArgs);
            break;
        case COMMENTS_EMAIL:
            Log.d(TAG, "In CommentContentProvider.update(), uri is COMMENTS_EMAIL");
            if (selectionArgs != null) {
                String[] actualSelectionArgs = new String[selectionArgs.length + 1];
                System.arraycopy(selectionArgs, 0, actualSelectionArgs, 0, selectionArgs.length);
                actualSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();
                intToReturn = writableDatabase.update(MySQLiteOpenHelper.TABLE_COMMENTS, values,
                        selection + " AND recipient = ?", actualSelectionArgs);
            } else {
                intToReturn = writableDatabase.update(MySQLiteOpenHelper.TABLE_COMMENTS, values,
                        "recipient = \"" + uri.getLastPathSegment() + "\"", null);
            }

            break;
        default:
            Log.d(TAG, "In CommentContentProvider.delete(), uri is not matched: " + uri);
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        return intToReturn;
    }
}
