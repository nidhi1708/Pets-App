package com.example.pets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.pets.data.PetContract.PetEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    private static final int EXISTING_PET_LOADER = 0;
    private static Uri mCurrentPetUri;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    //Keeping the track if we have edited the pet or not
    private boolean mPetHasChanged=false;

    //Keeps the track if any of view is touched , which implies that user wants to update the pet
    private View.OnTouchListener mtouchListener = new View.OnTouchListener(){
      @Override
        public boolean onTouch(View v , MotionEvent event){
          mPetHasChanged=true;
          return false;
      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        if (mCurrentPetUri == null) {
            setTitle(R.string.editor_activity_title_new_pet);
            //So in add a pet activity we don't want delete option
            invalidateOptionsMenu();
            //this method is prewritten which will call onPrepareOptionMenu() which is defined @197 line
        } else {
            setTitle(R.string.editor_activity_title_edit_pet);

            //Initialising our Loader Manager to read the clicked Pet data and displaying it
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

        //Setting up the touch Listeners
        mNameEditText.setOnTouchListener(mtouchListener);
        mBreedEditText.setOnTouchListener(mtouchListener);
        mWeightEditText.setOnTouchListener(mtouchListener);
        mGenderSpinner.setOnTouchListener(mtouchListener);


        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    private void savePet() {
        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "To save a pet enter the required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);

        //for weight if user doesnot enter a input then by default we have to take it 0
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        if (mCurrentPetUri == null) {
            //we have to insert a new pet , hence performing indertion
            Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, R.string.editor_insertion_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_insertion_successfull, Toast.LENGTH_SHORT).show();
            }
        } else {
            //if(mcurrentUri!=null) we have to update one of the existing pet , return the no. of rows effected or 0 if no row is effected
            int rowsChanged = getContentResolver().update(mCurrentPetUri, values, null, null);
            if (rowsChanged == 0) {
                Toast.makeText(this, R.string.editor_updatio_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_updation_successfull, Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    //Hiding delete key from add a pet Activity
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        if(mCurrentPetUri==null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();//vapas hmm apni original activity p jaaenge that is main activity
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deletePetDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar

            case android.R.id.home:
                //if any view has not changed then we can simply go back to the MainActivity
                if(!mPetHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);

                            }
                        };


                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //When after editing the user hits back button instead of up button
    @Override
    public void onBackPressed()
    {
        // If the pet hasn't changed, continue with handling back button press
        if(!mPetHasChanged)
        {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        return new CursorLoader(
                this, mCurrentPetUri, projection, null, null, null);
        //We are using currentPetUri instead of PetEntry.contentUri because here we want information about the current pet and not the whole pet information
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //proceed to move the cursor to the first row and reading the data from it
        //this should be the only row in the cursor
        if (cursor.moveToNext()) {
            int name_column = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breed_column = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int gender_column = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weight_column = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            //Extracting out the values
            String name = cursor.getString(name_column);
            String breed = cursor.getString(breed_column);
            int weight = cursor.getInt(weight_column);
            int gender = cursor.getInt(gender_column);

            //Setting the values
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));  //** Converting int to String to set its value to weightEditText

            switch (gender) {
                case PetEntry.GENDER_UNKNOWN:
                    mGenderSpinner.setSelection(0);
                    break;

                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;

                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
            }
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If loader is invalidated clear out all data from the input
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.unchanged_changed_dialog);
        builder.setPositiveButton(R.string.discard , discardButtonClickListener); //we are passing the function performed by the discard button as a variable b/c we want to set
                                                                                  //its functionalities as our user press up or down button

        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() { //whereas if the user clicks keep editing then simply dismiss the dialog
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

    private void deletePetDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_delete_pet_dialog);
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
        if(mCurrentPetUri!=null) {
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);
            if (rowsDeleted != 0) {
                Toast.makeText(this, R.string.editor_deletion_successfull, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_deletion_failed, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}