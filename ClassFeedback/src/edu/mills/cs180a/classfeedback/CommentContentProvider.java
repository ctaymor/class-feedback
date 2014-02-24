package edu.mills.cs180a.classfeedback;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class CommentContentProvider extends ContentProvider {
    public static final String AUTHORITY = "edu.mills.cs180a.classfeedback";
    private static final String BASE_PATH = "comments";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + BASE_PATH);

    // Set up URI matching.
    private static final int COMMENTS = 1;
    private static final int COMMENTS_EMAIL = 2;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = "CommentContentProvider";
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, COMMENTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", COMMENTS_EMAIL);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        CommentsDataSource cds = new CommentsDataSource(getContext());
        Cursor cursor = null;
        switch (sURIMatcher.match(uri)) {
            case COMMENTS:
                cursor = cds.getCursorForAllComments();
                break;
            case COMMENTS_EMAIL:
                cursor = cds.getCursorForCommentsForRecipient(uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        // Notify anyone listening on the URI.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException("delete not supported");
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
        throw new UnsupportedOperationException("insert not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update not supported");
    }

}
