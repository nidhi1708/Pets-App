package com.example.pets;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pets.data.PetContract.PetEntry;
import com.example.pets.data.PetCursorAdapter;
import com.example.pets.data.PetDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

/**
 * Displays list of pets that were entered and stored in the app.
 */

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER=0;
    PetCursorAdapter mCursorAdapter;
    private Loader<Cursor> loader;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //if list has no pets then our app should show emptyView on the mainActivity
        ListView petListView = (ListView) findViewById(R.id.list);

        View emptyView=findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView); //setEmptyView is already prewritten that checks if the petListview is empty than sets emptyView on it else show our pets


        mCursorAdapter=new PetCursorAdapter(this , null);
        petListView.setAdapter(mCursorAdapter);


        //Setting on item click on pet list view to update any existing pet
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //on clicking any item 2 operations are to be performed
                //1. Going from mainActivity to Editor Activity
                //2. We also pass the Uri of the current pet that is clicked by the user to open up its information

                Intent intent=new Intent(MainActivity.this , EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI , id);

                //setting the Uri on the data field of the Intent
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });

        //kick off the loader manager
       getLoaderManager().initLoader(PET_LOADER,null , this);

    }


    private void insertPet(){
        //Updating the data in Pets Table

        ContentValues values=new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME , "ToTo");
        values.put(PetEntry.COLUMN_PET_BREED , "Terrer");
        values.put(PetEntry.COLUMN_PET_GENDER , PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT , 7);

        Uri newUri=getContentResolver().insert(PetEntry.CONTENT_URI , values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Inserting dummy data using insertPet()
                insertPet();
                return true;

            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deletePetDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String projection[]={
          PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
        };

        return new CursorLoader(
                this , PetEntry.CONTENT_URI , projection , null , null , null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
         mCursorAdapter.swapCursor(null);
    }

    private void deletePetDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.main_delete_all_pets);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface!=null){
                    dialogInterface.dismiss();
                }
            }
        });
        //Creating and showing the dialog box
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void deletePet(){
        int rowsDeleted=getContentResolver().delete(PetEntry.CONTENT_URI,null , null);;
        if (rowsDeleted != 0) {
            Toast.makeText(this, R.string.editor_deletion_successfull, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.editor_deletion_failed, Toast.LENGTH_SHORT).show();
        }
    }
}