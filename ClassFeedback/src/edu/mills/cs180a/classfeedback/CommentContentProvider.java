package edu.mills.cs180a.classfeedback;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "In CommentContentProvider.query()");
        Log.d(TAG, "In CommentContentProvider, getContext().toString(): " + getContext().toString());
        CommentsDataSource cds = new CommentsDataSource(getContext());
        cds.open();
        Cursor cursor = null;
        switch (sURIMatcher.match(uri)) {
            case COMMENTS:
                Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS");
                cursor = cds.getCursorForAllComments(projection);
                break;
            case COMMENTS_EMAIL:
                Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS_EMAIL");
                cursor = cds.getCursorForCommentsForRecipient(uri.getLastPathSegment(), projection);
                break;
            default:
                Log.d(TAG, "In CommentContentProvider.query(), uri is not matched: " + uri);
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
