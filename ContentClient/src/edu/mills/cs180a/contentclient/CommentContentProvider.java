/**
 * 
 */
package edu.mills.cs180a.contentclient;

import android.net.Uri;

/**
 * @author Ellen
 *
 */
public class CommentContentProvider {
    // Copied from the content provider.
    public static final String AUTHORITY = "edu.mills.cs180a.classfeedback";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/comments");

}
