
package edu.mills.cs180a.contentclient;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int PICK_REQUEST = 1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button fetchOneButton = (Button) findViewById(R.id.fetchOneButton);
        fetchOneButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, PICK_REQUEST);
            }
        });
    }

    // Code adapted from section 11.17 of The Android Cookbook by Ian Darwin (O'Reilly).
    // Returns the empty string if record has no email address.
    private String getContactEmail(Uri contactUri) {
        // Get id of contact.
        Cursor contactCursor = getContentResolver().query(contactUri, null, null, null, null);
        int columnIndexForId = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);
        String contactId = contactCursor.getString(columnIndexForId);

        // Now that we have an id, we can request the email address.
        Cursor emailsCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + contactId,
                null, null);

        // Get the (first) email address.
        String email = "";
        if (emailsCursor.moveToFirst()) {
            email = emailsCursor.getString(emailsCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.ADDRESS));
            if (emailsCursor.moveToNext()) {
                Log.d(TAG, "Additional emails ignored.");
            }
        } else {
            Log.w(TAG, "No email address found for contact.");
        }
        
        // Clean up before returning.
        contactCursor.close();
        emailsCursor.close();
        return email;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_REQUEST && resultCode == RESULT_OK) {
            String email = getContactEmail(data.getData());
            Toast.makeText(this, "Found email: " + email, Toast.LENGTH_LONG).show();
            // TODO: Request comments.
        } else {
            Log.w(TAG, "Did not pick contact.");
        }
    }
}
