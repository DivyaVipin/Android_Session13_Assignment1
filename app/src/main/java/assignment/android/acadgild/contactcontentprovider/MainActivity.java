package assignment.android.acadgild.contactcontentprovider;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
Button btnAdd;
    EditText name,phoneno,mail;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAdd=(Button)findViewById(R.id.btnAdd);

        name=(EditText)findViewById(R.id.editTextName);
        phoneno=(EditText)findViewById(R.id.editTextPhone);
        mail=(EditText)findViewById(R.id.editTextMail);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactname=name.getText().toString();
                String contactphone=phoneno.getText().toString();
                String contactemail=mail.getText().toString();
                if(contactname.equals("")&&contactphone.equals("") && contactemail.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Please enter fields",Toast.LENGTH_LONG).show();
                    return;
                }
                checkPermission();
                if (!checkPermission()) {

                    requestPermission();


                }
                createContact(contactname,contactphone,contactemail);
                Toast.makeText(MainActivity.this,"Contact added successfully",Toast.LENGTH_LONG).show();

            }
        });

    }
    private void createContact(String name,String phoneno,String email)
    {

        Cursor cursor=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);//Select query from database
        int count=cursor.getCount();//Get the count of existing records
        if(count > 0) {
            while(cursor.moveToNext())
            {
                String displayName=ContactsContract.Contacts.DISPLAY_NAME;//Getting column name of name
                int colIndex=cursor.getColumnIndex(displayName);
                String existName=cursor.getString(colIndex);//After geting index get the name of corresponding index
                if(existName.equals(name))//Checking names of newly going to add contactnames and exising names
                {
                    Toast.makeText(MainActivity.this,"Name "+name+"already exists",Toast.LENGTH_LONG).show();
                    return;
                }

        }
        }
        ArrayList<ContentProviderOperation> ops=new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex=ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,null)//First time adding to RawContact
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,null).build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,name).build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,phoneno).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).
                        build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA,email).build());
        try
        {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops);
        }
        catch(RemoteException e)
        {
            e.printStackTrace();
        }
        catch(OperationApplicationException  e)
        {
            e.printStackTrace();
        }
    }
    private boolean checkPermission()
    {

        int read = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS);
        int write=ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS);

        return read == PackageManager.PERMISSION_GRANTED && write==PackageManager.PERMISSION_GRANTED;
    }
    private boolean requestPermission()
    {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},   //request specific permission from user
                PERMISSION_REQUEST_CODE);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {

            //If permission is granted

            if (grantResults.length > 0) {

                boolean readContact = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writeContact = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (readContact && writeContact) {
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_LONG).show();

                }
            }
        }
        }
    }